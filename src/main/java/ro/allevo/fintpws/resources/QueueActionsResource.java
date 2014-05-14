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
import ro.allevo.fintpws.model.QueueActionEntity;
import ro.allevo.fintpws.util.JsonResponseWrapper;

/**
 * @author anda
 * @version $Revision: 1.0 $
 */

public class QueueActionsResource extends PagedCollection {

	/**
	 * Field logger
	 */
	private static final Logger logger = LogManager
			.getLogger(QueueActionsResource.class.getName());

	/**
	 * Field ERROR_MESSAGE_GET_QUEUE_ACTIONS. (value is ""Error returning queue
	 * actions: "")
	 */
	static final String ERROR_MESSAGE_GET_QUEUE_ACTIONS = "Error returning queue actions : ";
	/**
	 * Field ERROR_MESSAGE_POST_QUEUE_ACTIONS. (value is ""Error creating queue
	 * actions : "")
	 */
	static final String ERROR_MESSAGE_POST_QUEUE_ACTIONS = "Error creating queue actions : ";

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
	 */
	
	public QueueActionsResource(UriInfo uriInfo,
			EntityManager entityManagerConfig) {
		super(uriInfo, entityManagerConfig.createNamedQuery(
				"QueueActionEntity.findAll", QueueActionEntity.class),
				entityManagerConfig.createNamedQuery(
						"QueueActionEntity.findTotal", Long.class));

		this.entityManagerConfig = entityManagerConfig;
	}
	
	/**
	 * Returns a queue action sub-resource with action
	 * 
	 * @param action
	 *            String action of the queue action as last element in the path
	 * 
	 * @return QueueActionResource The queue action sub-resource
	 */
	@Path("{action}")
	public QueueActionResource getQueueActionsResource(
			@PathParam("action") String action) {
		return new QueueActionResource(getUriInfo(), entityManagerConfig, action);
	}

	/**
	 * GET method : returns an application/json formatted list of queue actions
	 * 
	 * @return JSONObject The list of queue actions
	 */
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public JSONObject getQueueActionsAsJson() {
		try {
			getPage();
			return asJson();
		} catch (JSONException je) {
			logger.error(ERROR_MESSAGE_GET_QUEUE_ACTIONS + ERROR_REASON_JSON, je);
			throw new ApplicationJsonException(je,
					ERROR_MESSAGE_GET_QUEUE_ACTIONS + ERROR_REASON_JSON,
					Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
		}
	}
	
	/**
	 * POST method : creates a queue action
	 * 
	 * @param jsonEntity
	 *            JSONObject
	 * @return Response
	 */
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	public Response postForm(JSONObject jsonEntity) {
		final String id;
		final QueueActionEntity queueActionEntity = new QueueActionEntity();
		URI queueActionUri = null;
		try {
			//Fill required data
			queueActionEntity.setAction(jsonEntity.getString("action"));
			
			//Fill optional data
			queueActionEntity.setDescription(jsonEntity.optString("description"));
			queueActionEntity.setSelmsg(new BigDecimal (jsonEntity.optInt("selmsg")));
			queueActionEntity.setGroupmsg(new BigDecimal (jsonEntity.optInt("groupmsg")));
			queueActionEntity.setAddoptions(jsonEntity.optInt("addoptions"));
			queueActionEntity.setCurrmsg(new BigDecimal(jsonEntity.optInt("currmsg")));
			
			entityManagerConfig.getTransaction().begin();
			entityManagerConfig.persist(queueActionEntity);
			id = entityManagerConfig.getEntityManagerFactory()
					.getPersistenceUnitUtil().getIdentifier(queueActionEntity).toString();
			entityManagerConfig.getTransaction().commit();
		} catch (JSONException je) {
			logger.error(ERROR_MESSAGE_POST_QUEUE_ACTIONS + ERROR_REASON_JSON, je);
			throw new ApplicationJsonException(je,
					ERROR_MESSAGE_POST_QUEUE_ACTIONS + ERROR_REASON_JSON,
					Response.Status.BAD_REQUEST.getStatusCode());
		} catch (NumberFormatException nfe) {
			logger.error(ERROR_MESSAGE_POST_QUEUE_ACTIONS
					+ ERROR_REASON_NUMBER_FORMAT, nfe);
			throw new ApplicationJsonException(
					nfe,
					ERROR_MESSAGE_POST_QUEUE_ACTIONS + ERROR_REASON_NUMBER_FORMAT,
					Response.Status.BAD_REQUEST.getStatusCode());
		} catch (RollbackException re) {
			ApplicationJsonException.handleSQLException(re,
					ERROR_MESSAGE_POST_QUEUE_ACTIONS, logger);
			logger.error(
					ERROR_MESSAGE_POST_QUEUE_ACTIONS + ERROR_REASON_ROLLBACK, re);
			throw re;
		} finally {
			if (null != entityManagerConfig) {
				entityManagerConfig.close();
			}
		}
		queueActionUri = getUriInfo().getAbsolutePathBuilder()
				.path(queueActionEntity.toString()).build();

		return JsonResponseWrapper.getResponse(id, Response.Status.CREATED,
				queueActionUri);
	}

	/**
	 * Returns the resource formatted as json
	 * 
	 * @throws JSONException
	 * @return JSONObject
	 * 
	 */
	@SuppressWarnings("unchecked")
	public JSONObject asJson() throws JSONException {
		final JSONObject queueActionsAsJson = super.asJson();

		// fill data
		final JSONArray queueActionsArray = new JSONArray();
		final List<?> items = getItems();

		if (items.size() > 0) {
			for (QueueActionEntity queueActionEntity : (List<QueueActionEntity>) items) {
				queueActionsArray.put(QueueActionResource.asJson(
						queueActionEntity,
						UriBuilder.fromPath(getUriInfo().getPath())
								.path(queueActionEntity.toString()).build()
								.getPath()));
			}
		}
		queueActionsAsJson.put("queueactions", queueActionsArray);
		return queueActionsAsJson;
	}
	
}
