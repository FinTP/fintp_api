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
 * @author remus
 * @version $Revision: 1.0 $
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
import ro.allevo.fintpws.model.RoutingSchemaEntity;
import ro.allevo.fintpws.model.TimeLimitEntity;
import ro.allevo.fintpws.util.JsonResponseWrapper;


public class RoutingSchemasResource extends PagedCollection{

	
	/**
	 * Field logger
	 */
	private static final Logger logger = LogManager
			.getLogger(RoutingRulesResource.class.getName());
	
	/**
	 * Field ERROR_MESSAGE_GET_ROUTING_SCHEMAS. (value is ""Error returning
	 * routing schemas : "")
	 */
	static final String ERROR_MESSAGE_GET_ROUTING_SCHEMAS = "Error returning routing schemas : ";
	/**
	 * Field ERROR_MESSAGE_POST_ROUTING_RULES. (value is ""Error creating
	 * routing schemas : "")
	 */
	static final String ERROR_MESSAGE_POST_ROUTING_SCHEMAS = "Error creating routing schemas : ";
	
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
	 * 
	 * @param uriInfo UriInfo
	 * @param entityManagerConfig EntityManager
	 */
	public RoutingSchemasResource(UriInfo uriInfo, EntityManager entityManagerConfig) {
		super(uriInfo, 
				entityManagerConfig.createNamedQuery("RoutingSchemaEntity.findAll", RoutingSchemaEntity.class), 
				entityManagerConfig.createNamedQuery("RoutingSchemaEntity.findTotal", Long.class));
		
		this.entityManagerConfig = entityManagerConfig;
	}
	
	@Path("{name}")
	public RoutingSchemaResource getRoutingSchemaResource(
			@PathParam("name") String name){
		return new RoutingSchemaResource(getUriInfo(), entityManagerConfig, name);
	}
	
	/**
	 * GET method : returns an application/json formatted list of routing schemas
	 * 
	 * @return JSONObject The list of routing schemas
	 */
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public JSONObject getRoutingSchemasAsJson(){
		try{
			getPage();
			return asJson();
		} catch(JSONException je){
			logger.error(ERROR_MESSAGE_GET_ROUTING_SCHEMAS + ERROR_REASON_JSON,
					je);
			throw new ApplicationJsonException(je,
					ERROR_MESSAGE_GET_ROUTING_SCHEMAS + ERROR_REASON_JSON,
					Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
		}
	}
	
	/**
	 * POST method : creates a routing schema
	 * 
	 * @param jsonEntity JSONObject
	 * @return Response
	 */
	
	@POST 
	@Consumes(MediaType.APPLICATION_JSON)
	public Response postForm(JSONObject jsonEntity){
		//String id;
		final RoutingSchemaEntity routingSchemaEntity = new RoutingSchemaEntity();
		URI routingSchemaUri = null;
		try{
			
			//fill required data
			if(jsonEntity.has("guid")){
				routingSchemaEntity.setGuid(new Long(jsonEntity.getString("guid")).longValue());
			}
			
			routingSchemaEntity.setName(jsonEntity.getString("name"));
			routingSchemaEntity.setActive(new BigDecimal(jsonEntity.getInt("active")));
			TimeLimitEntity startLimit = TimeLimitResource.findByName(
					this.entityManagerConfig,
					jsonEntity.optString("startlimit"));
			routingSchemaEntity.setStartLimitEntity(startLimit);
			TimeLimitEntity stopLimit = TimeLimitResource.findByName(
					this.entityManagerConfig,
					jsonEntity.optString("stoplimit"));
			routingSchemaEntity.setStopLimitEntity(stopLimit);
			
			//fill optional data
			if(jsonEntity.has("sessioncode")){
				routingSchemaEntity.setSessioncode(jsonEntity.getString("sessioncode"));
			}
			
			if(jsonEntity.has("isvisible")){
				routingSchemaEntity.setIsvisible(jsonEntity.getString("isvisible"));
			}
			
			if(jsonEntity.has("description")){
				routingSchemaEntity.setDescription(jsonEntity.getString("description"));
			}
			
			entityManagerConfig.getTransaction().begin();
			entityManagerConfig.persist(routingSchemaEntity);
			//id = entityManagerConfig.getEntityManagerFactory()
			//	.getPersistenceUnitUtil().getIdentifier(routingSchemaEntity).toString();
			entityManagerConfig.getTransaction().commit();
		} catch (JSONException je){
			logger.error(ERROR_MESSAGE_POST_ROUTING_SCHEMAS + ERROR_REASON_JSON, je);
			throw new ApplicationJsonException(je,
					ERROR_MESSAGE_POST_ROUTING_SCHEMAS + ERROR_REASON_JSON,
					Response.Status.BAD_REQUEST.getStatusCode());
		} catch (NumberFormatException nfe) {
			logger.error(ERROR_MESSAGE_POST_ROUTING_SCHEMAS
					+ ERROR_REASON_NUMBER_FORMAT, nfe);
			throw new ApplicationJsonException(nfe,
					ERROR_MESSAGE_POST_ROUTING_SCHEMAS
							+ ERROR_REASON_NUMBER_FORMAT,
					Response.Status.BAD_REQUEST.getStatusCode());
		}catch (RollbackException re) {
			ApplicationJsonException.handleSQLException(re,
					ERROR_MESSAGE_POST_ROUTING_SCHEMAS, logger);
			logger.error(ERROR_MESSAGE_POST_ROUTING_SCHEMAS
					+ ERROR_REASON_ROLLBACK, re);
			throw re;
		} finally{
			if(null != entityManagerConfig){
				entityManagerConfig.close();
			}
		}
		
		routingSchemaUri = getUriInfo().getAbsolutePathBuilder()
				.path(routingSchemaEntity.getName()).build();
	
		return JsonResponseWrapper.getResponse(Response.Status.CREATED,
				routingSchemaUri);
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
		final JSONObject routingSchemasAsJson = super.asJson();

		// fill data
		final JSONArray routingSchemasArray = new JSONArray();
		final List<?> items = getItems();

		if (items.size() > 0) {
			for (RoutingSchemaEntity routingSchemaEntity : (List<RoutingSchemaEntity>) items) {
				routingSchemasArray.put(RoutingSchemaResource.asJson(
						routingSchemaEntity,
						UriBuilder.fromPath(getUriInfo().getPath())
								.path(String.valueOf(routingSchemaEntity.getName())).build()
								.getPath()));
			}
		}
		routingSchemasAsJson.put("routingschemas", routingSchemasArray);
		return routingSchemasAsJson;
	}
}
