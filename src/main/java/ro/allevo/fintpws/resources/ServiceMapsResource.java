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
import ro.allevo.fintpws.model.QueueEntity;
import ro.allevo.fintpws.model.ServiceMapEntity;
import ro.allevo.fintpws.util.JsonResponseWrapper;
import ro.allevo.fintpws.util.ResourcesUtils;

public class ServiceMapsResource extends PagedCollection {

	/**
	 * Field logger
	 */
	private static final Logger logger = LogManager
			.getLogger(ServiceMapsResource.class.getName());

	/**
	 * Field ERROR_MESSAGE_GET_SERVICE_MAPS. (value is ""Error returning service
	 * maps: "")
	 */
	static final String ERROR_MESSAGE_GET_SERVICE_MAPS = "Error returning service maps : ";
	/**
	 * Field ERROR_MESSAGE_POST_SERVICE_MAPS. (value is ""Error creating service
	 * maps : "")
	 */
	static final String ERROR_MESSAGE_POST_SERVICE_MAPS = "Error creating service maps : ";

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
	 *            EntityManager
	 * @param queueEntity
	 *            QueueEntity
	 */
	public ServiceMapsResource(UriInfo uriInfo,
			EntityManager entityManagerConfig, QueueEntity queueEntity) {
		super(uriInfo, entityManagerConfig.createNamedQuery(
				"ServiceMapEntity.findAll", ServiceMapEntity.class),
				entityManagerConfig.createNamedQuery(
						"ServiceMapEntity.findTotal", Long.class));
		if (null != queueEntity) {
			this.setItemsQuery(entityManagerConfig
					.createNamedQuery("ServiceMapEntity.findAllQueue",
							ServiceMapEntity.class)
					.setParameter("serviceid", queueEntity.getConnector()));
			this.setTotalQuery(entityManagerConfig.createNamedQuery(
					"ServiceMapEntity.findTotalQueue", Long.class)
					.setParameter("serviceid", queueEntity.getConnector()));
		}
		this.entityManagerConfig = entityManagerConfig;
	}

	@Path("{friendlyname}")
	public ServiceMapResource getServiceMapsResource(
			@PathParam("friendlyname") String friendlyName) {
		return new ServiceMapResource(getUriInfo(), entityManagerConfig,
				friendlyName);
	}

