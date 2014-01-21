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

import java.text.ParseException;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.ws.rs.core.UriInfo;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import ro.allevo.fintpws.model.EntryQueueEntity;
import ro.allevo.fintpws.model.QueueEntity;
import ro.allevo.fintpws.model.RoutedMessageEntity;
import ro.allevo.fintpws.util.ResourcesUtils;

/**
 * Resource class implementing /messages/{id} path methods.
 * 
 * @author costi
 * @version $Revision: 1.0 $
 */
public class GroupMessageResource {
	/**
	 * Field logger.
	 */
	private static Logger logger = LogManager.getLogger(GroupMessageResource.class
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
	
	/**
	 * Field messageQueueEntity.
	 */
	private EntryQueueEntity messageQueueEntity;

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
	public GroupMessageResource(UriInfo uriInfo, EntityManager entityManagerData, EntityManager entityManagerConfig,
			String messageId, boolean isMessageInQueue, boolean needsPayload, QueueEntity queueEntity) {
		this.uriInfo = uriInfo;
		this.entityManagerData = entityManagerData;
		this.entityManagerConfig = entityManagerConfig;
		this.isMessageInQueue = isMessageInQueue;
		this.needsPayload = needsPayload;
		this.messageId = messageId;
		this.queueEntity = queueEntity;
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
		}
	}
	
	/**
	 * Returns the resource formatted as json
	 * 
	 * @param messageEntity2
	 *            BusinessMessage
	 * @param path
	 *            String
	 * @param isMessageInQueue
	 *            boolean
	 * @param needsPayload
	 *            boolean
	 * @return JSONObject * @throws JSONException
	 */

	public static JSONObject asJson(Object[] messageEntity,
			boolean isMessageInQueue, boolean needsPayload, String path)
			throws JSONException, ParseException {
		JSONObject messageAsJson = ApiResource.getMetaResource(path,
				GroupMessageResource.class);
		messageAsJson.put("receiver", messageEntity[0]);
		messageAsJson.put("valuedate", ResourcesUtils.getISODate(((Date)messageEntity[1])));
		return messageAsJson;
	}


}
