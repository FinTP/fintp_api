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

import java.text.ParseException;

import ro.allevo.fintpws.exceptions.ApplicationJsonException;
import ro.allevo.fintpws.model.ServiceMapEntity;
import ro.allevo.fintpws.util.JsonResponseWrapper;
import ro.allevo.fintpws.util.ResourcesUtils;

public class ServiceMapResource {

	/**
	 * Field logger
	 */
	private static final Logger logger = LogManager
			.getLogger(ServiceMapResource.class);

	/**
	 * Field ERROR_MESSAGE_GET_SERVICE_MAP. (value is ""Error returning service
	 * map : "")
	 */
	static final String ERROR_MESSAGE_GET_SERVICE_MAP = "Error returning service map : ";
	/**
	 * Field ERROR_MESSAGE_PUT_SERVICE_MAP. (value is ""Error updating service
	 * map : "")
	 */
	static final String ERROR_MESSAGE_PUT_SERVICE_MAP = "Error updating service map : ";
	/**
	 * Field ERROR_MESSAGE_SERVICE_MAP_NOT_FOUND. (value is ""Service map with
	 * friendlyname [%s] not found"")
	 */
	static final String ERROR_MESSAGE_SERVICE_MAP_NOT_FOUND = "Service map with friendlyname [%s] not found";
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
	private ServiceMapEntity serviceMapEntity;

	/**
	 * default constructor
	 */
	public ServiceMapResource() {

	}

	/**
	 * friendlyName
	 */
	private String friendlyName;

	/**
	 * Constructor for ServiceMapResource.
	 * 
	 * @param uriInfo
	 *            UriInfo
	 * @param entityManagerConfig
	 *            EntityManager
	 * @param friendlyName
	 *            String
	 */
	public ServiceMapResource(UriInfo uriInfo,
			EntityManager entityManagerConfig, String friendlyName) {
		this.uriInfo = uriInfo;
		this.entityManagerConfig = entityManagerConfig;
		this.friendlyName = friendlyName;
		serviceMapEntity = null;

		serviceMapEntity = findByFriendlyName(entityManagerConfig, friendlyName);
	}

	/**
	 * 
	 * @param entityManager
	 *            EntityManager
	 * @param friendlyName
	 *            String
	 * @return ServiceMapEntity
	 */

	public static ServiceMapEntity findByFriendlyName(
			EntityManager entityManager, String friendlyName) {

		final TypedQuery<ServiceMapEntity> query = entityManager
				.createNamedQuery("ServiceMapEntity.findByFriendlyName",
						ServiceMapEntity.class);

		final java.util.List<ServiceMapEntity> results = query.setParameter(
				"friendlyname", friendlyName).getResultList();
		if (!results.isEmpty()) {
			return results.get(0);
		}
		return null;
	}

