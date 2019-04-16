package pt.unl.fct.di.apdc.projind.resources;

import java.util.List;
import java.util.logging.Logger;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
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
import com.google.gson.Gson;

import pt.unl.fct.di.apdc.projind.util.GBOAuthUser;
import pt.unl.fct.di.apdc.projind.util.RegisterData;

@Path("/gbo")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class BackOfficeResources {

	private static final Logger LOG = Logger.getLogger(BackOfficeResources.class.getName());
	private final Gson g = new Gson();
	private static final DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

	@POST
	@Path("/getuserdata")
	public Response getUserData(GBOAuthUser data) {
		LOG.fine("Attempt to retrieve user data: " + data.username);

		Transaction txn = datastore.beginTransaction();
		Key gboUKey = KeyFactory.createKey("User", data.token.username);
		Key userKey = KeyFactory.createKey("User", data.username);

		try {
			Entity gboUser = datastore.get(txn, gboUKey);

			Query ctrQuery = new Query("UserLog").setAncestor(gboUKey);
			List<Entity> results = datastore.prepare(ctrQuery).asList(FetchOptions.Builder.withDefaults());
			Entity ulog = null;
			if (results.isEmpty()) {
				//User not logged in
				return Response.status(Status.FORBIDDEN).entity("User not logged in").build();
			} else {
				ulog = results.get(0);
			}
			if (!ulog.getProperty("user_login_token").equals(data.token.tokenID)) {
				//Authtication Error
				return Response.status(Status.FORBIDDEN).entity("Authtentication error!").build();
			}
			if ((long) ulog.getProperty("user_login_token_expiration") < System.currentTimeMillis()) {
				return Response.status(Status.FORBIDDEN).entity("Authtentication error!").build();
			}
			if (!gboUser.getProperty("user_role").equals(RegisterResource.USER_ROLE_BACKOFFICE))
				//Verify Permission
				return Response.status(Status.FORBIDDEN).entity("Permission error!").build();

			Entity user = datastore.get(txn, userKey);
			RegisterData rd = new RegisterData(data.username, "", user.getProperty("user_email").toString(),
					user.getProperty("user_name").toString(), user.getProperty("user_profile").toString(),
					user.getProperty("user_phone").toString(), user.getProperty("user_mobilephone").toString(),
					user.getProperty("user_address").toString());
			txn.commit();
			return Response.ok().entity(g.toJson(rd)).build();
		} catch (EntityNotFoundException e) { //Username does not exist
			LOG.warning("Failed login attempt for username: " + data.token.username);
			return Response.status(Status.BAD_REQUEST).build();
		} finally {
			if (txn.isActive()) {
				txn.rollback();
				//return Response.status(Status.INTERNAL_SERVER_ERROR).build();
			}
		}
	}

}
