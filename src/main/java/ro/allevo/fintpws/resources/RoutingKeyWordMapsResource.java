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
import ro.allevo.fintpws.model.RoutingKeyWordEntity;
import ro.allevo.fintpws.model.RoutingKeyWordMapEntity;
import ro.allevo.fintpws.util.JsonResponseWrapper;

/**
 * 
 * @author Edi
 * @version $Revision: 1.0 $
 * 
 */
public class RoutingKeyWordMapsResource extends PagedCollection {

	private static final Logger logger = LogManager
			.getLogger(RoutingKeyWordMapsResource.class.getName());

	/**
	 * Field ERROR_MESSAGE_GET_ROUTING_KEY_WORD_MAPS. (value is ""Error
	 * returning routing key word maps: "")
	 */
	static final String ERROR_MESSAGE_GET_ROUTING_KEY_WORD_MAPS = "Error returning routing key word maps : ";
	/**
	 * Field ERROR_MESSAGE_POST_ROUTING_KEY_WORD_MAPS. (value is ""Error
	 * creating routing key word maps : "")
	 */
	static final String ERROR_MESSAGE_POST_ROUTING_KEY_WORD_MAPS = "Error creating routing key word maps : ";

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

	public RoutingKeyWordMapsResource(UriInfo uriInfo,
			EntityManager entityManagerConfig) {

		super(uriInfo, entityManagerConfig.createNamedQuery(
				"RoutingKeyWordMapEntity.findAll",
				RoutingKeyWordMapEntity.class), entityManagerConfig
				.createNamedQuery("RoutingKeyWordMapEntity.findTotal",
						BigDecimal.class));

		this.entityManagerConfig = entityManagerConfig;
	}

	/**
	 * Returns a routing key word map sub-resource with mapid
	 * 
	 * @param mapid
	 *            String mapid of the routing key word map as last element
	 *            in the path
	 * @return RoutingKeyWordMapResource The routing key word map sub-resource
	 */
	@Path("{id}")
	public RoutingKeyWordMapResource getRoutingKeyWordMapResource(
			@PathParam("id") String mapid) {
		return new RoutingKeyWordMapResource(getUriInfo(), entityManagerConfig,
				mapid);
	}

	/**
	 * GET method : returns an application/json formatted list of routing key
	 * word maps
	 * 
	 * @return JSONObject The list of routing key word maps
	 */
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public JSONObject getRoutingKeyWordMapsAsJson() {
		try {
			getPage();
			return asJson();
		} catch (JSONException je) {
			logger.error(ERROR_MESSAGE_GET_ROUTING_KEY_WORD_MAPS
					+ ERROR_REASON_JSON, je);
			throw new ApplicationJsonException(
					je,
					ERROR_MESSAGE_GET_ROUTING_KEY_WORD_MAPS + ERROR_REASON_JSON,
					Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
		}
	}

	/**
	 * POST method : creates a routing key word map
	 * 
	 * @param jsonEntity
	 *            JSONObject
	 * @return Response
	 */

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	public Response postForm(JSONObject jsonEntity) {
		String id = new String();
		final RoutingKeyWordMapEntity routingKeyWordMapEntity = new RoutingKeyWordMapEntity();
		URI routingKeyWordMapUri = null;
		try {
			if (jsonEntity.has("mapid")) {
				routingKeyWordMapEntity.setMapid(new Long(jsonEntity
						.getInt("mapid")));
			}

			routingKeyWordMapEntity.setTag(jsonEntity.getString("tag"));
			
			try{
				RoutingKeyWordEntity routingKeyword = RoutingKeyWordResource
					.findByKeyword(entityManagerConfig,
							jsonEntity.getString("keyword"));
				routingKeyWordMapEntity.setRoutingKeywordEntity(routingKeyword);
			}catch (NullPointerException e) {
				logger.error(ERROR_MESSAGE_POST_ROUTING_KEY_WORD_MAPS+ ERROR_REASON_JSON, e);
				throw new ApplicationJsonException(e,
						ERROR_MESSAGE_POST_ROUTING_KEY_WORD_MAPS + ERROR_REASON_JSON,
						Response.Status.BAD_REQUEST.getStatusCode());
			}
			routingKeyWordMapEntity.setMt(jsonEntity.getString("mt"));
			routingKeyWordMapEntity.setSelector(jsonEntity
					.getString("selector"));
			entityManagerConfig.getTransaction().begin();
			entityManagerConfig.persist(routingKeyWordMapEntity);
			id = entityManagerConfig.getEntityManagerFactory()
					.getPersistenceUnitUtil()
					.getIdentifier(routingKeyWordMapEntity).toString();
			entityManagerConfig.getTransaction().commit();
		} catch (JSONException je) {
			logger.error(ERROR_MESSAGE_POST_ROUTING_KEY_WORD_MAPS
					+ ERROR_REASON_JSON, je);
			throw new ApplicationJsonException(je,
					ERROR_MESSAGE_POST_ROUTING_KEY_WORD_MAPS
							+ ERROR_REASON_JSON,
					Response.Status.BAD_REQUEST.getStatusCode());
		} catch (NumberFormatException nfe) {
			logger.error(ERROR_MESSAGE_POST_ROUTING_KEY_WORD_MAPS
					+ ERROR_REASON_NUMBER_FORMAT, nfe);
			throw new ApplicationJsonException(nfe,
					ERROR_MESSAGE_POST_ROUTING_KEY_WORD_MAPS
							+ ERROR_REASON_NUMBER_FORMAT,
					Response.Status.BAD_REQUEST.getStatusCode());
		} catch (RollbackException re) {
			ApplicationJsonException.handleSQLException(re,
					ERROR_MESSAGE_POST_ROUTING_KEY_WORD_MAPS, logger);
			logger.error(ERROR_MESSAGE_POST_ROUTING_KEY_WORD_MAPS
					+ ERROR_REASON_ROLLBACK, re);
			throw re;
		} finally {
			if (null != entityManagerConfig) {
				entityManagerConfig.close();
			}
		}
		routingKeyWordMapUri = getUriInfo().getAbsolutePathBuilder()
				.path(routingKeyWordMapEntity.toString()).build();

		return JsonResponseWrapper.getResponse(id, Response.Status.CREATED,
				routingKeyWordMapUri);
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
		final JSONObject routingKeyWordMapAsJson = super.asJson();

		// fill data
		final JSONArray routingKeyWordMapsArray = new JSONArray();
		final List<?> items = getItems();

		if (items.size() > 0) {
			for (RoutingKeyWordMapEntity routingKeyWordMapEntity : (List<RoutingKeyWordMapEntity>) items) {
				routingKeyWordMapsArray.put(RoutingKeyWordMapResource.asJson(
						routingKeyWordMapEntity,
						UriBuilder.fromPath(getUriInfo().getPath())
								.path(routingKeyWordMapEntity.toString())
								.build().getPath()));
			}
		}
		routingKeyWordMapAsJson.put("routingkeywordmaps",
				routingKeyWordMapsArray);
		return routingKeyWordMapAsJson;
	}

}
