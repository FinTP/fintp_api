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

/**
 * 
 */
package ro.allevo.fintpws.resources;

import java.math.BigDecimal;

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

import ro.allevo.fintpws.exceptions.ApplicationJsonException;
import ro.allevo.fintpws.model.QueueActionEntity;
import ro.allevo.fintpws.util.JsonResponseWrapper;

/**
 * @author anda
 * @version $Revision: 1.0 $
 */

public class QueueActionResource {

	/**
	 * Field logger
	 */
	private static final Logger logger = LogManager
			.getLogger(QueueActionResource.class);

	/**
	 * Field ERROR_MESSAGE_GET_QUEUE_ACTION. (value is ""Error returning queue
	 * action : "")
	 */
	static final String ERROR_MESSAGE_GET_QUEUE_ACTION = "Error returning queue action : ";
	/**
	 * Field ERROR_MESSAGE_PUT_QUEUE_ACTION. (value is ""Error updating queue
	 * action : "")
	 */
	static final String ERROR_MESSAGE_PUT_QUEUE_ACTION = "Error updating queue action : ";
	/**
	 * Field ERROR_MESSAGE_QUEUE_ACTION_NOT_FOUND. (value is ""Queue action with
	 * name [%s] not found"")
	 */
	static final String ERROR_MESSAGE_QUEUE_ACTION_NOT_FOUND = "Queue action with action [%s] not found";
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
	/**
	 * Field ERROR_REASON_PARSE. (value is ""parse"")
	 */
	static final String ERROR_REASON_PARSE = "parse";

	/**
	 * Field uriInfo
	 */
	private UriInfo uriInfo;
	/**
	 * Field entityManagerConfig.
	 */
	private EntityManager entityManagerConfig;

	/**
	 * the JPA entity
	 */
	private QueueActionEntity queueActionEntity;

	/**
	 * default constructor
	 */
	public QueueActionResource() {

	}

	/**
	 * action
	 */
	private String action;

	/**
	 * Constructor for QueueActionResourse.
	 * 
	 * @param uriInfo
	 *            UriInfo
	 * @param entityManagerConfig
	 *            EntityManager
	 * @param action
	 *            String
	 */
	public QueueActionResource(UriInfo uriInfo,
			EntityManager entityManagerConfig, String action) {
		this.uriInfo = uriInfo;
		this.entityManagerConfig = entityManagerConfig;
		this.action = action;
		queueActionEntity = null;

		queueActionEntity = findByAction(entityManagerConfig, action);
	}

	/**
	 * 
	 * @param entityManager
	 *            EntityManager
	 * @param action
	 *            String
	 * @return QueueActionEntity
	 */

	public static QueueActionEntity findByAction(EntityManager entityManager,
			String action) {

		final TypedQuery<QueueActionEntity> query = entityManager
				.createNamedQuery("QueueActionEntity.findByAction",
						QueueActionEntity.class);

		final java.util.List<QueueActionEntity> results = query.setParameter(
				"action", action).getResultList();
		if (!results.isEmpty()) {
			return results.get(0);
		}
		return null;
	}

