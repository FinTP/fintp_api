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
import ro.allevo.fintpws.model.RoutingKeyWordEntity;
import ro.allevo.fintpws.util.JsonResponseWrapper;

/**
 * 
 * @author Edi
 * @version $Revision: 1.0 $
 */
public class RoutingKeyWordResource {

	/**
	 * Field logger
	 */
	private static final Logger logger = LogManager
			.getLogger(RoutingKeyWordResource.class);

	/**
	 * Field ERROR_MESSAGE_GET_ROUTING_KEY_WORD. (value is ""Error returning
	 * routing key word : "")
	 */
	static final String ERROR_MESSAGE_GET_ROUTING_KEY_WORD = "Error returning routing key word : ";
	/**
	 * Field ERROR_MESSAGE_PUT_ROUTING_KEY_WORD. (value is ""Error updating
	 * routing key word : "")
	 */
	static final String ERROR_MESSAGE_PUT_ROUTING_KEY_WORD = "Error updating routing key word : ";
	/**
	 * Field ERROR_MESSAGE_ROUTING_KEY_WORD_NOT_FOUND. (value is ""Routing key
	 * word with name [%s] not found"")
	 */
	static final String ERROR_MESSAGE_ROUTING_KEY_WORD_NOT_FOUND = "Routing key word with guid [%s] not found";
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
	private RoutingKeyWordEntity routingKeyWordEntity;

	/**
	 * default constructor
	 */
	public RoutingKeyWordResource() {
	}

	/**
	 * keyword
	 */
	private String keyword;

	/**
	 * Constructor for RoutingKeyWordResource.
	 * 
	 * @param uriInfo
	 *            UriInfo
	 * @param entityManagerConfig
	 *            EntityManager
	 * @param keyword
	 *            String
	 */

	public RoutingKeyWordResource(UriInfo uriInfo,
			EntityManager entityManagerConfig, String keyword) {
		this.uriInfo = uriInfo;
		this.entityManagerConfig = entityManagerConfig;
		this.keyword = keyword;
		routingKeyWordEntity = null;

		routingKeyWordEntity = findByKeyword(entityManagerConfig, keyword);
	}

	/**
	 * 
	 * @param entityManager
	 *            EntityManager
	 * @param keyword
	 *            String
	 * @return RoutingKeyWordEntity
	 */

	public static RoutingKeyWordEntity findByKeyword(
			EntityManager entityManager, String keyword) {

		final TypedQuery<RoutingKeyWordEntity> query = entityManager
				.createNamedQuery("RoutingKeyWordEntity.findByKeyword",
						RoutingKeyWordEntity.class);

		final java.util.List<RoutingKeyWordEntity> results = query
				.setParameter("keyword", keyword).getResultList();
		if (!results.isEmpty()) {
			return results.get(0);
		}
		return null;
		
	}

