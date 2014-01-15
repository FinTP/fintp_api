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

import ro.allevo.fintpws.exceptions.ApplicationJsonException;
import ro.allevo.fintpws.model.QueueTypeEntity;
import ro.allevo.fintpws.util.JsonResponseWrapper;

/**
 * @author anda
 * @version $Revision: 1.0 $
 */

public class QueueTypesResource extends PagedCollection {

	/**
	 * Field logger
	 */
	private static final Logger logger = LogManager
			.getLogger(QueueTypesResource.class.getName());

	/**
	 * Field ERROR_MESSAGE_GET_QUEUE_TYPES. (value is ""Error returning queue
	 * types: "")
	 */
	static final String ERROR_MESSAGE_GET_QUEUE_TYPES = "Error returning queue types : ";
	/**
	 * Field ERROR_MESSAGE_POST_QUEUE_TYPES. (value is ""Error creating queue
	 * types : "")
	 */
	static final String ERROR_MESSAGE_POST_QUEUE_TYPES = "Error creating queue types : ";

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
	 * Field ERROR_REASON_PARSE. (value is ""parse"")
	 */
	static final String ERROR_REASON_PARSE = "parse";

	/**
	 * Field entityManagerConfig
	 */
	private final EntityManager entityManagerConfig;

	/**
	 * 
	 * @param uriInfo
	 *            UriInfo
	 * @param entityManagerConfig
	 *            EntityManagerConfig
	 * @param entityManagerData
	 *            EntityManagerData
	 */

	public QueueTypesResource(UriInfo uriInfo,
			EntityManager entityManagerConfig) {
		super(uriInfo, entityManagerConfig.createNamedQuery(
				"QueueTypeEntity.findAll", QueueTypeEntity.class),
				entityManagerConfig.createNamedQuery(
						"QueueTypeEntity.findTotal", Long.class));
		
		this.entityManagerConfig = entityManagerConfig;
	}
	
	/**
	 * Returns a queue type sub-resource with typename
	 * @param typename
	 *            String typename of the queue type as last element in the path
	 * @return QueueTypeResource The queue type sub-resource
	 */
	@Path("{name}")
	public QueueTypeResource getQueueTypesResource(
			@PathParam("name") String typeName) {
		return new QueueTypeResource(getUriInfo(), entityManagerConfig, typeName);
	}
	
	/**
	 * GET method : returns an application/json formatted list of queue types
	 * @return JSONObject The list of queue types
	 */
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public JSONObject getQueueTypesAsJson() {
		try {
			getPage();
			return asJson();
		} catch (JSONException je) {
			logger.error(ERROR_MESSAGE_GET_QUEUE_TYPES + ERROR_REASON_JSON, je);
			throw new ApplicationJsonException(je,
					ERROR_MESSAGE_GET_QUEUE_TYPES + ERROR_REASON_JSON,
					Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
		}
	}
	
	/**
	 * POST method : creates a queue type
	 * @param jsonEntity
	 *            JSONObject
	 * @return Response
	 */
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	public Response postForm(JSONObject jsonEntity) {
		final String id;
		final QueueTypeEntity queueTypeEntity = new QueueTypeEntity();
		URI queueTypeUri = null;
		try {
			//Fill required data
			queueTypeEntity.setTypename(jsonEntity.getString("typename"));
			entityManagerConfig.getTransaction().begin();
			entityManagerConfig.persist(queueTypeEntity);
			id = entityManagerConfig.getEntityManagerFactory()
					.getPersistenceUnitUtil().getIdentifier(queueTypeEntity).toString();
			entityManagerConfig.getTransaction().commit();
		} catch (JSONException je) {
			logger.error(ERROR_MESSAGE_POST_QUEUE_TYPES + ERROR_REASON_JSON, je);
			throw new ApplicationJsonException(je,
					ERROR_MESSAGE_POST_QUEUE_TYPES + ERROR_REASON_JSON,
					Response.Status.BAD_REQUEST.getStatusCode());
		} catch (NumberFormatException nfe) {
			logger.error(ERROR_MESSAGE_POST_QUEUE_TYPES
					+ ERROR_REASON_NUMBER_FORMAT, nfe);
			throw new ApplicationJsonException(
					nfe,
					ERROR_MESSAGE_POST_QUEUE_TYPES + ERROR_REASON_NUMBER_FORMAT,
					Response.Status.BAD_REQUEST.getStatusCode());
		} catch (RollbackException re) {
			ApplicationJsonException.handleSQLException(re,
					ERROR_MESSAGE_POST_QUEUE_TYPES, logger);
			logger.error(
					ERROR_MESSAGE_POST_QUEUE_TYPES + ERROR_REASON_ROLLBACK, re);
			throw re;
		} finally {
			if (null != entityManagerConfig) {
				entityManagerConfig.close();
			}
		}
		queueTypeUri = getUriInfo().getAbsolutePathBuilder()
				.path(queueTypeEntity.toString()).build();

		return JsonResponseWrapper.getResponse(id, Response.Status.CREATED,
				queueTypeUri);
	}
	
	/**
	 * Returns the resource formatted as json
	 * @throws JSONException
	 * @return JSONObject
	 */
	@SuppressWarnings("unchecked")
	public JSONObject asJson() throws JSONException {
		final JSONObject queueTypesAsJson = super.asJson();

		// fill data
		final JSONArray queueTypesArray = new JSONArray();
		final List<?> items = getItems();

		if (items.size() > 0) {
			for (QueueTypeEntity queueTypeEntity : (List<QueueTypeEntity>) items) {
				queueTypesArray.put(QueueTypeResource.asJson(
						queueTypeEntity,
						UriBuilder.fromPath(getUriInfo().getPath())
								.path(queueTypeEntity.toString()).build()
								.getPath()));
			}
		}
		queueTypesAsJson.put("queuetypes", queueTypesArray);
		return queueTypesAsJson;
	}
	
}