	/**
	 * GET Method : returns an application/json formatted queue action
	 * 
	 * @return JSONObject the queeu action
	 */

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public JSONObject getQueueAction() {

		if (null == queueActionEntity) {
			logger.error(String.format(ERROR_MESSAGE_QUEUE_ACTION_NOT_FOUND,
					action));
			throw new EntityNotFoundException(String.format(
					ERROR_MESSAGE_QUEUE_ACTION_NOT_FOUND, action));
		}
		try {
			return QueueActionResource.asJson(queueActionEntity,
					uriInfo.getPath());
		} catch (JSONException je) {
			logger.error(ERROR_MESSAGE_GET_QUEUE_ACTION + ERROR_REASON_JSON, je);
			throw new ApplicationJsonException(je,
					ERROR_MESSAGE_GET_QUEUE_ACTION + ERROR_REASON_JSON,
					Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
		}
	}

	/**
	 * PUT method: updates the queue action
	 * 
	 * @param jsonEntity
	 *            JSONObject the queue action holding new values
	 * @return Response
	 */
	@PUT
	@Consumes(MediaType.APPLICATION_JSON)
	public Response updateQueueAction(JSONObject jsonEntity) {
		if (null == queueActionEntity) {
			logger.error(String.format(ERROR_MESSAGE_QUEUE_ACTION_NOT_FOUND,
					action));
			throw new EntityNotFoundException(String.format(
					ERROR_MESSAGE_QUEUE_ACTION_NOT_FOUND, action));
		}

		try {
			
			if (jsonEntity.has("action")) {
				queueActionEntity.setAction(jsonEntity.optString("action"));
			}
			if (jsonEntity.has("description")) {
				queueActionEntity.setDescription(jsonEntity
						.optString("description"));
			}
			if (jsonEntity.has("selmsg")) {
				queueActionEntity.setSelmsg(new BigDecimal(jsonEntity
						.optInt("selmsg")));
			}
			if (jsonEntity.has("groupmsg")) {
				queueActionEntity.setGroupmsg(new BigDecimal(jsonEntity
						.optInt("groupmsg")));
			}
			if (jsonEntity.has("optionvalues")) {
				queueActionEntity.setOptionvalues(jsonEntity
						.optString("optionvalues"));
			}
			
			if(jsonEntity.has("currmsg")){
				queueActionEntity.setCurrmsg(new BigDecimal(jsonEntity
						.optInt("currmsg")));
			}

			entityManagerConfig.getTransaction().begin();
			entityManagerConfig.merge(queueActionEntity);
			entityManagerConfig.getTransaction().commit();
		}catch (NumberFormatException nfe) {
			logger.error(ERROR_MESSAGE_PUT_QUEUE_ACTION
					+ ERROR_REASON_NUMBER_FORMAT, nfe);
			throw new ApplicationJsonException(nfe,
					ERROR_MESSAGE_PUT_QUEUE_ACTION + ERROR_REASON_JSON,
					Response.Status.BAD_REQUEST.getStatusCode());
		} catch (RollbackException re) {
			ApplicationJsonException.handleSQLException(re,
					ERROR_MESSAGE_PUT_QUEUE_ACTION, logger);
			logger.error(
					ERROR_MESSAGE_PUT_QUEUE_ACTION + ERROR_REASON_ROLLBACK, re);
			throw re;
		} finally {
			if (null != entityManagerConfig) {
				entityManagerConfig.close();
			}
		}

		return JsonResponseWrapper.getResponse(Response.Status.OK,
				"action queue updated");
	}

	/**
	 * DELETE method : deletes the queue action
	 * 
	 * @return Response
	 */

	@DELETE
	public Response deleteQueueAction() {
		if (null == queueActionEntity) {
			logger.error(String.format(ERROR_MESSAGE_QUEUE_ACTION_NOT_FOUND,
					action));
			throw new EntityNotFoundException(String.format(
					ERROR_MESSAGE_QUEUE_ACTION_NOT_FOUND, action));
		}
		try {
			entityManagerConfig.getTransaction().begin();
			entityManagerConfig.remove(queueActionEntity);
			entityManagerConfig.getTransaction().commit();
		} finally {
			if (null != entityManagerConfig) {
				entityManagerConfig.close();
			}
		}

		return JsonResponseWrapper.getResponse(Response.Status.OK,
				"queue action deleted");
	}

	/**
	 * Returns the resource formatted as json
	 * 
	 * @param queueActionEntity
	 *            queueActionEntity
	 * @param path
	 *            String
	 * @return JSONObject
	 * @throws JSONException
	 */
	public static JSONObject asJson(QueueActionEntity queueActionEntity,
			String path) throws JSONException {
		final JSONObject queueActionAsJson = ApiResource.getMetaResource(path,
				QueueActionResource.class);

		queueActionAsJson
				.put("action", queueActionEntity.getAction())
				.put("description", queueActionEntity.getDescription())
				.put("currmsg", queueActionEntity.getCurrmsg())
				.put("selmsg", queueActionEntity.getSelmsg())
				.put("groupmsg", queueActionEntity.getGroupmsg())
				.put("optionvalues", queueActionEntity.getOptionvalues());

		return queueActionAsJson;
	}

	/**
	 * Method toString.
	 * 
	 * @return String
	 */
	public String toString() {
		return String.valueOf(queueActionEntity.getActionid());
	}
}
