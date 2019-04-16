package pt.unl.fct.di.apdc.projind.resources;

import java.util.List;
import java.util.logging.Logger;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Transaction;

import pt.unl.fct.di.apdc.projind.util.AuthToken;

@Path("/logout")
public class LogoutResource {

	private static final Logger LOG = Logger.getLogger(LoginResource.class.getName());
	//private final Gson g = new Gson();
	private static final DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

	public LogoutResource() {}

	@POST
	@Path("/")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response doLogout(AuthToken data) {
		LOG.fine("Attempt to logout user: " + data.username);

		Transaction txn = datastore.beginTransaction();
		Key userKey = KeyFactory.createKey("User", data.username);

		try {
			Entity user = datastore.get(userKey);

			Query ctrQuery = new Query("UserLog").setAncestor(userKey);
			List<Entity> results = datastore.prepare(ctrQuery).asList(FetchOptions.Builder.withDefaults());
			Entity ulog = null;
			if (results.isEmpty()) {
				//User not logged in
				return Response.status(Status.FORBIDDEN).entity("User not logged in").build();
			} else {
				ulog = results.get(0);
			}
			if ( ! ulog.getProperty("user_login_token").toString().equals(data.tokenID) ) {
				//Authtication Error
				return Response.status(Status.FORBIDDEN).entity("Authtentication error!").build();
			}
			if ((long) ulog.getProperty("user_login_token_expiration") < System.currentTimeMillis()) {
				//ulog.setProperty("user_login_token", "");
				//ulog.setProperty("user_login_token_expiration", 0L);
				datastore.delete(txn, ulog.getKey());
				txn.commit();
				return Response.ok().entity("Session already expired.").build();
			}
			// OK to logout user

			datastore.delete(txn, ulog.getKey());
			txn.commit();

			LOG.info("user '" + data.username + "' logged out sucessfully.");
			return Response.ok().entity("Logged out successfully").build();
		} catch (EntityNotFoundException e) { //Username does not exist
			LOG.warning("Failed logout attempt for username: " + data.username);
			txn.rollback();
			return Response.status(Status.FORBIDDEN).build();
		} finally {
			if (txn.isActive()) {
				txn.rollback();
				//return Response.status(Status.INTERNAL_SERVER_ERROR).build();
			}
		}
	}
}
