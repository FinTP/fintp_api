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
import org.springframework.security.access.AccessDeniedException;


import ro.allevo.fintpws.exceptions.ApplicationJsonException;
import ro.allevo.fintpws.model.EntryQueueEntity;
import ro.allevo.fintpws.model.MsgTypeListEntity;
import ro.allevo.fintpws.model.QueueEntity;
import ro.allevo.fintpws.security.RolesUtils;

/**
 * @author costi
 * @version $Revision: 1.0 $
 */
public class MessageTypesResource extends PagedCollection {
	/**
	 * Field logger.
	 */
	private static Logger logger = LogManager
			.getLogger(MessageTypesResource.class.getName());

	/**
	 * Field ERROR_MESSAGE_GET_MESSAGES. (value is ""Error returning messages :
	 * "")
	 */
	static final String ERROR_MESSAGE_GET_MESSAGETYPES = "Error returning messages : ";
	/**
	 * Field ERROR_REASON_JSON. (value is ""json"")
	 */
	static final String ERROR_REASON_JSON = "json";

	private QueueEntity queueEntity = null;

	private MsgTypeListEntity msgEntity = null;


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
	public MessageTypesResource(UriInfo uriInfo,
			EntityManager entityManagerData, EntityManager entityManagerConfig,
			QueueEntity queueEntity) {
		super(uriInfo, entityManagerData.createNamedQuery(
				"EntryQueueEntity.findDistinctMessagesQueue",
				MsgTypeListEntity.class).setParameter("queuename",
						queueEntity.getName()), entityManagerData.createNamedQuery(
								"EntryQueueEntity.findTotalDistinctMessagesQueue", Long.class)
								.setParameter("queuename", queueEntity.getName()));
		this.queueEntity = queueEntity;
	}

	/**
	 * GET method : returns an application/json formatted list of messages
	 * 
	 * @return JSONObject The list of messages
	 */
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public JSONObject getMessagesAsJson() {
		// authorization
		if (!RolesUtils.hasReadAuthorityOnQueue(queueEntity)) {
			throw new AccessDeniedException("forbidden");
		}

		try {
			getPage();
			return asJson();
			//return MessageTypesResource.asJson(msgtyoelistentity, uriInfo.getPath());
		} catch (JSONException je) {
			logger.error(ERROR_MESSAGE_GET_MESSAGETYPES + ERROR_REASON_JSON, je);
			throw new ApplicationJsonException(je, ERROR_MESSAGE_GET_MESSAGETYPES
					+ ERROR_REASON_JSON,
					Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
		}
	}

	/**
	 * Returns the resource formatted as json
	 * 
	 * @return JSONObject * @throws JSONException
	 */
	@SuppressWarnings("unchecked")
	public JSONObject asJson() throws JSONException {
		final JSONObject messageTypesAsJson = super.asJson();
		final JSONArray jsonArray = new JSONArray();
		// fill data

		List<MsgTypeListEntity> items = (List<MsgTypeListEntity>) getItems();
		/*System.out.println("Message types: " + items);*/

		if (items.size() > 0) {

			for(MsgTypeListEntity messagetype : items){
				JSONObject msgtypeasJson = super.asJson();
				msgtypeasJson.put("messagetype" , messagetype.getMessagetype());
				msgtypeasJson.put("friendlyname" , messagetype.getFriendlyname());
				msgtypeasJson.put("storage" , messagetype.getStorage());
				msgtypeasJson.put("businessarea" , messagetype.getBusinessarea());
				msgtypeasJson.put("reportingstorage" , messagetype.getReportingstorage());
				msgtypeasJson.put("parentmsgtype" , messagetype.getParentmsgtype());

				jsonArray.put(msgtypeasJson);
			}

			//mark messages without type as undefined 
			if(items.contains(null)){
				MsgTypeListEntity nullEntity = new MsgTypeListEntity();
				nullEntity.setMessagetype("undefined");
				items.set(items.indexOf(null), nullEntity);
				}
			messageTypesAsJson.put("messagetypes", jsonArray);
		}else{
			messageTypesAsJson.put("messagetypes", new JSONArray());
		}

		return messageTypesAsJson;
	}

}