	/**
	 * GET Method : returns an application/json formatted routing key word
	 * 
	 * @return JSONObject the routing key word
	 */

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public JSONObject getRoutingKeyWord() {

		if (null == routingKeyWordEntity) {
			logger.error(String.format(
					ERROR_MESSAGE_ROUTING_KEY_WORD_NOT_FOUND, keyword));
			throw new EntityNotFoundException(String.format(
					ERROR_MESSAGE_ROUTING_KEY_WORD_NOT_FOUND, keyword));
		}
		try {
			return RoutingKeyWordResource.asJson(routingKeyWordEntity,
					uriInfo.getPath());
		} catch (JSONException je) {
			logger.error(
					ERROR_MESSAGE_GET_ROUTING_KEY_WORD + ERROR_REASON_JSON, je);
			throw new ApplicationJsonException(je,
					ERROR_MESSAGE_GET_ROUTING_KEY_WORD + ERROR_REASON_JSON,
					Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
		}
	}

	/**
	 * PUT method: updates the routing key word
	 * 
	 * @param jsonEntity
	 *            JSONObject the routing key word holding new values
	 * @return Response
	 */
	@PUT
	@Consumes(MediaType.APPLICATION_JSON)
	public Response updateRoutingKeyWord(JSONObject jsonEntity) {
		if (null == routingKeyWordEntity) {
			logger.error(String.format(
					ERROR_MESSAGE_ROUTING_KEY_WORD_NOT_FOUND, keyword));
			throw new EntityNotFoundException(String.format(
					ERROR_MESSAGE_ROUTING_KEY_WORD_NOT_FOUND, keyword));
		}

		try {
			if (jsonEntity.has("keyword")) {
				routingKeyWordEntity
						.setKeyword(jsonEntity.getString("keyword"));
			}
			if (jsonEntity.has("comparer")) {

				routingKeyWordEntity.setComparer(jsonEntity
						.getString("comparer"));

			}
			if (jsonEntity.has("selector")) {
				routingKeyWordEntity.setSelector(jsonEntity
						.optString("selector"));
			}
			if (jsonEntity.has("description")) {
				routingKeyWordEntity.setDescription(jsonEntity
						.optString("description"));
			}
			if (jsonEntity.has("selectoriso")) {
				routingKeyWordEntity.setSelectoriso(jsonEntity
						.optString("selectoriso"));
			}

			entityManagerConfig.getTransaction().begin();
			entityManagerConfig.merge(routingKeyWordEntity);
			entityManagerConfig.getTransaction().commit();
		} catch (NumberFormatException nfe) {
			logger.error(ERROR_MESSAGE_PUT_ROUTING_KEY_WORD
					+ ERROR_REASON_NUMBER_FORMAT, nfe);
			throw new ApplicationJsonException(nfe,
					ERROR_MESSAGE_PUT_ROUTING_KEY_WORD + ERROR_REASON_JSON,
					Response.Status.BAD_REQUEST.getStatusCode());
		} catch (RollbackException re) {
			ApplicationJsonException.handleSQLException(re,
					ERROR_MESSAGE_PUT_ROUTING_KEY_WORD, logger);
			logger.error(ERROR_MESSAGE_PUT_ROUTING_KEY_WORD
					+ ERROR_REASON_ROLLBACK, re);
			throw re;
		} catch (JSONException je) {
			logger.error(
					ERROR_MESSAGE_PUT_ROUTING_KEY_WORD + ERROR_REASON_JSON, je);
			throw new ApplicationJsonException(je,
					ERROR_MESSAGE_PUT_ROUTING_KEY_WORD + ERROR_REASON_JSON,
					Response.Status.BAD_REQUEST.getStatusCode());
		} finally {
			if (null != entityManagerConfig) {
				entityManagerConfig.close();
			}
		}

		return JsonResponseWrapper.getResponse(Response.Status.OK,
				"routing key word updated");
	}

	/**
	 * DELETE method : deletes the routing key word
	 * 
	 * @return Response
	 */

	@DELETE
	public Response deleteRoutingKeyWord() {
		if (null == routingKeyWordEntity) {
			logger.error(String.format(
					ERROR_MESSAGE_ROUTING_KEY_WORD_NOT_FOUND, keyword));
			throw new EntityNotFoundException(String.format(
					ERROR_MESSAGE_ROUTING_KEY_WORD_NOT_FOUND, keyword));
		}
		try {
			entityManagerConfig.getTransaction().begin();
			entityManagerConfig.remove(routingKeyWordEntity);
			entityManagerConfig.getTransaction().commit();
		} finally {
			if (null != entityManagerConfig) {
				entityManagerConfig.close();
			}
		}

		return JsonResponseWrapper.getResponse(Response.Status.OK,
				"routing key word deleted");
	}

	/**
	 * Returns the resource formatted as json
	 * 
	 * @param routingKeyWordEntity
	 *            RoutingKeyWordEntity
	 * @param path
	 *            String
	 * @return JSONObject
	 * @throws JSONException
	 */

	public static JSONObject asJson(RoutingKeyWordEntity routingKeyWordEntity,
			String path) throws JSONException {
		final JSONObject routingKeyWordAsJson = ApiResource.getMetaResource(
				path, RoutingKeyWordResource.class);

		routingKeyWordAsJson.put("keyword", routingKeyWordEntity.getKeyword())
				.put("comparer", routingKeyWordEntity.getComparer())
				.put("selector", routingKeyWordEntity.getSelector())
				.put("description", routingKeyWordEntity.getDescription())
				.put("selectoriso", routingKeyWordEntity.getSelectoriso());

		return routingKeyWordAsJson;
	}

	/**
	 * toString method
	 * 
	 * @return res
	 */
	public String toString() {
		return String.valueOf(routingKeyWordEntity.getGuid());
	}
}
