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
import ro.allevo.fintpws.model.QueueEntity;
import ro.allevo.fintpws.model.QueueTypeEntity;
import ro.allevo.fintpws.model.RoutingRuleEntity;
import ro.allevo.fintpws.model.RoutingSchemaEntity;
import ro.allevo.fintpws.model.ServiceMapEntity;
import ro.allevo.fintpws.util.JsonResponseWrapper;

/**
 * @author remus
 * @version $Revision: 1.0 $
 */
public class RoutingRulesResource extends PagedCollection {

	/**
	 * Field logger
	 */
	private static final Logger logger = LogManager
			.getLogger(RoutingRulesResource.class.getName());

	/**
	 * Field ERROR_MESSAGE_GET_ROUTING_RULES. (value is ""Error returning
	 * routing rules : "")
	 */
	static final String ERROR_MESSAGE_GET_ROUTING_RULES = "Error returning routing rules : ";
	/**
	 * Field ERROR_MESSAGE_POST_ROUTING_RULES. (value is ""Error creating
	 * routing rules : "")
	 */
	static final String ERROR_MESSAGE_POST_ROUTING_RULES = "Error creating routing rules : ";

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
	 * @param uriInfo
	 *            UriInfo
	 * @param entityManagerConfig
	 *            EntityManager
	 */

	public RoutingRulesResource(UriInfo uriInfo,
			EntityManager entityManagerConfig,
			RoutingSchemaEntity routingSchemaEntity) {
		super(uriInfo, entityManagerConfig.createNamedQuery(
				"RoutingRuleEntity.findAll", RoutingRuleEntity.class),
				entityManagerConfig.createNamedQuery(
						"RoutingRuleEntity.findTotal", Long.class));
		if (null != routingSchemaEntity) {
			this.setItemsQuery(entityManagerConfig.createNamedQuery(
					"RoutingRuleEntity.findAllRoutingSchema",
					RoutingSchemaEntity.class).setParameter("schemaguid",
					routingSchemaEntity.getGuid()));
			this.setTotalQuery(entityManagerConfig.createNamedQuery(
					"RoutingRuleEntity.findTotalRoutingSchema", Long.class)
					.setParameter("schemaguid", routingSchemaEntity.getGuid()));
		}

		this.entityManagerConfig = entityManagerConfig;

	}

	/**
	 * Returns a routing rule sub-resource with guid
	 * 
	 * @param guid
	 *            String guid of the routing rule as last element in the path
	 * 
	 * @return RoutingRuleResource The routing rule sub-resource
	 */
	@Path("{id}")
	public RoutingRuleResource getRoutingRuleResource(
			@PathParam("id") String guid) {
		return new RoutingRuleResource(getUriInfo(), entityManagerConfig, guid);
	}

