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
import ro.allevo.fintpws.model.QueueEntity;
import ro.allevo.fintpws.model.QueueTypeEntity;
import ro.allevo.fintpws.model.RoutingRuleEntity;
import ro.allevo.fintpws.model.RoutingSchemaEntity;
import ro.allevo.fintpws.util.JsonResponseWrapper;

/**
 * @author remus
 * @version $Revision: 1.0 $
 */
public class RoutingRuleResource {

	/**
	 * Field logger
	 */
	private static final Logger logger = LogManager
			.getLogger(RoutingRuleResource.class);

	/**
	 * Field ERROR_MESSAGE_GET_ROUTING_RULE. (value is ""Error returning routing
	 * rule : "")
	 */
	static final String ERROR_MESSAGE_GET_ROUTING_RULE = "Error returning routing rule : ";
	/**
	 * Field ERROR_MESSAGE_PUT_ROUTING_RULE. (value is ""Error updating routing
	 * rule : "")
	 */
	static final String ERROR_MESSAGE_PUT_ROUTING_RULE = "Error updating routing rule : ";
	/**
	 * Field ERROR_MESSAGE_ROUT_RULE_NOT_FOUND. (value is ""Routing rule with
	 * name [%s] not found"")
	 */
	static final String ERROR_MESSAGE_ROUT_RULE_NOT_FOUND = "Routing rule with guid [%s] not found";
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
	private RoutingRuleEntity routingRuleEntity;

	/**
	 * default constructor
	 */
	public RoutingRuleResource() {

	}

	/**
	 * guid
	 */
	private String guid;

	/**
	 * Constructor for RoutingRuleResource.
	 * 
	 * @param uriInfo
	 *            UriInfo
	 * @param entityManagerConfig
	 *            EntityManager
	 * @param guid
	 *            String
	 */
	public RoutingRuleResource(UriInfo uriInfo,
			EntityManager entityManagerConfig, String guid) {
		this.uriInfo = uriInfo;
		this.entityManagerConfig = entityManagerConfig;
		this.guid = guid;
		routingRuleEntity = null;

		routingRuleEntity = RoutingRuleResource.findByGuid(entityManagerConfig,
				guid);
	}

	/**
	 * 
	 * @param entityManager
	 *            EntityManager
	 * @param guid
	 *            String
	 * @return RoutingRuleEntity
	 */

	public static RoutingRuleEntity findByGuid(EntityManager entityManager,
			String guid) {

		final TypedQuery<RoutingRuleEntity> query = entityManager
				.createNamedQuery("RoutingRuleEntity.findByGuid",
						RoutingRuleEntity.class);

		try{
			final java.util.List<RoutingRuleEntity> results = query.setParameter(
					"guid", new Long(guid).longValue()).getResultList();
			if (!results.isEmpty()) {
				return results.get(0);
			}
			return null;
		}catch(NumberFormatException nfe){
			logger.error(String.format(ERROR_MESSAGE_ROUT_RULE_NOT_FOUND, guid));
			throw new EntityNotFoundException(String.format(
					ERROR_MESSAGE_ROUT_RULE_NOT_FOUND, guid));
		}
		
	}

