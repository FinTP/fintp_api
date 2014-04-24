/*
* FinTP - Financial Transactions Processing Application
* Copyright (C) 2013 Business Information Systems (Allevo) S.R.L.
*
* This program is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with this program. If not, see <http://www.gnu.org/licenses/>
* or contact Allevo at : 031281 Bucuresti, 23C Calea Vitan, Romania,
* phone +40212554577, office@allevo.ro <mailto:office@allevo.ro>, www.allevo.ro.
*/

package ro.allevo.fintpws.resources;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import ro.allevo.fintpws.exceptions.ApplicationJsonException;
import ro.allevo.fintpws.util.ResourcesUtils;

import com.sun.jersey.spi.resource.Singleton;

/**
 * Resource class implementing /api entry point and acting as sub-resource
 * locator for all main resources
 * 
 * @author horia
 * @version $Revision: 1.0 $
 */
@Path("/api")
@Singleton
public class ApiResource {
	/**
	 * Field logger.
	 */
	private static Logger logger = LogManager.getLogger(ApiResource.class
			.getName());

	/**
	 * Field PERSISTENCE_UNITNAME_CONFIG. (value is ""fintpCFG"")
	 */
	public static final String PERSISTENCE_UNITNAME_CONFIG = "fintpCFG";
	/**
	 * Field PERSISTENCE_UNITNAME_DATA. (value is ""fintpDATA"")
	 */
	static final String PERSISTENCE_UNITNAME_DATA = "fintpDATA";

	/**
	 * Field uriInfo.
	 */
	@Context
	private UriInfo uriInfo;

	/**
	 * Field configEntityManagerFactory.
	 */
	private EntityManagerFactory configEntityManagerFactory;
	/**
	 * Field dataEntityManagerFactory.
	 */
	private EntityManagerFactory dataEntityManagerFactory;

	/** Creates a new instance of ApiResource */
	public ApiResource() {
		logger.debug("Entering ApiResource.()");

		configEntityManagerFactory = Persistence
				.createEntityManagerFactory(PERSISTENCE_UNITNAME_CONFIG);
		dataEntityManagerFactory = Persistence
				.createEntityManagerFactory(PERSISTENCE_UNITNAME_DATA);

		logger.debug("Exiting ApiResource.()");
	}

	/**
	 * Sub-resource locator for /queues resource
	 * 
	 * @return QueuesResource
	 */
	@Path("queues")
	public QueuesResource getQueues() {
		// 1.Tomcat: No @PersistenceContext injection of a container managed
		// persistence unit is available - use
		// Persistence.createEntityManagerFactory(JTA_PU_NAME)
		// 2.All EntityManager instances injected using the @PersistenceContext
		// annotation are container managed. This means that the container takes
		// care of the mundane task of looking up, opening, and closing the
		// EntityManager behind the scenes

		EntityManager emc = configEntityManagerFactory.createEntityManager();
		EntityManager emd = dataEntityManagerFactory.createEntityManager();

		return new QueuesResource(uriInfo, emc, emd);
	}

	/**
	 * Sub-resource locator for /routingrules resource
	 * 
	 * @return RoutingRulesResource
	 */
	@Path("routingrules")
	public RoutingRulesResource getRoutingRules() {
		// logger.info("Requested " + uriInfo.getRequestUri());
		EntityManager emc = configEntityManagerFactory.createEntityManager();
		return new RoutingRulesResource(uriInfo, emc, null);
	}

	/**
	 * 
	 * Sub-resource locator for /timelimits resource
	 * 
	 * @return TimeLimitsResource
	 */
	@Path("timelimits")
	public TimeLimitsResource getTimeLimits() {
		// logger.info("Requested " + uriInfo.getRequestUri());
		EntityManager emc = configEntityManagerFactory.createEntityManager();
		return new TimeLimitsResource(uriInfo, emc, null);
	}
	
	/**
	 * 
	 * Sub-resource locator for /users resource
	 * 
	 * @return TimeLimitsResource
	 */
	@Path("users")
	public UsersResource getUsers() {
		// logger.info("Requested " + uriInfo.getRequestUri());
		EntityManager emc = configEntityManagerFactory.createEntityManager();
		return new UsersResource(uriInfo, emc, null);
	}

	/**
	 * Sub-resource locator for /alerts resource
	 * 
	 * @return alerts
	 */
	@Path("alerts")
	public AlertsResource getAlerts() {
		EntityManager emc = configEntityManagerFactory.createEntityManager();
		return new AlertsResource(uriInfo, emc, null, null);
	}

