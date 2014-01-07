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
import java.text.ParseException;

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
import ro.allevo.fintpws.model.HistoryEntity;
import ro.allevo.fintpws.util.JsonResponseWrapper;
import ro.allevo.fintpws.util.ResourcesUtils;

/**
 * @author anda
 * @version $Revision: 1.0 $
 */

public class HistoryResource {
	/**
	 * Field logger
	 */
	private static final Logger logger = LogManager
			.getLogger(HistoryResource.class);

	/**
	 * Field ERROR_MESSAGE_GET_HISTORY. (value is ""Error returning history : "")
	 */
	static final String ERROR_MESSAGE_GET_HISTORY = "Error returning history : ";
	/**
	 * Field ERROR_MESSAGE_PUT_HISTORY. (value is ""Error updating history : "")
	 */
	static final String ERROR_MESSAGE_PUT_HISTORY = "Error updating history : ";
	/**
	 * Field ERROR_MESSAGE_HISTORY_NOT_FOUND. (value is ""History with
	 * guid [%s] not found"")
	 */
	static final String ERROR_MESSAGE_HISTORY_NOT_FOUND = "Historyn with guid [%s] not found";
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
	private HistoryEntity historyEntity;

	/**
	 * default constructor
	 * @return 
	 */
	public HistoryResource() {

	}

	/**
	 * guid
	 */
	private String guid;

	/**
	 * Constructor for HistoryResourse.
	 * 
	 * @param uriInfo
	 *            UriInfo
	 * @param entityManagerData
	 *            EntityManager
	 * @param guid
	 *            String
	 */
	
	public HistoryResource(UriInfo uriInfo,
			EntityManager entityManagerData, String guid) {
		this.uriInfo = uriInfo;
		this.entityManagerData = entityManagerData;
		this.guid = guid;
		historyEntity = null;

		historyEntity = findByGuid(entityManagerData, guid);
	}

	/**
	 * 
	 * @param entityManager
	 *            EntityManager
	 * @param guid
	 *            String
	 * @return HistoryEntity
	 */

	public static HistoryEntity findByGuid(EntityManager entityManager,
			String guid) {

		final TypedQuery<HistoryEntity> query = entityManager
				.createNamedQuery("HistoryEntity.findByGuid",
						HistoryEntity.class);

		final java.util.List<HistoryEntity> results = query.setParameter(
				"guid", guid).getResultList();
		if (!results.isEmpty()) {
			return results.get(0);
		}
		return null;
	}
	