	/**
	 * GET method : returns an application/json formatted list of routing rules
	 * 
	 * @return JSONObject The list of routing rules
	 */
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public JSONObject getRoutingRulesAsJson() {
		try {
			getPage();
			return asJson();
		} catch (JSONException je) {
			logger.error(ERROR_MESSAGE_GET_ROUTING_RULES + ERROR_REASON_JSON,
					je);
			throw new ApplicationJsonException(je,
					ERROR_MESSAGE_GET_ROUTING_RULES + ERROR_REASON_JSON,
					Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
		}
	}

	/**
	 * POST method : creates a routing rule
	 * 
	 * @param jsonEntity
	 *            JSONObject
	 * @return Response
	 */

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	public Response postForm(JSONObject jsonEntity) {
		String id;
		final RoutingRuleEntity routingRuleEntity = new RoutingRuleEntity();
		URI routingRuleUri = null;
		try {

			// RoutingRule

			// fill required data
			if (jsonEntity.has("guid")) {
				routingRuleEntity.setGuid(new Long(jsonEntity.getString("guid")).longValue());
			}

			try {
				QueueEntity queueEntity = QueueResource
						.findByName(entityManagerConfig,
								jsonEntity.getString("queue"));
				RoutingSchemaEntity rsEntity= RoutingSchemaResource
						.findByName(entityManagerConfig, 
								jsonEntity.getString("schema"));
				routingRuleEntity.setQueueEntity(queueEntity);
				routingRuleEntity.setRoutingSchemaEntity(rsEntity);
				
			} catch (NullPointerException e) {
				//TODO: change reason
				logger.error(ERROR_MESSAGE_POST_ROUTING_RULES
						+ ERROR_REASON_JSON, e);
				throw new ApplicationJsonException(e,
						ERROR_MESSAGE_POST_ROUTING_RULES
								+ ERROR_REASON_JSON,
						Response.Status.BAD_REQUEST.getStatusCode());
			}
			
			routingRuleEntity.setAction(jsonEntity.getString("action"));

			// fill optional data
			routingRuleEntity.setDescription(jsonEntity
					.optString("description"));
			routingRuleEntity.setMessagecondition(jsonEntity.optString("messagecondition"));
			routingRuleEntity.setFunctioncondition(jsonEntity.optString("functioncondition"));
			routingRuleEntity.setMetadatacondition(jsonEntity.optString("metadatacondition"));
			routingRuleEntity.setSequence(new BigDecimal(jsonEntity
					.getInt("sequence")));
			routingRuleEntity.setRuletype(new BigDecimal(jsonEntity
					.getInt("ruletype")));

			entityManagerConfig.getTransaction().begin();
			entityManagerConfig.persist(routingRuleEntity);
			id = entityManagerConfig.getEntityManagerFactory()
					.getPersistenceUnitUtil().getIdentifier(routingRuleEntity).toString();
			entityManagerConfig.getTransaction().commit();

		} catch (JSONException je) {
			logger.error(ERROR_MESSAGE_POST_ROUTING_RULES + ERROR_REASON_JSON,
					je);
			throw new ApplicationJsonException(je,
					ERROR_MESSAGE_POST_ROUTING_RULES + ERROR_REASON_JSON,
					Response.Status.BAD_REQUEST.getStatusCode());
		} catch (NumberFormatException nfe) {
			logger.error(ERROR_MESSAGE_POST_ROUTING_RULES
					+ ERROR_REASON_NUMBER_FORMAT, nfe);
			throw new ApplicationJsonException(nfe,
					ERROR_MESSAGE_POST_ROUTING_RULES
							+ ERROR_REASON_NUMBER_FORMAT,
					Response.Status.BAD_REQUEST.getStatusCode());
		} catch (RollbackException re) {
			ApplicationJsonException.handleSQLException(re,
					ERROR_MESSAGE_POST_ROUTING_RULES, logger);
			logger.error(ERROR_MESSAGE_POST_ROUTING_RULES
					+ ERROR_REASON_ROLLBACK, re);
			throw re;
		} finally {
			if (null != entityManagerConfig) {
				entityManagerConfig.close();
			}
		}

		routingRuleUri = getUriInfo().getAbsolutePathBuilder()
				.path(String.valueOf(routingRuleEntity.getGuid())).build();

		return JsonResponseWrapper.getResponse(id, Response.Status.CREATED,
				routingRuleUri);

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
		final JSONObject routingRulesAsJson = super.asJson();

		// fill data
		final JSONArray routingRulesArray = new JSONArray();
		final List<?> items = getItems();

		if (items.size() > 0) {
			for (RoutingRuleEntity routingRuleEntity : (List<RoutingRuleEntity>) items) {
				routingRulesArray.put(RoutingRuleResource.asJson(
						routingRuleEntity,
						UriBuilder.fromPath(getUriInfo().getPath())
								.path(String.valueOf(routingRuleEntity.getGuid())).build()
								.getPath()));
			}
		}
		routingRulesAsJson.put("routingrules", routingRulesArray);
		return routingRulesAsJson;
	}
}
