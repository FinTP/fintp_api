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
import ro.allevo.fintpws.model.RoutingRuleEntity;
import ro.allevo.fintpws.model.ServicePerformanceEntity;
import ro.allevo.fintpws.util.JsonResponseWrapper;
import ro.allevo.fintpws.util.ResourcesUtils;

import java.math.BigDecimal;
import java.text.ParseException;
import java.util.List;

public class ServicePerformanceResource {

	/**
	 * Field logger
	 */
	private static final Logger logger = LogManager
			.getLogger(ServicePerformanceResource.class);

	/**
	 * Field ERROR_MESSAGE_GET_SERVICE_PERFORMANCE. (value is ""Error returning
	 * service performance : "")
	 */
	static final String ERROR_MESSAGE_GET_SERVICE_PERFORMANCE = "Error returning service performance : ";
	/**
	 * Field ERROR_MESSAGE_PUT_SERVICE_PERFORMANCE. (value is ""Error updating
	 * service performance : "")
	 */
	static final String ERROR_MESSAGE_PUT_SERVICE_PERFORMANCE = "Error updating service performance : ";
	/**
	 * Field ERROR_MESSAGE_SERVICE_PERFORMANCE_NOT_FOUND. (value is ""Service
	 * performance with serviceid [%s] not found"")
	 */
	static final String ERROR_MESSAGE_SERVICE_PERFORMANCE_NOT_FOUND = "Service performance with serviceid [%s] not found";
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
	 * Field entityManagerData.
	 */
	private EntityManager entityManagerData;

	/**
	 * the JPA entity
	 */
	private ServicePerformanceEntity servicePerformanceEntity;

	/**
	 * default constructor
	 */
	public ServicePerformanceResource() {

	}

	/**
	 * serviceId
	 */
	private String serviceId;

	/**
	 * Constructor for ServicePerformanceResource.
	 * 
	 * @param uriInfo
	 *            UriInfo
	 * @param entityManagerData
	 *            EntityManager
	 * @param serviceId
	 *            String
	 */
	public ServicePerformanceResource(UriInfo uriInfo,
			EntityManager entityManagerData, String serviceId) {
		this.uriInfo = uriInfo;
		this.entityManagerData = entityManagerData;
		this.serviceId = serviceId;
		servicePerformanceEntity = null;

		servicePerformanceEntity = findByServiceId(entityManagerData,
				serviceId);
	}

	/**
	 * 
	 * @param entityManager
	 *            EntityManager
	 * @param serviceId
	 *            String
	 * @return ServicePerformanceEntity
	 */

	public static ServicePerformanceEntity findByServiceId(
			EntityManager entityManager, String serviceId) {

		final TypedQuery<ServicePerformanceEntity> query = entityManager
				.createNamedQuery("ServicePerformanceEntity.findByServiceId",
						ServicePerformanceEntity.class);

		try{
			final java.util.List<ServicePerformanceEntity> results = query.setParameter(
					"serviceid", Long.parseLong(serviceId)).getResultList();
			if (!results.isEmpty()) {
				return results.get(0);
			}
			return null;
		}catch(NumberFormatException nfe){
			logger.error(String.format(ERROR_MESSAGE_SERVICE_PERFORMANCE_NOT_FOUND, serviceId));
			throw new EntityNotFoundException(String.format(
					ERROR_MESSAGE_SERVICE_PERFORMANCE_NOT_FOUND, serviceId));
		}
		
	}

