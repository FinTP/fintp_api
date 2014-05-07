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

import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityNotFoundException;
import javax.persistence.Query;
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
import javax.ws.rs.core.Response.Status;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.springframework.security.access.AccessDeniedException;

import ro.allevo.fintpws.exceptions.ApplicationJsonException;
import ro.allevo.fintpws.model.EntryQueueEntity;
import ro.allevo.fintpws.model.QueueEntity;
import ro.allevo.fintpws.model.RoutedMessageEntity;
import ro.allevo.fintpws.model.messagesViews.MessageTypeToViewsEnum;
import ro.allevo.fintpws.model.messagesViews.MtView;
import ro.allevo.fintpws.security.RolesUtils;
import ro.allevo.fintpws.util.JsonResponseWrapper;
import ro.allevo.fintpws.util.ReflectionUtils;
import ro.allevo.fintpws.util.ResourcesUtils;

/**
 * Resource class implementing /messages/{id} path methods.
 * 
 * @author costi
 * @version $Revision: 1.0 $
 */
public class MessageResource {
	/**
	 * Field logger.
	 */
	private static Logger logger = LogManager.getLogger(MessageResource.class
			.getName());

	/**
	 * Field ERROR_MESSAGE_GET_MESSAGE. (value is ""Error returning message :
	 * "")
	 */
	static final String ERROR_MESSAGE_GET_MESSAGE = "Error returning message : ";
	/**
	 * Field ERROR_MESSAGE_PUT_MESSAGE. (value is ""Error updating message : "")
	 */
	static final String ERROR_MESSAGE_PUT_MESSAGE = "Error updating message : ";
	/**
	 * Field ERROR_MESSAGE_Q_NOT_FOUND. (value is ""Queue with name [%s] not
	 * found"")
	 */
	static final String ERROR_MESSAGE_M_NOT_FOUND = "Message with guid [%s] not found";

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
	 * Field ERROR_REASON_REFLECTION_ERROR.
	 * (value is ""reflection error"")
	 */
	static final String ERROR_REASON_REFLECTION_ERROR = "reflection error";

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
	
	// the JPA entity
	/**
	 * Field messageEntity.
	 */
	private RoutedMessageEntity messageEntity;
	/**
	 * Field messageQueueEntity.
	 */
	private EntryQueueEntity messageQueueEntity;

	/**
	 * Field viewEntity
	 */
	private MtView viewEntity;
	
	/**
	 * Field isMessageInQueue.
	 */
	private boolean isMessageInQueue = false;

	/**
	 * Field needsPayload.
	 */
	private boolean needsPayload = false;

	/**
	 * Field messageId.
	 */
	private String messageId;
	
	/**
	 * Field queueEntity.
	 */
	private QueueEntity queueEntity = null;
	
	/**
	 * Creates a new instance of MessageResource
	 * 
	 * @param uriInfo
	 *            UriInfo actual uri passed by parent resource
	 * @param entityManagerData
	 *            EntityManager passed by parent resource
	 * 
	 * @param messageId
	 *            String
	 * @param isMessageInQueue
	 *            boolean
	 * @param needsPayload
	 *            boolean
	 * @param entityManagerConfig EntityManager
	 */
	public MessageResource(UriInfo uriInfo, EntityManager entityManagerData, EntityManager entityManagerConfig,
			String messageId, boolean isMessageInQueue, boolean needsPayload, QueueEntity queueEntity) {
		this.uriInfo = uriInfo;
		this.entityManagerData = entityManagerData;
		this.entityManagerConfig = entityManagerConfig;
		this.messageEntity = null;
		this.isMessageInQueue = isMessageInQueue;
		this.needsPayload = needsPayload;
		this.messageId = messageId;
		this.queueEntity = queueEntity;
		
		if(uriInfo.getQueryParameters().containsKey("type")){
			MessageTypeToViewsEnum messageType = MessageTypeToViewsEnum
					.getMessageType(uriInfo.getQueryParameters().getFirst(
							"type"));
			Query query = messageType.getFindByGuidQuery(entityManagerData, messageId);
			@SuppressWarnings("unchecked")
			List<? extends MtView> results = query.getResultList();
			if(!results.isEmpty()){
				viewEntity = results.get(0);
			}
			return;
		}
		
		if(isMessageInQueue){
			TypedQuery<EntryQueueEntity> query = entityManagerData.createNamedQuery(
					"EntryQueueEntity.findByGuid", EntryQueueEntity.class);
			List<EntryQueueEntity> results = query.setHint("eclipselink.left-join-fetch", "b.routedmessage")
			.setParameter("guid",
					messageId).getResultList();
			if (!results.isEmpty()) {
				messageQueueEntity = results.get(0);
			}
		}else{
			TypedQuery<RoutedMessageEntity> query = entityManagerData.createNamedQuery(
					"RoutedMessageEntity.findByGuid", RoutedMessageEntity.class);
			List<RoutedMessageEntity> results = query.setParameter("guid",
					messageId).getResultList();
			if (!results.isEmpty()) {
				messageEntity = results.get(0);
			}
		}
	}
	