	/**
	 * Sub-resource locator for /messages resource
	 * 
	 * @return MessagesResource
	 */
	@Path("messages")
	public MessagesResource getMessages() {
		// logger.info("Requested " + uriInfo.getRequestUri());

		EntityManager emc = configEntityManagerFactory.createEntityManager();
		EntityManager emd = dataEntityManagerFactory.createEntityManager();
		return new MessagesResource(uriInfo, emd, emc, null);
	}

	/**
	 * Sub-resource locator for /events resource
	 * 
	 * @return EventsResource
	 */
	@Path("events")
	public EventsResource getEvents() {
		EntityManager em = dataEntityManagerFactory.createEntityManager();
		return new EventsResource(uriInfo, em, null);
	}
	
	/**
	 * Sub-resource locator for /queueactions resource
	 * 
	 * @return QueueActionsResource
	 */
	@Path("queueactions")
	public QueueActionsResource getQueueActions(){
		EntityManager emc = configEntityManagerFactory.createEntityManager();
		return new QueueActionsResource(uriInfo, emc);
	}
	
	/**
	 * Sub-resource locator for /queuetypes resource
	 * 
	 * @return QueueTypesResource
	 */
	@Path("queuetypes")
	public QueueTypesResource getQueueTypes(){
		EntityManager emc = configEntityManagerFactory.createEntityManager();
		return new QueueTypesResource(uriInfo, emc);
	}
	
	
	
	/**
	 * Sub-resource locator for /routingschemas resource
	 * 
	 * @return RoutingSchemasResource
	 */
	@Path("routingschemas")
	public RoutingSchemasResource getRoutingSchemas(){
		EntityManager emc = configEntityManagerFactory.createEntityManager();
		return new RoutingSchemasResource(uriInfo, emc);
	}
	
	/**
	 * Sub-resource locator for /routingkeywords resource
	 * 
	 * @return RoutingKeyWordsResource
	 */
	@Path("routingkeywords")
	public RoutingKeyWordsResource getRoutingKeyWords(){
		EntityManager emc = configEntityManagerFactory.createEntityManager();
		return new RoutingKeyWordsResource(uriInfo, emc,null);
	}
	
	/**
	 * Sub-resource locator for /routingkeywordmaps resource
	 * 
	 * @return RoutingKeyWordMapsResource
	 */
	@Path("routingkeywordmaps")
	public RoutingKeyWordMapsResource getRoutingKeyWordMaps(){
		EntityManager emc = configEntityManagerFactory.createEntityManager();
		return new RoutingKeyWordMapsResource(uriInfo, emc);
	}
	
	/**
	 * Sub-resource locator for /servicemaps resource
	 * 
	 * @return ServiceMapsResource
	 */
	@Path("servicemaps")
	public ServiceMapsResource getServiceMaps(){
		EntityManager emc = configEntityManagerFactory.createEntityManager();
		return new ServiceMapsResource(uriInfo, emc, null);
	}
	
	/**
	 * Sub-resource locator for /serviceperformances resource
	 * 
	 * @return ServicePerformancesResource
	 */
	@Path("serviceperformances")
	public ServicePerformancesResource getServicePerformances(){
		EntityManager emd = dataEntityManagerFactory.createEntityManager();
		return new ServicePerformancesResource(uriInfo, emd, null);
	}
	
	/**
	 * Sub-resource locator for /histories resource
	 * 
	 * @return HistoriesResource
	 */
	@Path("histories")
	public HistoriesResource getHistories(){
		EntityManager emd = dataEntityManagerFactory.createEntityManager();
		return new HistoriesResource(uriInfo, emd);
	}
	
	/**
	 * Sub-resource locator for /routingjobs resource
	 * 
	 * @return RoutingJobsResource
	 */
	@Path("routingjobs")
	public RoutingJobsResource getRoutingJobs(){
		EntityManager emd = dataEntityManagerFactory.createEntityManager();
		return new RoutingJobsResource(uriInfo, emd);
	}
	
	/**
	 * Sub-resource locator for /roles resource
	 * 
	 * @return RoutingJobsResource
	 */
	@Path("roles")
	public RolesResource getRoles(){
		EntityManager emc = configEntityManagerFactory.createEntityManager();
		return new RolesResource(uriInfo, emc);
	}
	
	/**
	 * Sub-resource locator for /batches resource
	 * 
	 * @return BatchesResource
	 */
	@Path("batches")
	public BatchesResource getBatches(){
		EntityManager emd = dataEntityManagerFactory.createEntityManager();
		return new BatchesResource(uriInfo, emd);
	}
	