	/**
	 * GET method : returns an application/json formatted list of service maps
	 * 
	 * @return JSONObject The list of service maps
	 */
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public JSONObject getServiceMapsAsJson() {
		try {
			getPage();
			return asJson();
		} catch (JSONException je) {
			logger.error(ERROR_MESSAGE_GET_SERVICE_MAPS + ERROR_REASON_JSON, je);
			throw new ApplicationJsonException(je,
					ERROR_MESSAGE_GET_SERVICE_MAPS + ERROR_REASON_JSON,
					Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
		}
	}

	/**
	 * POST method : creates a service map
	 * 
	 * @param jsonEntity
	 *            JSONObject
	 * @return Response
	 */
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	public Response postForm(JSONObject jsonEntity) {
		String id;
		final ServiceMapEntity serviceMapEntity = new ServiceMapEntity();
		URI serviceMapUri = null;
		try {
			// fill required data
						
						serviceMapEntity.setFriendlyname(jsonEntity
								.getString("friendlyname"));
						serviceMapEntity.setStatus(new BigDecimal(jsonEntity
								.getString("status")));
						serviceMapEntity.setHeartbeatinterval(new BigDecimal(jsonEntity
								.getString("heartbeatinterval")));

						// fill optional data
						serviceMapEntity.setLastsessionid(new BigDecimal(jsonEntity
								.optInt("lastsessionid")));
						serviceMapEntity.setLastheartbeat(ResourcesUtils
								.getTimestamp(jsonEntity.optString("lastheartbeat")));
						serviceMapEntity.setVersion(jsonEntity.optString("version"));
						serviceMapEntity.setPartner(jsonEntity.optString("partner"));
						serviceMapEntity.setServicetype(new BigDecimal(jsonEntity
								.optInt("servicetype")));
						serviceMapEntity.setIoidentifier(new BigDecimal(jsonEntity
								.optInt("ioidentifier")));
						serviceMapEntity.setExitpoint(jsonEntity.optString("exitpoint"));
						serviceMapEntity.setSessionid(jsonEntity.optString("sessionid"));
						serviceMapEntity.setDuplicatecheck(new BigDecimal(jsonEntity
								.optInt("duplicatecheck")));
						serviceMapEntity.setDuplicateq(jsonEntity.optString("duplicateq"));
						serviceMapEntity.setDuplicatemap(jsonEntity
								.optString("duplicatemap"));
						serviceMapEntity.setDelayednotifq(jsonEntity
								.optString("delayednotifq"));
						serviceMapEntity.setDuplicatenotifq(jsonEntity
								.optString("duplicatenotifq"));

						//perform update 
						entityManagerConfig.getTransaction().begin();
						entityManagerConfig.persist(serviceMapEntity);
						id = entityManagerConfig.getEntityManagerFactory()
								.getPersistenceUnitUtil().getIdentifier(serviceMapEntity)
								.toString();
						entityManagerConfig.getTransaction().commit();
		} catch (JSONException je) {
			logger.error(ERROR_MESSAGE_POST_SERVICE_MAPS + ERROR_REASON_JSON, je);
			throw new ApplicationJsonException(je,
					ERROR_MESSAGE_POST_SERVICE_MAPS + ERROR_REASON_JSON,
					Response.Status.BAD_REQUEST.getStatusCode());
		} catch (NumberFormatException nfe) {
			logger.error(ERROR_MESSAGE_POST_SERVICE_MAPS
					+ ERROR_REASON_NUMBER_FORMAT, nfe);
			throw new ApplicationJsonException(
					nfe,
					ERROR_MESSAGE_POST_SERVICE_MAPS + ERROR_REASON_NUMBER_FORMAT,
					Response.Status.BAD_REQUEST.getStatusCode());
		} catch (RollbackException re) {
			ApplicationJsonException.handleSQLException(re,
					ERROR_MESSAGE_POST_SERVICE_MAPS, logger);
			logger.error(
					ERROR_MESSAGE_POST_SERVICE_MAPS + ERROR_REASON_ROLLBACK, re);
			throw re;
		} catch (ParseException pe) {
			logger.error(ERROR_MESSAGE_POST_SERVICE_MAPS + ERROR_REASON_PARSE,
					pe);
			throw new ApplicationJsonException(pe,
					ERROR_MESSAGE_POST_SERVICE_MAPS + ERROR_REASON_PARSE,
					Response.Status.BAD_REQUEST.getStatusCode());
		} finally {
			if (null != entityManagerConfig) {
				entityManagerConfig.close();
			}
		}
		serviceMapUri = getUriInfo().getAbsolutePathBuilder()
				.path(serviceMapEntity.toString()).build();

		return JsonResponseWrapper.getResponse(id, Response.Status.CREATED,
				serviceMapUri);
	}
	
	/**
	 * Returns the resource formatted as json
	 * 
	 * @return JSONObject * @throws JSONException
	 */
	@SuppressWarnings("unchecked")
	public JSONObject asJson() throws JSONException {
		JSONObject serviceMapAsJson = super.asJson();

		// fill data
		JSONArray serviceMapsArray = new JSONArray();
		List<?> items = getItems();

		if (items.size() > 0) {
			for (ServiceMapEntity serviceMapEntity : (List<ServiceMapEntity>) items) {
				serviceMapsArray
						.put(ServiceMapResource.asJson(serviceMapEntity,
								UriBuilder.fromPath(getUriInfo().getPath())
										.path(serviceMapEntity.toString()).build()
										.getPath()));
			}
		}
		serviceMapAsJson.put("servicemaps", serviceMapsArray);
		return serviceMapAsJson;
	}
}
