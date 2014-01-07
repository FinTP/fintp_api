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
import ro.allevo.fintpws.model.RoutingSchemaEntity;
import ro.allevo.fintpws.model.TimeLimitEntity;
import ro.allevo.fintpws.util.JsonResponseWrapper;
import ro.allevo.fintpws.util.ResourcesUtils;

/**
 * @author remus
 * @version $Revision: 1.0 $
 */
public class RoutingSchemaResource {

	/**
	 * Field logger
	 */
	private static final Logger logger = LogManager
			.getLogger(RoutingSchemaResource.class);
	
	/**
	 * Field ERROR_MESSAGE_GET_ROUTING_SCHEMA. (value is ""Error returning routing
	 * schema : "")
	 */
	static final String ERROR_MESSAGE_GET_ROUTING_SCHEMA = "Error returning routing schema : ";
	/**
	 * Field ERROR_MESSAGE_PUT_ROUTING_SCHEMA. (value is ""Error updating routing
	 * schema : "")
	 */
	static final String ERROR_MESSAGE_PUT_ROUTING_SCHEMA= "Error updating routing schema : ";
	/**
	 * Field ERROR_MESSAGE_ROUT_SCHEMA_NOT_FOUND. (value is ""Routing schema with
	 * name [%s] not found"")
	 */
	static final String ERROR_MESSAGE_ROUT_SCHEMA_NOT_FOUND = "Routing schema with guid [%s] not found";
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
	private RoutingSchemaEntity routingSchemaEntity;

	/**
	 * default constructor
	 */
	public RoutingSchemaResource() {

	}
	
	/**
	 * name
	 */
	private String name;
	
	public RoutingSchemaResource(UriInfo uriInfo, 
			EntityManager entityManagerConfig, String name){
		this.uriInfo = uriInfo;
		this.entityManagerConfig = entityManagerConfig;
		this.name = name;
		routingSchemaEntity = RoutingSchemaResource.findByName(entityManagerConfig, name);
	}
	
	
	/**
	 * 
	 * @param entityManager
	 *            EntityManager
	 * @param name
	 *            String
	 * @return RoutingSchemaEntity
	 */

	public static RoutingSchemaEntity findByName(EntityManager entityManager,
			String name) {

		final TypedQuery<RoutingSchemaEntity> query = entityManager
				.createNamedQuery("RoutingSchemaEntity.findByName",
						RoutingSchemaEntity.class);

		final java.util.List<RoutingSchemaEntity> results = query.setParameter(
				"name", name).getResultList();
		if (!results.isEmpty()) {
			return results.get(0);
		}
		return null;
	}
	
	/**
	 * Sub-resource locator for /timelimits resource
	 * @return timelimits
	 */
	@Path("timelimits")
	public TimeLimitsResource getTimeLimits() {
		if (null == this.routingSchemaEntity) {
			logger.error(String.format(ERROR_MESSAGE_ROUT_SCHEMA_NOT_FOUND,
					this.name));
			throw new EntityNotFoundException(String.format(
					ERROR_MESSAGE_ROUT_SCHEMA_NOT_FOUND, this.name));
		}
		return new TimeLimitsResource(uriInfo, entityManagerConfig,
				this.routingSchemaEntity);
	}
	