	/**
	 * GET Method : returns an application/json formatted service performance
	 * 
	 * @return JSONObject the service performance
	 */
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public JSONObject getServicePerformance() {

		if (null == servicePerformanceEntity) {
			logger.error(String.format(
					ERROR_MESSAGE_SERVICE_PERFORMANCE_NOT_FOUND, serviceId));
			throw new EntityNotFoundException(String.format(
					ERROR_MESSAGE_SERVICE_PERFORMANCE_NOT_FOUND, serviceId));
		}
		try {
			return ServicePerformanceResource.asJson(servicePerformanceEntity,
					uriInfo.getPath());
		} catch (JSONException je) {
			logger.error(ERROR_MESSAGE_GET_SERVICE_PERFORMANCE
					+ ERROR_REASON_JSON, je);
			throw new ApplicationJsonException(je,
					ERROR_MESSAGE_GET_SERVICE_PERFORMANCE + ERROR_REASON_JSON,
					Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
		}
	}

	/**
	 * PUT method: updates the service performance
	 * 
	 * @param jsonEntity
	 *            JSONObject the service performance holding new values
	 * @return Response
	 */
	@PUT
	@Consumes(MediaType.APPLICATION_JSON)
	public Response updateServicePerformance(JSONObject jsonEntity) {
		if (null == servicePerformanceEntity) {
			logger.error(String.format(
					ERROR_MESSAGE_SERVICE_PERFORMANCE_NOT_FOUND, serviceId));
			throw new EntityNotFoundException(String.format(
					ERROR_MESSAGE_SERVICE_PERFORMANCE_NOT_FOUND, serviceId));
		}

		try {
			if (jsonEntity.has("insertdate")) {
				servicePerformanceEntity.setInsertdate(ResourcesUtils
						.getTimestamp(jsonEntity.getString("insertdate")));
			}
			if (jsonEntity.has("mintransactiontime")) {
				servicePerformanceEntity.setMintransactiontime(new BigDecimal(
						jsonEntity.getString("mintransactiontime")));
			}
			if (jsonEntity.has("maxtransactiontime")) {
				servicePerformanceEntity.setMaxtransactiontime(new BigDecimal(
						jsonEntity.getString("maxtransactiontime")));
			}
			if (jsonEntity.has("meantransactiontime")) {
				servicePerformanceEntity.setMeantransactiontime(new BigDecimal(
						jsonEntity.getString("meantransactiontime")));
			}
			if (jsonEntity.has("sequenceno")) {
				servicePerformanceEntity.setSequenceno(new BigDecimal(
						jsonEntity.getString("sequenceno")));
			}
			if (jsonEntity.has("ioidentifier")) {
				servicePerformanceEntity.setIoidentifier(new BigDecimal(
						jsonEntity.getString("ioidentifier")));
			}
			if (jsonEntity.has("sessionid")) {
				servicePerformanceEntity.setSessionid(new BigDecimal(jsonEntity
						.getString("sessionid")));
			}
			
			// optional data
			if (jsonEntity.has("commitedtrns")) {
				servicePerformanceEntity.setCommitedtrns(new BigDecimal(
						jsonEntity.optInt("commitedtrns")));
			}
			if (jsonEntity.has("abortedtrns")) {
				servicePerformanceEntity.setAbortedtrns(new BigDecimal(
						jsonEntity.optInt("abortedtrns")));
			}

			entityManagerData.getTransaction().begin();
			entityManagerData.merge(servicePerformanceEntity);
			entityManagerData.getTransaction().commit();
		} catch (NumberFormatException nfe) {
			logger.error(ERROR_MESSAGE_PUT_SERVICE_PERFORMANCE
					+ ERROR_REASON_NUMBER_FORMAT, nfe);
			throw new ApplicationJsonException(nfe,
					ERROR_MESSAGE_PUT_SERVICE_PERFORMANCE + ERROR_REASON_JSON,
					Response.Status.BAD_REQUEST.getStatusCode());
		} catch (RollbackException re) {
			ApplicationJsonException.handleSQLException(re,
					ERROR_MESSAGE_PUT_SERVICE_PERFORMANCE, logger);
			logger.error(ERROR_MESSAGE_PUT_SERVICE_PERFORMANCE + ERROR_REASON_ROLLBACK,
					re);
			throw re;
		} catch (ParseException pe) {
			logger.error(ERROR_MESSAGE_PUT_SERVICE_PERFORMANCE + ERROR_REASON_PARSE, pe);
			throw new ApplicationJsonException(pe,
					ERROR_MESSAGE_PUT_SERVICE_PERFORMANCE + ERROR_REASON_PARSE,
					Response.Status.BAD_REQUEST.getStatusCode());
		} catch (JSONException je) {
			logger.error(ERROR_MESSAGE_PUT_SERVICE_PERFORMANCE + ERROR_REASON_JSON, je);
			throw new ApplicationJsonException(je,
					ERROR_MESSAGE_PUT_SERVICE_PERFORMANCE + ERROR_REASON_JSON,
					Response.Status.BAD_REQUEST.getStatusCode());
		} finally {
			if (null != entityManagerData) {
				entityManagerData.close();
			}
		}

		return JsonResponseWrapper.getResponse(Response.Status.OK,
				"service performance updated");
	}
	
	/**
	 * DELETE method : deletes the service performance
	 * 
	 * @return Response
	 */

	@DELETE
	public Response deleteServicePerformance() {
		if (null == servicePerformanceEntity) {
			logger.error(String.format(ERROR_MESSAGE_SERVICE_PERFORMANCE_NOT_FOUND,
					serviceId));
			throw new EntityNotFoundException(String.format(
					ERROR_MESSAGE_SERVICE_PERFORMANCE_NOT_FOUND, serviceId));
		}
		try {
			entityManagerData.getTransaction().begin();
			entityManagerData.remove(servicePerformanceEntity);
			entityManagerData.getTransaction().commit();
		} finally {
			if (null != entityManagerData) {
				entityManagerData.close();
			}
		}

		return JsonResponseWrapper.getResponse(Response.Status.OK,
				"service performance deleted");
	}
	
	/**
	 * Returns the resource formatted as json
	 * 
	 * @param servicePerformanceEntity
	 *            ServicePerformanceEntity
	 * @param path
	 *            String
	 * @return JSONObject
	 * @throws JSONException
	 */
	public static JSONObject asJson(ServicePerformanceEntity servicePerformanceEntity,
			String path) throws JSONException {
		final JSONObject servicePerformanceAsJson = ApiResource.getMetaResource(path,
				ServicePerformanceResource.class);

		servicePerformanceAsJson
				.put("serviceid", servicePerformanceEntity.getServiceid())
				.put("insertdate",
						ResourcesUtils
								.getIsoDateFromTimestamp(servicePerformanceEntity
										.getInsertdate()))
				.put("mintransactiontime",
						servicePerformanceEntity.getMintransactiontime())
				.put("maxtransactiontime",
						servicePerformanceEntity.getMaxtransactiontime())
				.put("meantransactiontime", servicePerformanceEntity.getMeantransactiontime())
				.put("sequenceno", servicePerformanceEntity.getSequenceno())
				.put("ioidentifier", servicePerformanceEntity.getIoidentifier())
				.put("sessionid", servicePerformanceEntity.getSessionid())
				.put("commitedtrns", servicePerformanceEntity.getCommitedtrns())
				.put("abortedtrns", servicePerformanceEntity.getAbortedtrns());

		return servicePerformanceAsJson;
	}

}
