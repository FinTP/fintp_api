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
import java.sql.Timestamp;
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
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.springframework.security.access.AccessDeniedException;

import ro.allevo.fintpws.exceptions.ApplicationJsonException;
import ro.allevo.fintpws.model.EntryQueueEntity;
import ro.allevo.fintpws.model.QueueEntity;
import ro.allevo.fintpws.model.RoutedMessageEntity;
import ro.allevo.fintpws.model.messagesViews.MtView;
import ro.allevo.fintpws.model.messagesViews.MessageTypeToViewsEnum;
import ro.allevo.fintpws.security.RolesUtils;
import ro.allevo.fintpws.util.JsonResponseWrapper;
import ro.allevo.fintpws.util.ResourcesUtils;

/**
 * Resource class implementing /messages path methods and acting as
 * /messages/{id} sub-resource locator.
 * 
 * @author horia
 * @version $Revision: 1.0 $
 */
public class MessagesResource extends PagedCollection {
	/**
	 * Field logger.
	 */
	private static Logger logger = LogManager.getLogger(MessagesResource.class
			.getName());

	/**
	 * Field ERROR_MESSAGE_GET_MESSAGES. (value is ""Error returning messages :
	 * "")
	 */
	static final String ERROR_MESSAGE_GET_MESSAGES = "Error returning messages : ";
	/**
	 * Field ERROR_MESSAGE_POST_MESSAGES. (value is ""Error creating message :
	 * "")
	 */
	static final String ERROR_MESSAGE_POST_MESSAGES = "Error creating message : ";

	/**
	 * Field ERROR_REASON_JSON. (value is ""json"")
	 */
	static final String ERROR_REASON_JSON = "json";
	/**
	 * Field ERROR_REASON_NUMBER_FORMAT. (value is ""number format"")
	 */
	static final String ERROR_REASON_NUMBER_FORMAT = "number format";
	/**
	 * Field ERROR_REASON_TIME_FORMAT. (value is ""time format"")
	 */
	static final String ERROR_REASON_TIME_FORMAT = "time format";
	/**
	 * Field ERROR_REASON_ROLLBACK. (value is ""rollback"")
	 */
	static final String ERROR_REASON_ROLLBACK = "rollback";

	/**
	 * Field isMessageInQueue.
	 */
	private boolean isMessageInQueue = false;

	/**
	 * Field needsPayload.
	 */
	private boolean needsPayload = false;

	/**
	 * Field isDisplayFeatureRequested
	 */
	private boolean isDisplayFeatureRequested = false;

	// actual uri info provided by parent resource
	/**
	 * Field uriInfo.
	 */
	private UriInfo uriInfo;
	/**
	 * Field entityManagerData.
	 */
	private EntityManager entityManagerData;

	/**
	 * Field entityManagerConfig.
	 */
	private EntityManager entityManagerConfig;

	/**
	 * Field queueEntity.
	 */
	private QueueEntity queueEntity = null;

