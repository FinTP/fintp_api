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
import ro.allevo.fintpws.model.RoutingJobEntity;
import ro.allevo.fintpws.util.JsonResponseWrapper;

/**
 * @author anda
 * @version $Revision: 1.0 $
 */

public class RoutingJobResource {

	/**
	 * Field logger
	 */
	private static final Logger logger = LogManager
			.getLogger(RoutingJobResource.class);

	/**
	 * Field ERROR_MESSAGE_GET_ROUTING_JOB. (value is ""Error returning routing job : "")
	 */
	static final String ERROR_MESSAGE_GET_ROUTING_JOB = "Error returning routing job : ";
	/**
	 * Field ERROR_MESSAGE_PUT_ROUTING_JOB. (value is ""Error updating routing job : "")
	 */
	static final String ERROR_MESSAGE_PUT_ROUTING_JOB = "Error updating routing job : ";
	/**
	 * Field ERROR_MESSAGE_ROUTING_JOB_NOT_FOUND. (value is ""routing job with
	 * guid [%s] not found"")
	 */
	static final String ERROR_MESSAGE_ROUTING_JOB_NOT_FOUND = "Routing job with guid [%s] not found";
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
	 * Field entityManagerData.
	 */
	private EntityManager entityManagerData;

	/**
	 * the JPA entity
	 */
	private RoutingJobEntity routingJobEntity;

	/**
	 * default constructor
	 * @return 
	 */
	public RoutingJobResource() {

	}

	/**
	 * guid
	 */
	private String guid;

	/**
	 * Constructor for RoutingJobResourse.
	 * 
	 * @param uriInfo
	 *            UriInfo
	 * @param entityManagerData
	 *            EntityManager
	 * @param guid
	 *            String
	 */
	
	public RoutingJobResource(UriInfo uriInfo,
			EntityManager entityManagerData, String guid) {
		this.uriInfo = uriInfo;
		this.entityManagerData = entityManagerData;
		this.guid = guid;
		routingJobEntity = null;

		routingJobEntity = findByGuid(entityManagerData, guid);
	}

	/**
	 * 
	 * @param entityManager
	 *            EntityManager
	 * @param guid
	 *            String
	 * @return RoutingJobEntity
	 */

	public static RoutingJobEntity findByGuid(EntityManager entityManager,
			String guid) {

		final TypedQuery<RoutingJobEntity> query = entityManager
				.createNamedQuery("RoutingJobEntity.findByGuid",
						RoutingJobEntity.class);

		final java.util.List<RoutingJobEntity> results = query.setParameter(
				"guid", guid).getResultList();
		if (!results.isEmpty()) {
			return results.get(0);
		}
		return null;
	}
	