	/**
	 * Sub-resource locator for /routingrules resource
	 * @return routingrules
	 */
	@Path("routingrules")
	public RoutingRulesResource getRoutingRules() {
		if (null == this.routingSchemaEntity) {
			logger.error(String.format(ERROR_MESSAGE_ROUT_SCHEMA_NOT_FOUND,
					this.name));
			throw new EntityNotFoundException(String.format(
					ERROR_MESSAGE_ROUT_SCHEMA_NOT_FOUND, this.name));
		}
		return new RoutingRulesResource(uriInfo, entityManagerConfig, routingSchemaEntity);
	}
	
	
	/**
	 * GET Method : returns an application/json formatted routing schema
	 * 
	 * @return JSONObject the routing schema
	 */
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public JSONObject getRoutingSchema(){
		
		if(null == routingSchemaEntity){
			logger.error(ERROR_MESSAGE_ROUT_SCHEMA_NOT_FOUND, name);
			throw new EntityNotFoundException(String.format(ERROR_MESSAGE_ROUT_SCHEMA_NOT_FOUND, name));
		}
		try{
			return RoutingSchemaResource.asJson(routingSchemaEntity, uriInfo.getPath());
		}catch(JSONException je){
			logger.error(ERROR_MESSAGE_GET_ROUTING_SCHEMA + ERROR_REASON_JSON, je);
			throw new ApplicationJsonException(je,
					ERROR_MESSAGE_GET_ROUTING_SCHEMA + ERROR_REASON_JSON,
					Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
		}
	}
	
	/**
	 * PUT method : updates the routing schema
	 * 
	 * @param jsonEntity
	 *            JSONObject the routing schema holding new values
	 * @return Response
	 */
	
	@PUT
	@Consumes(MediaType.APPLICATION_JSON)
	public Response updateRoutingSchema(JSONObject jsonEntity){
		if(null == routingSchemaEntity){
			logger.error(ERROR_MESSAGE_ROUT_SCHEMA_NOT_FOUND, name);
			throw new EntityNotFoundException(String.format(ERROR_MESSAGE_ROUT_SCHEMA_NOT_FOUND, name));
			
		}
		
		try{
			if(jsonEntity.has("guid")){
				routingSchemaEntity.setGuid(new Long(jsonEntity.getString("guid")).longValue());
			}
			
			if(jsonEntity.has("name")){
				routingSchemaEntity.setName(jsonEntity.getString("name"));
			}
			
			if(jsonEntity.has("active")){
				routingSchemaEntity.setActive(new BigDecimal(jsonEntity.getInt("active")));
			}
			
			if(jsonEntity.has("description")){
				routingSchemaEntity.setDescription(jsonEntity.getString("description"));
			}
			
			if(jsonEntity.has("startlimit")){
				TimeLimitEntity startLimit = TimeLimitResource.findByName(
						this.entityManagerConfig,
						jsonEntity.optString("startlimit"));
				routingSchemaEntity.setStartLimitEntity(startLimit);
			}
			
			if(jsonEntity.has("stoplimit")){
				TimeLimitEntity stopLimit = TimeLimitResource.findByName(
						this.entityManagerConfig,
						jsonEntity.optString("stoplimit"));
				routingSchemaEntity.setStopLimitEntity(stopLimit);
			}
			
			if(jsonEntity.has("sessioncode")){
				routingSchemaEntity.setSessioncode(jsonEntity.getString("sessioncode"));
			}
			
			if(jsonEntity.has("isvisible")){
				routingSchemaEntity.setIsvisible(jsonEntity.getString("isvisible"));
			}
			
			entityManagerConfig.getTransaction().begin();
			entityManagerConfig.merge(routingSchemaEntity);
			entityManagerConfig.getTransaction().commit();
		} catch(JSONException je){
			logger.error(ERROR_MESSAGE_PUT_ROUTING_SCHEMA + ERROR_REASON_JSON, je);
			throw new ApplicationJsonException(je,
					ERROR_MESSAGE_PUT_ROUTING_SCHEMA + ERROR_REASON_JSON,
					Response.Status.BAD_REQUEST.getStatusCode());
		} catch (NumberFormatException nfe){
			logger.error(ERROR_MESSAGE_PUT_ROUTING_SCHEMA + ERROR_REASON_NUMBER_FORMAT, nfe);
			throw new ApplicationJsonException(nfe,
					ERROR_MESSAGE_PUT_ROUTING_SCHEMA + ERROR_REASON_NUMBER_FORMAT,
					Response.Status.BAD_REQUEST.getStatusCode());		
		} catch(RollbackException re){
			ApplicationJsonException.handleSQLException(re,
					ERROR_MESSAGE_PUT_ROUTING_SCHEMA, logger);
			logger.error(
					ERROR_MESSAGE_PUT_ROUTING_SCHEMA + ERROR_REASON_ROLLBACK, re);
			throw re;
		} finally{
			if(null != entityManagerConfig){
				entityManagerConfig.close();
			}
		}
		
		return JsonResponseWrapper.getResponse(Response.Status.OK, "routing schema updated");
	}
	
	/**
	 * DELETE method : deletes the routing schema
	 * 
	 * @return Response
	 */
	@DELETE
	public Response deleteRoutingSchema(){
		if(null == routingSchemaEntity){
			logger.error(String.format(ERROR_MESSAGE_ROUT_SCHEMA_NOT_FOUND, name));
			throw new EntityNotFoundException(String.format(ERROR_MESSAGE_ROUT_SCHEMA_NOT_FOUND, name));
		}
		try{
			entityManagerConfig.getTransaction().begin();
			entityManagerConfig.remove(routingSchemaEntity);
			entityManagerConfig.getTransaction().commit();
		} finally{
			if(null != entityManagerConfig){
				entityManagerConfig.close();
			}
		}
		
		return JsonResponseWrapper.getResponse(Response.Status.OK, "routing schema deleted");
	}
	
	/**
	 * Returns the resource formatted as json
	 * 
	 * @param routingSchemaEntity
	 *            RoutingSchemaEntity
	 * @param path
	 *            String
	 * @throws JSONException
	 * @return JSONObject * @throws JSONException
	 */

	public static JSONObject asJson(RoutingSchemaEntity routingSchemaEntity,
			String path) throws JSONException {
		JSONObject routingSchemaAsJson = ApiResource.getMetaResource(path,
				RoutingSchemaResource.class);

		routingSchemaAsJson.put("guid", routingSchemaEntity.getGuid())
				.put("name", routingSchemaEntity.getName())
				.put("description", routingSchemaEntity.getDescription())
				.put("active", routingSchemaEntity.getActive())
				.put("startlimit", routingSchemaEntity.getStartLimitEntity().getLimitname())
				.put("stoplimit", routingSchemaEntity.getStopLimitEntity().getLimitname())
				.put("sessioncode", routingSchemaEntity.getSessioncode())
				.put("isvisible", routingSchemaEntity.getIsvisible());
		
		routingSchemaAsJson = ResourcesUtils.createLink(routingSchemaAsJson, path + "/timelimits", "timelimits");
		
		return routingSchemaAsJson;
	}

}
