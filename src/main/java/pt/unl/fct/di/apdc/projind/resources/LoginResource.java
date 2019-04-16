package pt.unl.fct.di.apdc.projind.resources;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.codec.digest.DigestUtils;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Transaction;
import com.google.gson.Gson;

import pt.unl.fct.di.apdc.projind.resources.LoginResource;
import pt.unl.fct.di.apdc.projind.util.AuthToken;
import pt.unl.fct.di.apdc.projind.util.LoginData;

@Path("/login")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class LoginResource {

	private static final Logger LOG = Logger.getLogger(LoginResource.class.getName());
	private final Gson g = new Gson();
	private static final DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

	public LoginResource() {}

	@POST
	@Path("/")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response doLogin(LoginData data, @Context HttpServletRequest request, @Context HttpHeaders headers) {
		LOG.fine("Attempt to login user: " + data.username);

		Transaction txn = datastore.beginTransaction();
		Key userKey = KeyFactory.createKey("User", data.username);

		try {
			Entity user = datastore.get(txn,userKey);

			Query ctrQuery = new Query("UserStats").setAncestor(userKey);
			List<Entity> results = datastore.prepare(ctrQuery).asList(FetchOptions.Builder.withDefaults());
			Entity ustats = null;
			if (results.isEmpty()) {
				ustats = new Entity("UserStats", user.getKey());
				ustats.setProperty("user_stats_logins", 0L);
				ustats.setProperty("user_stats_failed", 0L);
			} else {
				ustats = results.get(0);
			}

			String hashedPWD = (String) user.getProperty("user_pwd");
			if (hashedPWD.equals(DigestUtils.sha512Hex(data.password))) {
				AuthToken token = new AuthToken(data.username);

				Query ctrQuery1 = new Query("UserLog").setAncestor(userKey);
				List<Entity> results1 = datastore.prepare(ctrQuery1).asList(FetchOptions.Builder.withDefaults());
				Entity log = null;
				if (results1.isEmpty()) {
					log = new Entity("UserLog", user.getKey());
				} else {
					log = results1.get(0);
				}

				log.setProperty("user_login_ip", request.getRemoteAddr());
				log.setProperty("user_login_host", request.getRemoteAddr());
				log.setProperty("user_login_latlon", headers.getHeaderString("X-AppEngine-CityLatLong"));
				log.setProperty("user_login_city", headers.getHeaderString("X-AppEngine-City"));
				log.setProperty("user_login_country", headers.getHeaderString("X-AppEngine-Country"));
				log.setProperty("user_login_time", new Date());
				log.setProperty("user_login_token", token.tokenID);
				log.setProperty("user_login_token_expiration", token.expirationData);

				ustats.setProperty("user_stats_logins", 1L + (long) ustats.getProperty("user_stats_logins"));
				ustats.setProperty("user_stats_failed", 0L);
				ustats.setProperty("user_stats_last", new Date());

				List<Entity> logs = Arrays.asList(log,ustats);
				datastore.put(txn,logs);
				txn.commit();


				LOG.info("user '" + data.username + "' logged in sucessfully.");
				return Response.ok(g.toJson(token)).build();

			} else { //Incorrect Password
				ustats.setProperty("user_stats_failed", 1L + (long) ustats.getProperty("user_stats_failed"));
				datastore.put(txn, ustats);
				txn.commit();

				LOG.warning("Wrong password for username: " + data.username);
				return Response.status(Status.FORBIDDEN).entity("Wrong username or password").build();
			}
		} catch (EntityNotFoundException e) { //Username does not exist
			LOG.warning("Failed login attempt for username: " + data.username);
			txn.rollback();
			return Response.status(Status.FORBIDDEN).entity("Wrong username or password").build();
		} finally {
			if (txn.isActive()) {
				txn.rollback();
				//return Response.status(Status.INTERNAL_SERVER_ERROR).build();
			}
		}
	}

}
