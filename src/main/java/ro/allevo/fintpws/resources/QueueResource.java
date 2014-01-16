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
import javax.persistence.Query;
import javax.persistence.RollbackException;
import javax.persistence.TypedQuery;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
//import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.springframework.security.access.AccessDeniedException;

import ro.allevo.fintpws.exceptions.ApplicationJsonException;
//import ro.allevo.fintpws.model.QMovePrivMapEntity;
import ro.allevo.fintpws.model.QueueEntity;
import ro.allevo.fintpws.model.QueueTypeEntity;
import ro.allevo.fintpws.model.ServiceMapEntity;
import ro.allevo.fintpws.security.RolesUtils;
import ro.allevo.fintpws.util.JsonResponseWrapper;
import ro.allevo.fintpws.util.ResourcesUtils;

/**
 * Resource class implementing /queues/{name} path methods.
 * 
 * @author horia
 * @version $Revision: 1.0 $
 */
public class QueueResource {
	/**
	 * Field logger.
	 */
	private static Logger logger = LogManager.getLogger(QueueResource.class
			.getName());

	/**
	 * Field ERROR_MESSAGE_GET_QUEUE. (value is ""Error returning queue : "")
	 */
	static final String ERROR_MESSAGE_GET_QUEUE = "Error returning queue : ";
	/**
	 * Field ERROR_MESSAGE_PUT_QUEUE. (value is ""Error updating queue : "")
	 */
	static final String ERROR_MESSAGE_PUT_QUEUE = "Error updating queue : ";
	/**
	 * Field ERROR_MESSAGE_Q_NOT_FOUND. (value is ""Queue with name [%s] not
	 * found"")
	 */
	static final String ERROR_MESSAGE_Q_NOT_FOUND = "Queue with name [%s] not found";
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
	 * Field entityManagerData.
	 */
	private EntityManager entityManagerData;

	/**
	 * the JPA entity
	 */
	private QueueEntity queueEntity;

	/**
	 * queue name
	 */
	private String queueName;

	/**
	 * Creates a new instance of QueueResource
	 * 
	 * @param uriInfo
	 *            UriInfo actual uri passed by parent resource
	 * 
	 * @param queueName
	 *            String Queue name
	 * @param entityManagerConfig
	 *            EntityManager
	 * @param entityManagerData
	 *            EntityManager
	 */
	public QueueResource(UriInfo uriInfo, EntityManager entityManagerConfig,
			EntityManager entityManagerData, String queueName) {
		this.uriInfo = uriInfo;
		this.entityManagerConfig = entityManagerConfig;
		this.entityManagerData = entityManagerData;
		this.queueName = queueName;
		this.queueEntity = null;

		queueEntity = QueueResource.findByName(entityManagerConfig, queueName);
	}

