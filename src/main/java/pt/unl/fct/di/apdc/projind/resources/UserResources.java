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

import pt.unl.fct.di.apdc.projind.util.AuthToken;
import pt.unl.fct.di.apdc.projind.util.MapCoordsData;

@Path("/user")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class UserResources {

	private static final Logger LOG = Logger.getLogger(LoginResource.class.getName());
	private final Gson g = new Gson();
	private static final DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

	@POST
	@Path("/getmap")
	public Response getMap(AuthToken token) {
		LOG.fine("Attempt to retrieve map coords: " + token.username);

		Transaction txn = datastore.beginTransaction();
		Key userKey = KeyFactory.createKey("User", token.username);

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
			if (!ulog.getProperty("user_login_token").equals(token.tokenID)) {
				//Authtication Error
				return Response.status(Status.FORBIDDEN).entity("Authtentication error!").build();
			}
			if ((long) ulog.getProperty("user_login_token_expiration") < System.currentTimeMillis()) {
				return Response.status(Status.FORBIDDEN).entity("Authtentication error!").build();
			}
			String[] mapCoordsString = ((String) ulog.getProperty("user_login_latlon")).split(",");
			MapCoordsData mapCoords = new MapCoordsData(Double.parseDouble(mapCoordsString[0]), Double.parseDouble(mapCoordsString[1]));
			txn.commit();
			return Response.ok().entity(g.toJson(mapCoords)).build();
		} catch (EntityNotFoundException e) { //Username does not exist
			LOG.warning("Failed login attempt for username: " + token.username);
			return Response.status(Status.FORBIDDEN).build();
		} finally {
			if (txn.isActive()) {
				txn.rollback();
				//return Response.status(Status.INTERNAL_SERVER_ERROR).build();
			}
		}
	}
}
