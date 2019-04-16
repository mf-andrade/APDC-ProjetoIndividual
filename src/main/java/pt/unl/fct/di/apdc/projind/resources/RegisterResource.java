package pt.unl.fct.di.apdc.projind.resources;

import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
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

import pt.unl.fct.di.apdc.projind.util.GBORegisterData;
import pt.unl.fct.di.apdc.projind.util.RegisterData;
import pt.unl.fct.di.apdc.projind.resources.RegisterResource;

@Path("/register")
public class RegisterResource {

	public static final String USER_ROLE_DEFAULT = "DEFAULT";
	public static final String USER_ROLE_BACKOFFICE = "GBO";
	public static final String USER_ROLE_ADMIN = "GS";

	private static final Logger LOG = Logger.getLogger(RegisterResource.class.getName());
	//private final Gson g = new Gson();
	private static final DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

	public RegisterResource() {}

	@POST
	@Path("/")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response doRegistration(RegisterData data) {
		if (!data.validRegistration())
			return Response.status(Status.BAD_REQUEST).entity("Missing or wrong parameter.").build();

		Transaction txn = datastore.beginTransaction();
		try {
			Key userKey = KeyFactory.createKey("User", data.username);
			Entity user = datastore.get(userKey);
			txn.rollback();
			return Response.status(Status.BAD_REQUEST).entity("User already exists.").build();
		} catch (EntityNotFoundException ex) {
			Entity user = new Entity("User", data.username);
			user.setProperty("user_pwd", DigestUtils.sha512Hex(data.password));
			user.setUnindexedProperty("user_creation_time", new Date());
			user.setUnindexedProperty("user_email", data.email);
			user.setUnindexedProperty("user_name", data.name);
			user.setProperty("user_profile", data.profileStatus);
			user.setProperty("user_phone", data.phoneNumber);
			user.setProperty("user_mobilephone", data.mobilePhoneNumber);
			user.setUnindexedProperty("user_address", data.address);
			user.setProperty("user_role", USER_ROLE_DEFAULT);
			datastore.put(txn, user);
			LOG.info("User registered: " + data.username);
			txn.commit();
			return Response.ok().entity("User registered").build();
		} finally {
			if (txn.isActive())
				txn.rollback();
		}
	}

	@POST
	@Path("/gs/gbo")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response doRegistrationGBO(GBORegisterData data) {
		if (!data.isValid())
			return Response.status(Status.BAD_REQUEST).entity("Missing or wrong parameter.").build();

		Transaction txn = datastore.beginTransaction();
		Key userKey = KeyFactory.createKey("User", data.token.username);
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
			if (!ulog.getProperty("user_login_token").equals(data.token.tokenID)) {
				//Authtication Error
				return Response.status(Status.FORBIDDEN).entity("Authtentication error!").build();
			}
			if ((long) ulog.getProperty("user_login_token_expiration") < System.currentTimeMillis()) {
				return Response.status(Status.FORBIDDEN).entity("Authtentication error!").build();
			}
			if (!user.getProperty("user_role").equals(USER_ROLE_ADMIN))
				//Verify Permission
				return Response.status(Status.FORBIDDEN).entity("Permission error!").build();

			// OK to modify user data
			txn.commit();
		} catch (EntityNotFoundException ex) {
			LOG.info("User does not exist");
			txn.rollback();
			return Response.status(Status.BAD_REQUEST).entity("User not found").build();
		} finally {
			if (txn.isActive()) {
				txn.rollback();
			}
		}

		Transaction txn1 = datastore.beginTransaction();
		try {
			Key newUserKey = KeyFactory.createKey("User", data.rd.username);
			Entity user = datastore.get(newUserKey);
			txn1.rollback();
			return Response.status(Status.BAD_REQUEST).entity("User already exists.").build();
		} catch (EntityNotFoundException ex) {
			Entity user = new Entity("User", data.rd.username);
			user.setProperty("user_pwd", DigestUtils.sha512Hex(data.rd.password));
			user.setUnindexedProperty("user_creation_time", new Date());
			user.setUnindexedProperty("user_email", data.rd.email);
			user.setUnindexedProperty("user_name", data.rd.name);
			user.setProperty("user_profile", data.rd.profileStatus);
			user.setProperty("user_phone", data.rd.phoneNumber);
			user.setProperty("user_mobilephone", data.rd.mobilePhoneNumber);
			user.setUnindexedProperty("user_address", data.rd.address);
			user.setProperty("user_role", USER_ROLE_BACKOFFICE);
			datastore.put(txn1, user);
			LOG.info("Back Office User registered: " + data.rd.username);
			txn1.commit();
			return Response.ok().entity("Back office user registered").build();
		} finally {
			if (txn1.isActive())
				txn1.rollback();
		}
	}

	@POST
	@Path("/admin/add")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response doRegistrationGS(RegisterData data) {
		if (!data.validRegistration())
			return Response.status(Status.BAD_REQUEST).entity("Missing or wrong parameter.").build();

		Transaction txn1 = datastore.beginTransaction();
		try {
			Key newUserKey = KeyFactory.createKey("User", data.username);
			Entity user = datastore.get(txn1,newUserKey);
			txn1.rollback();
			return Response.status(Status.BAD_REQUEST).entity("User already exists.").build();
		} catch (EntityNotFoundException ex) {
			Entity user = new Entity("User", data.username);
			user.setProperty("user_pwd", DigestUtils.sha512Hex(data.password));
			user.setUnindexedProperty("user_creation_time", new Date());
			user.setUnindexedProperty("user_email", data.email);
			user.setUnindexedProperty("user_name", data.name);
			user.setProperty("user_profile", data.profileStatus);
			user.setProperty("user_phone", data.phoneNumber);
			user.setProperty("user_mobilephone", data.mobilePhoneNumber);
			user.setUnindexedProperty("user_address", data.address);
			user.setProperty("user_role", USER_ROLE_ADMIN);
			datastore.put(txn1, user);
			LOG.info("Back Office User registered: " + data.username);
			txn1.commit();
			return Response.ok().entity("Gestor de sistema criado com sucesso").build();
		} finally {
			if (txn1.isActive())
				txn1.rollback();
		}
	}
}
