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

import java.net.URI;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityNotFoundException;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.Produces;
import javax.ws.rs.core.UriInfo;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.springframework.security.access.AccessDeniedException;

import ro.allevo.fintpws.exceptions.ApplicationJsonException;
import ro.allevo.fintpws.model.QueueEntity;
import ro.allevo.fintpws.model.QueuesRoleMapEntity;
import ro.allevo.fintpws.model.RoleEntity;
import ro.allevo.fintpws.security.RolesUtils;
import ro.allevo.fintpws.util.JsonResponseWrapper;

public class QueuesRoleMappingsResource extends PagedCollection {

	/**
	 * Field logger
	 */
	private static final Logger logger = LogManager
			.getLogger(QueuesRoleMappingsResource.class.getName());

	/**
	 * Field ERROR_MESSAGE_GET_TIME_LIMITS. (value is ""Error returning mappings
	 * : "")
	 */
	static final String ERROR_MESSAGE_GET_MAPPINGS = "Error returning mappings : ";
	/**
	 * Field ERROR_MESSAGE_POST_TIME_LIMITS. (value is ""Error creating
	 * mappings: "")
	 */
	static final String ERROR_MESSAGE_POST_MAPPINGS = "Error creating mappings : ";

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
	 * Field ERROR_MESSAGE_Q_NOT_FOUND. (value is ""Queue with name [%s] not
	 * found"")
	 */
	static final String ERROR_MESSAGE_Q_NOT_FOUND = "Queue with name [%s] not found";
	
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
	 * RoleEntity roleEntity
	 */
	private RoleEntity roleEntity;
	

	/**
	 * 
	 * @param uriInfo
	 * @param entityManagerConfig
	 * @param mappingEntity
	 */
	public QueuesRoleMappingsResource(UriInfo uriInfo,
			EntityManager entityManagerConfig, RoleEntity roleEntity) {
		super(uriInfo, entityManagerConfig.createNamedQuery(
				"QueuesRoleMapEntity.findAll", QueuesRoleMapEntity.class)
				.setParameter("rolename", roleEntity.getName()),
				entityManagerConfig.createNamedQuery(
						"QueuesRoleMapEntity.findTotal", Long.class)
						.setParameter("rolename", roleEntity.getName()));
		this.entityManagerConfig = entityManagerConfig;
		this.roleEntity = roleEntity;
	}

	/**
	 * Returns a mapping sub-resource named queueName
	 * 
	 * @param queueName
	 *            String Name of the queue as last element in the path
	 * @return QueueResource The mapping sub-resource
	 */
	@Path("{queuename}")
	public QueuesRoleMappingResource getMapping(
			@PathParam("queuename") String queueName) {
		return new QueuesRoleMappingResource(getUriInfo(), entityManagerConfig,
				roleEntity.getName(), queueName);
	}
	
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public JSONObject getMappings() {
		try {
			getPage();
			return asJson();
		} catch (JSONException je) {
			logger.error(ERROR_MESSAGE_GET_MAPPINGS + ERROR_REASON_JSON, je);
			throw new ApplicationJsonException(je, ERROR_MESSAGE_GET_MAPPINGS
					+ ERROR_REASON_JSON,
					Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
		}
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	public Response postMapping(JSONObject jsonEntity) {
		if (!RolesUtils.hasAdministratorRole()) {
			throw new AccessDeniedException("Access denied");
		}
		String id;
		final QueuesRoleMapEntity mappingEntity = new QueuesRoleMapEntity();
		URI mappingUri = null;
		try {
			mappingEntity.setRoleId(roleEntity.getRoleid());
			//mappingEntity.setRoleEntity(roleEntity);
			mappingEntity.setActiontype(jsonEntity.getString("actiontype"));
			QueueEntity queueEntity = QueueResource.findByName(
					entityManagerConfig, jsonEntity.getString("queuename"));
			if(null == queueEntity){
				logger.error(String.format(ERROR_MESSAGE_Q_NOT_FOUND,
						jsonEntity.getString("queuename")));
				throw new EntityNotFoundException(String.format(
						ERROR_MESSAGE_Q_NOT_FOUND, jsonEntity.getString("queuename")));
			}
			mappingEntity.setQueueId(queueEntity.getGuid());
			mappingEntity.setQueueEntity(queueEntity);
			entityManagerConfig.getTransaction().begin();
			entityManagerConfig.persist(mappingEntity);
			id = entityManagerConfig.getEntityManagerFactory()
					.getPersistenceUnitUtil().getIdentifier(mappingEntity)
					.toString();
			entityManagerConfig.getTransaction().commit();
		} catch (JSONException je) {
			logger.error(ERROR_MESSAGE_POST_MAPPINGS + ERROR_REASON_JSON, je);
			throw new ApplicationJsonException(je, ERROR_MESSAGE_POST_MAPPINGS
					+ ERROR_REASON_JSON,
					Response.Status.BAD_REQUEST.getStatusCode());
		} catch (NumberFormatException nfe) {
			logger.error(ERROR_MESSAGE_POST_MAPPINGS
					+ ERROR_REASON_NUMBER_FORMAT, nfe);
			throw new ApplicationJsonException(nfe, ERROR_MESSAGE_POST_MAPPINGS
					+ ERROR_REASON_NUMBER_FORMAT,
					Response.Status.BAD_REQUEST.getStatusCode());
		} finally {
			if (null != entityManagerConfig) {
				entityManagerConfig.close();
			}
		}
		mappingUri = getUriInfo().getAbsolutePathBuilder()
				.path(mappingEntity.getQueueName()).build();
		return JsonResponseWrapper.getResponse(Response.Status.CREATED,
				mappingUri);
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
		final JSONObject mappingsAsJson = super.asJson();

		// fill data
		final JSONArray mappingsArray = new JSONArray();
		final List<?> items = getItems();

		if (items.size() > 0) {
			for (QueuesRoleMapEntity mappingEntity : (List<QueuesRoleMapEntity>) items) {
				mappingsArray.put(QueuesRoleMappingResource.asJson(
						mappingEntity,
						UriBuilder.fromPath(getUriInfo().getPath())
								.path(mappingEntity.getQueueName()).build()
								.getPath()));
			}
		}
		mappingsAsJson.put("mappings", mappingsArray);
		return mappingsAsJson;
	}
}
