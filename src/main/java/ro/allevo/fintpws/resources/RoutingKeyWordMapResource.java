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

import org.apache.logging.log4j.*;

import org.apache.logging.log4j.Logger;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import ro.allevo.fintpws.exceptions.ApplicationJsonException;
import ro.allevo.fintpws.model.RoutingKeyWordEntity;
import ro.allevo.fintpws.model.RoutingKeyWordMapEntity;

import ro.allevo.fintpws.util.JsonResponseWrapper;

public class RoutingKeyWordMapResource {

	/**
	 * Field logger
	 */

	private static final Logger logger = LogManager
			.getLogger(RoutingKeyWordMapResource.class);

	/**
	 * Field ERROR_MESSAGE_GET_ROUTING_KEY_WORD_MAP. (value is ""Error returning routing
	 * key word map: "")
	 */
	static final String ERROR_MESSAGE_GET_ROUTING_KEY_WORD_MAP = "Error returning routing key word map : ";
	/**
	 * Field ERROR_MESSAGE_PUT_ROUTING_KEY_WORD_MAP. (value is ""Error updating routing
	 * key word map : "")
	 */
	static final String ERROR_MESSAGE_PUT_ROUTING_KEY_WORD_MAP = "Error updating routing key word map : ";
	/**
	 * Field ERROR_MESSAGE_ROUTING_KEY_WORD_MAP_NOT_FOUND. (value is ""Routing key word
	 * map with id [%s] not found"")
	 */
	static final String ERROR_MESSAGE_ROUTING_KEY_WORD_MAP_NOT_FOUND = "Routing key word map with mapid [%s] not found";
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
	
	//TODO: REASON FOR FK NOT FOUND

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
	private RoutingKeyWordMapEntity routingKeyWordMapEntity;

	/**
	 * default constructor
	 */
	public RoutingKeyWordMapResource() {
	}

	/**
	 * mapid
	 */
	private String mapid;

	/**
	 * Constructor for RoutingKeyWordMapResource.
	 * 
	 * @param uriInfo
	 *            UriInfo
	 * @param entityManagerConfig
	 *            EntityManager
	 * @param mapid
	 *            String
	 */

	public RoutingKeyWordMapResource(UriInfo uriInfo,
			EntityManager entityManagerConfig, String mapid) {
		this.uriInfo = uriInfo;
		this.entityManagerConfig = entityManagerConfig;
		this.mapid = mapid;
		routingKeyWordMapEntity = null;

		routingKeyWordMapEntity = findByMapId(entityManagerConfig, mapid);
	}

	/**
	 * 
	 * @param entityManeger
	 *            EntityManeger
	 * @param mapid
	 *            String
	 * @return RoutingKeyWordMapEntity
	 */

	public static RoutingKeyWordMapEntity findByMapId(
			EntityManager entityManeger, String mapid) {

		final TypedQuery<RoutingKeyWordMapEntity> query = entityManeger
				.createNamedQuery("RoutingKeyWordMapEntity.findByMapId",
						RoutingKeyWordMapEntity.class);

		try{
			final java.util.List<RoutingKeyWordMapEntity> results = query
					.setParameter("mapid", new Long(mapid).longValue()).getResultList();
			if (!results.isEmpty()) {
				return results.get(0);
			}
			return null;
		}catch(NumberFormatException nfe){
			logger.error(String.format(ERROR_MESSAGE_ROUTING_KEY_WORD_MAP_NOT_FOUND, mapid));
			throw new EntityNotFoundException(String.format(
					ERROR_MESSAGE_ROUTING_KEY_WORD_MAP_NOT_FOUND, mapid));
		}
	}
	
	/**
	 * Sub-resource locator for /routingrules resource
	 * @return routingrules
	 */
	@Path("routingkeywords")
	public RoutingKeyWordsResource getRoutingKeyWords() {
		if (null == this.routingKeyWordMapEntity) {
			logger.error(String.format(ERROR_MESSAGE_ROUTING_KEY_WORD_MAP_NOT_FOUND,
					this.mapid));
			throw new EntityNotFoundException(String.format(
					ERROR_MESSAGE_ROUTING_KEY_WORD_MAP_NOT_FOUND, this.mapid));
		}
		return new RoutingKeyWordsResource(uriInfo, entityManagerConfig, routingKeyWordMapEntity);
	}

