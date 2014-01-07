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
import javax.persistence.EntityNotFoundException;
import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import ro.allevo.fintpws.exceptions.ApplicationJsonException;
import ro.allevo.fintpws.model.StatusEntity;
import ro.allevo.fintpws.util.ResourcesUtils;

/**
 * Resource class implementing /events/{id} path methods.
 * 
 * @author costi
 * @version $Revision: 1.0 $
 */
public class EventResource {
	/**
	 * Field logger.
	 */
	private static Logger logger = LogManager.getLogger(EventResource.class
			.getName());

	/**
	 * Field ERROR_MESSAGE_GET_EVENT. (value is ""Error returning event : "")
	 */
	static final String ERROR_MESSAGE_GET_EVENT = "Error returning event : ";
	
	/**
	 * Field ERROR_MESSAGE_E_NOT_FOUND. (value is ""Event with guid [%s] not
	 * found"")
	 */
	static final String ERROR_MESSAGE_E_NOT_FOUND = "Event with guid [%s] not found";

	/**
	 * Field ERROR_REASON_JSON. (value is ""json"")
	 */
	static final String ERROR_REASON_JSON = "json";
	/**
	 * Field ERROR_REASON_NUMBER_FORMAT. (value is ""number format"")
	 */
	static final String ERROR_REASON_NUMBER_FORMAT = "number format";

	/**
	 * Field eventId.
	 */
	private String eventId = "";
	// actual uri info provided by parent resource
	/**
	 * Field uriInfo.
	 */
	private UriInfo uriInfo;

	// the JPA entity
	/**
	 * Field eventEntity.
	 */
	private StatusEntity eventEntity;

	/**
	 * Creates a new instance of EventResource
	 * 
	 * @param uriInfo
	 *            UriInfo actual uri passed by parent resource
	 * @param entityManager
	 *            EntityManager passed by parent resource
	 * @param eventId
	 *            String Event id
	 */
	public EventResource(UriInfo uriInfo, EntityManager entityManager,
			String eventId) {
		this.uriInfo = uriInfo;
		this.eventId = eventId;
		this.eventEntity =  entityManager.find(StatusEntity.class, eventId);
	}

	/**
	 * GET method : returns an application/json formatted event
	 * 
	 * @return JSONObject the event
	 */
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public JSONObject getEvent() {
		if (null == eventEntity) {
			logger.error(String.format(ERROR_MESSAGE_E_NOT_FOUND, eventId));
			throw new EntityNotFoundException(String.format(
					ERROR_MESSAGE_E_NOT_FOUND, eventId));
		}
		try {
			return EventResource.asJson(eventEntity, uriInfo.getPath());
		} catch (JSONException je) {
			logger.error(ERROR_MESSAGE_GET_EVENT + ERROR_REASON_JSON, je);
			throw new ApplicationJsonException(je, ERROR_MESSAGE_GET_EVENT
					+ ERROR_REASON_JSON,
					Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
		}
	}

	/**
	 * Returns the resource formatted as json
	 * 
	 * @param eventEntity
	 *            BusinessEvent
	 * @param path
	 *            String
	 * 
	 * @return JSONObject * @throws JSONException
	 */
	public static JSONObject asJson(StatusEntity eventEntity, String path)
			throws JSONException {
		JSONObject eventAsJson = ApiResource.getMetaResource(path,
				EventResource.class);

		eventAsJson.put("guid", eventEntity.getGuid())
				.put("additionalinfo", eventEntity.getAdditionalinfo())
				.put("correlationid", eventEntity.getCorrelationid())
				.put("eventdate",
						ResourcesUtils.getIsoDateFromTimestamp(eventEntity
								.getEventdate()))
				
				.put("innerexception", eventEntity.getInnerexception())
				.put("insertdate",
						ResourcesUtils.getIsoDateFromTimestamp(eventEntity
								.getInsertdate()))
				.put("machine", eventEntity.getMachine())
				.put("message", eventEntity.getMessage())
				.put("service", eventEntity.getService())
				.put("type", eventEntity.getType());

		return eventAsJson;
	}

	/**
	 * Method toString.
	 * 
	 * @return String
	 */
	public String toString() {
		return eventEntity.getGuid();
	}
}