	/**
	 * Sub-resource locator for /batchrequests resource
	 * 
	 * @return BatchRequestResource
	 */
	@Path("batchrequests")
	public BatchRequestsResource getBatchRequests(){
		EntityManager emd = dataEntityManagerFactory.createEntityManager();
		return new BatchRequestsResource(uriInfo, emd);
	}
	


	@GET
	@Produces(MediaType.TEXT_HTML)
	public String getApisAsHTML() {
		logger.info("Requested html " + uriInfo.getRequestUri());

		/*
		 * try { return Response.ok(new
		 * Viewable("/apidoc")).type(MediaType.TEXT_HTML).build(); }
		 * catch(Exception e ) { logger.error(e); throw e; }
		 */

		StringBuilder responseBuilder = new StringBuilder();
		responseBuilder
				.append("<html><head><title>FinTP REST API</title></head><body>");
		responseBuilder.append(String.format("<div>%s</div>",
				"This is the entry point for the FinTP API"));
		responseBuilder.append("<b>General</b><br/>");
		responseBuilder.append(String.format("<p>%s</p>",
				"All API responses are JSON"));
		responseBuilder
				.append(String
						.format("<p>%s%s%s</p>",
								"The number of objects in a collection is limited to a maximum page_size of 100.",
								"A custom page_size query parameter may be specified to alter the number of results being returned in one page.",
								"A custom page query parameter may be specified in order to retrieve a specific page."));

		// queues
		responseBuilder.append(String.format(
				"<h1><a href=\"%s\">%s</a></h1>%s",
				UriBuilder.fromPath(uriInfo.getPath()).path("queues").build()
						.getPath(), "Queues", "Provides access to queues"));
		responseBuilder.append("<h2>POST</h2>Create a queue");
		responseBuilder.append("<h2>GET</h2>List queues");

		responseBuilder.append(String.format(
				"<h1><a href=\"%s\">%s</a></h1>%s",
				UriBuilder.fromPath(uriInfo.getPath()).path("queues")
						.path("{name}").build().getPath(), "Queue",
				"Provides access to a queue named {name}"));
		responseBuilder.append("<h2>GET</h2>Show queue with name = {name}");
		responseBuilder.append("<h2>PUT</h2>Update queue with name = {name}");
		responseBuilder
				.append("<h2>DELETE</h2>Delete queue with name = {name}");

		// messages
		responseBuilder.append(String.format(
				"<h1><a href=\"%s\">%s</a></h1>%s",
				UriBuilder.fromPath(uriInfo.getPath()).path("messages").build()
						.getPath(), "Messages", "Provides access to messages"));
		responseBuilder.append("<h2>POST</h2>Create a message");
		responseBuilder.append("<h2>GET</h2>List messages");

		// routingrules
		responseBuilder.append(String.format(
				"<h1><a href=\"%s\">%s</a></h1>%s",
				UriBuilder.fromPath(uriInfo.getPath()).path("routingrules")
						.build().getPath(), "Routing rules",
				"Provides access to routing rules"));
		responseBuilder.append("<h2>POST</h2>Create a routing rule");
		responseBuilder.append("<h2>GET</h2>List routing rules");

		responseBuilder.append(String.format(
				"<h1><a href=\"%s\">%s</a></h1>%s",
				UriBuilder.fromPath(uriInfo.getPath()).path("routingrules")
						.path("{guid}").build().getPath(), "Routing Rule",
				"Provides access to a routing rule with guid {guid}"));
		responseBuilder
				.append("<h2>GET</h2>Show routing rule with guid = {guid}");
		responseBuilder
				.append("<h2>PUT</h2>Update routing rule with guid = {guid}");
		responseBuilder
				.append("<h2>DELETE</h2>Delete routing rule with guid = {guid}");

		// alerts
		responseBuilder.append(String.format(
				"<h1><a href=\"%s\">%s</a></h1>%s",
				UriBuilder.fromPath(uriInfo.getPath()).path("alerts").build()
						.getPath(), "Alerts", "Provides access to alerts"));
		responseBuilder.append("<h2>POST</h2>Create an alert");
		responseBuilder.append("<h2>GET</h2>List alerts");

		responseBuilder.append(String.format(
				"<h1><a href=\"%s\">%s</a></h1>%s",
				UriBuilder.fromPath(uriInfo.getPath()).path("alerts")
						.path("{name}").build().getPath(), "Alert",
				"Provides access to an alert named {name}"));
		responseBuilder.append("<h2>GET</h2>Show alert with name = {name}");
		responseBuilder.append("<h2>PUT</h2>Update alert with name = {name}");
		responseBuilder
				.append("<h2>DELETE</h2>Delete alert with name = {name}");

		// timelimits
		responseBuilder.append(String.format(
				"<h1><a href=\"%s\">%s</a></h1>%s",
				UriBuilder.fromPath(uriInfo.getPath()).path("timelimits")
						.build().getPath(), "Time limits",
				"Provides access to time limits"));
		responseBuilder.append("<h2>POST</h2>Create a time limit");
		responseBuilder.append("<h2>GET</h2>List time limits");

		responseBuilder.append(String.format(
				"<h1><a href=\"%s\">%s</a></h1>%s",
				UriBuilder.fromPath(uriInfo.getPath()).path("timelimits")
						.path("{guid}").build().getPath(), "Time limit",
				"Provides access to a time limit with guid {guid}"));
		responseBuilder
				.append("<h2>GET</h2>Show time limit with guid = {guid}");
		responseBuilder
				.append("<h2>PUT</h2>Update time limit with guid = {guid}");
		responseBuilder
				.append("<h2>DELETE</h2>Delete time limit with guid = {guid}");
		
		// routingschemas
		responseBuilder.append(String.format(
				"<h1><a href=\"%s\">%s</a></h1>%s",
				UriBuilder.fromPath(uriInfo.getPath()).path("routingschemas")
						.build().getPath(), "Routing schemas",
				"Provides access to routing schemas"));
		responseBuilder.append("<h2>POST</h2>Create a routing schema");
		responseBuilder.append("<h2>GET</h2>List routing schemas");

		responseBuilder.append(String.format(
				"<h1><a href=\"%s\">%s</a></h1>%s",
				UriBuilder.fromPath(uriInfo.getPath()).path("routingschemas")
						.path("{name}").build().getPath(), "Routing schema",
				"Provides access to a routing schema with name {name}"));
		responseBuilder
				.append("<h2>GET</h2>Show routing schema with name = {name}");
		responseBuilder
				.append("<h2>PUT</h2>Update routing schema with name = {name}");
		responseBuilder
				.append("<h2>DELETE</h2>Delete routing schema with name = {name}");

		responseBuilder.append("</body></html>");
		return responseBuilder.toString();
	}

