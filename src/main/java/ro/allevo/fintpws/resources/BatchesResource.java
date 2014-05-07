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

import java.util.List;

import javax.persistence.EntityManager;
import javax.ws.rs.GET;
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
import ro.allevo.fintpws.model.BatchJobEntity;

/**
 * Resource class implementing /batchres path methods
 * 
 * @author remus
 *
 */
public class BatchesResource extends PagedCollection{

	/**
	 * Field logger
	 */
	private static final Logger logger = LogManager
			.getLogger(BatchesResource.class.getName());

	/**
	 * Field ERROR_MESSAGE_GET_BATCHES. (value is ""Error returning
	 * batches : "")
	 */
	static final String ERROR_MESSAGE_GET_BATCHES = "Error returning batches : ";
	/**
	 * Field ERROR_MESSAGE_POST_BATCHES. (value is ""Error creating
	 * batches : "")
	 */
	static final String ERROR_MESSAGE_POST_BATCHES = "Error creating batches : ";

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
	 * Field entityManagerData
	 */
	private final EntityManager entityManagerData;
	
	/**
	 * Class Constructor
	 * @param uriInfo
	 *            UriInfo
	 * @param entityManagerConfig
	 *            EntityManager
	 */

	public BatchesResource(UriInfo uriInfo,
			EntityManager entityManagerData) {
		super(uriInfo, entityManagerData.createNamedQuery(
				"BatchJobEntity.findAll", BatchJobEntity.class),
				entityManagerData.createNamedQuery(
						"BatchJobEntity.findTotal", Long.class));
		this.entityManagerData = entityManagerData;
	}
	
	/**
	 * Returns a batch sub-resource with id
	 * 
	 * @param id
	 *            String id of the batch as last element in the path
	 * 
	 * @return BatchResource The batch sub-resource
	 */
	@Path("{id}")
	public BatchResource getRoutingRuleResource(
			@PathParam("id") String guid) {
		return new BatchResource(getUriInfo(), entityManagerData, guid);
	}
	
	/**
	 * GET method : returns an application/json formatted list of batches
	 * 
	 * @return JSONObject The list of batches
	 */
	
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public JSONObject getBatchesAsJson() {
		try {
			getPage();
			return asJson();
		} catch (JSONException je) {
			logger.error(ERROR_MESSAGE_GET_BATCHES + ERROR_REASON_JSON,
					je);
			throw new ApplicationJsonException(je,
					ERROR_MESSAGE_GET_BATCHES + ERROR_REASON_JSON,
					Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
		}
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
		final JSONObject batchesAsJson = super.asJson();

		// fill data
		final JSONArray batchesArray = new JSONArray();
		final List<?> items = getItems();

		if (items.size() > 0) {
			for (BatchJobEntity batchEntity : (List<BatchJobEntity>) items) {
				batchesArray.put(BatchResource.asJson(
						batchEntity,
						UriBuilder.fromPath(getUriInfo().getPath())
								.path(String.valueOf(batchEntity.getCombatchid())).build()
								.getPath()));
			}
		}
		batchesAsJson.put("batches", batchesArray);
		return batchesAsJson;
	}
}