	/**
	 * GET Method : returns an application/json formatted routing key word map
	 * 
	 * @return JSONObject the routing key word map
	 */
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public JSONObject getRoutingKeyWordMap() {

		if (null == routingKeyWordMapEntity) {
			logger.error(String.format(
					ERROR_MESSAGE_ROUTING_KEY_WORD_MAP_NOT_FOUND, mapid));
			throw new EntityNotFoundException(String.format(
					ERROR_MESSAGE_ROUTING_KEY_WORD_MAP_NOT_FOUND, mapid));
		}
		try {
			return RoutingKeyWordMapResource.asJson(routingKeyWordMapEntity,
					uriInfo.getPath());
		} catch (JSONException je) {
			logger.error(ERROR_MESSAGE_GET_ROUTING_KEY_WORD_MAP
					+ ERROR_REASON_JSON, je);
			throw new ApplicationJsonException(je,
					ERROR_MESSAGE_GET_ROUTING_KEY_WORD_MAP + ERROR_REASON_JSON,
					Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
		}
	}

	/**
	 * Returns the resource formatted as json
	 * 
	 * @param routingKeyWordMapEntity
	 *            RoutingKeyWordMapEntity
	 * @param path
	 *            String
	 * @return JSONObject
	 * @throws JSONException
	 */
	public static JSONObject asJson(
			RoutingKeyWordMapEntity routingKeyWordMapEntity, String path)
			throws JSONException {
		final JSONObject routingKeyWordMapAsJson = ApiResource.getMetaResource(
				path, RoutingKeyWordMapResource.class);

		routingKeyWordMapAsJson
				.put("mapid", routingKeyWordMapEntity.getMapid())
				.put("keyword", routingKeyWordMapEntity.getRoutingKeywordEntity().getKeyword())
				.put("tag", routingKeyWordMapEntity.getTag())
				.put("mt", routingKeyWordMapEntity.getMt())
				.put("selector", routingKeyWordMapEntity.getSelector());

		return routingKeyWordMapAsJson;
	}

	/**
	 * PUT method: updates the routing key word map
	 * 
	 * @param jsonEntity
	 *            JSONObject the routing key word map holding new values
	 * @return Response
	 */

	@PUT
	@Consumes(MediaType.APPLICATION_JSON)
	public Response updateRoutingKeyWordMap(JSONObject jsonEntity) {
		if (null == routingKeyWordMapEntity) {
			logger.error(String.format(
					ERROR_MESSAGE_ROUTING_KEY_WORD_MAP_NOT_FOUND, mapid));
			throw new EntityNotFoundException(String.format(
					ERROR_MESSAGE_ROUTING_KEY_WORD_MAP_NOT_FOUND, mapid));
		}

		try {
			if (jsonEntity.has("keyword")) {
				try {

					RoutingKeyWordEntity routingKeyword = RoutingKeyWordResource
							.findByKeyword(entityManagerConfig,
									jsonEntity.getString("keyword"));
					routingKeyWordMapEntity
							.setRoutingKeywordEntity(routingKeyword);
				} catch (NullPointerException e) {
					logger.error(ERROR_MESSAGE_PUT_ROUTING_KEY_WORD_MAP
							+ ERROR_REASON_JSON, e);
					throw new ApplicationJsonException(e,
							ERROR_MESSAGE_PUT_ROUTING_KEY_WORD_MAP
									+ ERROR_REASON_JSON,
							Response.Status.BAD_REQUEST.getStatusCode());
				}
			}
			if (jsonEntity.has("tag")) {
				routingKeyWordMapEntity.setTag(jsonEntity.getString("tag"));
			}
			if (jsonEntity.has("mt")) {
				routingKeyWordMapEntity.setMt(jsonEntity.getString("mt"));
			}
			if (jsonEntity.has("selector")) {
				routingKeyWordMapEntity.setSelector(jsonEntity
						.optString("selector"));
			}

			entityManagerConfig.getTransaction().begin();
			entityManagerConfig.merge(routingKeyWordMapEntity);
			entityManagerConfig.getTransaction().commit();
		}
		catch (NumberFormatException nfe) {
			logger.error(ERROR_MESSAGE_PUT_ROUTING_KEY_WORD_MAP
					+ ERROR_REASON_NUMBER_FORMAT, nfe);
			throw new ApplicationJsonException(nfe,
					ERROR_MESSAGE_PUT_ROUTING_KEY_WORD_MAP + ERROR_REASON_JSON,
					Response.Status.BAD_REQUEST.getStatusCode());
		} catch (RollbackException re) {
			ApplicationJsonException.handleSQLException(re,
					ERROR_MESSAGE_PUT_ROUTING_KEY_WORD_MAP, logger);
			logger.error(ERROR_MESSAGE_PUT_ROUTING_KEY_WORD_MAP
					+ ERROR_REASON_ROLLBACK, re);
			throw re;
		} catch (JSONException je) {
			logger.error(ERROR_MESSAGE_PUT_ROUTING_KEY_WORD_MAP
					+ ERROR_REASON_JSON, je);
			throw new ApplicationJsonException(je,
					ERROR_MESSAGE_PUT_ROUTING_KEY_WORD_MAP + ERROR_REASON_JSON,
					Response.Status.BAD_REQUEST.getStatusCode());
		} finally {
			if (null != entityManagerConfig) {
				entityManagerConfig.close();
			}
		}

		return JsonResponseWrapper.getResponse(Response.Status.OK,
				"routing key word map updated");
	}

	/**
	 * DELETE method : deletes the routing key word map
	 * 
	 * @return Response
	 */
	@DELETE
	public Response deleteRoutingKeyWordMap() {
		if (null == routingKeyWordMapEntity) {
			logger.error(String.format(
					ERROR_MESSAGE_ROUTING_KEY_WORD_MAP_NOT_FOUND, mapid));
			throw new EntityNotFoundException(String.format(
					ERROR_MESSAGE_ROUTING_KEY_WORD_MAP_NOT_FOUND, mapid));
		}
		try {
			entityManagerConfig.getTransaction().begin();
			entityManagerConfig.remove(routingKeyWordMapEntity);
			entityManagerConfig.getTransaction().commit();
		} finally {
			if (null != entityManagerConfig) {
				entityManagerConfig.close();
			}
		}

		return JsonResponseWrapper.getResponse(Response.Status.OK,
				"routing key word map deleted");
	}
}
