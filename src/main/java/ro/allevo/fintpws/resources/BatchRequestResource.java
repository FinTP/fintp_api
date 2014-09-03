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
import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse.Status;

import ro.allevo.fintpws.exceptions.ApplicationJsonException;
import ro.allevo.fintpws.model.BatchJobEntity;
import ro.allevo.fintpws.model.BatchRequestEntity;

/**
 * Resource class implementing /batchrequests/{groupkey} path methods
 * 
 * @author remus
 * 
 */
public class BatchRequestResource {
	/**
	 * Field logger.
	 */
	private static Logger logger = LogManager
			.getLogger(BatchRequestResource.class);

	/**
	 * Field ERROR_MESSAGE_GET_BATCH_REQUEST. (value is ""Error returning batch
	 * request : "")
	 */
	static final String ERROR_MESSAGE_GET_BATCH_REQUEST = "Error returning batch request : ";
	/**
	 * Field ERROR_MESSAGE_PUT_BATCH_REQUEST . (value is ""Error updating batch
	 * request : "")
	 */
	static final String ERROR_MESSAGE_PUT_BATCH_REQUEST = "Error updating batch request : ";
	/**
	 * Field ERROR_MESSAGE_BR_NOT_FOUND. (value is ""Batch request with name
	 * [%s] not found"")
	 */
	static final String ERROR_MESSAGE_BR_NOT_FOUND = "Batch request with name [%s] not found";
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
	 * Field uriInfo.
	 */
	private UriInfo uriInfo;
	/**
	 * Field entityManagerData.
	 */
	private EntityManager entityManagerData;
	/**
	 * the JPA entities
	 */
	private List<BatchRequestEntity> batchRequestEntities;
	/**
	 * group key
	 */
	private String groupKey;
	
	static int userid  ;
	/**
	 * Constructor
	 * 
	 * @param uriInfo
	 *            UriInfo actual uri passed by parent resource
	 * 
	 * @param queueName
	 *            String Queue name
	 * @param entityManagerData
	 *            EntityManager
	 */
	public BatchRequestResource(UriInfo uriInfo,
			EntityManager entityManagerData, String groupKey) {
		this.uriInfo = uriInfo;
		this.entityManagerData = entityManagerData;
		this.groupKey = groupKey;
		this.batchRequestEntities = findByGroupKey(entityManagerData, groupKey);
	}

	/**
	 * Finds all batchRequests having given groupKey
	 * 
	 * @param entityManagerData
	 *            EntityManager
	 * @param groupKey
	 *            String
	 * @return ArrayList<BatchRequestEntity>
	 */
	public static List<BatchRequestEntity> findByGroupKey(
			EntityManager entityManagerData, String groupKey) {
		TypedQuery<BatchRequestEntity> query = entityManagerData
				.createNamedQuery("BatchRequestEntity.findAllByGroupKey",
						BatchRequestEntity.class);
		List<BatchRequestEntity> batchRequestEntities = query.setParameter(
				"key", groupKey).getResultList();
		return batchRequestEntities;
	}

	/**
	 * GET method : returns an application/json formatted batch request
	 * 
	 * @return JSONObject the batch request
	 */
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getBatchRequest() {
		try {
			return asJson(groupKey, batchRequestEntities, uriInfo.getPath(),
					entityManagerData);
		} catch (JSONException e) {
			logger.error(ERROR_MESSAGE_GET_BATCH_REQUEST + ERROR_REASON_JSON, e);
			throw new ApplicationJsonException(e,
					ERROR_MESSAGE_GET_BATCH_REQUEST + ERROR_REASON_JSON,
					Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
		}
	}

	/**
	 * Returns the resource formatted as json
	 * 
	 * @param groupKey
	 * @param batchRequestEntities
	 * @param path
	 * @param entityManager
	 * @return
	 * @throws JSONException
	 */
	public static Response asJson(String groupKey,
			List<BatchRequestEntity> batchRequestEntities, String path,
			EntityManager entityManager) throws JSONException {
		JSONObject requestAsJson = ApiResource.getMetaResource(path,
				BatchRequestResource.class);
		JSONArray comBatchIds = new JSONArray();
		ArrayList<String> usedBatchUids = new ArrayList<>();
		int processedCount = 0;
		int totalCount = 0;
		boolean inProgress = false;

		for (BatchRequestEntity batchRequestEntity : batchRequestEntities) {
			String batchUid = batchRequestEntity.getBatchuid();
			final TypedQuery<BatchJobEntity> query = entityManager
					.createNamedQuery("BatchJobEntity.findByBatchID",
							BatchJobEntity.class).setParameter("id", batchUid);
			List<BatchJobEntity> batches = query.getResultList();

			for (BatchJobEntity batch : batches) {
				processedCount += batch.getDefjobcount();
				totalCount += batch.getBatchcount();
				if (batch.getBatchstatus() < 20) {
					inProgress = true;
				}
				int percentage = batch.getDefjobcount() * 100
						/ batch.getBatchcount();

				JSONObject batchJSON = new JSONObject();
				BatchStatus status = BatchStatus.fromInteger(batch
						.getBatchstatus());
				batchJSON.put("id", batch.getCombatchid());
				batchJSON.put("status", status.name());
				batchJSON.put("progress", percentage);
				batchJSON.put("user", batch.getUserid());
				
				if (!usedBatchUids.contains(batch.getCombatchid())) {
					comBatchIds.put(batchJSON);
					usedBatchUids.add(batch.getCombatchid());
				}
				userid = batch.getUserid();
			}
		}

		if (comBatchIds.length() == 0) {
			JSONObject entity = new JSONObject().put("code", 202)
					.put("progress", 0)
					.put("nb_batches", batchRequestEntities.size())
							.put("user", userid);
			
			return Response.status(Status.ACCEPTED).entity(entity).build();
		}
		if (inProgress) {
			int percentage = processedCount * 100 / totalCount;
			JSONObject entity = new JSONObject().put("code", 202)
					.put("progress", percentage).put("batches", comBatchIds)
					.put("nb_batches", batchRequestEntities.size()).put("user", userid);
			return Response.status(Status.ACCEPTED).entity(entity).build();
		}

		requestAsJson.put("groupkey", groupKey).put("batches", comBatchIds)
				.put("nb_batches", batchRequestEntities.size()).put("user", userid);

		return Response.status(Status.CREATED).entity(requestAsJson).build();
	}

	/**
	 * Enum type of batch status
	 * @author remus
	 *
	 */
	private enum BatchStatus {
		NEW(0), IN_PROGRESS(10), READY(15), FAILED(20), SUCCESS(30);

		private final int value;

		private BatchStatus(int value) {
			this.value = value;
		}

		public static BatchStatus fromInteger(int value) {
			for (BatchStatus status : BatchStatus.values()) {
				if (status.value == value) {
					return status;
				}
			}
			// TODO: throw illegal exception
			return null;
		}

	}
}
