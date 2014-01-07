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
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityNotFoundException;
import javax.persistence.RollbackException;
import javax.persistence.TypedQuery;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import ro.allevo.fintpws.converters.AlertsState;
import ro.allevo.fintpws.converters.AlertsType;
import ro.allevo.fintpws.exceptions.ApplicationJsonException;
import ro.allevo.fintpws.model.AlertEntity;
import ro.allevo.fintpws.util.JsonResponseWrapper;

/**
 * Resource class implementing /alerts/{name} path methods.
 * 
 * @author costi
 * @version $Revision: 1.0 $
 */
public class AlertResource {
	/**
	 * Field logger.
	 */
	private static Logger logger = LogManager.getLogger(AlertResource.class
			.getName());

	/**
	 * Field ERROR_MESSAGE_GET_ALERT. (value is ""Error returning alert : "")
	 */
	static final String ERROR_MESSAGE_GET_ALERT = "Error returning alert : ";
	/**
	 * Field ERROR_MESSAGE_PUT_ALERT. (value is ""Error updating alert : "")
	 */
	static final String ERROR_MESSAGE_PUT_ALERT = "Error updating alert : ";
	/**
	 * Field ERROR_MESSAGE_ALERT_NOT_FOUND. (value is ""Alert with name [%s] not
	 * found"")
	 */
	static final String ERROR_MESSAGE_ALERT_NOT_FOUND = "Alert with name [%s] not found";
	/**
	 * Field ERROR_REASON_JSON. (value is ""json"")
	 */
	static final String ERROR_REASON_JSON = "json";
	/**
	 * Field ERROR_REASON_NUMBER_FORMAT. (value is ""number format"")
	 */
	static final String ERROR_REASON_NUMBER_FORMAT = "number format";
	/**
	 * Field ERROR_REASON_CONFLICT. (value is ""conflict"")
	 */
	static final String ERROR_REASON_CONFLICT = "conflict";

	/**
	 * Field ERROR_REASON_ROLLBACK. (value is ""rollback"")
	 */
	static final String ERROR_REASON_ROLLBACK = "rollback";

	// actual uri info provided by parent resource
	/**
	 * Field uriInfo.
	 */
	private UriInfo uriInfo;
	/**
	 * Field entityManagerConfig.
	 */
	private EntityManager entityManagerConfig;

	/**
	 * the JPA entity
	 */
	private AlertEntity alertEntity;

	/**
	 * alert name
	 */
	private String alertName;

	/**
	 * Creates a new instance of AlertResource
	 * 
	 * @param uriInfo
	 *            UriInfo actual uri passed by parent resource
	 * @param alertName
	 *            String Alert name
	 * @param entityManagerConfig
	 *            EntityManager
	 */
	public AlertResource(UriInfo uriInfo, EntityManager entityManagerConfig,
			String alertName) {
		this.uriInfo = uriInfo;
		this.entityManagerConfig = entityManagerConfig;
		this.alertName = alertName;
		this.alertEntity = null;
		alertEntity = AlertResource.findByName(entityManagerConfig, alertName);
	}

	/**
	 * Method findByName. Looks for a AlertEntity based on it's name.
	 * 
	 * @param entityManager
	 *            EntityManager
	 * @param name
	 *            String
	 * @return AlertEntity
	 */
	public static AlertEntity findByName(EntityManager entityManager,
			String name) {
		TypedQuery<AlertEntity> query = entityManager.createNamedQuery(
				"AlertEntity.findByName", AlertEntity.class);
		List<AlertEntity> results = query.setParameter("alertname", name)
				.getResultList();
		if (!results.isEmpty()) {
			return results.get(0);
		}
		return null;
	}

