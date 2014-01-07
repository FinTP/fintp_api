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

import ro.allevo.fintpws.converters.AlertsState;
import ro.allevo.fintpws.converters.AlertsType;
import ro.allevo.fintpws.exceptions.ApplicationJsonException;
import ro.allevo.fintpws.model.AlertEntity;
import ro.allevo.fintpws.model.EntryQueueEntity;
import ro.allevo.fintpws.model.QueueEntity;
import ro.allevo.fintpws.util.JsonResponseWrapper;

/**
 * Resource class implementing /alerts path methods and acting as /alerts/{name}
 * sub-resource locator to {@link AlertResource}.
 * 
 * @author costi
 * @version $Revision: 1.0 $
 */
public class AlertsResource extends PagedCollection {
	/**
	 * Field logger.
	 */
	private static Logger logger = LogManager.getLogger(AlertsResource.class
			.getName());

	/**
	 * Field ERROR_MESSAGE_GET_ALERTS. (value is ""Error returning alerts : "")
	 */
	static final String ERROR_MESSAGE_GET_ALERTS = "Error returning alerts : ";
	/**
	 * Field ERROR_MESSAGE_POST_ALERTS. (value is ""Error creating alert : "")
	 */
	static final String ERROR_MESSAGE_POST_ALERTS = "Error creating alert : ";

	/**
	 * Field ERROR_REASON_JSON. (value is ""json"")
	 */
	static final String ERROR_REASON_JSON = "json";
	/**
	 * Field ERROR_REASON_NUMBER_FORMAT. (value is ""number format"")
	 */
	static final String ERROR_REASON_NUMBER_FORMAT = "number format";
	/**
	 * Field ERROR_REASON_ROLLBACK_EXCEPTION. (value is ""conflict"")
	 */
	static final String ERROR_REASON_CONFLICT = "conflict";

	/**
	 * Field ERROR_REASON_ROLLBACK. (value is ""rollback"")
	 */
	static final String ERROR_REASON_ROLLBACK = "rollback";

	/**
	 * Field entityManagerConfig.
	 */
	private EntityManager entityManagerConfig;

	/**
	 * Creates a new instance of AlertsResource
	 * 
	 * @param uriInfo
	 *            UriInfo
	 * 
	 * @param entityManagerConfig
	 *            EntityManager
	 * @param alertsEntity
	 *            AlertEntity
	 * @param queueEntity QueueEntity
	 */
	public AlertsResource(UriInfo uriInfo, EntityManager entityManagerConfig,
			AlertEntity alertsEntity, QueueEntity queueEntity) {
		super(uriInfo, entityManagerConfig.createNamedQuery(
				"AlertEntity.findAll", AlertEntity.class), entityManagerConfig
				.createNamedQuery("AlertEntity.findTotal", Long.class));
		if (null != queueEntity) {
			this.setItemsQuery(entityManagerConfig
					.createNamedQuery("AlertEntity.findAllQueue",
							EntryQueueEntity.class)
					.setParameter("queueid", queueEntity.getGuid())
					);
			this.setTotalQuery(entityManagerConfig.createNamedQuery(
					"AlertEntity.findTotalQueue", Long.class).setParameter(
					"queueid", queueEntity.getGuid()));
		}
		this.entityManagerConfig = entityManagerConfig;
	}

	/**
	 * Returns a alert sub-resource named alertName
	 * 
	 * @param alertName
	 *            String name of the alert as last element in the path
	 * @return AlertResource The alert sub-resource
	 */
	@Path("{name}")
	public AlertResource getAlert(@PathParam("name") String alertName) {
		return new AlertResource(getUriInfo(), entityManagerConfig, alertName);
	}

	/**
	 * GET method : returns an application/json formatted list of alerts
	 * 
	 * @return JSONObject The list of alerts
	 */
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public JSONObject getAlertsAsJson() {
		try {
			getPage();
			return asJson();
		} catch (JSONException je) {
			logger.error(ERROR_MESSAGE_GET_ALERTS + ERROR_REASON_JSON, je);
			throw new ApplicationJsonException(je, ERROR_MESSAGE_GET_ALERTS
					+ ERROR_REASON_JSON,
					Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
		}
	}

	/**
	 * POST method : creates a alert
	 * @param jsonEntity
	 *            JSONObject The alert to be created
	 * @return Response The URI of the newly created alert
	 * @throws JSONException
	 */
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	public Response postForm(JSONObject jsonEntity) {
		final AlertEntity alertEntity = new AlertEntity();
		try {

			alertEntity.setAlertname(jsonEntity.optString("alertname"));
			alertEntity.setDescription(jsonEntity.optString("description"));
			alertEntity.setEmailaddress(jsonEntity.optString("emailaddress"));
			alertEntity.setEndtime(jsonEntity.optString("endtime"));
			alertEntity.setFrequency(new BigDecimal(jsonEntity
					.optInt("frequency")));
			alertEntity
					.setQueueid(new BigDecimal(jsonEntity.optInt("queueid")));
			alertEntity.setStarttime(jsonEntity.optString("starttime"));
			alertEntity.setState(AlertsState.fromName(jsonEntity.optString("state")));
			alertEntity.setType(AlertsType.fromName(jsonEntity.optString("type"))); 
			alertEntity.setWeekdays(jsonEntity.optString("weekdays"));
			// perform update
			entityManagerConfig.getTransaction().begin();
			entityManagerConfig.persist(alertEntity);
			entityManagerConfig.getTransaction().commit();
		} catch (NumberFormatException nfe) {
			logger.error(
					ERROR_MESSAGE_POST_ALERTS + ERROR_REASON_NUMBER_FORMAT, nfe);
			throw new ApplicationJsonException(nfe, ERROR_MESSAGE_POST_ALERTS
					+ ERROR_REASON_NUMBER_FORMAT,
					Response.Status.BAD_REQUEST.getStatusCode());
		} catch (RollbackException re) {
			ApplicationJsonException.handleSQLException(re,
					ERROR_MESSAGE_POST_ALERTS, logger);

			// log and rethrow the original error
			logger.error(ERROR_MESSAGE_POST_ALERTS + ERROR_REASON_ROLLBACK, re);
			throw re;
		} finally {
			if (null != entityManagerConfig) {
				entityManagerConfig.close();
			}
		}

		// path to the new alert
		URI alertUri = getUriInfo().getAbsolutePathBuilder()
				.path(alertEntity.getAlertname()).build();
		return JsonResponseWrapper.getResponse(Response.Status.CREATED,
				alertUri);
	}

	/**
	 * Returns the resource formatted as json
	 * @return JSONObject * @throws JSONException * @throws JSONException
	 */
	@SuppressWarnings("unchecked")
	public JSONObject asJson() throws JSONException {
		JSONObject alertsAsJson = super.asJson();

		// fill data
		JSONArray alertsArray = new JSONArray();
		List<?> items = getItems();
		if (items.size() > 0) {
			for (AlertEntity alertEntity : (List<AlertEntity>) items) {
				alertsArray.put(AlertResource.asJson(
						alertEntity,
						UriBuilder.fromPath(getUriInfo().getPath())
								.path(alertEntity.getAlertname()).build()
								.getPath()));
			}
		}
		alertsAsJson.put("alerts", alertsArray);
		return alertsAsJson;
	}
}
