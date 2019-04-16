package pt.unl.fct.di.apdc.projind.resources;

import java.util.Arrays;
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
import com.google.gson.Gson;

import pt.unl.fct.di.apdc.projind.util.AuthToken;
import pt.unl.fct.di.apdc.projind.util.GBOAuthUser;

@Path("/remove")
public class RemoveUserResource {

	private static final Logger LOG = Logger.getLogger(RemoveUserResource.class.getName());
	//private final Gson g = new Gson();
	private static final DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

	@POST
	@Path("/")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response removeUser(AuthToken data) {
		LOG.fine("Attempt to remove user: " + data.username);

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
			if (!ulog.getProperty("user_login_token").equals(data.tokenID)) {
				//Authtication Error
				return Response.status(Status.FORBIDDEN).entity("Authtentication error!").build();
			}
			if ((long) ulog.getProperty("user_login_token_expiration") < System.currentTimeMillis()) {
				return Response.status(Status.FORBIDDEN).entity("Authtentication error!").build();
			}

			Query ctrQuery1 = new Query("UserStats").setAncestor(userKey);
			List<Entity> results1 = datastore.prepare(ctrQuery1).asList(FetchOptions.Builder.withDefaults());
			Entity ustats = null;
			if (results1.isEmpty()) {
				//User never logged in
				return Response.status(Status.INTERNAL_SERVER_ERROR).entity("User never logged in").build();
			} else {
				ustats = results1.get(0);
			}
			List<Key> toDelete = Arrays.asList(ulog.getKey(), user.getKey(), ustats.getKey());
			datastore.delete(txn, toDelete);
			txn.commit();

			LOG.info("user '" + data.username + "' removed sucessfully.");
			return Response.ok().entity("User account removed").build();
		} catch (EntityNotFoundException e) { //Username does not exist
			LOG.warning("Failed removal attempt for username: " + data.username);
			txn.rollback();
			return Response.status(Status.BAD_REQUEST).entity("User does not exist").build();
		} finally {
			if (txn.isActive()) {
				txn.rollback();
				//return Response.status(Status.INTERNAL_SERVER_ERROR).build();
			}
		}
	}

	@POST
	@Path("/gbo")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response removeUserGBO(GBOAuthUser data) {
		LOG.fine("Attempt to remove user: " + data.username);
		if (!data.isValid())
			return Response.status(Status.BAD_REQUEST).entity("Missing or wrong parameter.").build();

		Transaction txn = datastore.beginTransaction();

		try {
			Key userKey = KeyFactory.createKey("User", data.token.username);
			Entity user = datastore.get(txn,userKey);

			Query ctrQuery = new Query("UserLog").setAncestor(userKey);
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
			if (!user.getProperty("user_role").equals(RegisterResource.USER_ROLE_BACKOFFICE))
				//Verify Permission
				return Response.status(Status.FORBIDDEN).entity("Permission error!").build();

			//datastore.delete(userKey);
			//txn.commit();

			//LOG.info("user '" + data.username + "' removed sucessfully.");
			//return Response.ok().build();

		} catch (EntityNotFoundException e) { //Username does not exist
			txn.rollback();
			return Response.status(Status.BAD_REQUEST).entity("User does not exist").build();
		}

		try {
			Key userKey = KeyFactory.createKey("User", data.username);
			Entity user = datastore.get(txn,userKey);

			datastore.delete(txn, userKey);
			txn.commit();

			LOG.info("user '" + data.username + "' removed sucessfully.");
			return Response.ok().entity("User account removed").build();
		} catch (EntityNotFoundException e) { //Username does not exist
			LOG.warning("Failed removal attempt for username: " + data.username);
			txn.rollback();
			return Response.status(Status.BAD_REQUEST).entity("User does not exist").build();
		} finally {
			if (txn.isActive()) {
				txn.rollback();
				//return Response.status(Status.INTERNAL_SERVER_ERROR).build();
			}
		}
	}
}
