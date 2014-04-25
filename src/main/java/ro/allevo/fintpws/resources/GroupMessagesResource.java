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
import java.sql.SQLIntegrityConstraintViolationException;
import java.text.ParseException;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.RollbackException;
import javax.persistence.TypedQuery;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriBuilderException;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.core.Response.Status;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.springframework.security.access.AccessDeniedException;

import ro.allevo.fintpws.exceptions.ApplicationJsonException;
import ro.allevo.fintpws.model.QueueEntity;
import ro.allevo.fintpws.security.RolesUtils;

/**
 * @author costi
 * @version $Revision: 1.0 $
 */
public class GroupMessagesResource extends PagedCollection {
	/**
	 * Field logger.
	 */
	private static Logger logger = LogManager.getLogger(GroupMessagesResource.class
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
	 * @param entityManagerConfig EntityManager
	 */
	public GroupMessagesResource(UriInfo uriInfo, EntityManager entityManagerData, EntityManager entityManagerConfig,
			QueueEntity queueEntity) {
		super(uriInfo, entityManagerData.createNamedQuery(
				"MtFitoficstmrcdttrfView.findAllGroups", Object[].class),
				entityManagerData.createNamedQuery(
						"MtFitoficstmrcdttrfView.findTotalGroups", Long.class));
		
		this.uriInfo = uriInfo;
		this.entityManagerData = entityManagerData;
		this.entityManagerConfig = entityManagerConfig;
		this.queueEntity = queueEntity;
	}	

	
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public JSONObject getMessagesAsJson() {logger.debug(queueEntity);
		//authorization
		if(!RolesUtils.hasReadAuthorityOnQueue(queueEntity)){
			throw new ApplicationJsonException(new AccessDeniedException("forbidden"), "forbidden", 
					Status.FORBIDDEN.getStatusCode()) ;
		}
		
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
	 * Returns the resource formatted as json
	 * 
	 * @return JSONObject * @throws JSONException
	 */
	@SuppressWarnings("unchecked")
	public JSONObject asJson() throws JSONException {
		JSONObject messagesAsJson = super.asJson();

		// fill data
		JSONArray queuesArray = new JSONArray();
		List<?> items = getItems();
		if (items.size() > 0) {
			if (null != queueEntity){
				for (Object[] messageEntity : (List<Object[]>) items) {
					try {
						queuesArray.put(GroupMessageResource.asJson(
								messageEntity,
								isMessageInQueue,
								needsPayload,UriBuilder.fromPath(getUriInfo().getPath())
								.path(messageEntity[0].toString()).build()
								.getPath()));
					} catch (IllegalArgumentException | UriBuilderException
							| ParseException e) {
						// TODO Auto-generated catch block
					}
				}
			}
		}
		messagesAsJson.put("groupmessages", queuesArray);
		return messagesAsJson;
	}
}
