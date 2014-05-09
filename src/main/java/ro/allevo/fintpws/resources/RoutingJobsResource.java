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
import ro.allevo.fintpws.model.RoutingJobEntity;
import ro.allevo.fintpws.util.JsonResponseWrapper;

/**
 * @author anda
 * @version $Revision: 1.0 $
 */

public class RoutingJobsResource extends PagedCollection {
	/**
	 * Field logger
	 */
	private static final Logger logger = LogManager
			.getLogger(RoutingJobsResource.class.getName());

	/**
	 * Field ERROR_MESSAGE_GET_ROUTING_JOBS. (value is ""Error returning routing jobs : "")
	 */
	static final String ERROR_MESSAGE_GET_ROUTING_JOBS = "Error returning routing jobs : ";
	/**
	 * Field ERROR_MESSAGE_POST_ROUTING_JOBS. (value is ""Error creating routing jobs : "")
	 */
	static final String ERROR_MESSAGE_POST_ROUTING_JOBS = "Error creating routing jobs : ";

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
	private final EntityManager entityManagerData;

	/**
	 * 
	 * @param uriInfo
	 *            UriInfo
	 * @param entityManagerData
	 *            EntityManagerData
	 */
	
	public RoutingJobsResource(UriInfo uriInfo,
			EntityManager entityManagerData) {
		super(uriInfo, entityManagerData.createNamedQuery(
				"RoutingJobEntity.findAll", RoutingJobEntity.class), entityManagerData
				.createNamedQuery("RoutingJobEntity.findTotal", Long.class));
		this.entityManagerData = entityManagerData;
	}

	
	/**
	 * Returns a routing job sub-resource with guid
	 * 
	 * @param guid
	 *            String guid of the routing job as last element in the path
	 * 
	 * @return RoutingJobResource The routing job sub-resource
	 */
	@Path("{id}")
	public RoutingJobResource getRoutingJobsResource(
			@PathParam("id") String guid) {
		return new RoutingJobResource(getUriInfo(), entityManagerData, guid);
	}

	/**
	 * GET method : returns an application/json formatted list of routing jobs
	 * 
	 * @return JSONObject The list of routing jobs
	 */
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public JSONObject getRoutingJobsAsJson() {
		try {
			getPage();
			return asJson();
		} catch (JSONException je) {
			logger.error(ERROR_MESSAGE_GET_ROUTING_JOBS + ERROR_REASON_JSON, je);
			throw new ApplicationJsonException(je,
					ERROR_MESSAGE_GET_ROUTING_JOBS + ERROR_REASON_JSON,
					Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
		}
	}
	
	/**
	 * POST method : creates a routing job
	 * 
	 * @param jsonEntity
	 *            JSONObject
	 * @return Response
	 */
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	public Response postForm(JSONObject jsonEntity) {
		final String id;
		final RoutingJobEntity routingJobEntity = new RoutingJobEntity();
		URI routingJobUri = null;
		try {
			//Fill required data
			routingJobEntity.setGuid(jsonEntity.getString("guid"));
			routingJobEntity.setStatus(new BigDecimal (jsonEntity.optInt("status",0)));
			routingJobEntity.setBackout(new BigDecimal (jsonEntity.optInt("backout",0)));
			routingJobEntity.setPriority(new BigDecimal (jsonEntity.optInt("priority",0)));
			routingJobEntity.setFunction(jsonEntity.getString("function"));
		
			//Fill optional data
			routingJobEntity.setRoutingpoint(jsonEntity.optString("routingpoint"));
			routingJobEntity.setUserid(jsonEntity.optInt("userid"));
					
			entityManagerData.getTransaction().begin();
			entityManagerData.persist(routingJobEntity);
			id = entityManagerData.getEntityManagerFactory()
					.getPersistenceUnitUtil().getIdentifier(routingJobEntity).toString();
			entityManagerData.getTransaction().commit();
		} catch (JSONException je) {
			logger.error(ERROR_MESSAGE_POST_ROUTING_JOBS + ERROR_REASON_JSON, je);
			throw new ApplicationJsonException(je,
					ERROR_MESSAGE_POST_ROUTING_JOBS + ERROR_REASON_JSON,
					Response.Status.BAD_REQUEST.getStatusCode());
		} catch (NumberFormatException nfe) {
			logger.error(ERROR_MESSAGE_POST_ROUTING_JOBS
					+ ERROR_REASON_NUMBER_FORMAT, nfe);
			throw new ApplicationJsonException(
					nfe,
					ERROR_MESSAGE_POST_ROUTING_JOBS + ERROR_REASON_NUMBER_FORMAT,
					Response.Status.BAD_REQUEST.getStatusCode());
		} catch (RollbackException re) {
			ApplicationJsonException.handleSQLException(re,
					ERROR_MESSAGE_POST_ROUTING_JOBS, logger);
			logger.error(
					ERROR_MESSAGE_POST_ROUTING_JOBS + ERROR_REASON_ROLLBACK, re);
			throw re;
		} finally {
			if (null != entityManagerData) {
				entityManagerData.close();
			}
		}
		routingJobUri = getUriInfo().getAbsolutePathBuilder()
				.path(routingJobEntity.toString()).build();

		return JsonResponseWrapper.getResponse(id, Response.Status.CREATED,
				routingJobUri);
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
		final JSONObject routingJobsAsJson = super.asJson();

		// fill data
		final JSONArray routingJobsArray = new JSONArray();
		final List<?> items = getItems();

		if (items.size() > 0) {
			for (RoutingJobEntity routingJobEntity : (List<RoutingJobEntity>) items) {
				routingJobsArray.put(RoutingJobResource.asJson(
						routingJobEntity,
						UriBuilder.fromPath(getUriInfo().getPath())
								.path(routingJobEntity.toString()).build()
								.getPath()));
			}
		}
		routingJobsAsJson.put("routingjobs", routingJobsArray);
		return routingJobsAsJson;
	}
	
}