	/**
	 * GET method : returns an application/json formatted alert
	 * 
	 * @return JSONObject the alert
	 */
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public JSONObject getAlert() {
		if (null == alertEntity) {
			logger.error(String
					.format(ERROR_MESSAGE_ALERT_NOT_FOUND, alertName));
			throw new EntityNotFoundException(String.format(
					ERROR_MESSAGE_ALERT_NOT_FOUND, alertName));
		}
		try {
			return AlertResource.asJson(alertEntity, uriInfo.getPath());
		} catch (JSONException je) {
			logger.error(ERROR_MESSAGE_GET_ALERT + ERROR_REASON_JSON, je);
			throw new ApplicationJsonException(je, ERROR_MESSAGE_GET_ALERT
					+ ERROR_REASON_JSON,
					Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
		}
	}

	/**
	 * PUT method : updates the alert
	 * 
	 * @param jsonEntity
	 *            JSONObject the alert holding new values
	 * @return Response
	 */
	@PUT
	@Consumes(MediaType.APPLICATION_JSON)
	public Response updateAlert(JSONObject jsonEntity) {
		if (null == alertEntity) {
			logger.error(String
					.format(ERROR_MESSAGE_ALERT_NOT_FOUND, alertName));
			throw new EntityNotFoundException(String.format(
					ERROR_MESSAGE_ALERT_NOT_FOUND, alertName));
		}

		try {
			if (jsonEntity.has("alertid")) {
				alertEntity.setAlertid(jsonEntity.optString("alertid"));
			}
			if (jsonEntity.has("alertname")) {
				alertEntity.setAlertname(jsonEntity.optString("alertname"));
			}
			if (jsonEntity.has("description")) {
				alertEntity.setDescription(jsonEntity.optString("description"));
			}
			if (jsonEntity.has("emailaddress")) {
				alertEntity.setEmailaddress(jsonEntity
						.optString("emailaddress"));
			}
			if (jsonEntity.has("endtime")) {
				alertEntity.setEndtime(jsonEntity.optString("endtime"));
			}
			if (jsonEntity.has("frequency")) {
				alertEntity.setFrequency(new BigDecimal(jsonEntity
						.optInt("frequency")));
			}
			if (jsonEntity.has("queueid")) {
				alertEntity.setQueueid(new BigDecimal(jsonEntity
						.optInt("queueid")));
			}
			if (jsonEntity.has("starttime")) {
				alertEntity.setStarttime(jsonEntity.optString("starttime"));
			}
			if (jsonEntity.has("state")) {
				alertEntity
						.setState(AlertsState.fromName(jsonEntity.optString("state")));
			}
			if (jsonEntity.has("type")) {
				alertEntity.setType(AlertsType.fromName(jsonEntity.optString("type"))); 
			}
			if (jsonEntity.has("weekdays")) {
				alertEntity.setWeekdays(jsonEntity.optString("weekdays"));
			}

			entityManagerConfig.getTransaction().begin();
			entityManagerConfig.merge(alertEntity);
			entityManagerConfig.getTransaction().commit();

		} catch (NumberFormatException nfe) {
			logger.error(ERROR_MESSAGE_PUT_ALERT + ERROR_REASON_NUMBER_FORMAT,
					nfe);
			throw new ApplicationJsonException(nfe, ERROR_MESSAGE_PUT_ALERT
					+ ERROR_REASON_NUMBER_FORMAT,
					Response.Status.BAD_REQUEST.getStatusCode());
		} catch (RollbackException re) {
			ApplicationJsonException.handleSQLException(re,
					ERROR_MESSAGE_PUT_ALERT, logger);

			// log and rethrow the original error
			logger.error(ERROR_MESSAGE_PUT_ALERT + ERROR_REASON_ROLLBACK, re);
			throw re;
		} finally {
			// Tomcat does not support container managed transactions
			if (null != entityManagerConfig) {
				entityManagerConfig.close();
			}
		}

		return JsonResponseWrapper.getResponse(Response.Status.OK,
				"alert updated");
	}

	/**
	 * DELETE method : deletes the alert
	 * 
	 * @return Response
	 */
	@DELETE
	public Response deleteAlert() {
		if (null == alertEntity) {
			logger.error(String
					.format(ERROR_MESSAGE_ALERT_NOT_FOUND, alertName));
			throw new EntityNotFoundException(String.format(
					ERROR_MESSAGE_ALERT_NOT_FOUND, alertName));
		}
		try {
			entityManagerConfig.getTransaction().begin();
			entityManagerConfig.remove(alertEntity);
			entityManagerConfig.getTransaction().commit();
		} finally {
			if (null != entityManagerConfig) {
				entityManagerConfig.close();
			}
		}
		return JsonResponseWrapper.getResponse(Response.Status.OK,
				"alert deleted");
	}

	/**
	 * Returns the resource formatted as json
	 * 
	 * @param alertEntity
	 *            AlertEntity
	 * @param path
	 *            String
	 * @return JSONObject
	 * @throws JSONException
	 */
	public static JSONObject asJson(AlertEntity alertEntity, String path)
			throws JSONException {
		JSONObject alertAsJson = ApiResource.getMetaResource(path,
				AlertResource.class);

		// fill data
		alertAsJson.put("alertid", alertEntity.getAlertid())
				.put("queuename", alertEntity.getQueueEntity().getName())
				.put("alertname", alertEntity.getAlertname())
				.put("description", alertEntity.getDescription())
				.put("emailaddress", alertEntity.getEmailaddress())
				.put("endtime", alertEntity.getEndtime())
				.put("frequency", alertEntity.getFrequency())
				.put("starttime", alertEntity.getStarttime())
				.put("state", alertEntity.getState().getName())
				.put("type", alertEntity.getType().getName())
				.put("weekdays", alertEntity.getWeekdays());

		return alertAsJson;
	}

	/**
	 * Method toString.
	 * 
	 * @return String
	 */
	public String toString() {
		return alertEntity.getAlertname();
	}
}
