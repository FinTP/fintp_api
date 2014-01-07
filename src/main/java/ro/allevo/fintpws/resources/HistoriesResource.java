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
import ro.allevo.fintpws.model.HistoryEntity;
import ro.allevo.fintpws.util.JsonResponseWrapper;
import ro.allevo.fintpws.util.ResourcesUtils;

/**
 * @author anda
 * @version $Revision: 1.0 $
 */

public class HistoriesResource extends PagedCollection {

	/**
	 * Field logger
	 */
	private static final Logger logger = LogManager
			.getLogger(HistoriesResource.class.getName());

	/**
	 * Field ERROR_MESSAGE_GET_HISTORIES. (value is ""Error returning histories : "")
	 */
	static final String ERROR_MESSAGE_GET_HISTORIES = "Error returning histories : ";
	/**
	 * Field ERROR_MESSAGE_POST_HISTORIES. (value is ""Error creating histories : "")
	 */
	static final String ERROR_MESSAGE_POST_HISTORIES = "Error creating histories : ";

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
	 * Field entityManagerData
	 */
	private final EntityManager entityManagerData;

	/**
	 * 
	 * @param uriInfo
	 *            UriInfo
	 * @param entityManagerData
	 *            EntityManagerData
	 */
	
	public HistoriesResource(UriInfo uriInfo,
			EntityManager entityManagerData) {
		super(uriInfo, entityManagerData.createNamedQuery(
				"HistoryEntity.findAll", HistoryEntity.class), entityManagerData
				.createNamedQuery("HistoryEntity.findTotal", Long.class));
		this.entityManagerData = entityManagerData;
	}

	
	/**
	 * Returns a history sub-resource with guid
	 * 
	 * @param guid
	 *            String guid of the history as last element in the path
	 * 
	 * @return HistoryResource The history sub-resource
	 */
	@Path("{id}")
	public HistoryResource getHystoriesResource(
			@PathParam("id") String guid) {
		return new HistoryResource(getUriInfo(), entityManagerData, guid);
	}

	/**
	 * GET method : returns an application/json formatted list of histories
	 * 
	 * @return JSONObject The list of histories
	 */
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public JSONObject getHistoriesAsJson() {
		try {
			getPage();
			return asJson();
		} catch (JSONException je) {
			logger.error(ERROR_MESSAGE_GET_HISTORIES + ERROR_REASON_JSON, je);
			throw new ApplicationJsonException(je,
					ERROR_MESSAGE_GET_HISTORIES + ERROR_REASON_JSON,
					Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
		}
	}
	
	/**
	 * POST method : creates a history
	 * 
	 * @param jsonEntity
	 *            JSONObject
	 * @return Response
	 */
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	public Response postForm(JSONObject jsonEntity) {
		final String id;
		final HistoryEntity historyEntity = new HistoryEntity();
		URI historyUri = null;
		try {
			//Fill required data
			historyEntity.setGuid(jsonEntity.getString("guid"));
			historyEntity.setPayload(jsonEntity.getString("payload"));
			historyEntity.setCorrelationid(jsonEntity.getString("correlationid"));
			historyEntity.setRequestorservice(jsonEntity.getString("requestorservice"));
			historyEntity.setRequesttype(jsonEntity.getString("requesttype"));
			historyEntity.setPriority(Long.parseLong(jsonEntity.getString("priority")));
			historyEntity.setHoldstatus(new BigDecimal (jsonEntity.optInt("holdstatus",0)));
			
			//Fill optional data
			historyEntity.setBatchid(jsonEntity.optString("batchid"));
			historyEntity.setSessionid(jsonEntity.optString("sessionid"));
			historyEntity.setResponderservice(jsonEntity.optString("responderservice"));
			historyEntity.setSequence(new BigDecimal (jsonEntity.optInt("sequence")));
			historyEntity.setInsertdate(ResourcesUtils.getTimestamp(jsonEntity
						.optString("insertdate")));
			historyEntity.setFeedback(jsonEntity.optString("feedback"));
				
			entityManagerData.getTransaction().begin();
			entityManagerData.persist(historyEntity);
			id = entityManagerData.getEntityManagerFactory()
					.getPersistenceUnitUtil().getIdentifier(historyEntity).toString();
			entityManagerData.getTransaction().commit();
		} catch (JSONException je) {
			logger.error(ERROR_MESSAGE_POST_HISTORIES + ERROR_REASON_JSON, je);
			throw new ApplicationJsonException(je,
					ERROR_MESSAGE_POST_HISTORIES + ERROR_REASON_JSON,
					Response.Status.BAD_REQUEST.getStatusCode());
		} catch (NumberFormatException nfe) {
			logger.error(ERROR_MESSAGE_POST_HISTORIES
					+ ERROR_REASON_NUMBER_FORMAT, nfe);
			throw new ApplicationJsonException(
					nfe,
					ERROR_MESSAGE_POST_HISTORIES + ERROR_REASON_NUMBER_FORMAT,
					Response.Status.BAD_REQUEST.getStatusCode());
		} catch (RollbackException re) {
			ApplicationJsonException.handleSQLException(re,
					ERROR_MESSAGE_POST_HISTORIES, logger);
			logger.error(
					ERROR_MESSAGE_POST_HISTORIES + ERROR_REASON_ROLLBACK, re);
			throw re;
		} catch (ParseException pe) {
			logger.error(ERROR_MESSAGE_POST_HISTORIES + ERROR_REASON_PARSE, pe);
			throw new ApplicationJsonException(pe, ERROR_MESSAGE_POST_HISTORIES
					+ ERROR_REASON_PARSE,
					Response.Status.BAD_REQUEST.getStatusCode());

		} finally {
			if (null != entityManagerData) {
				entityManagerData.close();
			}
		}
		historyUri = getUriInfo().getAbsolutePathBuilder()
				.path(historyEntity.toString()).build();

		return JsonResponseWrapper.getResponse(id, Response.Status.CREATED,
				historyUri);
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
		final JSONObject historiesAsJson = super.asJson();

		// fill data
		final JSONArray historiesArray = new JSONArray();
		final List<?> items = getItems();

		if (items.size() > 0) {
			for (HistoryEntity historyEntity : (List<HistoryEntity>) items) {
				historiesArray.put(HistoryResource.asJson(
						historyEntity,
						UriBuilder.fromPath(getUriInfo().getPath())
								.path(historyEntity.toString()).build()
								.getPath()));
			}
		}
		historiesAsJson.put("histories", historiesArray);
		return historiesAsJson;
	}
	
}
