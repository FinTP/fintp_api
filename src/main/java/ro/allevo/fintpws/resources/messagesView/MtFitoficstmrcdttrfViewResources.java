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

package ro.allevo.fintpws.resources.messagesView;

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
import javax.ws.rs.core.UriBuilderException;
import javax.ws.rs.core.UriInfo;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.springframework.security.access.AccessDeniedException;

import ro.allevo.fintpws.exceptions.ApplicationJsonException;



import ro.allevo.fintpws.model.QueueEntity;
import ro.allevo.fintpws.model.messagesViews.MtFitoficstmrcdttrfView;
import ro.allevo.fintpws.resources.PagedCollection;
import ro.allevo.fintpws.security.RolesUtils;
import ro.allevo.fintpws.util.JsonResponseWrapper;
import ro.allevo.fintpws.util.ResourcesUtils;

/**
 * Resource class implementing /queues path methods and acting as /queues/{name}
 * sub-resource locator to {@link QueueResource}.
 * 
 * @author horia
 * @version $Revision: 1.0 $
 */
public class MtFitoficstmrcdttrfViewResources extends PagedCollection {
	/**
	 * Field logger.
	 */
	private static Logger logger = LogManager.getLogger(MtFitoficstmrcdttrfViewResources.class
			.getName());

	/**
	 * Field ERROR_MESSAGE_GET_QUEUES. (value is ""Error returning queues : "")
	 */
	static final String ERROR_MESSAGE_GET_QUEUES = "Error returning queues : ";
	/**
	 * Field ERROR_MESSAGE_POST_QUEUES. (value is ""Error creating queue : "")
	 */
	static final String ERROR_MESSAGE_POST_QUEUES = "Error creating queue : ";

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
	 * Field entityManagerConfig.
	 */
	private EntityManager entityManagerConfig;
	/**
	 * Field entityManagerData.
	 */
	private EntityManager entityManagerData;

	/**
	 * Creates a new instance of QueuesResource
	 * 
	 * @param uriInfo
	 *            UriInfo
	 * 
	 * @param entityManagerConfig
	 *            EntityManager
	 * @param entityManagerData
	 *            EntityManager
	 */
	public MtFitoficstmrcdttrfViewResources(UriInfo uriInfo, EntityManager entityManagerConfig,
			EntityManager entityManagerData) {
		super(uriInfo, entityManagerConfig.createNamedQuery(
				"MtFitoficstmrcdttrfView.findAll", MtFitoficstmrcdttrfView.class), entityManagerConfig
				.createNamedQuery("MtFitoficstmrcdttrfView.findTotal", Long.class));
		
		if(ResourcesUtils.hasSortOrFilter(uriInfo)){
			this.setItemsQuery(ResourcesUtils.getTypedQuery(uriInfo, entityManagerConfig, 
					MtFitoficstmrcdttrfView.class, null, null));
			this.setTotalQuery(ResourcesUtils.getCountTypedQuery(uriInfo, entityManagerConfig, 
					MtFitoficstmrcdttrfView.class, null, null));
		}

		this.entityManagerConfig = entityManagerConfig;
		this.entityManagerData = entityManagerData;
	}

	/**
	 * Returns a queue sub-resource named queueName
	 * 
	 * @param queueName
	 *            String Name of the queue as last element in the path
	 * @return QueueResource The queue sub-resource
	 */
	@Path("{name}")
	public MtFitoficstmrcdttrfViewResource getQueue(@PathParam("name") String queueName) {
		return new MtFitoficstmrcdttrfViewResource(getUriInfo(), entityManagerConfig,
				entityManagerData, queueName);
	}

	/**
	 * GET method : returns an application/json formatted list of queues
	 * 
	 * @return JSONObject The list of queues
	 */
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public JSONObject getQueuesAsJson() {
		if(!RolesUtils.hasUserOrAdministratorRole()){
			throw new AccessDeniedException("Access denied");
		}
		try {
			getPage();
			return asJson();
		} catch (JSONException je) {
			logger.error(ERROR_MESSAGE_GET_QUEUES + ERROR_REASON_JSON, je);
			throw new ApplicationJsonException(je, ERROR_MESSAGE_GET_QUEUES
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
		JSONObject queuesAsJson = super.asJson();

		// fill data
		JSONArray queuesArray = new JSONArray();
		List<?> items = getItems();

		if (items.size() > 0) {
			for (MtFitoficstmrcdttrfView mtFitoficstmrcdttrfView : (List<MtFitoficstmrcdttrfView>) items) {
				try {
					queuesArray
							.put(MtFitoficstmrcdttrfViewResource.asJson(mtFitoficstmrcdttrfView,
									UriBuilder.fromPath(getUriInfo().getPath())
											.path(mtFitoficstmrcdttrfView.getGuid()).build()
											.getPath()));
				} catch (IllegalArgumentException | UriBuilderException
						| ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		queuesAsJson.put("queues", queuesArray);
		return queuesAsJson;
	}
}