	/**
	 * Method findByName. Looks for a QueueEntity based on it's name.
	 * 
	 * @param entityManager
	 *            EntityManager
	 * @param name
	 *            String
	 * @return QueueEntity
	 */
	public static QueueEntity findByName(EntityManager entityManager,
			String name) {
		// entityManager.find is much faster than a query, but name is char (
		// not varchar ) and can't be used as ID
		// queueEntity = entityManager.find(QueueEntity.class, queueName);
		// TODO : change datatype to varchar and mark name as @ID in the entity
		// Also, not using getSingleResult because unchecked exception
		// NoResultException can actually be recovered from
		TypedQuery<QueueEntity> query = entityManager.createNamedQuery(
				"QueueEntity.findByName", QueueEntity.class);
		List<QueueEntity> results = query.setParameter("name", name)
				.getResultList();
		if (!results.isEmpty()) {
			return results.get(0);
		}
		return null;
	}
	
	
	/**
	 * GET method : returns an application/json formatted queue
	 * 
	 * @return JSONObject the queue
	 */
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public JSONObject getQueue() {
		if(!RolesUtils.hasUserOrAdministratorRole()){
			throw new AccessDeniedException("Access denied");
		}
		if (null == queueEntity) {
			logger.error(String.format(ERROR_MESSAGE_Q_NOT_FOUND, queueName));
			throw new EntityNotFoundException(String.format(
					ERROR_MESSAGE_Q_NOT_FOUND, queueName));
		}
		try {
			return QueueResource.asJson(queueEntity, uriInfo.getPath());
		} catch (JSONException je) {
			logger.error(ERROR_MESSAGE_GET_QUEUE + ERROR_REASON_JSON, je);
			throw new ApplicationJsonException(je, ERROR_MESSAGE_GET_QUEUE
					+ ERROR_REASON_JSON,
					Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
		}
	}

	/**
	 * Sub-resource locator for /alerts resource
	 * 
	 * @return alerts
	 */
	/*
	 * @Path("alerts") public AlertsResource getAlerts() { if (null ==
	 * this.queueEntity) { logger.error(String.format(ERROR_MESSAGE_Q_NOT_FOUND,
	 * this.queueName)); throw new EntityNotFoundException(String.format(
	 * ERROR_MESSAGE_Q_NOT_FOUND, this.queueName)); } return new
	 * AlertsResource(uriInfo, entityManagerConfig, null, queueEntity); }
	 */

	/**
	 * Method getMessages.
	 * 
	 * @return MessagesResource
	 */
	@Path("messages")
	public MessagesResource getMessages() {
		if (null == this.queueEntity) {
			logger.error(String.format(ERROR_MESSAGE_Q_NOT_FOUND,
					this.queueName));
			throw new EntityNotFoundException(String.format(
					ERROR_MESSAGE_Q_NOT_FOUND, this.queueName));
		}
		return new MessagesResource(uriInfo, entityManagerData,
				entityManagerConfig, queueEntity);
	}
	
	/**
	 * Method getMessageTypes.
	 * 
	 * @return MessageTypesResource
	 */
	@Path("messagetypes")
	public MessageTypesResource getMessageTypes() {
		if (null == this.queueEntity) {
			logger.error(String.format(ERROR_MESSAGE_Q_NOT_FOUND,
					this.queueName));
			throw new EntityNotFoundException(String.format(
					ERROR_MESSAGE_Q_NOT_FOUND, this.queueName));
		}
		return new MessageTypesResource(uriInfo, entityManagerData,
				entityManagerConfig, queueEntity);
	}

	/**
	 * Method getServiceMaps.
	 * 
	 * @return ServiceMapsResource
	 */
	/*
	 * @Path("servicemaps") public ServiceMapsResource getServiceMaps(){ if
	 * (null == this.queueEntity) {
	 * logger.error(String.format(ERROR_MESSAGE_Q_NOT_FOUND, this.queueName));
	 * throw new EntityNotFoundException(String.format(
	 * ERROR_MESSAGE_Q_NOT_FOUND, this.queueName)); } return new
	 * ServiceMapsResource(uriInfo, entityManagerConfig, queueEntity); }
	 */

	/**
	 * PUT method : updates the queue
	 * 
	 * @param jsonEntity
	 *            JSONObject the queue holding new values
	 * @return Response
	 */
	@PUT
	@Consumes(MediaType.APPLICATION_JSON)
	public Response updateQueue(JSONObject jsonEntity) {
		if(!RolesUtils.hasAdministratorRole()){
			throw new AccessDeniedException("Access denied");
		}
		if (null == queueEntity) {
			logger.error(String.format(ERROR_MESSAGE_Q_NOT_FOUND, queueName));
			throw new EntityNotFoundException(String.format(
					ERROR_MESSAGE_Q_NOT_FOUND, queueName));
		}

		try {
			// fill data
			if (jsonEntity.has("name")) {
				queueEntity.setName(jsonEntity.getString("name"));
			}
			if (jsonEntity.has("holdstatus")) {
				queueEntity.setHoldstatus(new BigDecimal(jsonEntity
						.getInt("holdstatus")));
			}
			if (jsonEntity.has("type")) {

				try {
					QueueTypeEntity queueType = QueueTypeResource
							.findByTypeName(entityManagerConfig,
									jsonEntity.getString("type"));
					queueEntity.setQueueTypeEntity(queueType);
				} catch (NullPointerException e) {
					// TODO: change reason
					logger.error(ERROR_MESSAGE_PUT_QUEUE + ERROR_REASON_JSON, e);
					throw new ApplicationJsonException(e,
							ERROR_MESSAGE_PUT_QUEUE + ERROR_REASON_JSON,
							Response.Status.BAD_REQUEST.getStatusCode());
				}
			}
			
			if (jsonEntity.has("connector")) {

				try {
					ServiceMapEntity serviceMap = ServiceMapResource
							.findByFriendlyName(entityManagerConfig,
									jsonEntity.getString("connector"));
					queueEntity.setServiceMapEntity(serviceMap);
				} catch (NullPointerException e) {
					// TODO: change reason
					logger.error(ERROR_MESSAGE_PUT_QUEUE + ERROR_REASON_JSON, e);
					throw new ApplicationJsonException(e,
							ERROR_MESSAGE_PUT_QUEUE + ERROR_REASON_JSON,
							Response.Status.BAD_REQUEST.getStatusCode());
				}
			}
			
			
			if (jsonEntity.has("description")) {
				queueEntity.setDescription(jsonEntity.getString("description"));
			}
		
			if (jsonEntity.has("batchno")) {
				queueEntity.setBatchno(new BigDecimal(jsonEntity
						.getInt("batchno")));
			}
			if (jsonEntity.has("priority")) {
				queueEntity.setPriority(new BigDecimal(jsonEntity
						.getInt("priority")));
			}
			entityManagerConfig.getTransaction().begin();
			entityManagerConfig.merge(queueEntity);
			entityManagerConfig.getTransaction().commit();

			/*
			 * if (jsonEntity.has("moveto")) {
			 * entityManagerConfig.getTransaction().begin(); boolean find =
			 * false; JSONArray moveto = jsonEntity.getJSONArray("moveto"); for
			 * (int i = 0; i < moveto.length(); i++) { JSONObject row =
			 * moveto.getJSONObject(i); if (row.has("destqueueid")){ find =
			 * false; for (QMovePrivMapEntity moveTo : queueEntity
			 * .getqMovePrivMapEntity()) { if
			 * (moveTo.getDestqueueid().intValue() == row
			 * .getInt("destqueueid")) { find = true; } } if (!find) {
			 * QMovePrivMapEntity qMovePrivMapEntity = new QMovePrivMapEntity();
			 * qMovePrivMapEntity.setDestqueueid(new BigDecimal(
			 * row.getInt("destqueueid")));
			 * qMovePrivMapEntity.setSourcequeueid(queueEntity .getGuid());
			 * entityManagerConfig.persist(qMovePrivMapEntity); } } }
			 */

			/*
			 * for (QMovePrivMapEntity moveTo : queueEntity
			 * .getqMovePrivMapEntity()){ find = false; for (int i = 0; i <
			 * moveto.length(); i++) { JSONObject row = moveto.getJSONObject(i);
			 * if (moveTo.getDestqueueid().intValue() == row
			 * .getInt("destqueueid")) { find = true; } } if (!find) {
			 * entityManagerConfig.remove(moveTo); } }
			 * 
			 * entityManagerConfig.getTransaction().commit(); }
			 */
		} catch (JSONException je) {
			logger.error(ERROR_MESSAGE_PUT_QUEUE + ERROR_REASON_JSON, je);
			throw new ApplicationJsonException(je, ERROR_MESSAGE_PUT_QUEUE
					+ ERROR_REASON_JSON,
					Response.Status.BAD_REQUEST.getStatusCode());
		} catch (NumberFormatException nfe) {
			logger.error(ERROR_MESSAGE_PUT_QUEUE + ERROR_REASON_NUMBER_FORMAT,
					nfe);
			throw new ApplicationJsonException(nfe, ERROR_MESSAGE_PUT_QUEUE
					+ ERROR_REASON_NUMBER_FORMAT,
					Response.Status.BAD_REQUEST.getStatusCode());
		} catch (RollbackException re) {
			ApplicationJsonException.handleSQLException(re,
					ERROR_MESSAGE_PUT_QUEUE, logger);

			// log and rethrow the original error
			logger.error(ERROR_MESSAGE_PUT_QUEUE + ERROR_REASON_ROLLBACK, re);
			throw re;
		} finally {
			// Tomcat does not support container managed transactions
			if (null != entityManagerConfig) {
				entityManagerConfig.close();
			}
		}

		return JsonResponseWrapper.getResponse(Response.Status.OK,
				"queue updated");
	}

	/**
	 * DELETE method : deletes the queue
	 * 
	 * @return Response
	 */
	@DELETE
	public Response deleteQueue() {
		if(!RolesUtils.hasAdministratorRole()){
			throw new AccessDeniedException("Access denied");
		}
		if (null == queueEntity) {
			logger.error(String.format(ERROR_MESSAGE_Q_NOT_FOUND, queueName));
			throw new EntityNotFoundException(String.format(
					ERROR_MESSAGE_Q_NOT_FOUND, queueName));
		}
		try {
			entityManagerConfig.getTransaction().begin();
			entityManagerConfig.remove(queueEntity);
			entityManagerConfig.getTransaction().commit();
		} finally {
			if (null != entityManagerConfig) {
				entityManagerConfig.close();
			}
		}
		return JsonResponseWrapper.getResponse(Response.Status.OK,
				"queue deleted");
	}

	/**
	 * Returns the resource formatted as json
	 * 
	 * @param queueEntity
	 *            QueueEntity
	 * @param path
	 *            String
	 * @return JSONObject
	 * @throws JSONException
	 */
	public static JSONObject asJson(QueueEntity queueEntity, String path)
			throws JSONException {
		JSONObject queueAsJson = ApiResource.getMetaResource(path,
				QueueResource.class);

		// fill data
		queueAsJson.put("name", queueEntity.getName())
				.put("description", queueEntity.getDescription())
				.put("holdstatus", queueEntity.getHoldstatus())
				.put("batchno", queueEntity.getBatchno())
				.put("type", queueEntity.getQueueTypeEntity().getTypename())
				.put("priority", queueEntity.getPriority());
		
		if (queueEntity.getServiceMapEntity() != null) {
			queueAsJson.put("connector", queueEntity.getServiceMapEntity()
					.getFriendlyname());

		}
		/*
		 * if (!hasAll) { JSONArray moveToArray = new JSONArray(); for
		 * (QMovePrivMapEntity queueMove : queueEntity .getqMovePrivMapEntity())
		 * { moveToArray.put(new JSONObject().put("destqueueid",
		 * queueMove.getDestqueueid())); } queueAsJson.put("moveto",
		 * moveToArray); }
		 */
		ResourcesUtils.createLink(queueAsJson, path + "/messages", "messages");

		return queueAsJson;
	}
	

	/**
	 * Method toString.
	 * 
	 * @return String
	 */
	public String toString() {
		return queueEntity.getName();
	}
}
