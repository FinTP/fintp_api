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
import javax.ws.rs.Path;
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
import ro.allevo.fintpws.model.RoleEntity;
import ro.allevo.fintpws.model.UserEntity;
import ro.allevo.fintpws.model.UserRoleEntity;
import ro.allevo.fintpws.util.JsonResponseWrapper;

public class RoleResource {

	/**
	 * Field logger
	 */
	private static final Logger logger = LogManager
			.getLogger(RoleResource.class);

	/**
	 * Field ERROR_MESSAGE_GET_ROLE. (value is ""Error returning role: "")
	 */
	static final String ERROR_MESSAGE_GET_ROLE = "Error returning role : ";
	/**
	 * Field ERROR_MESSAGE_PUT_ROLE. (value is ""Error updating role
	 * : "")
	 */
	static final String ERROR_MESSAGE_PUT_ROLE = "Error updating role: ";
	/**
	 * Field ERROR_MESSAGE_ROLE_NOT_FOUND. (value is ""Role with roleid [%s] not
	 * found"")
	 */
	
	static final String ERROR_MESSAGE_ROLE_NOT_FOUND = "Role with roleid [%s] not found";
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
	 * roleid
	 */
	private String roleid;

	/**
	 * the JPA entity
	 */
	private RoleEntity roleEntity;

	/**
	 * the JPA QueueRoleMap entity
	 */
	private RoleEntity queueRoleMapEntity;
	/**
	 * username
	 */
	private long userid;

	/**
	 * queueid
	 */
	private BigDecimal queueid;

	/**
	 * default constructor
	 */
	public RoleResource() {

	}

	/**
	 * Constructor for RoleResource
	 * 
	 * @param uriInfo
	 * @param entityManagerConfig
	 * @param roleid
	 */
	public RoleResource(UriInfo uriInfo, EntityManager entityManagerConfig,
			String name, UserEntity userEntity) {
		this.uriInfo = uriInfo;
		this.entityManagerConfig = entityManagerConfig;
		roleEntity = RoleResource.findByRoleName(entityManagerConfig, name);
		if (userEntity != null) {
			this.userid = userEntity.getUserid();
		} else {
			this.userid = -1;
		}
	}

	/**
	 * 
	 * @param entityManager
	 *            EntityManager
	 * @param roleid
	 *            String
	 * @return RoleEntity
	 */
	public static RoleEntity findByRoleName(EntityManager entityManager,
			String name) {

		final TypedQuery<RoleEntity> query = entityManager.createNamedQuery(
				"RoleEntity.findByName", RoleEntity.class);

		final java.util.List<RoleEntity> results = query.setParameter("name",
				name).getResultList();
		if (!results.isEmpty()) {
			return results.get(0);
		}
		return null;
	}

	/**
	 * 
	 * @param entityManager
	 *            EntityManager
	 * @param roleid
	 *            String
	 * @param username
	 *            String
	 * @return UserRoleEntity
	 */

	public static QueuesRoleMapEntity findByRoleidAndQueueid(
			EntityManager entityManager, long roleid, long queueid) {

		final TypedQuery<QueuesRoleMapEntity> query = entityManager
				.createNamedQuery("QueuesRoleMapEntity.findQueueRoles",
						QueuesRoleMapEntity.class);

		final java.util.List<QueuesRoleMapEntity> results = query
				.setParameter("roleid", roleid)
				.setParameter("queueid", queueid).getResultList();
		if (!results.isEmpty()) {
			return results.get(0);
		}
		return null;
	}

	/**
	 * 
	 * @param entityManager
	 *            EntityManager
	 * @param roleid
	 *            String
	 * @param queueid
	 *            String
	 * @return UserRoleEntity
	 */

	public static UserRoleEntity findByRoleidAndUsername(
			EntityManager entityManager, long roleid, long userid) {

		final TypedQuery<UserRoleEntity> query = entityManager
				.createNamedQuery("UserRoleEntity.findUserAuthorities",
						UserRoleEntity.class);

		final java.util.List<UserRoleEntity> results = query
				.setParameter("roleid", roleid).setParameter("userid", userid)
				.getResultList();
		if (!results.isEmpty()) {
			return results.get(0);
		}
		return null;
	}
	
	/**
	 * Sub-resource locator for /mappings resource
	 * 
	 * @return QueuesRoleappingResource
	 */
	@Path("mappings")
	public QueuesRoleMappingsResource getMappings() {
		//logger.info("Requested " + uriInfo.getRequestUri());
		return new QueuesRoleMappingsResource(uriInfo, entityManagerConfig, roleEntity);
	}

		
	