	/**
	 * GET Method : returns an application/json formatted history
	 * 
	 * @return JSONObject the history
	 */

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public JSONObject getHistory() {

		if (null == historyEntity) {
			logger.error(String.format(ERROR_MESSAGE_HISTORY_NOT_FOUND,
					guid));
			throw new EntityNotFoundException(String.format(
					ERROR_MESSAGE_HISTORY_NOT_FOUND, guid));
		}
		try {
			return HistoryResource.asJson(historyEntity,
					uriInfo.getPath());
		} catch (JSONException je) {
			logger.error(ERROR_MESSAGE_GET_HISTORY + ERROR_REASON_JSON, je);
			throw new ApplicationJsonException(je,
					ERROR_MESSAGE_GET_HISTORY + ERROR_REASON_JSON,
					Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
		}
	}
	
	/**
	 * PUT method: updates the history
	 * 
	 * @param jsonEntity
	 *            JSONObject the history holding new values
	 * @return Response
	 * @throws JSONException 
	 */
	@PUT
	@Consumes(MediaType.APPLICATION_JSON)
	public Response updateHistory(JSONObject jsonEntity) throws JSONException {
		if (null == historyEntity) {
			logger.error(String.format(ERROR_MESSAGE_HISTORY_NOT_FOUND,
					guid));
			throw new EntityNotFoundException(String.format(
					ERROR_MESSAGE_HISTORY_NOT_FOUND, guid));
		}

		try {
			if (jsonEntity.has("payload")) {
				historyEntity.setPayload(jsonEntity
						.getString("payload"));
			}
			if (jsonEntity.has("batchid")) {
				historyEntity.setBatchid(jsonEntity
						.getString("batchid"));
			}
			if (jsonEntity.has("correlationid")) {
				historyEntity.setCorrelationid(jsonEntity
						.getString("correlationid"));
			}
			if (jsonEntity.has("sessionid")) {
				historyEntity.setSessionid(jsonEntity
						.getString("sessionid"));
			}
			if (jsonEntity.has("requestorservice")) {
				historyEntity.setRequestorservice(jsonEntity
						.getString("requestorservice"));
			}
			if (jsonEntity.has("responderservice")) {
				historyEntity.setResponderservice(jsonEntity
						.getString("responderservice"));
			}
			if (jsonEntity.has("requesttype")) {
				historyEntity.setRequesttype(jsonEntity
						.getString("requesttype"));
			}
			if (jsonEntity.has("priority")) {
				historyEntity.setPriority(Long.parseLong(jsonEntity
						.getString("priority")));
			}
			if (jsonEntity.has("holdstatus")) {
				historyEntity.setHoldstatus(new BigDecimal (jsonEntity
						.getInt("holdstatus")));
			}
			if (jsonEntity.has("sequence")) {
				historyEntity.setSequence(new BigDecimal (jsonEntity
						.getInt("sequence")));
			}
			if (jsonEntity.has("insertdate")) {
				historyEntity.setInsertdate(ResourcesUtils
						.getTimestamp(jsonEntity.optString("insertdate")));
			}

			if (jsonEntity.has("feedback")) {
				historyEntity.setFeedback(jsonEntity
						.getString("feedback"));
			}
			
			entityManagerData.getTransaction().begin();
			entityManagerData.merge(historyEntity);
			entityManagerData.getTransaction().commit();
		}
		
		catch (JSONException je) {
			logger.error(ERROR_MESSAGE_PUT_HISTORY + ERROR_REASON_JSON, je);
			throw new ApplicationJsonException(je, ERROR_MESSAGE_PUT_HISTORY
					+ ERROR_REASON_JSON,
					Response.Status.BAD_REQUEST.getStatusCode());
		}

		catch (NumberFormatException nfe) {
			logger.error(ERROR_MESSAGE_PUT_HISTORY
					+ ERROR_REASON_NUMBER_FORMAT, nfe);
			throw new ApplicationJsonException(nfe,
					ERROR_MESSAGE_PUT_HISTORY + ERROR_REASON_JSON,
					Response.Status.BAD_REQUEST.getStatusCode());
		} catch (RollbackException re) {
			ApplicationJsonException.handleSQLException(re,
					ERROR_MESSAGE_PUT_HISTORY, logger);
			logger.error(
					ERROR_MESSAGE_PUT_HISTORY + ERROR_REASON_ROLLBACK, re);
			throw re;
		} catch (ParseException pe) {
			logger.error(ERROR_MESSAGE_PUT_HISTORY+ ERROR_REASON_PARSE, pe);
			throw new ApplicationJsonException(pe, ERROR_MESSAGE_PUT_HISTORY
					+ ERROR_REASON_PARSE,
					Response.Status.BAD_REQUEST.getStatusCode());

		} finally {
			if (null != entityManagerData) {
				entityManagerData.close();
			}
		}

		return JsonResponseWrapper.getResponse(Response.Status.OK,
				"history updated");
	}
	
	/**
	 * DELETE method : deletes the history
	 * 
	 * @return Response
	 */

	@DELETE
	public Response deleteHistory() {
		if (null == historyEntity) {
			logger.error(String.format(ERROR_MESSAGE_HISTORY_NOT_FOUND,
					guid));
			throw new EntityNotFoundException(String.format(
					ERROR_MESSAGE_HISTORY_NOT_FOUND, guid));
		}
		try {
			entityManagerData.getTransaction().begin();
			entityManagerData.remove(historyEntity);
			entityManagerData.getTransaction().commit();
		} finally {
			if (null != entityManagerData) {
				entityManagerData.close();
			}
		}

		return JsonResponseWrapper.getResponse(Response.Status.OK,
				"history deleted");
	}

	/**
	 * Returns the resource formatted as json
	 * 
	 * @param historyEntity
	 *            historyEntity
	 * @param path
	 *            String
	 * @return JSONObject
	 * @throws JSONException
	 */
	public static JSONObject asJson(HistoryEntity historyEntity,
			String path) throws JSONException {
		final JSONObject historyAsJson = ApiResource.getMetaResource(path,
				HistoryResource.class);

		historyAsJson.put("guid", historyEntity.getGuid())
				.put("payload", historyEntity.getPayload())
				.put("batchid", historyEntity.getBatchid())
				.put("correlationid", historyEntity.getCorrelationid())
				.put("sessionid", historyEntity.getSessionid())
				.put("requestorservice", historyEntity.getRequestorservice())
				.put("responderservice", historyEntity.getResponderservice())
				.put("requesttype", historyEntity.getRequesttype())
				.put("priority", historyEntity.getPriority())
				.put("holdstatus", historyEntity.getHoldstatus())
				.put("sequence", historyEntity.getSequence())
				.put("insertdate",
						ResourcesUtils.getIsoDateFromTimestamp(historyEntity
								.getInsertdate()))
				.put("feedback", historyEntity.getFeedback());

		return historyAsJson;
	}

	
	
}
