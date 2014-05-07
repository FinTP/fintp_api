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
import javax.persistence.TypedQuery;
import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import ro.allevo.fintpws.exceptions.ApplicationJsonException;
import ro.allevo.fintpws.model.BatchJobEntity;

/**
 * Resource class implementing /batchrequests path methods
 * @author remus
 * @version $Revision: 1.0 $
 */
public class BatchResource {
	/**
	 * Field logger
	 */
	private static final Logger logger = LogManager
			.getLogger(BatchResource.class);
	/**
	 * Field ERROR_MESSAGE_GET_BATCH. (value is ""Error returning batch : "")
	 */
	static final String ERROR_MESSAGE_GET_BATCH = "Error returning batch : ";
	/**
	 * Field ERROR_MESSAGE_PUT_BATCH. (value is ""Error updating batch : "")
	 */
	static final String ERROR_MESSAGE_PUT_BATCH = "Error updating batch : ";
	/**
	 * Field ERROR_MESSAGE_BATCH_NOT_FOUND. (value is ""Batch with
	 * id [%s] not found"")
	 */
	static final String ERROR_MESSAGE_BATCH_NOT_FOUND = "Batch with id [%s] not found";
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
	 * Field uriInfo
	 */
	private UriInfo uriInfo;
	/**
	 * the JPA entity
	 */
	private BatchJobEntity batchJobEntity;
	/**
	 * combatchid
	 */
	private String combatchid;

	/**
	 * default constructor
	 */
	public BatchResource() {

	}

	/**
	 * Constructor for RoutingRuleResource.
	 * 
	 * @param uriInfo
	 *            UriInfo
	 * @param entityManagerConfig
	 *            EntityManager
	 * @param guid
	 *            String
	 */
	public BatchResource(UriInfo uriInfo,
			EntityManager entityManagerConfig, String id) {
		this.uriInfo = uriInfo;
		this.combatchid= id;
		batchJobEntity = findByGuid(entityManagerConfig, id);
	}
	
	/**
	 * 
	 * @param entityManager
	 *            EntityManager
	 * @param guid
	 *            String
	 * @return BatchJobEntity
	 */

	public static BatchJobEntity findByGuid(EntityManager entityManager,
			String id) {

		final TypedQuery<BatchJobEntity> query = entityManager
				.createNamedQuery("BatchJobEntity.findByGuid",
						BatchJobEntity.class).setParameter("id", id);
		
		try{
			final java.util.List<BatchJobEntity> results = query
					.getResultList();
			if (!results.isEmpty()) {
				return results.get(0);
			}
			return null;
		}catch(NumberFormatException nfe){
			logger.error(String.format(ERROR_MESSAGE_BATCH_NOT_FOUND, id));
			throw new EntityNotFoundException(String.format(
					ERROR_MESSAGE_BATCH_NOT_FOUND, id));
		}
	}
	
	/**
	 * GET Method : returns an application/json formatted batch
	 * 
	 * @return JSONObject the batch
	 */

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public JSONObject getBatch() {

		if (null == batchJobEntity) {
			logger.error(String.format(ERROR_MESSAGE_BATCH_NOT_FOUND,
					combatchid));
			throw new EntityNotFoundException(String.format(
					ERROR_MESSAGE_BATCH_NOT_FOUND, combatchid));
		}
		try {
			return asJson(batchJobEntity,
					uriInfo.getPath());
		} catch (JSONException je) {
			logger.error(ERROR_MESSAGE_GET_BATCH + ERROR_REASON_JSON, je);
			throw new ApplicationJsonException(je,
					ERROR_MESSAGE_GET_BATCH + ERROR_REASON_JSON,
					Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
		}
	}
	
	/**
	 * Returns the resource formatted as json
	 * 
	 * @param batchJobEntity
	 *            BatchJobEntity
	 * @param path
	 *            String
	 * @throws JSONException
	 * @return JSONObject 
	 * @throws JSONException
	 */

	public static JSONObject asJson(BatchJobEntity batchJobEntity,
			String path) throws JSONException {
		final JSONObject batchAsJson = ApiResource.getMetaResource(path,
				BatchResource.class);

		batchAsJson.put("combatchid", batchJobEntity.getCombatchid())
				.put("batchamount", batchJobEntity.getBatchamount())
				.put("batchcount", batchJobEntity.getBatchcount())
				.put("batchid", batchJobEntity.getBatchid())
				.put("batchstatus", batchJobEntity.getBatchstatus())
				.put("batchtype", batchJobEntity.getBatchtype())
				.put("batchuid", batchJobEntity.getBatchuid())
				.put("combatchamt", batchJobEntity.getCombatchamt())
				.put("defjobcount", batchJobEntity.getDefjobcount())
				.put("insertdate", batchJobEntity.getInsertdate())
				.put("reason", batchJobEntity.getReason())
				.put("routingpoint", batchJobEntity.getRoutingpoint())
				.put("userid", batchJobEntity.getUserid());

		return batchAsJson;
	}
}