	/**
	 * GET Method : returns an application/json formatted role
	 * 
	 * @return JSONObject the role
	 */

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public JSONObject getRole() {

		if (null == roleEntity) {
			logger.error(String.format(ERROR_MESSAGE_ROLE_NOT_FOUND, roleid));
			throw new EntityNotFoundException(String.format(
					ERROR_MESSAGE_ROLE_NOT_FOUND, roleid));
		}
		try {
			return RoleResource.asJson(roleEntity, uriInfo.getPath());
		} catch (JSONException je) {
			logger.error(ERROR_MESSAGE_GET_ROLE + ERROR_REASON_JSON, je);
			throw new ApplicationJsonException(je, ERROR_MESSAGE_GET_ROLE
					+ ERROR_REASON_JSON,
					Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
		}
	}

	@PUT
	@Consumes(MediaType.APPLICATION_JSON)
	public Response updateRole(JSONObject jsonEntity) {
		if (userid != -1) {
			try {
				System.out.println("here");
				return Response
						.status(405)
						.entity(JsonResponseWrapper.getEntity(405,
								"method not allowed", null, null))
						.type(MediaType.APPLICATION_JSON).build();
			} catch (JSONException je) {
				logger.error("Error formatting response as JSON", je);
				// we are potentially already in an exception block, so don't
				// attempt to throw some other error
				return null;
			}
		} else {
			try {
				if (jsonEntity.has("name")) {
					roleEntity.setName(jsonEntity.getString("name"));
				}
				if (jsonEntity.has("description")) {
					roleEntity.setDescription(jsonEntity
							.getString("description"));
				}

				entityManagerConfig.getTransaction().begin();
				entityManagerConfig.merge(roleEntity);
				entityManagerConfig.getTransaction().commit();
			} catch (NumberFormatException nfe) {
				logger.error(ERROR_MESSAGE_PUT_ROLE
						+ ERROR_REASON_NUMBER_FORMAT, nfe);
				throw new ApplicationJsonException(nfe, ERROR_MESSAGE_PUT_ROLE
						+ ERROR_REASON_JSON,
						Response.Status.BAD_REQUEST.getStatusCode());
			} catch (RollbackException re) {
				ApplicationJsonException.handleSQLException(re,
						ERROR_MESSAGE_PUT_ROLE, logger);
				logger.error(ERROR_MESSAGE_PUT_ROLE + ERROR_REASON_ROLLBACK, re);
				throw re;
			} catch (JSONException je) {
				logger.error(ERROR_MESSAGE_PUT_ROLE + ERROR_REASON_JSON, je);
				throw new ApplicationJsonException(je, ERROR_MESSAGE_PUT_ROLE
						+ ERROR_REASON_JSON,
						Response.Status.BAD_REQUEST.getStatusCode());

			} finally {
			if (null != entityManagerConfig) {
				entityManagerConfig.close();
			}
		}

		return JsonResponseWrapper.getResponse(Response.Status.OK,
				"role updated");
		}

	}

	/**
	 * DELETE method : deletes the selected role
	 * 
	 * @return
	 */
	@DELETE
	public Response deleteRole() {
		if (null == roleEntity) {
			logger.error(String.format(ERROR_MESSAGE_ROLE_NOT_FOUND, roleid));
			throw new EntityNotFoundException(String.format(
					ERROR_MESSAGE_ROLE_NOT_FOUND, roleid));
		}
		if (userid != -1) {
			UserRoleEntity userRoleEntity = findByRoleidAndUsername(
					entityManagerConfig, this.roleEntity.getRoleid(), userid);
			entityManagerConfig.getTransaction().begin();
			entityManagerConfig.remove(userRoleEntity);
			entityManagerConfig.getTransaction().commit();
		} else {
			entityManagerConfig.getTransaction().begin();
			entityManagerConfig.remove(roleEntity);
			entityManagerConfig.getTransaction().commit();
		}
		if (null != entityManagerConfig) {
			entityManagerConfig.close();
		}

		if (userid != -1) {
			return JsonResponseWrapper.getResponse(Response.Status.OK,
					"role removed for user with id" + userid);
		} else {
			return JsonResponseWrapper.getResponse(Response.Status.OK,
					"role removed" + userid);
		}

	}

	/**
	 * Returns the resource formatted as json
	 * 
	 * @param routingRuleEntity
	 *            RoutingRuleEntity
	 * @param path
	 *            String
	 * @throws JSONException
	 * @return JSONObject * @throws JSONException
	 */

	public static JSONObject asJson(RoleEntity roleEntity, String path)
			throws JSONException {
		final JSONObject roleAsJson = ApiResource.getMetaResource(path,
				RoleResource.class);

		roleAsJson.put("roleid", roleEntity.getRoleid())
				.put("name", roleEntity.getName())
				.put("description", roleEntity.getDescription())
				.put("isusercreated", roleEntity.isUserCreated());
		List<QueuesRoleMapEntity> queueRolesList = roleEntity.getQueueRoles();
		String queueRoles = "";
		for (int i = 0; i < queueRolesList.size(); i++) {
			queueRoles += queueRolesList.get(i).getQueueId();
		}
		roleAsJson.put("queueroles", queueRoles);

		return roleAsJson;
	}
}
