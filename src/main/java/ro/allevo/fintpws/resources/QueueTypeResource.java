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
import ro.allevo.fintpws.model.QueueTypeEntity;
import ro.allevo.fintpws.util.JsonResponseWrapper;

/**
 * @author anda
 * @version $Revision: 1.0 $
 */

public class QueueTypeResource {

	/**
	 * Field logger
	 */
	private static final Logger logger = LogManager
			.getLogger(QueueTypeResource.class);

	/**
	 * Field ERROR_MESSAGE_GET_QUEUE_TYPE. (value is ""Error returning queue
	 * type : "")
	 */
	static final String ERROR_MESSAGE_GET_QUEUE_TYPE = "Error returning queue type : ";
	/**
	 * Field ERROR_MESSAGE_PUT_QUEUE_TYPE. (value is ""Error updating queue type
	 * : "")
	 */
	static final String ERROR_MESSAGE_PUT_QUEUE_TYPE = "Error updating queue type : ";
	/**
	 * Field ERROR_MESSAGE_QUEUE_TYPE_NOT_FOUND. (value is ""Queue type with
	 * name [%s] not found"")
	 */
	static final String ERROR_MESSAGE_QUEUE_TYPE_NOT_FOUND = "Queue type with name [%s] not found";
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
	private QueueTypeEntity queueTypeEntity;

	/**
	 * default constructor
	 */
	public QueueTypeResource() {

	}

	/**
	 * typeName
	 */
	private String typeName;
	
	/**
	 * Constructor for QueueTypeResource.
	 * 
	 * @param uriInfo
	 *            UriInfo
	 * @param entityManagerConfig
	 *            EntityManager
	 * @param typeName
	 *            String
	 */
	public QueueTypeResource(UriInfo uriInfo,
			EntityManager entityManagerConfig, String typeName) {
		this.uriInfo = uriInfo;
		this.entityManagerConfig = entityManagerConfig;
		this.typeName = typeName;
		queueTypeEntity = findByTypeName(entityManagerConfig, typeName);
	}
	
	/**
	 * 
	 * @param entityManager
	 *            EntityManager
	 * @param typeName
	 *            String
	 * @return QueueTypeEntity
	 */

	public static QueueTypeEntity findByTypeName(EntityManager entityManager,
			String typeName) {

		final TypedQuery<QueueTypeEntity> query = entityManager
				.createNamedQuery("QueueTypeEntity.findByTypename",
						QueueTypeEntity.class);

		final java.util.List<QueueTypeEntity> results = query.setParameter(
				"typename", typeName).getResultList();
		if (!results.isEmpty()) {
			return results.get(0);
		}
		return null;
	}
	
	/**
	 * GET Method : returns an application/json formatted queue type
	 * 
	 * @return JSONObject the queue type
	 */

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public JSONObject getQueueType() {

		if (null == queueTypeEntity) {
			logger.error(String
					.format(ERROR_MESSAGE_QUEUE_TYPE_NOT_FOUND, typeName));
			throw new EntityNotFoundException(String.format(
					ERROR_MESSAGE_QUEUE_TYPE_NOT_FOUND, typeName));
		}
		try {
			return QueueTypeResource.asJson(queueTypeEntity, uriInfo.getPath());
		} catch (JSONException je) {
			logger.error(ERROR_MESSAGE_GET_QUEUE_TYPE + ERROR_REASON_JSON, je);
			throw new ApplicationJsonException(je, ERROR_MESSAGE_GET_QUEUE_TYPE
					+ ERROR_REASON_JSON,
					Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
		}
	}
	
	/**
	 * PUT method: updates the queue type
	 * 
	 * @param jsonEntity
	 *            JSONObject the queue type holding new values
	 * @return Response
	 */
	@PUT
	@Consumes(MediaType.APPLICATION_JSON)
	public Response updateQueueType(JSONObject jsonEntity) {
		if (null == queueTypeEntity) {
			logger.error(String
					.format(ERROR_MESSAGE_QUEUE_TYPE_NOT_FOUND, typeName));
			throw new EntityNotFoundException(String.format(
					ERROR_MESSAGE_QUEUE_TYPE_NOT_FOUND, typeName));
		}

		try {
			
			if (jsonEntity.has("typename")) {
				queueTypeEntity.setTypename(jsonEntity
						.optString("typename"));
			}
			entityManagerConfig.getTransaction().begin();
			entityManagerConfig.merge(queueTypeEntity);
			entityManagerConfig.getTransaction().commit();
		}
//		catch (JSONException je) { logger.error(ERROR_MESSAGE_PUT_QUEUE_TYPE+
//		  ERROR_REASON_JSON, je); throw new ApplicationJsonException(je,
//		  ERROR_MESSAGE_PUT_QUEUE_TYPE+ ERROR_REASON_JSON,
//		  Response.Status.BAD_REQUEST.getStatusCode()); 
//		 }
		catch (NumberFormatException nfe) {
			logger.error(ERROR_MESSAGE_PUT_QUEUE_TYPE
					+ ERROR_REASON_NUMBER_FORMAT, nfe);
			throw new ApplicationJsonException(nfe,
					ERROR_MESSAGE_PUT_QUEUE_TYPE + ERROR_REASON_JSON,
					Response.Status.BAD_REQUEST.getStatusCode());
		} catch (RollbackException re) {
			ApplicationJsonException.handleSQLException(re,
					ERROR_MESSAGE_PUT_QUEUE_TYPE, logger);
			logger.error(ERROR_MESSAGE_PUT_QUEUE_TYPE + ERROR_REASON_ROLLBACK,
					re);
			throw re;
		} finally {
			if (null != entityManagerConfig) {
				entityManagerConfig.close();
			}
		}

		return JsonResponseWrapper.getResponse(Response.Status.OK,
				"queue type updated");
	}
	
	/**
	 * DELETE method : deletes the queue type
	 * 
	 * @return Response
	 */

	@DELETE
	public Response deleteQueueType() {
		if (null == queueTypeEntity) {
			logger.error(String
					.format(ERROR_MESSAGE_QUEUE_TYPE_NOT_FOUND, typeName));
			throw new EntityNotFoundException(String.format(
					ERROR_MESSAGE_QUEUE_TYPE_NOT_FOUND, typeName));
		}
		try {
			entityManagerConfig.getTransaction().begin();
			entityManagerConfig.remove(queueTypeEntity);
			entityManagerConfig.getTransaction().commit();
		} finally {
			if (null != entityManagerConfig) {
				entityManagerConfig.close();
			}
		}

		return JsonResponseWrapper.getResponse(Response.Status.OK,
				"queue type deleted");
	}
	
	/**
	 * Returns the resource formatted as json
	 * 
	 * @param queueTypeEntity
	 *            QueueTypeEntity
	 * @param path
	 *            String
	 * @return JSONObject
	 * @throws JSONException
	 */
	public static JSONObject asJson(QueueTypeEntity queueTypeEntity, String path)
			throws JSONException {
		final JSONObject queueTypeAsJson = ApiResource.getMetaResource(path,
				QueueTypeResource.class);

		queueTypeAsJson.put("typename", queueTypeEntity.getTypename());
		return queueTypeAsJson;
	}
	
	/**
	 * Method toString.
	 * 
	 * @return String
	 */
	public String toString() {
		return String.valueOf(queueTypeEntity.getTypeid());
	}
}