	/**
	 * GET Method : returns an application/json formatted service map
	 * 
	 * @return JSONObject the service map
	 */
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public JSONObject getServiceMap() {

		if (null == serviceMapEntity) {
			logger.error(String.format(ERROR_MESSAGE_SERVICE_MAP_NOT_FOUND,
					friendlyName));
			throw new EntityNotFoundException(String.format(
					ERROR_MESSAGE_SERVICE_MAP_NOT_FOUND, friendlyName));
		}
		try {
			return ServiceMapResource.asJson(serviceMapEntity,
					uriInfo.getPath());
		} catch (JSONException je) {
			logger.error(ERROR_MESSAGE_GET_SERVICE_MAP + ERROR_REASON_JSON, je);
			throw new ApplicationJsonException(je,
					ERROR_MESSAGE_GET_SERVICE_MAP + ERROR_REASON_JSON,
					Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
		}
	}

	/**
	 * PUT method: updates the service map
	 * 
	 * @param jsonEntity
	 *            JSONObject the service map holding new values
	 * @return Response
	 */
	@PUT
	@Consumes(MediaType.APPLICATION_JSON)
	public Response updateServiceMap(JSONObject jsonEntity) {
		if (null == serviceMapEntity) {
			logger.error(String.format(ERROR_MESSAGE_SERVICE_MAP_NOT_FOUND,
					friendlyName));
			throw new EntityNotFoundException(String.format(
					ERROR_MESSAGE_SERVICE_MAP_NOT_FOUND, friendlyName));
		}

		try {
			if (jsonEntity.has("friendlyname")) {
				serviceMapEntity.setFriendlyname(jsonEntity
						.getString("friendlyname"));
			}
			if (jsonEntity.has("status")) {
				serviceMapEntity.setStatus(new BigDecimal(jsonEntity
						.getString("status")));
			}
			if (jsonEntity.has("heartbeatinterval")) {
				serviceMapEntity.setHeartbeatinterval(new BigDecimal(jsonEntity
						.getString("heartbeatinterval")));
			}
			if (jsonEntity.has("lastsessionid")) {
				serviceMapEntity.setLastsessionid(new BigDecimal(jsonEntity
						.optInt("lastsessionid")));
			}
			if (jsonEntity.has("lastheartbeat")) {
				serviceMapEntity.setLastheartbeat(ResourcesUtils
						.getTimestamp(jsonEntity.optString("lastheartbeat")));
			}
			if (jsonEntity.has("version")) {
				serviceMapEntity.setVersion(jsonEntity.optString("version"));
			}
			if (jsonEntity.has("partner")) {
				serviceMapEntity.setPartner(jsonEntity.optString("partner"));
			}
			if (jsonEntity.has("servicetype")) {
				serviceMapEntity.setServicetype(new BigDecimal(jsonEntity
						.optInt("servicetype")));
			}
			if (jsonEntity.has("ioidentifier")) {
				serviceMapEntity.setIoidentifier(new BigDecimal(jsonEntity
						.optInt("ioidentifier")));
			}
			if (jsonEntity.has("exitpoint")) {
				serviceMapEntity
						.setExitpoint(jsonEntity.optString("exitpoint"));
			}
			if (jsonEntity.has("sessionid")) {
				serviceMapEntity
						.setSessionid(jsonEntity.optString("sessionid"));
			}
			if (jsonEntity.has("duplicatecheck")) {
				serviceMapEntity.setDuplicatecheck(new BigDecimal(jsonEntity
						.optInt("duplicatecheck")));
			}
			if (jsonEntity.has("duplicateq") && !jsonEntity.optString("duplicateq").equals("") ) {
				serviceMapEntity.setDuplicateq(jsonEntity
						.optString("duplicateq"));
			}
			else
				serviceMapEntity.setDuplicateq(null);
			
			if (jsonEntity.has("duplicatemap")) {
				serviceMapEntity.setDuplicatemap(jsonEntity
						.optString("duplicatemap"));
			}
			if (jsonEntity.has("delayednotifq") && !jsonEntity.optString("delayednotifq").equals("")) {
				serviceMapEntity.setDelayednotifq(jsonEntity
						.optString("delayednotifq"));
			}
			else
				serviceMapEntity.setDelayednotifq(null);
			
			if (jsonEntity.has("duplicatenotifq")&& !jsonEntity.optString("duplicatenotifq").equals("")) {
				serviceMapEntity.setDuplicatenotifq(jsonEntity
						.optString("duplicatenotifq"));
			}
			else
				serviceMapEntity.setDuplicatenotifq(null);
			

			entityManagerConfig.getTransaction().begin();
			entityManagerConfig.merge(serviceMapEntity);
			entityManagerConfig.getTransaction().commit();
		} catch (NumberFormatException nfe) {
			logger.error(ERROR_MESSAGE_PUT_SERVICE_MAP
					+ ERROR_REASON_NUMBER_FORMAT, nfe);
			throw new ApplicationJsonException(nfe,
					ERROR_MESSAGE_PUT_SERVICE_MAP + ERROR_REASON_JSON,
					Response.Status.BAD_REQUEST.getStatusCode());
		} catch (RollbackException re) {
			ApplicationJsonException.handleSQLException(re,
					ERROR_MESSAGE_PUT_SERVICE_MAP, logger);
			logger.error(ERROR_MESSAGE_PUT_SERVICE_MAP + ERROR_REASON_ROLLBACK,
					re);
			throw re;
		} catch (ParseException pe) {
			logger.error(ERROR_MESSAGE_PUT_SERVICE_MAP + ERROR_REASON_PARSE, pe);
			throw new ApplicationJsonException(pe,
					ERROR_MESSAGE_PUT_SERVICE_MAP + ERROR_REASON_PARSE,
					Response.Status.BAD_REQUEST.getStatusCode());
		} catch (JSONException je) {
				logger.error(ERROR_MESSAGE_PUT_SERVICE_MAP + ERROR_REASON_JSON,
						je);
				throw new ApplicationJsonException(je,
						ERROR_MESSAGE_PUT_SERVICE_MAP + ERROR_REASON_JSON,
						Response.Status.BAD_REQUEST.getStatusCode());
		} finally {
			if (null != entityManagerConfig) {
				entityManagerConfig.close();
			}
		}

		return JsonResponseWrapper.getResponse(Response.Status.OK,
				"service map updated");
	}

	/**
	 * DELETE method : deletes the service map
	 * 
	 * @return Response
	 */

	@DELETE
	public Response deleteServiceMap() {
		if (null == serviceMapEntity) {
			logger.error(String.format(ERROR_MESSAGE_SERVICE_MAP_NOT_FOUND,
					friendlyName));
			throw new EntityNotFoundException(String.format(
					ERROR_MESSAGE_SERVICE_MAP_NOT_FOUND, friendlyName));
		}
		try {
			entityManagerConfig.getTransaction().begin();
			entityManagerConfig.remove(serviceMapEntity);
			entityManagerConfig.getTransaction().commit();
		} finally {
			if (null != entityManagerConfig) {
				entityManagerConfig.close();
			}
		}

		return JsonResponseWrapper.getResponse(Response.Status.OK,
				"service map deleted");
	}

	/**
	 * Returns the resource formatted as json
	 * 
	 * @param serviceMapEntity
	 *            ServiceMapEntity
	 * @param path
	 *            String
	 * @return JSONObject
	 * @throws JSONException
	 */
	public static JSONObject asJson(ServiceMapEntity serviceMapEntity,
			String path) throws JSONException {
		final JSONObject serviceMapAsJson = ApiResource.getMetaResource(path,
				ServiceMapResource.class);

		serviceMapAsJson
				.put("friendlyname", serviceMapEntity.getFriendlyname())
				.put("status", serviceMapEntity.getStatus())
				.put("heartbeatinterval",
						serviceMapEntity.getHeartbeatinterval())
				.put("lastsessionid", serviceMapEntity.getLastsessionid())
				.put("lastheartbeat",
						ResourcesUtils.getIsoDateFromTimestamp(serviceMapEntity
								.getLastheartbeat()))
				.put("version", serviceMapEntity.getVersion())
				.put("partner", serviceMapEntity.getPartner())
				.put("servicetype", serviceMapEntity.getServicetype())
				.put("ioidentifier", serviceMapEntity.getIoidentifier())
				.put("exitpoint", serviceMapEntity.getExitpoint())
				.put("sessionid", serviceMapEntity.getSessionid())
				.put("duplicatecheck", serviceMapEntity.getDuplicatecheck())
				.put("duplicateq", serviceMapEntity.getDuplicateq())
				.put("duplicatemap", serviceMapEntity.getDuplicatemap())
				.put("duplicatenotifq", serviceMapEntity.getDuplicatenotifq())
				.put("delayednotifq", serviceMapEntity.getDelayednotifq());

		return serviceMapAsJson;
	}

	/**
	 * Method toString.
	 * 
	 * @return String
	 */
	@Override
	public String toString() {
		return serviceMapEntity.getFriendlyname();
	}

}
