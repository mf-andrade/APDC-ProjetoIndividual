package pt.unl.fct.di.apdc.projind.resources;

import java.util.List;
import java.util.logging.Logger;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
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

import pt.unl.fct.di.apdc.projind.util.GBOModifyUserData;
import pt.unl.fct.di.apdc.projind.util.ModifyUserData;

@Path("/modify")
public class ModifyUserResource {

	private static final Logger LOG = Logger.getLogger(ModifyUserResource.class.getName());
	//private final Gson g = new Gson();
	private static final DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

	@POST
	@Path("/")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response modifyUser(ModifyUserData data) {

		Transaction txn = datastore.beginTransaction();
		Key userKey = KeyFactory.createKey("User", data.token.username);

		try {
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
			// OK to modify user data

			if (!data.password.isEmpty())
				user.setProperty("user_pwd", DigestUtils.sha512Hex(data.password));
			if (!data.email.isEmpty())
				user.setUnindexedProperty("user_email", data.email);
			if (!data.profileStatus.isEmpty())
				user.setProperty("user_profile", data.profileStatus);
			if (!data.phoneNumber.isEmpty())
				user.setProperty("user_phone", data.phoneNumber);
			if (!data.mobilePhoneNumber.isEmpty())
				user.setProperty("user_mobilephone", data.mobilePhoneNumber);
			if (!data.address.isEmpty())
				user.setUnindexedProperty("user_address", data.address);

			datastore.put(txn, user);
			LOG.info("User data modified: " + data.token.username);
			txn.commit();
			return Response.ok().entity("User data modified").build();

		} catch ( EntityNotFoundException ex ) {
			//User not found
			LOG.warning("Failed modify attempt for username: " + data.token.username);
			txn.rollback();
			return Response.status(Status.BAD_REQUEST).entity("User does not exist").build();
		} finally {
			if (txn.isActive()) {
				txn.rollback(); //return Response.status(Status.INTERNAL_SERVER_ERROR).build();
			}
		}
	}

	@POST
	@Path("/gbo")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response modifyUserGBO(GBOModifyUserData data) {

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
			// OK to modify user data

		} catch ( EntityNotFoundException ex) {
			//User not found
			LOG.warning("Failed removal attempt for username: " + data.token.username);
			txn.rollback();
			return Response.status(Status.BAD_REQUEST).entity("User does not exist").build();
		}

		try {
			Key userKey = KeyFactory.createKey("User", data.username);
			Entity user = datastore.get(txn,userKey);

			if (!data.mud.password.isEmpty())
				user.setProperty("user_pwd", DigestUtils.sha512Hex(data.mud.password));
			if (!data.mud.email.isEmpty())
				user.setProperty("user_email", data.mud.email);
			if (!data.mud.profileStatus.isEmpty())
				user.setProperty("user_profile", data.mud.profileStatus);
			if (!data.mud.phoneNumber.isEmpty())
				user.setProperty("user_phone", data.mud.phoneNumber);
			if (!data.mud.mobilePhoneNumber.isEmpty())
				user.setProperty("user_mobilephone", data.mud.mobilePhoneNumber);
			if (!data.mud.address.isEmpty())
				user.setUnindexedProperty("user_address", data.mud.address);

			datastore.put(txn, user);
			LOG.info("User data modified: " + data.token.username);
			txn.commit();
			return Response.ok().entity("User data modified").build();

		} catch ( EntityNotFoundException ex) {
			//User not found
			LOG.warning("Failed removal attempt for username: " + data.token.username);
			txn.rollback();
			return Response.status(Status.BAD_REQUEST).entity("User does not exist").build();
		} finally {
			if (txn.isActive()) {
				txn.rollback(); }
		}
	}
}