	/**
	 * Creates a new instance of MessagesResource
	 * 
	 * @param uriInfo
	 *            UriInfo
	 * @param entityManagerData
	 *            EntityManager
	 * @param queueEntity
	 *            QueueEntity
	 * @param entityManagerConfig
	 *            EntityManager
	 */
	public MessagesResource(UriInfo uriInfo, EntityManager entityManagerData,
			EntityManager entityManagerConfig, QueueEntity queueEntity) {
		super(uriInfo, entityManagerData);

		if (uriInfo.getQueryParameters().containsKey("type")) {
			MessageTypeToViewsEnum messageType = MessageTypeToViewsEnum
					.getMessageType(uriInfo.getQueryParameters().getFirst(
							"type"));
			this.isDisplayFeatureRequested = true;
			Timestamp atArgument = null;
			if (uriInfo.getQueryParameters().containsKey("at")) {
				try {
					atArgument = ResourcesUtils.getTimestamp(uriInfo
							.getQueryParameters().getFirst("at"));
					this.entityClass = messageType.getClazz();

				} catch (ParseException e) {
					// TODO: throw json bad request exception
					e.printStackTrace();
				}

			}
			// TODO: throw here right JSONException for bad request

			this.entityClass = messageType.getClazz();
			this.setItemsQuery(messageType.getItemsQuery(entityManagerData,
					atArgument));
			this.setTotalQuery(messageType.getTotalQuery(entityManagerData,
					atArgument));
			
			if(null != queueEntity){
				this.isMessageInQueue = false;
			}

		} else {
			if (null != queueEntity) {
				this.isMessageInQueue = true;
				this.setItemsQuery(entityManagerData
						.createNamedQuery("EntryQueueEntity.findAllQueue",
								EntryQueueEntity.class).setParameter(
								"queuename", queueEntity.getName()));
				this.setTotalQuery(entityManagerData.createNamedQuery(
						"EntryQueueEntity.findTotalQueue", Long.class)
						.setParameter("queuename", queueEntity.getName()));
				this.entityClass = EntryQueueEntity.class;
			} else {
				this.setItemsQuery(entityManagerData.createNamedQuery(
						"RoutedMessageEntity.findAll",
						RoutedMessageEntity.class));
				this.setTotalQuery(entityManagerData.createNamedQuery(
						"RoutedMessageEntity.findTotalFeedbackagg", Long.class));
				this.entityClass = RoutedMessageEntity.class;
			}
		}
		boolean containFilter = uriInfo.getQueryParameters().containsKey(
				"filter");
		if (containFilter) {
			this.needsPayload = uriInfo.getQueryParameters().getFirst("filter")
					.contains("b");
		}
		this.uriInfo = uriInfo;
		this.entityManagerData = entityManagerData;
		this.entityManagerConfig = entityManagerConfig;
		this.queueEntity = queueEntity;

		// adding filter options
		// check if filter through queue is made
		if (queueEntity != null) {
			filterResource("queuename", queueEntity.getName());
		} else {
			filterResource();
		}

	}

	/**
	 * Returns a message sub-resource with id messageId
	 * 
	 * @param messageGuid
	 *            String
	 * @return MessageResource The message sub-resource
	 */
	@Path("{id}")
	public MessageResource getMessage(@PathParam("id") String messageGuid) {
		return new MessageResource(uriInfo, entityManagerData,
				entityManagerConfig, messageGuid, isMessageInQueue,
				needsPayload, queueEntity);
	}