	/**
	 * Method findDetailsMessage.
	 * @param message JSONObject
	 * @return JSONObject
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 * @throws ClassNotFoundException
	 * @throws JSONException
	 * @throws NoSuchMethodException 
	 * @throws InvocationTargetException 
	 * @throws SecurityException 
	 * @throws IllegalArgumentException 
	 */
	public JSONObject findDetailsMessage(JSONObject message)
			throws IllegalAccessException, InstantiationException,
			ClassNotFoundException, JSONException, IllegalArgumentException, SecurityException {
		
		
		String msgType = "";
		if (null == messageEntity) {
			if ( null != messageQueueEntity.getRoutedmessage()) {
				msgType = messageQueueEntity.getRoutedmessage().getMsgtype();
			}
		} else {
			if ( null != messageEntity.getMsgtype()) {
				msgType = messageEntity.getMsgtype();
			}
		}
		
		TypedQuery<String> query = entityManagerConfig.createNamedQuery(
				"MsgTypeListEntity.findByMsgType", String.class);
		List<String> results = query.setParameter("messagetype", msgType).getResultList();
		
		if (!results.isEmpty()) {
			//get table name with message details (Ex. ro.allevo.fintpws.resources.Mt103tab)
			//RoutedMessageEntity and Mt103tab(Mt202tab etc) are in the same package
			String tableNameMessageDetails = RoutedMessageEntity.class.getPackage().getName() + "." +
								StringUtils.capitalize(results.get(0).toLowerCase())
								+ "Entity";

			Object entityDetailsMessage = Class.forName(tableNameMessageDetails).newInstance();
			
			entityDetailsMessage = entityManagerData.find(
					entityDetailsMessage.getClass(),
					messageEntity.getCorrelationid());
			if( null != entityDetailsMessage){
				return ReflectionUtils.asReflectedJson(entityDetailsMessage, message);
			}
		}
		return message;
	}

