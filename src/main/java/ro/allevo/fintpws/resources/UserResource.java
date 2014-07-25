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

import java.text.ParseException;

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
import ro.allevo.fintpws.model.UserEntity;
import ro.allevo.fintpws.util.JsonResponseWrapper;
import ro.allevo.fintpws.util.ResourcesUtils;

public class UserResource {

	/**
	 * Field logger
	 */
	private static final Logger logger = LogManager
			.getLogger(TimeLimitResource.class);

	/**
	 * Field ERROR_MESSAGE_GET_USER. (value is ""Error returning user : "")
	 */
	static final String ERROR_MESSAGE_GET_USER = "Error returning user : ";
	/**
	 * Field ERROR_MESSAGE_PUT_USER. (value is ""Error updating user : "")
	 */
	static final String ERROR_MESSAGE_PUT_USER = "Error updating user : ";
	/**
	 * Field ERROR_MESSAGE_USER_NOT_FOUND. (value is ""User with username [%s]
	 * not found"")
	 */
	static final String ERROR_MESSAGE_USER_NOT_FOUND = "User with username [%s] not found";
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
	private UserEntity userEntity;

	/**
	 * username
	 */
	private String username;

	/**
	 * Constructor for UsersResource.
	 * 
	 * @param uriInfo
	 *            UriInfo
	 * @param entityManagerConfig
	 *            EntityManager
	 * @param guid
	 *            String
	 */
	public UserResource(UriInfo uriInfo, EntityManager entityManagerConfig,
			String username) {
		this.uriInfo = uriInfo;
		this.entityManagerConfig = entityManagerConfig;
		this.username = username;

		userEntity = findByUsername(entityManagerConfig, username);
	}

	/**
	 * 
	 * @param entityManager
	 *            EntityManager
	 * @param username
	 *            String
	 * @return UserEntity
	 */
	public static UserEntity findByUsername(EntityManager entityManager,
			String username) {

		final TypedQuery<UserEntity> query = entityManager.createNamedQuery(
				"UserEntity.findByUsername", UserEntity.class);

		final java.util.List<UserEntity> results = query.setParameter(
				"username", username).getResultList();
		if (!results.isEmpty()) {
			return results.get(0);
		}
		return null;
	}

	/**
	 * Sub-resource locator for /roles resource
	 * 
	 * @return roles
	 */
	@Path("roles")
	public RolesResource getRoles() {
		if (null == this.userEntity) {
			logger.error(String.format(ERROR_MESSAGE_USER_NOT_FOUND,
					this.username));
			throw new EntityNotFoundException(String.format(
					ERROR_MESSAGE_USER_NOT_FOUND, this.username));
		}
		return new RolesResource(uriInfo, entityManagerConfig, userEntity);
	}

	/**
	 * GET Method : returns an application/json formatted time limit
	 * 
	 * @return JSONObject the time limit
	 */

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public JSONObject getUser() {

		if (null == userEntity) {
			logger.error(String.format(ERROR_MESSAGE_USER_NOT_FOUND, username));
			throw new EntityNotFoundException(String.format(
					ERROR_MESSAGE_USER_NOT_FOUND, username));
		}
		try {
			return UserResource.asJson(userEntity, uriInfo.getPath());
		} catch (JSONException je) {
			logger.error(ERROR_MESSAGE_GET_USER + ERROR_REASON_JSON, je);
			throw new ApplicationJsonException(je, ERROR_MESSAGE_GET_USER
					+ ERROR_REASON_JSON,
					Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
		}
	}
	
	/**
	 * PUT method: updates the user
	 * 
	 * @param jsonEntity
	 *            JSONObject the time limit holding new values
	 * @return Response
	 */
	@PUT
	@Consumes(MediaType.APPLICATION_JSON)
	public Response updateTimeLimit(JSONObject jsonEntity) {
		if (null == userEntity) {
			logger.error(String
					.format(ERROR_MESSAGE_USER_NOT_FOUND, username));
			throw new EntityNotFoundException(String.format(
					ERROR_MESSAGE_USER_NOT_FOUND, username));
		}

		try {
			if (jsonEntity.has("username")) {
				userEntity.setUsername(jsonEntity.optString("username"));
			}
			if (jsonEntity.has("firstname")) {
				userEntity.setFirstname(jsonEntity.optString("firstname"));
			}
			if (jsonEntity.has("lastname")) {
				userEntity.setLastname(jsonEntity.optString("lastname"));
			}
			if(jsonEntity.has("skincolor")){
				userEntity.setSkincolor(jsonEntity.optString("skincolor"));
			}
			if(jsonEntity.has("email")){
				userEntity.setEmail(jsonEntity.optString("email"));
			}

			entityManagerConfig.getTransaction().begin();
			entityManagerConfig.merge(userEntity);
			entityManagerConfig.getTransaction().commit();
		}
		/*
		 * catch (JSONException je) { logger.error(ERROR_MESSAGE_PUT_TIME_LIMIT+
		 * ERROR_REASON_JSON, je); throw new ApplicationJsonException(je,
		 * ERROR_MESSAGE_PUT_TIME_LIMIT+ ERROR_REASON_JSON,
		 * Response.Status.BAD_REQUEST.getStatusCode()); }
		 */
		catch (NumberFormatException nfe) {
			logger.error(ERROR_MESSAGE_PUT_USER
					+ ERROR_REASON_NUMBER_FORMAT, nfe);
			throw new ApplicationJsonException(nfe,
					ERROR_MESSAGE_PUT_USER+ ERROR_REASON_JSON,
					Response.Status.BAD_REQUEST.getStatusCode());
		} catch (RollbackException re) {
			ApplicationJsonException.handleSQLException(re,
					ERROR_MESSAGE_PUT_USER, logger);
			logger.error(ERROR_MESSAGE_PUT_USER+ ERROR_REASON_ROLLBACK,
					re);
			throw re;
		} finally {
			if (null != entityManagerConfig) {
				entityManagerConfig.close();
			}
		}

		return JsonResponseWrapper.getResponse(Response.Status.OK,
				"user updated");
	}
	
	/**
	 * DLETE method: delete the user
	 * 
	 * @param null
	 * @return Response
	 */
	@DELETE
	public Response deleteUser(){
		if(null == userEntity){
			logger.error(String.format(ERROR_MESSAGE_USER_NOT_FOUND, username));
			throw new EntityNotFoundException(String.format(ERROR_MESSAGE_USER_NOT_FOUND, username));
		}
		try{
			entityManagerConfig.getTransaction().begin();
			entityManagerConfig.remove(userEntity);
			entityManagerConfig.getTransaction().commit();
		} finally{
			if(null != entityManagerConfig){
				entityManagerConfig.close();
			}
		}
		
		return JsonResponseWrapper.getResponse(Response.Status.OK, "user deleted");
	}
	

	public static JSONObject asJson(UserEntity userEntity, String path)
			throws JSONException {
		final JSONObject userAsJson = ApiResource.getMetaResource(path,
				UserResource.class);

		userAsJson.put("username", userEntity.getUsername());
		userAsJson.put("firstname", userEntity.getFirstname());
		userAsJson.put("lastname",userEntity.getLastname());
		userAsJson.put("skincolor", userEntity.getSkincolor());
		userAsJson.put("email", userEntity.getEmail());
		return userAsJson;
	}
}
