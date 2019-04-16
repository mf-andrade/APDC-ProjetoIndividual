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

import pt.unl.fct.di.apdc.projind.util.AdminFLogInData;

@Path("/gs")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class AdminResources {

	private static final Logger LOG = Logger.getLogger(AdminResources.class.getName());
	private final Gson g = new Gson();
	private static final DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

	@POST
	@Path("/getfailedattempts")
	public Response getUserData(AdminFLogInData data) {
		LOG.fine("Attempt to retrieve user data: " + data.username);

		Transaction txn = datastore.beginTransaction();
		Key adminKey = KeyFactory.createKey("User", data.username);

		try {
			Entity adminUser = datastore.get(txn, adminKey);

			Query ctrQuery = new Query("UserStats").setAncestor(adminKey);
			List<Entity> results = datastore.prepare(ctrQuery).asList(FetchOptions.Builder.withDefaults());
			Entity ustats = null;
			if (results.isEmpty()) {
				ustats = new Entity("UserStats", adminUser.getKey());
				ustats.setProperty("user_stats_logins", 0L);
				ustats.setProperty("user_stats_failed", 0L);
			} else {
				ustats = results.get(0);
			}
			if (!adminUser.getProperty("user_role").equals(RegisterResource.USER_ROLE_ADMIN))
				//Verify Permission
				return Response.status(Status.FORBIDDEN).entity("Permission error!").build();

			data.failedAttempts = (long) ustats.getProperty("user_stats_failed");

			return Response.ok().entity(g.toJson(data)).build();
		} catch (EntityNotFoundException e) { //Username does not exist
			LOG.warning("Failed login attempt for username: " + data.username);
			return Response.status(Status.BAD_REQUEST).build();
		} finally {
			if (txn.isActive()) {
				txn.rollback();
			}
		}
	}
}
