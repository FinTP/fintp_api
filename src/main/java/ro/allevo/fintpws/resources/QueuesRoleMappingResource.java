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

import ro.allevo.fintpws.exceptions.ApplicationJsonException;
import ro.allevo.fintpws.model.QueuesRoleMapEntity;
import ro.allevo.fintpws.model.TimeLimitEntity;
import ro.allevo.fintpws.util.JsonResponseWrapper;

public class QueuesRoleMappingResource {
	/**
	 * Field logger
	 */
	private static final Logger logger = LogManager
			.getLogger(QueuesRoleMappingResource.class);

	/**
	 * Field ERROR_MESSAGE_GET_MAPPING. (value is ""Error returning mapping: "")
	 */
	static final String ERROR_MESSAGE_GET_MAPPING = "Error returning mapping: ";
	/**
	 * Field ERROR_MESSAGE_PUT_TIME_LIMIT. (value is ""Error updating mapping :
	 * "")
	 */
	static final String ERROR_MESSAGE_PUT_MAPPING = "Error updating mapping: ";
	/**
	 * Field ERROR_MESSAGE_MAPPING_NOT_FOUND. (value is ""Mapping with id [%s]
	 * not found"")
	 */
	static final String ERROR_MESSAGE_MAPPING_NOT_FOUND = "Mapping with rolename [%s] and queuename [%s] not found";
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
	private QueuesRoleMapEntity mappingEntity;
	/**
	 * Field rolename
	 */
	private String rolename;
	/**
	 * Field queuename
	 */
	private String queuename;

	/**
	 * default constructor
	 */
	public QueuesRoleMappingResource() {
	}

	public QueuesRoleMappingResource(UriInfo uriInfo,
			EntityManager entityManagerConfig, String rolename, String queuename) {
		this.uriInfo = uriInfo;
		this.entityManagerConfig = entityManagerConfig;
		this.rolename = rolename;
		this.queuename = queuename;
		this.mappingEntity = findByQueuename(entityManagerConfig, rolename,
				queuename);

	}

	public static QueuesRoleMapEntity findByQueuename(
			EntityManager entityManager, String rolename, String queuename) {
		final TypedQuery<QueuesRoleMapEntity> query = entityManager
				.createNamedQuery("QueuesRoleMapEntity.findByQueueName",
						QueuesRoleMapEntity.class);
		final List<QueuesRoleMapEntity> results = query
				.setParameter("rolename", rolename)
				.setParameter("queuename", queuename).getResultList();
		if (!results.isEmpty()) {
			return results.get(0);
		}
		return null;
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public JSONObject getMapping() {
		if (null == mappingEntity) {
			logger.error(String.format(ERROR_MESSAGE_MAPPING_NOT_FOUND,
					rolename, queuename));
			throw new EntityNotFoundException(String.format(
					ERROR_MESSAGE_MAPPING_NOT_FOUND, rolename, queuename));
		}
		try {
			return asJson(mappingEntity, uriInfo.getPath());
		} catch (JSONException je) {
			logger.error(ERROR_MESSAGE_GET_MAPPING + ERROR_REASON_JSON, je);
			throw new ApplicationJsonException(je, ERROR_MESSAGE_GET_MAPPING
					+ ERROR_REASON_JSON,
					Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
		}
	}
	
	@PUT
	@Consumes(MediaType.APPLICATION_JSON)
	public Response updateMapping(JSONObject jsonEntity) {
		if (null == mappingEntity) {
			logger.error(String.format(ERROR_MESSAGE_MAPPING_NOT_FOUND,
					rolename, queuename));
			throw new EntityNotFoundException(String.format(
					ERROR_MESSAGE_MAPPING_NOT_FOUND, rolename, queuename));
		}

		try {
			if (jsonEntity.has("actiontype")) {
				mappingEntity.setActiontype(jsonEntity.optString("actiontype"));
				entityManagerConfig.getTransaction().begin();
				entityManagerConfig.persist(mappingEntity);
				entityManagerConfig.getTransaction().commit();
			}
		} catch (RollbackException re) {
			ApplicationJsonException.handleSQLException(re,
					ERROR_MESSAGE_PUT_MAPPING, logger);
			logger.error(ERROR_MESSAGE_PUT_MAPPING+ ERROR_REASON_ROLLBACK,
					re);
			throw re;
		} finally {
			if (null != entityManagerConfig) {
				entityManagerConfig.close();
			}
		}
		
		return JsonResponseWrapper.getResponse(Response.Status.OK,
				"mapping updated");

	}
	
	@DELETE
	public Response deleteMapping() {
		if (null == mappingEntity) {
			logger.error(String.format(ERROR_MESSAGE_MAPPING_NOT_FOUND,
					rolename, queuename));
			throw new EntityNotFoundException(String.format(
					ERROR_MESSAGE_MAPPING_NOT_FOUND, rolename, queuename));
		}
		try{
			entityManagerConfig.getTransaction().begin();
			entityManagerConfig.remove(mappingEntity);
			entityManagerConfig.getTransaction().commit();
		}finally{
			if(null != entityManagerConfig){
				entityManagerConfig.close();
			}
		}
		
		return JsonResponseWrapper.getResponse(Response.Status.OK, 
				"mapping deleted");
	}

	public static JSONObject asJson(QueuesRoleMapEntity mappingEntity,
			String path) throws JSONException {
		final JSONObject mappingAsJson = ApiResource.getMetaResource(path,
				QueuesRoleMappingResource.class);

		mappingAsJson.put("mapid", mappingEntity.getMapId())
				.put("queuename", mappingEntity.getQueueName())
				.put("actiontype", mappingEntity.getActiontype());

		return mappingAsJson;

	}
}