	/**
	 * GET Method : returns an application/json formatted routing job
	 * 
	 * @return JSONObject the routing job
	 */

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public JSONObject getRoutingJob() {

		if (null == routingJobEntity) {
			logger.error(String.format(ERROR_MESSAGE_ROUTING_JOB_NOT_FOUND,
					guid));
			throw new EntityNotFoundException(String.format(
					ERROR_MESSAGE_ROUTING_JOB_NOT_FOUND, guid));
		}
		try {
			return RoutingJobResource.asJson(routingJobEntity,
					uriInfo.getPath());
		} catch (JSONException je) {
			logger.error(ERROR_MESSAGE_GET_ROUTING_JOB + ERROR_REASON_JSON, je);
			throw new ApplicationJsonException(je,
					ERROR_MESSAGE_GET_ROUTING_JOB + ERROR_REASON_JSON,
					Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
		}
	}
	
	/**
	 * PUT method: updates the routing job
	 * 
	 * @param jsonEntity
	 *            JSONObject the routing job holding new values
	 * @return Response
	 * @throws JSONException 
	 */
	@PUT
	@Consumes(MediaType.APPLICATION_JSON)
	public Response updateRoutingJob(JSONObject jsonEntity) throws JSONException {
		if (null == routingJobEntity) {
			logger.error(String.format(ERROR_MESSAGE_ROUTING_JOB_NOT_FOUND,
					guid));
			throw new EntityNotFoundException(String.format(
					ERROR_MESSAGE_ROUTING_JOB_NOT_FOUND, guid));
		}

		try {
			if (jsonEntity.has("status")) {
				routingJobEntity.setStatus(new BigDecimal (jsonEntity
						.getInt("status")));
			}
			if (jsonEntity.has("backout")) {
				routingJobEntity.setBackout(new BigDecimal (jsonEntity
						.getInt("backout")));
			}
			if (jsonEntity.has("priority")) {
				routingJobEntity.setPriority(new BigDecimal (jsonEntity
						.getInt("priority")));
			}
			if (jsonEntity.has("routingpoint")) {
				routingJobEntity.setRoutingpoint(jsonEntity
						.getString("routingpoint"));
			}
			if (jsonEntity.has("function")) {
				routingJobEntity.setFunction(jsonEntity
						.getString("function"));
			}
			if (jsonEntity.has("userid")) {
				routingJobEntity.setUserid(jsonEntity
						.getString("userid"));
			}
			
			entityManagerData.getTransaction().begin();
			entityManagerData.merge(routingJobEntity);
			entityManagerData.getTransaction().commit();
		}
		
		 catch (JSONException je) {
			 logger.error(ERROR_MESSAGE_PUT_ROUTING_JOB+ ERROR_REASON_JSON, je);
			 throw new ApplicationJsonException(je,
				 ERROR_MESSAGE_PUT_ROUTING_JOB+ ERROR_REASON_JSON,
				 Response.Status.BAD_REQUEST.getStatusCode()); 
		}
		 
		catch (NumberFormatException nfe) {
			logger.error(ERROR_MESSAGE_PUT_ROUTING_JOB
					+ ERROR_REASON_NUMBER_FORMAT, nfe);
			throw new ApplicationJsonException(nfe,
					ERROR_MESSAGE_PUT_ROUTING_JOB + ERROR_REASON_JSON,
					Response.Status.BAD_REQUEST.getStatusCode());
		} catch (RollbackException re) {
			ApplicationJsonException.handleSQLException(re,
					ERROR_MESSAGE_PUT_ROUTING_JOB, logger);
			logger.error(
					ERROR_MESSAGE_PUT_ROUTING_JOB + ERROR_REASON_ROLLBACK, re);
			throw re;
		} finally {
			if (null != entityManagerData) {
				entityManagerData.close();
			}
		}

		return JsonResponseWrapper.getResponse(Response.Status.OK,
				"routing job updated");
	}
	
	/**
	 * DELETE method : deletes the routing job
	 * 
	 * @return Response
	 */

	@DELETE
	public Response deleteRoutingJob() {
		if (null == routingJobEntity) {
			logger.error(String.format(ERROR_MESSAGE_ROUTING_JOB_NOT_FOUND,
					guid));
			throw new EntityNotFoundException(String.format(
					ERROR_MESSAGE_ROUTING_JOB_NOT_FOUND, guid));
		}
		try {
			entityManagerData.getTransaction().begin();
			entityManagerData.remove(routingJobEntity);
			entityManagerData.getTransaction().commit();
		} finally {
			if (null != entityManagerData) {
				entityManagerData.close();
			}
		}

		return JsonResponseWrapper.getResponse(Response.Status.OK,
				"routing job deleted");
	}

	/**
	 * Returns the resource formatted as json
	 * 
	 * @param routingJobEntity
	 *            routingJobEntity
	 * @param path
	 *            String
	 * @return JSONObject
	 * @throws JSONException
	 */
	public static JSONObject asJson(RoutingJobEntity routingJobEntity,
			String path) throws JSONException {
		final JSONObject routingJobAsJson = ApiResource.getMetaResource(path,
				RoutingJobResource.class);

		routingJobAsJson.put("guid", routingJobEntity.getGuid())
				.put("status", routingJobEntity.getStatus())
				.put("backout", routingJobEntity.getBackout())
				.put("priority", routingJobEntity.getPriority())
				.put("routingpoint", routingJobEntity.getRoutingpoint())
				.put("function", routingJobEntity.getFunction())
				.put("userid", routingJobEntity.getUserid());

		return routingJobAsJson;
	}
	
}