	/**
	 * GET method : returns an application/json formatted message
	 * @return JSONObject the message
	 */
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public JSONObject getMessage() {
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
		if (null == messageEntity && null == messageQueueEntity && null == viewEntity) {
		logger.error(String.format(ERROR_MESSAGE_M_NOT_FOUND, messageId));
			throw new EntityNotFoundException(String.format(
					ERROR_MESSAGE_M_NOT_FOUND, messageId));
		}
		try {
			if(null != viewEntity){
				return viewEntity.toJSON();
			}
			if (null == messageEntity){
				return findDetailsMessage(MessageResource.asJson(
					messageQueueEntity,
					isMessageInQueue,
					needsPayload, uriInfo.getPath()));
			}else{
				return findDetailsMessage(MessageResource.asJson(
						messageEntity,
						isMessageInQueue,
						needsPayload, uriInfo.getPath()));
			}
		} catch (JSONException je) {
			logger.error(ERROR_MESSAGE_GET_MESSAGE + ERROR_REASON_JSON, je);
			throw new ApplicationJsonException(je, ERROR_MESSAGE_GET_MESSAGE
					+ ERROR_REASON_JSON,
					Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
		} catch (IllegalAccessException ias) {
			logger.error(ERROR_MESSAGE_GET_MESSAGE + ERROR_REASON_JSON, ias);
			throw new ApplicationJsonException(ias, ERROR_MESSAGE_GET_MESSAGE
					+ ERROR_REASON_REFLECTION_ERROR,
					Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
		} catch (InstantiationException ie) {
			logger.error(ERROR_MESSAGE_GET_MESSAGE + ERROR_REASON_JSON, ie);
			throw new ApplicationJsonException(ie, ERROR_MESSAGE_GET_MESSAGE
					+ ERROR_REASON_REFLECTION_ERROR,
					Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
		} catch (ClassNotFoundException clnfe) {
			logger.error(ERROR_MESSAGE_GET_MESSAGE + ERROR_REASON_JSON, clnfe);
			throw new ApplicationJsonException(clnfe, ERROR_MESSAGE_GET_MESSAGE
					+ ERROR_REASON_REFLECTION_ERROR,
					Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
		} catch (IllegalArgumentException e) {
			throw new ApplicationJsonException(e, ERROR_MESSAGE_GET_MESSAGE
					+ ERROR_REASON_REFLECTION_ERROR,
					Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
		} catch (SecurityException e) {
			throw new ApplicationJsonException(e, ERROR_MESSAGE_GET_MESSAGE
					+ ERROR_REASON_REFLECTION_ERROR,
					Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
		}
	}

	/**
	 * Method getEvents.
	 * 
	 * @return EventsResource
	 */
	@Path("events")
	public EventsResource getEvents() {
		if (null == this.messageEntity) {
			logger.error(String.format(ERROR_MESSAGE_M_NOT_FOUND,
					this.messageId));
			throw new EntityNotFoundException(String.format(
					ERROR_MESSAGE_M_NOT_FOUND, this.messageId));
		}
		return new EventsResource(uriInfo, entityManagerData, messageEntity);
	}

	/**
	 * PUT method : updates the message
	 * @param jsonEntity
	 *            JSONObject the message holding new values
	 * @return Response
	 */
	@PUT
	@Consumes(MediaType.APPLICATION_JSON)
	public Response updateMessage(JSONObject jsonEntity) {
		if (isMessageInQueue) {
			if (!RolesUtils.hasWriteAuthorityOnQueue(queueEntity)) {
				throw new ApplicationJsonException(new AccessDeniedException("forbidden"), "forbidden", 
						Status.FORBIDDEN.getStatusCode());
			}
		} else {
			if (!RolesUtils.hasReportsRole()) {
				throw new ApplicationJsonException(new AccessDeniedException("forbidden"), "forbidden", 
						Status.FORBIDDEN.getStatusCode());
			}
		}
		if (null == messageQueueEntity) {
			logger.error(String.format(ERROR_MESSAGE_M_NOT_FOUND, messageId));
			throw new EntityNotFoundException(String.format(
					ERROR_MESSAGE_M_NOT_FOUND, messageId));
		}
		try {
			
			if(jsonEntity.has("guid")){
				messageQueueEntity.setGuid(jsonEntity.getString("guid"));
			}
			
			if(jsonEntity.has("batchid")){
				messageQueueEntity.setBatchid(jsonEntity.optString("batchid"));
			}
			if(jsonEntity.has("correlationid")){
				messageQueueEntity.setCorrelationid(jsonEntity.optString("correlationid"));
			}
			if(jsonEntity.has("requestorservice")){
				messageQueueEntity.setRequestorservice(jsonEntity
					.getString("requestorservice"));
			}
			if(jsonEntity.has("responderservice")){
				messageQueueEntity.setResponderservice(jsonEntity
					.optString("responderservice"));
			}
			if(jsonEntity.has("requesttype")){
				messageQueueEntity
					.setRequesttype(jsonEntity.getString("requesttype"));
			}
			if(jsonEntity.has("feedback")){
				messageQueueEntity.setFeedback(jsonEntity.optString("feedback"));
			}
			if(jsonEntity.has("sessionid")){
				messageQueueEntity.setSessionid(jsonEntity.optString("sessionid"));
			}
			if(jsonEntity.has("priority")){
				messageQueueEntity.setPriority(new BigDecimal(jsonEntity
					.getInt("priority")));
			}
			if(jsonEntity.has("holdstatus")){
				messageQueueEntity.setHoldstatus(new BigDecimal(jsonEntity
					.getInt("holdstatus")));
			}
			if(jsonEntity.has("sequence")){
				messageQueueEntity.setSequence(new BigDecimal(jsonEntity
					.getInt("sequence")));
			}
			if(jsonEntity.has("payload")){
				messageQueueEntity.setPayload(jsonEntity.getString("payload"));
			}
			if(jsonEntity.has("queuename")){
				messageQueueEntity.setQueuename(jsonEntity.getString("queuename"));
			}
			
			entityManagerData.getTransaction().begin();
			entityManagerData.merge(messageQueueEntity);
			entityManagerData.getTransaction().commit();
		} catch (JSONException je) {
			logger.error(ERROR_MESSAGE_PUT_MESSAGE + ERROR_REASON_JSON, je);
			throw new ApplicationJsonException(je, ERROR_MESSAGE_PUT_MESSAGE
					+ ERROR_REASON_JSON,
					Response.Status.BAD_REQUEST.getStatusCode());
		} catch (NumberFormatException nfe) {
			logger.error(
					ERROR_MESSAGE_PUT_MESSAGE + ERROR_REASON_NUMBER_FORMAT, nfe);
			throw new ApplicationJsonException(nfe, ERROR_MESSAGE_PUT_MESSAGE
					+ ERROR_REASON_NUMBER_FORMAT,
					Response.Status.BAD_REQUEST.getStatusCode());
		} catch (RollbackException re) {
			// traverse the cause to find a possible constraint violation
			ApplicationJsonException.handleSQLException(re, ERROR_MESSAGE_PUT_MESSAGE, logger);
			
			// log and rethrow the original error
			logger.error(ERROR_MESSAGE_PUT_MESSAGE + ERROR_REASON_ROLLBACK, re);
			throw re;
		} finally {
			// Tomcat does not support container managed transactions
			if (null != entityManagerData) {
				entityManagerData.close();
			}
		}
		return JsonResponseWrapper.getResponse(Response.Status.OK,
				"message updated");
	}

	/**
	 * DELETE method : deletes the message
	 * @return Response
	 */
	@DELETE
	public Response deleteMessage() {
		if (isMessageInQueue) {
			if (!RolesUtils.hasWriteAuthorityOnQueue(queueEntity)) {
				throw new ApplicationJsonException(new AccessDeniedException("forbidden"), "forbidden", 
						Status.FORBIDDEN.getStatusCode());
			}
		} else {
			if (!RolesUtils.hasReportsRole()) {
				throw new ApplicationJsonException(new AccessDeniedException("forbidden"), "forbidden", 
						Status.FORBIDDEN.getStatusCode());
			}
		}
		if (null == messageEntity && null == messageQueueEntity ) {
			logger.error(String.format(ERROR_MESSAGE_M_NOT_FOUND, messageId));
			throw new EntityNotFoundException(String.format(
					ERROR_MESSAGE_M_NOT_FOUND, messageId));
		}
		try {
			entityManagerData.getTransaction().begin();
			entityManagerData.remove((null == messageEntity)?messageQueueEntity:messageEntity);
			entityManagerData.getTransaction().commit();
		} finally {
			if (null != entityManagerData) {
				entityManagerData.close();
			}
		}
		return JsonResponseWrapper.getResponse(Response.Status.OK,
				"message deleted");
	}

	/**
	 * Returns the resource formatted as json
	 * 
	 * @param messageEntity
	 *            BusinessMessage
	 * @param path
	 *            String
	 * @param isMessageInQueue
	 *            boolean
	 * @param needsPayload
	 *            boolean
	 * @return JSONObject * @throws JSONException
	 */
	public static JSONObject asJson(RoutedMessageEntity messageEntity,
			boolean isMessageInQueue, boolean needsPayload, String path)
			throws JSONException {
		JSONObject messageAsJson = ApiResource.getMetaResource(path,
				MessageResource.class);
		messageAsJson.put("guid", messageEntity.getGuid())
				.put("batchid", messageEntity.getEntryQueue().getBatchid())
				.put("correlationid",
							messageEntity.getEntryQueue().getCorrelationid())
				.put("responderservice",
							messageEntity.getEntryQueue().getResponderservice())
				.put("requestorservice",
							messageEntity.getEntryQueue().getRequestorservice())
				.put("requesttype",
							messageEntity.getEntryQueue().getRequesttype())
				.put("feedback",
							messageEntity.getEntryQueue().getFeedback())
				.put("sessionid",
							messageEntity.getEntryQueue().getSessionid())
				.put("priority",
							messageEntity.getEntryQueue().getPriority())
				.put("holdstatus",
							messageEntity.getEntryQueue().getHoldstatus())
				.put("sequence",
							messageEntity.getEntryQueue().getSequence())
							
				.put("msgtype", messageEntity.getMsgtype())
				.put("sender", messageEntity.getSender())
				.put("receiver", messageEntity.getReceiver())
				.put("trn", messageEntity.getTrn())
				.put("currentqueue", messageEntity.getCurrentqueue())
				.put("amount", messageEntity.getAmount());
				if (needsPayload){
					messageAsJson.put("payload", messageEntity.getEntryQueue()
							.getPayload());
				}	
		ResourcesUtils.createLink(messageAsJson, path + "/events", "events");
		
		return messageAsJson;
	}
	
	public static JSONObject asJson(EntryQueueEntity messageEntity,
			boolean isMessageInQueue, boolean needsPayload, String path)
			throws JSONException {
		JSONObject messageAsJson = ApiResource.getMetaResource(path,
				MessageResource.class);
		messageAsJson.put("guid", messageEntity.getGuid())
				.put("batchid", messageEntity.getBatchid())
				.put("correlationid",
							messageEntity.getCorrelationid())
				.put("responderservice",
							messageEntity.getResponderservice())
				.put("requestorservice",
							messageEntity.getRequestorservice())
				.put("requesttype",
							messageEntity.getRequesttype())
				.put("feedback",
							messageEntity.getFeedback())
				.put("sessionid",
							messageEntity.getSessionid())
				.put("priority",
							messageEntity.getPriority())
				.put("holdstatus",
							messageEntity.getHoldstatus())
				.put("sequence",
							messageEntity.getSequence())
				.put("queuename",
							messageEntity.getQueuename());
				if( null != messageEntity.getRoutedmessage()){
					messageAsJson
						.put("msgtype", messageEntity.getRoutedmessage().getMsgtype())
						.put("sender", messageEntity.getRoutedmessage().getSender())
						.put("receiver", messageEntity.getRoutedmessage().getReceiver())
						.put("trn", messageEntity.getRoutedmessage().getTrn())
						.put("currentqueue",
								messageEntity.getRoutedmessage().getCurrentqueue())
						.put("amount", messageEntity.getRoutedmessage().getAmount());
				}
				if (needsPayload){
					messageAsJson.put("payload", messageEntity.getPayload());
				}	
		ResourcesUtils.createLink(messageAsJson, path + "/events", "events");
		
		return messageAsJson;
	}

	/**
	 * Method toString.
	 * 
	 * @return String
	 */
	public String toString() {
		return messageEntity.getGuid();
	}
}