	/**
	 * Returns the list of supported apis formatted as an application/json array
	 * 
	 * @return JSONArray * @throws JSONException
	 */
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public JSONArray getApisAsJsonArray() {
		// logger.info("Requested /api");

		try {
			JSONArray uriArray = new JSONArray();

			// TODO see link format if it's useful like it is
			uriArray.put(ResourcesUtils.createLink(new JSONObject(), UriBuilder
					.fromPath(uriInfo.getPath()).path("queues").build()
					.getPath(), "queues"));
			uriArray.put(ResourcesUtils.createLink(new JSONObject(), UriBuilder
					.fromPath(uriInfo.getPath()).path("messages").build()
					.getPath(), "messages"));
			uriArray.put(ResourcesUtils.createLink(new JSONObject(), UriBuilder
					.fromPath(uriInfo.getPath()).path("users").build()
					.getPath(), "users"));
			uriArray.put(ResourcesUtils.createLink(new JSONObject(), UriBuilder
					.fromPath(uriInfo.getPath()).path("events").build()
					.getPath(), "events"));
			uriArray.put(ResourcesUtils.createLink(new JSONObject(), UriBuilder
					.fromPath(uriInfo.getPath()).path("routingrules").build()
					.getPath(), "routingrules"));
			uriArray.put(ResourcesUtils.createLink(new JSONObject(), UriBuilder
					.fromPath(uriInfo.getPath()).path("alerts").build()
					.getPath(), "alerts"));
			uriArray.put(ResourcesUtils.createLink(new JSONObject(), UriBuilder
					.fromPath(uriInfo.getPath()).path("timelimits").build()
					.getPath(), "timelimits"));
			uriArray.put(ResourcesUtils.createLink(new JSONObject(), UriBuilder
					.fromPath(uriInfo.getPath()).path("routingschemas").build()
					.getPath(), "routingschemas"));
			return uriArray;
		} catch (JSONException je) {
			logger.error("Error returning /api as json", je);
			throw new ApplicationJsonException(je,
					"Error returning /api as json",
					Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
		}
	}

	/**
	 * Returns a JSONObject prefilled with the required metadata.
	 * 
	 * @param resourceClass
	 *            Class<?>
	 * @param path
	 *            String
	 * @return JSONObject * @throws JSONException
	 */
	public static JSONObject getMetaResource(String path, Class<?> resourceClass)
			throws JSONException {
		// fill in required metadata
		return new JSONObject().put("href", path).put("_type",
				resourceClass.getName());
	}
}
