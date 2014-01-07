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

import java.math.BigDecimal;
import java.net.URI;
import java.sql.SQLIntegrityConstraintViolationException;
import java.text.ParseException;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.RollbackException;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
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
import ro.allevo.fintpws.model.RoutedMessageEntity;
import ro.allevo.fintpws.model.StatusEntity;
import ro.allevo.fintpws.util.JsonResponseWrapper;
import ro.allevo.fintpws.util.ResourcesUtils;

/**
 * Resource class implementing /events path methods and acting as /events/{id}
 * sub-resource locator.
 * 
 * @author costi
 * @version $Revision: 1.0 $
 */
public class EventsResource extends PagedCollection {
	/**
	 * Field logger.
	 */
	private static Logger logger = LogManager.getLogger(EventsResource.class
			.getName());

	/**
	 * Field ERROR_MESSAGE_GET_EVENTS. (value is ""Error returning events : "")
	 */
	static final String ERROR_MESSAGE_GET_EVENTS = "Error returning events : ";
	/**
	 * Field ERROR_MESSAGE_POST_EVENTS. (value is ""Error creating event : "")
	 */
	static final String ERROR_MESSAGE_POST_EVENTS = "Error creating event : ";
	/**
	 * Field ERROR_REASON_TIME_FORMAT. (value is ""time format"")
	 */
	static final String ERROR_REASON_TIME_FORMAT = "time format";
	/**
	 * Field ERROR_REASON_JSON. (value is ""json"")
	 */
	static final String ERROR_REASON_JSON = "json";
	/**
	 * Field ERROR_REASON_NUMBER_FORMAT. (value is ""number format"")
	 */
	static final String ERROR_REASON_NUMBER_FORMAT = "number format";
	/**
	 * Field ERROR_REASON_PARSE. (value is ""parse"")
	 */
	static final String ERROR_REASON_PARSE = "parse";
	/**
	 * Field ERROR_REASON_ROLLBACK. (value is ""rollback"")
	 */
	static final String ERROR_REASON_ROLLBACK = "rollback";

	/**
	 * actual uri info provided by parent resource
	 */
	private UriInfo uriInfo;

	/**
	 * Field entityManager.
	 */
	private EntityManager entityManager;

	/**
	 * Creates a new instance of EventsResource
	 * 
	 * @param uriInfo
	 *            UriInfo
	 * @param entityManager
	 *            EntityManager
	 * @param messageEntity
	 *            RoutedMessageEntity
	 */
	public EventsResource(UriInfo uriInfo, EntityManager entityManager,
			RoutedMessageEntity messageEntity) {
		// if the events are not related to a message, return all ( subject to
		// limits )
		super(uriInfo, entityManager.createNamedQuery("StatusEntity.findAll",
				StatusEntity.class), entityManager.createNamedQuery(
				"StatusEntity.findTotal", Integer.class));

		// if the events are related to a message, find them using the
		// correlation id
		if (null != messageEntity) {
			this.setItemsQuery(entityManager.createNamedQuery(
					"StatusEntity.findByCorrelationId", StatusEntity.class)
					.setParameter("correlationid", messageEntity.getCorrelationid()));
			this.setTotalQuery(entityManager.createNamedQuery(
					"StatusEntity.findTotalByCorrelationId", Integer.class)
					.setParameter("correlationid", messageEntity.getCorrelationid()));
		}
		
		/*
		if (ResourcesUtils.hasSortOrFilter(uriInfo)) {
			this.setItemsQuery(ResourcesUtils.getTypedQuery(uriInfo,
			entityManager, StatusEntity.class, null, null));
			this.setTotalQuery(ResourcesUtils.getCountTypedQuery(uriInfo,
			entityManager, StatusEntity.class, null, null));
		}*/
		
		this.uriInfo = uriInfo;
		this.entityManager = entityManager;
	}

	/**
	 * Returns a event sub-resource named eventId
	 * 
	 * @param eventGuid
	 *            String
	 * @return EventResource The event sub-resource
	 */
	@Path("{id}")
	public EventResource getEvent(@PathParam("id") String eventGuid) {
		return new EventResource(uriInfo, entityManager, eventGuid);
	}

