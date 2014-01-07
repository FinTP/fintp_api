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

public class RoutingKeyWordsResource extends PagedCollection {

	/**
	 * Field logger
	 */
	private static final Logger logger = LogManager
			.getLogger(RoutingKeyWordResource.class.getName());

	/**
	 * Field ERROR_MESSAGE_GET_ROUTING_KEY_WORDS. (value is ""Error returning
	 * routing key words: "")
	 */
	static final String ERROR_MESSAGE_GET_ROUTING_KEY_WORDS = "Error returning get routing key words : ";
	/**
	 * Field ERROR_MESSAGE_POST_ROUTING_KEY_WORDS. (value is ""Error creating
	 * routing key words : "")
	 */
	static final String ERROR_MESSAGE_POST_ROUTING_KEY_WORDS = "Error creating routing key words : ";

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

	
	public RoutingKeyWordsResource(UriInfo uriInfo,
			EntityManager entityManagerConfig,
			RoutingKeyWordMapEntity routingKeyWordMapEntity) {
		super(uriInfo, entityManagerConfig.createNamedQuery(
				"RoutingKeyWordEntity.findAll", RoutingKeyWordEntity.class),
				entityManagerConfig.createNamedQuery(
						"RoutingKeyWordEntity.findTotal", Long.class));
		if (null != routingKeyWordMapEntity) {
			this.setItemsQuery(entityManagerConfig.createNamedQuery(
					"RoutingKeyWordEntity.findAllRoutingKeyWordMaps",
					RoutingKeyWordMapEntity.class)
					.setParameter(
							"keywordid",
							routingKeyWordMapEntity.getRoutingKeywordEntity()
									.getGuid()));
			this.setTotalQuery(entityManagerConfig.createNamedQuery(
					"RoutingKeyWordEntity.findTotalRoutingKeyWordMaps",
					Long.class)
					.setParameter(
							"keywordid",
							routingKeyWordMapEntity.getRoutingKeywordEntity()
									.getGuid()));
		}

		this.entityManagerConfig = entityManagerConfig;

	}

	/**
	 * Returns a routing key word sub-resource with guid
	 * 
	 * @param guid
	 *            String guid of the routing key words as last element in the path
	 * 
	 * @return RoutingKeyWordResource The routing key word sub-resource
	 */
	@Path("{keyword}")
	public RoutingKeyWordResource getRoutingKeyWordResource(
			@PathParam("keyword") String keyword) {
		return new RoutingKeyWordResource(getUriInfo(), entityManagerConfig,
				keyword);
	}

	/**
	 * GET method : returns an application/json formatted list of routing key words
	 * 
	 * @return JSONObject The list of routing key words
	 */
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public JSONObject getRoutingKeyWordsAsJson() {
		try {
			getPage();
			return asJson();
		} catch (JSONException je) {
			logger.error(ERROR_MESSAGE_GET_ROUTING_KEY_WORDS
					+ ERROR_REASON_JSON, je);
			throw new ApplicationJsonException(je,
					ERROR_MESSAGE_GET_ROUTING_KEY_WORDS + ERROR_REASON_JSON,
					Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
		}
	}

	/**
	 * POST method : creates a routing key word
	 * 
	 * @param jsonEntity
	 *            JSONObject
	 * @return Response
	 */
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	public Response postForm(JSONObject jsonEntity) {
		String id;
		final RoutingKeyWordEntity routingKeyWordEntity = new RoutingKeyWordEntity();
		URI routingKeyWordUri = null;
		try {
			routingKeyWordEntity.setKeyword(jsonEntity.getString("keyword"));
			routingKeyWordEntity.setComparer(jsonEntity.getString("comparer"));
			routingKeyWordEntity.setSelector(jsonEntity.optString("selector"));
			routingKeyWordEntity.setDescription(jsonEntity
					.optString("description"));
			routingKeyWordEntity.setSelectoriso(jsonEntity
					.optString("selectoriso"));

			entityManagerConfig.getTransaction().begin();
			entityManagerConfig.persist(routingKeyWordEntity);
			id = entityManagerConfig.getEntityManagerFactory()
					.getPersistenceUnitUtil()
					.getIdentifier(routingKeyWordEntity).toString();
			entityManagerConfig.getTransaction().commit();
		} catch (JSONException je) {
			logger.error(ERROR_MESSAGE_POST_ROUTING_KEY_WORDS
					+ ERROR_REASON_JSON, je);
			throw new ApplicationJsonException(je,
					ERROR_MESSAGE_POST_ROUTING_KEY_WORDS + ERROR_REASON_JSON,
					Response.Status.BAD_REQUEST.getStatusCode());
		} catch (NumberFormatException nfe) {
			logger.error(ERROR_MESSAGE_POST_ROUTING_KEY_WORDS
					+ ERROR_REASON_NUMBER_FORMAT, nfe);
			throw new ApplicationJsonException(nfe,
					ERROR_MESSAGE_POST_ROUTING_KEY_WORDS
							+ ERROR_REASON_NUMBER_FORMAT,
					Response.Status.BAD_REQUEST.getStatusCode());
		} catch (RollbackException re) {
			ApplicationJsonException.handleSQLException(re,
					ERROR_MESSAGE_POST_ROUTING_KEY_WORDS, logger);
			logger.error(ERROR_MESSAGE_POST_ROUTING_KEY_WORDS
					+ ERROR_REASON_ROLLBACK, re);
			throw re;
		} finally {
			if (null != entityManagerConfig) {
				entityManagerConfig.close();
			}
		}
		routingKeyWordUri = getUriInfo().getAbsolutePathBuilder()
				.path(routingKeyWordEntity.toString()).build();

		return JsonResponseWrapper.getResponse(id, Response.Status.CREATED,
				routingKeyWordUri);
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
		final JSONObject routingKeyWordssAsJson = super.asJson();

		// fill data
		final JSONArray routingKeyWordsArray = new JSONArray();
		final List<?> items = getItems();

		if (items.size() > 0) {
			for (RoutingKeyWordEntity routingKeyWordEntity : (List<RoutingKeyWordEntity>) items) {
				routingKeyWordsArray.put(RoutingKeyWordResource.asJson(
						routingKeyWordEntity,
						UriBuilder.fromPath(getUriInfo().getPath())
								.path(routingKeyWordEntity.toString()).build()
								.getPath()));
			}
		}
		routingKeyWordssAsJson.put("routingkeywords", routingKeyWordsArray);
		return routingKeyWordssAsJson;
	}
}
