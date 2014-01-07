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
import java.text.ParseException;
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
import ro.allevo.fintpws.model.RoutingSchemaEntity;
import ro.allevo.fintpws.model.TimeLimitEntity;
import ro.allevo.fintpws.util.JsonResponseWrapper;
import ro.allevo.fintpws.util.ResourcesUtils;

/**
 * @author remus
 * @version $Revision: 1.0 $
 */
public class TimeLimitsResource extends PagedCollection {

	/**
	 * Field logger
	 */
	private static final Logger logger = LogManager
			.getLogger(TimeLimitsResource.class.getName());

	/**
	 * Field ERROR_MESSAGE_GET_TIME_LIMITS. (value is ""Error returning time
	 * limits: "")
	 */
	static final String ERROR_MESSAGE_GET_TIME_LIMITS = "Error returning time limits : ";
	/**
	 * Field ERROR_MESSAGE_POST_TIME_LIMITS. (value is ""Error creating time
	 * limits : "")
	 */
	static final String ERROR_MESSAGE_POST_TIME_LIMITS = "Error creating time limits : ";

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

	public TimeLimitsResource(UriInfo uriInfo,
			EntityManager entityManagerConfig, RoutingSchemaEntity routingSchemaEntity) {
		super(uriInfo, entityManagerConfig.createNamedQuery(
				"TimeLimitEntity.findAll", TimeLimitEntity.class),
				entityManagerConfig.createNamedQuery(
						"TimeLimitEntity.findTotal", Long.class));
//		if(ResourcesUtils.hasSortOrFilter(uriInfo)){
//				this.setItemsQuery(ResourcesUtils.getTypedQuery(uriInfo, entityManagerConfig, 
//						TimeLimitEntity.class, null, null));
//				this.setTotalQuery(ResourcesUtils.getCountTypedQuery(uriInfo, entityManagerConfig, 
//						TimeLimitEntity.class, null, null));
//		}
			
		if(null != routingSchemaEntity){
			this.setItemsQuery(entityManagerConfig
					.createNamedQuery("TimeLimitEntity.findAllRoutingSchema", TimeLimitEntity.class)
					.setParameter("startlimit", routingSchemaEntity.getStartLimitEntity().getGuid())
					.setParameter("stoplimit", routingSchemaEntity.getStopLimitEntity().getGuid()));
			this.setTotalQuery(entityManagerConfig
					.createNamedQuery("TimeLimitEntity.findTotalRoutingSchema", Long.class)
					.setParameter("startlimit", routingSchemaEntity.getStartLimitEntity().getGuid())
					.setParameter("stoplimit", routingSchemaEntity.getStopLimitEntity().getGuid()));
		}
		
		this.entityManagerConfig = entityManagerConfig;
	}

	/**
	 * Returns a time limit sub-resource with guid
	 * 
	 * @param guid
	 *            String guid of the time limit as last element in the path
	 * 
	 * @return TimeLimitResource The time limit sub-resource
	 */
	@Path("{name}")
	public TimeLimitResource getTimeLimitsResource(
			@PathParam("name") String name) {
		return new TimeLimitResource(getUriInfo(), entityManagerConfig, name);
	}

	/**
	 * GET method : returns an application/json formatted list of time limits
	 * 
	 * @return JSONObject The list of time limits
	 */
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public JSONObject getTimeLimitsAsJson() {
		try {
			getPage();
			return asJson();
		} catch (JSONException je) {
			logger.error(ERROR_MESSAGE_GET_TIME_LIMITS + ERROR_REASON_JSON, je);
			throw new ApplicationJsonException(je,
					ERROR_MESSAGE_GET_TIME_LIMITS + ERROR_REASON_JSON,
					Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
		}
	}

	/**
	 * POST method : creates a time limit
	 * 
	 * @param jsonEntity
	 *            JSONObject
	 * @return Response
	 */
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	public Response postTimelimit(JSONObject jsonEntity) {
		String id;
		final TimeLimitEntity timeLimitEntity = new TimeLimitEntity();
		URI timeLimitUri = null;
		try {
			
			timeLimitEntity.setLimitname(jsonEntity.getString("limitname"));
			timeLimitEntity.setLimittime(ResourcesUtils.getTime(jsonEntity
					.getString("limittime")));
			entityManagerConfig.getTransaction().begin();
			entityManagerConfig.persist(timeLimitEntity);
			id = timeLimitEntity.getLimitname();
			entityManagerConfig.getTransaction().commit();
		} catch (JSONException je) {
			logger.error(ERROR_MESSAGE_POST_TIME_LIMITS + ERROR_REASON_JSON, je);
			throw new ApplicationJsonException(je,
					ERROR_MESSAGE_POST_TIME_LIMITS + ERROR_REASON_JSON,
					Response.Status.BAD_REQUEST.getStatusCode());
		} catch (NumberFormatException nfe) {
			logger.error(ERROR_MESSAGE_POST_TIME_LIMITS
					+ ERROR_REASON_NUMBER_FORMAT, nfe);
			throw new ApplicationJsonException(
					nfe,
					ERROR_MESSAGE_POST_TIME_LIMITS + ERROR_REASON_NUMBER_FORMAT,
					Response.Status.BAD_REQUEST.getStatusCode());
		} catch (RollbackException re) {
			ApplicationJsonException.handleSQLException(re,
					ERROR_MESSAGE_POST_TIME_LIMITS, logger);
			logger.error(
					ERROR_MESSAGE_POST_TIME_LIMITS + ERROR_REASON_ROLLBACK, re);
			throw re;
		} catch (ParseException pe) {
			logger.error(ERROR_MESSAGE_POST_TIME_LIMITS + ERROR_REASON_PARSE,
					pe);
			throw new ApplicationJsonException(pe,
					ERROR_MESSAGE_POST_TIME_LIMITS + ERROR_REASON_PARSE,
					Response.Status.BAD_REQUEST.getStatusCode());
		} finally {
			if (null != entityManagerConfig) {
				entityManagerConfig.close();
			}
		}
		timeLimitUri = getUriInfo().getAbsolutePathBuilder()
				.path(timeLimitEntity.toString()).build();

		return JsonResponseWrapper.getResponse(id, Response.Status.CREATED,
				timeLimitUri);
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
		final JSONObject timeLimitsAsJson = super.asJson();
		// fill data
		final JSONArray timeLimitsArray = new JSONArray();
		final List<?> items = getItems();

		if (items.size() > 0) {
			for (TimeLimitEntity timeLimitEntity : (List<TimeLimitEntity>) items) {
				timeLimitsArray.put(TimeLimitResource.asJson(
						timeLimitEntity,
						UriBuilder.fromPath(getUriInfo().getPath())
								.path(timeLimitEntity.toString()).build()
								.getPath()));
			}
		}
		timeLimitsAsJson.put("timelimits", timeLimitsArray);
		return timeLimitsAsJson;
	}
	
	
}