	/**
	 * GET method : returns an application/json formatted list of events
	 * 
	 * @return JSONObject The list of events
	 */
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public JSONObject getEventsAsJson() {
		try {
			getPage();
			return asJson();
		} catch (JSONException je) {
			logger.error(ERROR_MESSAGE_GET_EVENTS + ERROR_REASON_JSON, je);
			throw new ApplicationJsonException(je, ERROR_MESSAGE_GET_EVENTS
					+ ERROR_REASON_JSON,
					Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
		}
	}

	/**
	 * POST method : creates an event
	 * 
	 * @param jsonEntity
	 *            JSONObject
	 * @return Response The URI of the newly created event
	 * @throws JSONException
	 */
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	public Response postForm(JSONObject jsonEntity) throws JSONException {
		final StatusEntity statusEntity = new StatusEntity();
		try {
			// fill required data
			statusEntity.setGuid(jsonEntity.getString("guid"));
			statusEntity
					.setCorrelationid(jsonEntity.getString("correlationid"));
			statusEntity.setEventdate(ResourcesUtils.getTimestamp(jsonEntity
					.getString("eventdate")));
			statusEntity.setInsertdate(ResourcesUtils.getTimestamp(jsonEntity
					.getString("insertdate")));
			statusEntity.setMachine(jsonEntity.getString("machine"));
			statusEntity.setMessage(jsonEntity.getString("message"));
			statusEntity
					.setService(new BigDecimal(jsonEntity.getInt("service")));
			statusEntity.setType(jsonEntity.getString("type"));
			// fill optional data
			statusEntity.setAdditionalinfo(jsonEntity
					.optString("additionalinfo"));
			statusEntity.setInnerexception(jsonEntity
					.optString("innerexception"));

			entityManager.getTransaction().begin();
			entityManager.persist(statusEntity);
			entityManager.getTransaction().commit();
		} catch (JSONException je) {
			logger.error(ERROR_MESSAGE_POST_EVENTS + ERROR_REASON_JSON, je);
			throw new ApplicationJsonException(je, ERROR_MESSAGE_POST_EVENTS
					+ ERROR_REASON_JSON,
					Response.Status.BAD_REQUEST.getStatusCode());
		} catch (NumberFormatException nfe) {
			logger.error(
					ERROR_MESSAGE_POST_EVENTS + ERROR_REASON_NUMBER_FORMAT, nfe);
			throw new ApplicationJsonException(nfe, ERROR_MESSAGE_POST_EVENTS
					+ ERROR_REASON_NUMBER_FORMAT,
					Response.Status.BAD_REQUEST.getStatusCode());
		} catch (ParseException pe) {
			logger.error(ERROR_MESSAGE_POST_EVENTS + ERROR_REASON_PARSE, pe);
			throw new ApplicationJsonException(pe, ERROR_MESSAGE_POST_EVENTS
					+ ERROR_REASON_PARSE,
					Response.Status.BAD_REQUEST.getStatusCode());
		} catch (RollbackException re) {
			// traverse the cause to find a possible constraint violation
			ApplicationJsonException.handleSQLException(re, ERROR_MESSAGE_POST_EVENTS, logger);
			
			// log and rethrow the original error
			logger.error(ERROR_MESSAGE_POST_EVENTS + ERROR_REASON_ROLLBACK, re);
			throw re;
		} finally {
			if (null != entityManager) {
				entityManager.close();
			}
		}
		// path to the new event
		URI queueUri = getUriInfo().getAbsolutePathBuilder()
				.path(statusEntity.getGuid()).build();
		return JsonResponseWrapper.getResponse(Response.Status.CREATED,
				queueUri);
	}

	/**
	 * Returns the resource formatted as json
	 * 
	 * @return JSONObject
	 */
	@SuppressWarnings("unchecked")
	public JSONObject asJson() throws JSONException {
		JSONObject eventsAsJson = super.asJson();
		// fill data
		JSONArray eventsArray = new JSONArray();
		List<?> items = getItems();

		if (items.size() > 0) {
			for (StatusEntity eventEntity : (List<StatusEntity>) items) {
				eventsArray
						.put(EventResource.asJson(eventEntity,
								UriBuilder.fromPath(getUriInfo().getPath())
										.path(eventEntity.getGuid()).build()
										.getPath()));
			}
		}
		eventsAsJson.put("events", eventsArray);
		return eventsAsJson;
	}
}
