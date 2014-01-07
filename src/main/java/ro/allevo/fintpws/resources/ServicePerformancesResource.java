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
import ro.allevo.fintpws.model.ServiceMapEntity;
import ro.allevo.fintpws.model.ServicePerformanceEntity;
import ro.allevo.fintpws.util.JsonResponseWrapper;
import ro.allevo.fintpws.util.ResourcesUtils;

public class ServicePerformancesResource extends PagedCollection {

	/**
	 * Field logger
	 */
	private static final Logger logger = LogManager
			.getLogger(ServicePerformancesResource.class.getName());

	/**
	 * Field ERROR_MESSAGE_GET_SERVICE_PERFORMANCES. (value is ""Error returning
	 * service performances: "")
	 */
	static final String ERROR_MESSAGE_GET_SERVICE_PERFORMANCES = "Error returning service performances : ";
	/**
	 * Field ERROR_MESSAGE_POST_SERVICE_PERFORMANCES. (value is ""Error creating
	 * service performances : "")
	 */
	static final String ERROR_MESSAGE_POST_SERVICE_PERFORMANCES = "Error creating service performances : ";

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
	private final EntityManager entityManagerData;

	/**
	 * 
	 * @param uriInfo
	 *            UriInfo
	 * @param entityManagerConfig
	 *            EntityManager
	 * @param queueEntity
	 *            QueueEntity
	 */
	public ServicePerformancesResource(UriInfo uriInfo,
			EntityManager entityManagerData, ServiceMapEntity serviceMapEntity) {
		super(uriInfo, entityManagerData.createNamedQuery(
				"ServicePerformanceEntity.findAll",
				ServicePerformanceEntity.class), entityManagerData
				.createNamedQuery("ServicePerformanceEntity.findTotal",
						Long.class));
		this.entityManagerData = entityManagerData;
	}

	@Path("{id}")
	public ServicePerformanceResource getServicePerformancesResource(
			@PathParam("id") String serviceId) {
		return new ServicePerformanceResource(getUriInfo(), entityManagerData,
				serviceId);
	}

	/**
	 * GET method : returns an application/json formatted list of service
	 * performances
	 * 
	 * @return JSONObject The list of service performances
	 */
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public JSONObject getServiceMapsAsJson() {
		try {
			getPage();
			return asJson();
		} catch (JSONException je) {
			logger.error(ERROR_MESSAGE_GET_SERVICE_PERFORMANCES
					+ ERROR_REASON_JSON, je);
			throw new ApplicationJsonException(je,
					ERROR_MESSAGE_GET_SERVICE_PERFORMANCES + ERROR_REASON_JSON,
					Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
		}
	}

	/**
	 * POST method : creates a service performance
	 * 
	 * @param jsonEntity
	 *            JSONObject
	 * @return Response
	 */
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	public Response postForm(JSONObject jsonEntity) {
		String id;
		final ServicePerformanceEntity servicePerformanceEntity = new ServicePerformanceEntity();
		URI servicePerformanceUri = null;
		try {
			// fill required data
			if (jsonEntity.has("serviceid")) {
				servicePerformanceEntity.setServiceid(Long.parseLong(jsonEntity
						.getString("serviceid")));
			}

			servicePerformanceEntity.setInsertdate(ResourcesUtils
					.getTimestamp(jsonEntity.getString("insertdate")));
			servicePerformanceEntity.setMintransactiontime(new BigDecimal(
					jsonEntity.getString("mintransactiontime")));
			servicePerformanceEntity.setMaxtransactiontime(new BigDecimal(
					jsonEntity.getString("maxtransactiontime")));
			servicePerformanceEntity.setMeantransactiontime(new BigDecimal(
					jsonEntity.getString("meantransactiontime")));
			servicePerformanceEntity.setSequenceno(new BigDecimal(jsonEntity
					.getString("sequenceno")));
			servicePerformanceEntity.setIoidentifier(new BigDecimal(jsonEntity
					.getString("ioidentifier")));
			servicePerformanceEntity.setSessionid(new BigDecimal(jsonEntity
					.getString("sessionid")));

			// optional data

			servicePerformanceEntity.setCommitedtrns(new BigDecimal(jsonEntity
					.optInt("commitedtrns")));
			servicePerformanceEntity.setAbortedtrns(new BigDecimal(jsonEntity
					.optInt("abortedtrns")));

			// perform update
			entityManagerData.getTransaction().begin();
			entityManagerData.persist(servicePerformanceEntity);
			id = entityManagerData.getEntityManagerFactory()
					.getPersistenceUnitUtil().getIdentifier(servicePerformanceEntity)
					.toString();
			entityManagerData.getTransaction().commit();
			
		} catch (JSONException je) {
			logger.error(ERROR_MESSAGE_POST_SERVICE_PERFORMANCES + ERROR_REASON_JSON,
					je);
			throw new ApplicationJsonException(je,
					ERROR_MESSAGE_POST_SERVICE_PERFORMANCES + ERROR_REASON_JSON,
					Response.Status.BAD_REQUEST.getStatusCode());
		} catch (NumberFormatException nfe) {
			logger.error(ERROR_MESSAGE_POST_SERVICE_PERFORMANCES
					+ ERROR_REASON_NUMBER_FORMAT, nfe);
			throw new ApplicationJsonException(nfe,
					ERROR_MESSAGE_POST_SERVICE_PERFORMANCES
							+ ERROR_REASON_NUMBER_FORMAT,
					Response.Status.BAD_REQUEST.getStatusCode());
		} catch (RollbackException re) {
			ApplicationJsonException.handleSQLException(re,
					ERROR_MESSAGE_POST_SERVICE_PERFORMANCES, logger);
			logger.error(ERROR_MESSAGE_POST_SERVICE_PERFORMANCES
					+ ERROR_REASON_ROLLBACK, re);
			throw re;
		} catch (ParseException pe) {
			logger.error(ERROR_MESSAGE_POST_SERVICE_PERFORMANCES + ERROR_REASON_PARSE,
					pe);
			throw new ApplicationJsonException(pe,
					ERROR_MESSAGE_POST_SERVICE_PERFORMANCES + ERROR_REASON_PARSE,
					Response.Status.BAD_REQUEST.getStatusCode());
		} finally {
			if (null != entityManagerData) {
				entityManagerData.close();
			}
		}
		servicePerformanceUri = getUriInfo().getAbsolutePathBuilder()
				.path(servicePerformanceEntity.toString()).build();

		return JsonResponseWrapper.getResponse(id, Response.Status.CREATED,
				servicePerformanceUri);
	}

	/**
	 * Returns the resource formatted as json
	 * 
	 * @return JSONObject * @throws JSONException
	 */
	@SuppressWarnings("unchecked")
	public JSONObject asJson() throws JSONException {
		JSONObject servicePerformanceAsJson = super.asJson();

		// fill data
		JSONArray servicePerformancesArray = new JSONArray();
		List<?> items = getItems();

		if (items.size() > 0) {
			for (ServicePerformanceEntity servicePerformanceEntity : (List<ServicePerformanceEntity>) items) {
				servicePerformancesArray
						.put(ServicePerformanceResource.asJson(servicePerformanceEntity,
								UriBuilder.fromPath(getUriInfo().getPath())
										.path(servicePerformanceEntity.toString()).build()
										.getPath()));
			}
		}
		servicePerformanceAsJson.put("serviceperformances", servicePerformancesArray);
		return servicePerformanceAsJson;
	}
}