	/**
	 * GET Method : returns an application/json formatted routing rule
	 * 
	 * @return JSONObject the routing rule
	 */

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public JSONObject getRoutingRule() {

		if (null == routingRuleEntity) {
			logger.error(String.format(ERROR_MESSAGE_ROUT_RULE_NOT_FOUND, guid));
			throw new EntityNotFoundException(String.format(
					ERROR_MESSAGE_ROUT_RULE_NOT_FOUND, guid));
		}
		try {
			return RoutingRuleResource.asJson(routingRuleEntity,
					uriInfo.getPath());
		} catch (JSONException je) {
			logger.error(ERROR_MESSAGE_GET_ROUTING_RULE + ERROR_REASON_JSON, je);
			throw new ApplicationJsonException(je,
					ERROR_MESSAGE_GET_ROUTING_RULE + ERROR_REASON_JSON,
					Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
		}
	}

	/**
	 * PUT method : updates the routing rule
	 * 
	 * @param jsonEntity
	 *            JSONObject the routing rule holding new values
	 * @return Response
	 */

	@PUT
	@Consumes(MediaType.APPLICATION_JSON)
	public Response updateRoutingRule(JSONObject jsonEntity) {
		if (null == routingRuleEntity) {
			logger.error(String.format(ERROR_MESSAGE_ROUT_RULE_NOT_FOUND, guid));
			throw new EntityNotFoundException(String.format(
					ERROR_MESSAGE_ROUT_RULE_NOT_FOUND, guid));
		}

		try {
			if (jsonEntity.has("queue")) {
				try {
					QueueEntity queueEntity = QueueResource
							.findByName(entityManagerConfig,
									jsonEntity.getString("queue"));
					routingRuleEntity.setQueueEntity(queueEntity);
				} catch (NullPointerException e) {
					// TODO: change reason
					logger.error(ERROR_MESSAGE_PUT_ROUTING_RULE+ ERROR_REASON_JSON, e);
					throw new ApplicationJsonException(e,
							ERROR_MESSAGE_PUT_ROUTING_RULE + ERROR_REASON_JSON,
							Response.Status.BAD_REQUEST.getStatusCode());
				}
			}
			
			if (jsonEntity.has("schema")) {
				try {
					RoutingSchemaEntity rsEntity = RoutingSchemaResource
							.findByName(entityManagerConfig,
									jsonEntity.getString("schema"));
					routingRuleEntity.setRoutingSchemaEntity(rsEntity);
				} catch (NullPointerException e) {
					// TODO: change reason
					logger.error(ERROR_MESSAGE_PUT_ROUTING_RULE+ ERROR_REASON_JSON, e);
					throw new ApplicationJsonException(e,
							ERROR_MESSAGE_PUT_ROUTING_RULE + ERROR_REASON_JSON,
							Response.Status.BAD_REQUEST.getStatusCode());
				}
			}
			if (jsonEntity.has("action")) {
				routingRuleEntity.setAction(jsonEntity.getString("action"));
			}
			if (jsonEntity.has("description")) {
				routingRuleEntity.setDescription(jsonEntity
						.getString("description"));
			}
			if (jsonEntity.has("messagecondition")) {
				routingRuleEntity.setMessagecondition(jsonEntity.getString("messagecondition"));
			}
			if (jsonEntity.has("functioncondition")) {
				routingRuleEntity.setFunctioncondition(jsonEntity.getString("functioncondition"));
			}
			if (jsonEntity.has("metadatacondition")) {
				routingRuleEntity.setMetadatacondition(jsonEntity.getString("metadatacondition"));
			}
			if (jsonEntity.has("sequence")) {
				routingRuleEntity.setSequence(new BigDecimal(jsonEntity
						.getInt("sequence")));
			}
			if (jsonEntity.has("ruletype")) {
				routingRuleEntity.setRuletype(new BigDecimal(jsonEntity
						.getInt("ruletype")));
			}

			entityManagerConfig.getTransaction().begin();
			entityManagerConfig.merge(routingRuleEntity);
			entityManagerConfig.getTransaction().commit();
		} catch (JSONException je) {
			logger.error(ERROR_MESSAGE_PUT_ROUTING_RULE + ERROR_REASON_JSON, je);
			throw new ApplicationJsonException(je,
					ERROR_MESSAGE_PUT_ROUTING_RULE + ERROR_REASON_JSON,
					Response.Status.BAD_REQUEST.getStatusCode());
		} catch (NumberFormatException nfe) {
			logger.error(ERROR_MESSAGE_PUT_ROUTING_RULE
					+ ERROR_REASON_NUMBER_FORMAT, nfe);
			throw new ApplicationJsonException(nfe,
					ERROR_MESSAGE_PUT_ROUTING_RULE + ERROR_REASON_NUMBER_FORMAT,
					Response.Status.BAD_REQUEST.getStatusCode());
		} catch (RollbackException re) {
			ApplicationJsonException.handleSQLException(re,
					ERROR_MESSAGE_PUT_ROUTING_RULE, logger);
			logger.error(
					ERROR_MESSAGE_PUT_ROUTING_RULE + ERROR_REASON_ROLLBACK, re);
			throw re;
		} finally {
			if (null != entityManagerConfig) {
				entityManagerConfig.close();
			}
		}

		return JsonResponseWrapper.getResponse(Response.Status.OK,
				"routing rule updated");
	}

	/**
	 * DELETE method : deletes the routing rule
	 * 
	 * @return Response
	 */

	@DELETE
	public Response deleteRoutingRule() {
		if (null == routingRuleEntity) {
			logger.error(String.format(ERROR_MESSAGE_ROUT_RULE_NOT_FOUND, guid));
			throw new EntityNotFoundException(String.format(
					ERROR_MESSAGE_ROUT_RULE_NOT_FOUND, guid));
		}
		try {
			entityManagerConfig.getTransaction().begin();
			entityManagerConfig.remove(routingRuleEntity);
			entityManagerConfig.getTransaction().commit();
		} finally {
			if (null != entityManagerConfig) {
				entityManagerConfig.close();
			}
		}

		return JsonResponseWrapper.getResponse(Response.Status.OK,
				"routing rule deleted");
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

	public static JSONObject asJson(RoutingRuleEntity routingRuleEntity,
			String path) throws JSONException {
		final JSONObject routingRuleAsJson = ApiResource.getMetaResource(path,
				RoutingRuleResource.class);

		routingRuleAsJson.put("guid", routingRuleEntity.getGuid())
				.put("queue", routingRuleEntity.getQueueEntity().getName())
				.put("description", routingRuleEntity.getDescription())
				.put("messagecondition", routingRuleEntity.getMessagecondition())
				.put("functioncondition", routingRuleEntity.getFunctioncondition())
				.put("metadatacondition", routingRuleEntity.getMetadatacondition())
				.put("action", routingRuleEntity.getAction())
				.put("schema", routingRuleEntity.getRoutingSchemaEntity().getName())
				.put("sequence", routingRuleEntity.getSequence())
				.put("ruletype", routingRuleEntity.getRuletype());

		return routingRuleAsJson;
	}

	/**
	 * Method toString.
	 * 
	 * @return String
	 */
	public String toString() {
		return String.valueOf(routingRuleEntity.getGuid());
	}
}