	/**
	 * GET method : returns an application/json formatted list of messages
	 * 
	 * @return JSONObject The list of messages
	 */
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public JSONObject getMessagesAsJson(@QueryParam("type") String type) {
		// authorization
		if (isMessageInQueue) {
			if (!RolesUtils.hasReadAuthorityOnQueue(queueEntity)) {
				throw new ApplicationJsonException(new AccessDeniedException("forbidden"), "forbidden", 
						Status.FORBIDDEN.getStatusCode());
			}
		} else {
			if (!RolesUtils.hasReportsRole()) {
				throw new ApplicationJsonException(new AccessDeniedException("forbidden"), "forbidden", 
						Status.FORBIDDEN.getStatusCode());
			}
		}

		// get type from query
		try {
			getPage();
			return asJson();
		} catch (JSONException je) {
			logger.error(ERROR_MESSAGE_GET_MESSAGES + ERROR_REASON_JSON, je);
			throw new ApplicationJsonException(je, ERROR_MESSAGE_GET_MESSAGES
					+ ERROR_REASON_JSON,
					Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
		}

	}

	/**
	 * POST method : creates a message
	 * 
	 * @param jsonEntity
	 *            JSONObject The message to be created
	 * @return Response The URI of the newly created message * @throws
	 *         JSONException
	 */
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	public Response postForm(JSONObject jsonEntity) {
		// authorization
		if (isMessageInQueue) {
			if (!RolesUtils.hasWriteAuthorityOnQueue(queueEntity)) {
				throw new AccessDeniedException("forbidden");
			}
		} else {
			if (!RolesUtils.hasReportsRole()) {
				throw new AccessDeniedException("forbidden");
			}
		}
		EntryQueueEntity entryQueueEntity = new EntryQueueEntity();
		try {
			if (jsonEntity.has("guid")) {
				entryQueueEntity.setGuid(jsonEntity.getString("guid"));
			}
			if (jsonEntity.has("batchid")) {
				entryQueueEntity.setBatchid(jsonEntity.optString("batchid"));
			}
			if (jsonEntity.has("correlationid")) {
				entryQueueEntity.setCorrelationid(jsonEntity
						.optString("correlationid"));
			}
			entryQueueEntity.setRequestorservice(jsonEntity
					.getString("requestorservice"));
			if (jsonEntity.has("responderservice")) {
				entryQueueEntity.setResponderservice(jsonEntity
						.optString("responderservice"));
			}
			entryQueueEntity
					.setRequesttype(jsonEntity.getString("requesttype"));
			if (jsonEntity.has("feedback")) {
				entryQueueEntity.setFeedback(jsonEntity.optString("feedback"));
			}
			if (jsonEntity.has("sessionid")) {
				entryQueueEntity
						.setSessionid(jsonEntity.optString("sessionid"));
			}
			if (jsonEntity.has("priority")) {
				entryQueueEntity.setPriority(new BigDecimal(jsonEntity
						.getInt("priority")));
			}
			if (jsonEntity.has("holdstatus")) {
				entryQueueEntity.setHoldstatus(new BigDecimal(jsonEntity
						.getInt("holdstatus")));
			}
			if (jsonEntity.has("sequence")) {
				entryQueueEntity.setSequence(new BigDecimal(jsonEntity
						.getInt("sequence")));
			}
			if (jsonEntity.has("payload")) {
				entryQueueEntity.setPayload(jsonEntity.getString("payload"));
			}
			if (jsonEntity.has("queuename")) {
				entryQueueEntity
						.setQueuename(jsonEntity.getString("queuename"));
			} else {
				if (null != queueEntity) {
					entryQueueEntity.setQueuename(queueEntity.getName());
				}
			}
			entityManagerData.getTransaction().begin();
			entityManagerData.persist(entryQueueEntity);
			entityManagerData.getTransaction().commit();
		} catch (JSONException je) {
			logger.error(ERROR_MESSAGE_POST_MESSAGES + ERROR_REASON_JSON, je);
			throw new ApplicationJsonException(je, ERROR_MESSAGE_POST_MESSAGES
					+ ERROR_REASON_JSON,
					Response.Status.BAD_REQUEST.getStatusCode());
		} catch (NumberFormatException nfe) {
			logger.error(ERROR_MESSAGE_POST_MESSAGES
					+ ERROR_REASON_NUMBER_FORMAT, nfe);
			throw new ApplicationJsonException(nfe, ERROR_MESSAGE_POST_MESSAGES
					+ ERROR_REASON_NUMBER_FORMAT,
					Response.Status.BAD_REQUEST.getStatusCode());
		} catch (RollbackException re) {
			// traverse the cause to find a possible constraint violation
			ApplicationJsonException.handleSQLException(re,
					ERROR_MESSAGE_POST_MESSAGES, logger);

			// log and rethrow the original error
			logger.error(ERROR_MESSAGE_POST_MESSAGES + ERROR_REASON_ROLLBACK,
					re);
			throw re;
		} finally {
			if (null != entityManagerData) {
				entityManagerData.close();
			}
		}
		URI messageUri = getUriInfo().getAbsolutePathBuilder()
				.path(entryQueueEntity.getGuid()).build();
		return JsonResponseWrapper.getResponse(Response.Status.CREATED,
				messageUri);
	}

	/**
	 * Returns the resource formatted as json
	 * 
	 * @return JSONObject * @throws JSONException
	 */
	@SuppressWarnings("unchecked")
	public JSONObject asJson() throws JSONException {
		JSONObject messagesAsJson = super.asJson();

		// fill data
		JSONArray messagesArray = new JSONArray();
		List<?> items = getItems();

		if (items.size() > 0) {
			if (isDisplayFeatureRequested) {
				for (MtView messageEntity : (List<? extends MtView>) items) {
					messagesArray.put(messageEntity.toJSON());
				}
			} else {

				if (null != queueEntity) {
					for (EntryQueueEntity messageEntity : (List<EntryQueueEntity>) items) {
						messagesArray.put(MessageResource.asJson(messageEntity,
								isMessageInQueue, needsPayload,
								UriBuilder.fromPath(getUriInfo().getPath())
										.path(messageEntity.getGuid()).build()
										.getPath()));
					}
				} else {
					for (RoutedMessageEntity messageEntity : (List<RoutedMessageEntity>) items) {
						messagesArray.put(MessageResource.asJson(messageEntity,
								isMessageInQueue, needsPayload,
								UriBuilder.fromPath(getUriInfo().getPath())
										.path(messageEntity.getGuid()).build()
										.getPath()));
					}
				}
			}
		}
		messagesAsJson.put("messages", messagesArray);
		return messagesAsJson;
	}

}
