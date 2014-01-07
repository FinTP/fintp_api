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
import ro.allevo.fintpws.model.RoleEntity;
import ro.allevo.fintpws.model.UserEntity;
import ro.allevo.fintpws.model.UserRoleEntity;
import ro.allevo.fintpws.util.JsonResponseWrapper;

public class RolesResource extends PagedCollection {

	/**
	 * Field logger
	 */
	private static final Logger logger = LogManager
			.getLogger(RoutingRulesResource.class.getName());

	/**
	 * Field ERROR_MESSAGE_GET_ROLES. (value is ""Error returning roles : "")
	 */
	static final String ERROR_MESSAGE_GET_ROLES = "Error returning roles : ";
	/**
	 * Field ERROR_MESSAGE_POST_ROLES. (value is ""Error creating roles : "")
	 */
	static final String ERROR_MESSAGE_POST_ROLES = "Error creating roles : ";

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
	 * Field entityManagerConfig
	 */
	private final EntityManager entityManagerConfig;

	/**
	 * Users Entity
	 */
	private UserEntity userEntity;
	
	public RolesResource(UriInfo uriInfo, EntityManager entityManagerConfig) {

		super(uriInfo, entityManagerConfig.createNamedQuery(
				"RoleEntity.findAll", RoleEntity.class),
				entityManagerConfig.createNamedQuery(
						"RoleEntity.findTotal", Long.class));
		this.entityManagerConfig = entityManagerConfig;
		this.userEntity = null;
	}
	

	/**
	 * 
	 * @param uriInfo
	 *            UriInfo
	 * @param entityManagerConfig
	 *            EntityManager
	 */

	public RolesResource(UriInfo uriInfo, EntityManager entityManagerConfig,
			UserEntity usersEntity) {

		super(uriInfo, entityManagerConfig.createNamedQuery(
				"RoleEntity.findUserAuthorities", RoleEntity.class)
				.setParameter("userid", usersEntity.getUserid()),
				entityManagerConfig.createNamedQuery(
						"RoleEntity.findTotalUserAuthorities", Long.class)
						.setParameter("userid", usersEntity.getUserid()));
		this.entityManagerConfig = entityManagerConfig;
		this.userEntity = usersEntity;
	}


	/**
	 * Returns a role sub-resource with roleid
	 * 
	 * @param roleid
	 *            String roleid of the role as last element in the path
	 * 
	 * @return RoleResource The role sub-resource
	 */
	@Path("{name}")
	public RoleResource getRoleResource(@PathParam("name") String rolename) {
		return new RoleResource(getUriInfo(), entityManagerConfig, rolename, userEntity);
	}

	/**
	 * GET method : returns an application/json formatted list of roles
	 * 
	 * @return JSONObject The list of roles
	 */
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public JSONObject getRolesAsJson() {
		try {
			getPage();
			return asJson();
		} catch (JSONException je) {
			logger.error(ERROR_MESSAGE_GET_ROLES + ERROR_REASON_JSON, je);
			throw new ApplicationJsonException(je, ERROR_MESSAGE_GET_ROLES
					+ ERROR_REASON_JSON,
					Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
		}
	}

	/**
	 * POST method: creates a role
	 * 
	 * @param jsonEntity
	 *            JSONObject
	 * @return Response
	 */

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	public Response postRole(JSONObject jsonEntity) {
		String id = "-1";
		System.out.println(userEntity);
		URI roleUri = null;
		try {
			//check if you will insert role, a role for a specific user
			//or grant authority for queue
			
			// if you attach role to a user
			if (userEntity != null) {
				final UserRoleEntity userRoleEntity = new UserRoleEntity();
				
				if (jsonEntity.has("mapid")) {
					userRoleEntity.setMapId(new Long(jsonEntity
							.getString("mapid")).longValue());
				}
				
				RoleEntity foundRoleEntity = RoleResource.findByRoleName(
						entityManagerConfig, jsonEntity.getString("name"));
				
				userRoleEntity.setUserid(userEntity.getUserid());
				userRoleEntity.setRoleid(foundRoleEntity.getRoleid());

				entityManagerConfig.getTransaction().begin();
				entityManagerConfig.persist(userRoleEntity);
				id = entityManagerConfig.getEntityManagerFactory()
						.getPersistenceUnitUtil().getIdentifier(userRoleEntity)
						.toString();
				entityManagerConfig.getTransaction().commit();
				roleUri = getUriInfo().getAbsolutePathBuilder()
						.path(String.valueOf(foundRoleEntity.getName())).build();

			}
			else {
				final RoleEntity roleEntity = new RoleEntity();
				
				roleEntity.setName(jsonEntity.getString("name"));
				roleEntity.setDescription(jsonEntity.optString("description"));
				roleEntity.setIsUserCreated(1);
				entityManagerConfig.getTransaction().begin();
				entityManagerConfig.persist(roleEntity);
				id = entityManagerConfig.getEntityManagerFactory()
						.getPersistenceUnitUtil().getIdentifier(roleEntity).toString();
				entityManagerConfig.getTransaction().commit();
				roleUri = getUriInfo().getAbsolutePathBuilder()
						.path(String.valueOf(roleEntity.getName())).build();
			}			
			
		} catch (JSONException je) {
			logger.error(ERROR_MESSAGE_POST_ROLES + ERROR_REASON_JSON, je);
			throw new ApplicationJsonException(je, ERROR_MESSAGE_POST_ROLES
					+ ERROR_REASON_JSON,
					Response.Status.BAD_REQUEST.getStatusCode());
		} catch (RollbackException re) {
			ApplicationJsonException.handleSQLException(re,
					ERROR_MESSAGE_POST_ROLES, logger);
			logger.error(ERROR_MESSAGE_POST_ROLES + ERROR_REASON_ROLLBACK, re);
			throw re;
		} catch (NumberFormatException nfe) {
			logger.error(ERROR_MESSAGE_POST_ROLES + ERROR_REASON_NUMBER_FORMAT,
					nfe);
			throw new ApplicationJsonException(nfe, ERROR_MESSAGE_POST_ROLES
					+ ERROR_REASON_NUMBER_FORMAT,
					Response.Status.BAD_REQUEST.getStatusCode());
		} finally {
			if (null != entityManagerConfig) {
				entityManagerConfig.close();
			}
		}

		
		return JsonResponseWrapper.getResponse(id, Response.Status.CREATED,
				roleUri);
	}

	/**
	 * Returns the resource formatted as json
	 * 
	 * @throws JSONException
	 * @return JSONObject
	 * @throws JSONException
	 * 
	 */
	@SuppressWarnings("unchecked")
	public JSONObject asJson() throws JSONException {
		final JSONObject rolesAsJson = super.asJson();

		// fill data
		final JSONArray rolesArray = new JSONArray();
		final List<?> items = getItems();

		if (items.size() > 0) {
			for (RoleEntity roleEntity : (List<RoleEntity>) items) {
				rolesArray.put(RoleResource.asJson(
						roleEntity,
						UriBuilder.fromPath(getUriInfo().getPath())
								.path(String.valueOf(roleEntity.getRoleid()))
								.build().getPath()));
			}
		}
		rolesAsJson.put("roles", rolesArray);
		return rolesAsJson;
	}
}
