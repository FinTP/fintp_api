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

import javax.persistence.EntityManager;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import ro.allevo.fintpws.exceptions.ApplicationJsonException;
import ro.allevo.fintpws.model.RoutingJobEntity;
import ro.allevo.fintpws.model.UserEntity;
import ro.allevo.fintpws.util.JsonResponseWrapper;

public class RouteActionResource {

	/**
	 * Field logger
	 */
	private static final Logger logger = LogManager
			.getLogger(BatchesResource.class.getName());
	/**
	 * Field MESSAGE_POST_ROUTE. (value is ""Action requested"")
	 */
	static final String MESSAGE_POST_ACTION = "Action requested";

	/**
	 * Field ERROR_MESSAGE_POST_ROUTE. (value is ""Error requesting routing
	 * action : "")
	 */
	static final String ERROR_MESSAGE_POST_ACTION = "Error requesting route action: ";
	/**
	 * Field ERROR_REASON_JSON. (value is ""json"")
	 */
	static final String ERROR_REASON_JSON = "json";
	/**
	 * Field ERROR_REASON_INVALID_VALUE . (value is ""invalid field value"")
	 */
	static final String ERROR_REASON_INVALID_VALUE = "invalid field value";
	/*
	 * Constatns (inserted fields in batchjobs table
	 */
	private static final BigDecimal STATUS = new BigDecimal(0);
	private static final BigDecimal BACKOUT = new BigDecimal(0);
	private static final String FEEDBACK_FIELD = "FTP09";

	
	/**
	 * Routing action type enumeration
	 * (MOVE, REJECT, AUTHORIZE)
	 * @author remus
	 *
	 */
	private enum ActionType {
		MOVE(new BigDecimal(50)), REJECT(new BigDecimal(70)), AUTHORIZE(new BigDecimal(80));
		public final BigDecimal priority;

		private ActionType(BigDecimal priority) {
			this.priority = priority;
		}

		/**
		 * Ruturns routing job 'function' field
		 * @param destinationQueue
		 * @return
		 */
		public String getFunction(String destinationQueue) {
			switch (this) {
			
			case MOVE:
				return "F=Unhold, F=Dispose, P=Destination(" + destinationQueue
						+ ")";
			case REJECT:
				return "F=Unhold, F=Complete, P=Feedback(" + FEEDBACK_FIELD
						+ ")";
			case AUTHORIZE:
				return "F=Route, F=Unhold";
			default:
				return null;
			}
		}

		/**
		 * Returns ActionType depending on given argument
		 * @param name
		 * @return
		 */
		public static ActionType getActionByName(String name) {
			for (ActionType enumVal : ActionType.values()) {
				if (name.compareToIgnoreCase(enumVal.name()) == 0) {
					return enumVal;
				}
			}
			return null;
		}

	}

	/**
	 * Class Default Constructor
	 */
	public RouteActionResource() {

	}
	
	/**
	 * POST method: creates a new routing job
	 * 
	 * @param requestEntity
	 * 		JSONObject the arguments required in order to perform the desired request
	 * @return Response
	 * 		HTTP status and a message describing the result
	 */

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	public Response createRouteAction(JSONObject requestEntity) {
		try {
			final EntityManager entityManager = ApiResource.entitiyManagerData;
			JSONArray messageIds = requestEntity.getJSONArray("messages");
			UserEntity user = UserResource.findByUsername(
					ApiResource.entitiyManagerConfig,
					requestEntity.getString("user"));
			for (int i = 0; i < messageIds.length(); i++) {
				RoutingJobEntity routingJob = new RoutingJobEntity();
				String messageId = messageIds.get(i).toString();
				ActionType action = ActionType.getActionByName(requestEntity
						.getString("action"));
				if (action == null) {
					logger.error(ERROR_MESSAGE_POST_ACTION
							+ ERROR_REASON_INVALID_VALUE);
					throw new ApplicationJsonException(null,
							ERROR_MESSAGE_POST_ACTION
									+ ERROR_REASON_INVALID_VALUE,
							Response.Status.BAD_REQUEST.getStatusCode());

				}
				routingJob.setGuid(messageId);
				routingJob.setStatus(STATUS);
				routingJob.setBackout(BACKOUT);
				routingJob.setPriority(action.priority);
				routingJob.setRoutingpoint(requestEntity.optString("source"));
				routingJob.setFunction(action.getFunction(requestEntity
						.optString("destination", null)));
				if(user != null){
					routingJob.setUserid(user.getUserid());
				}
				System.out.println("JSON : " + requestEntity);
				entityManager.getTransaction().begin();
				entityManager.persist(routingJob);
				entityManager.getTransaction().commit();
			}
		} catch (JSONException e) {
			logger.error(ERROR_MESSAGE_POST_ACTION + ERROR_REASON_JSON, e);
			throw new ApplicationJsonException(e, ERROR_MESSAGE_POST_ACTION
					+ ERROR_REASON_JSON,
					Response.Status.BAD_REQUEST.getStatusCode());
		}
		return JsonResponseWrapper.getResponse(Response.Status.CREATED,
				MESSAGE_POST_ACTION);
	}

}
