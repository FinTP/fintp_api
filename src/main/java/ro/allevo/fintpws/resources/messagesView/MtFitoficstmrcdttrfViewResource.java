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

import java.text.ParseException;
import java.util.List;

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
//import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.springframework.security.access.AccessDeniedException;

import ro.allevo.fintpws.exceptions.ApplicationJsonException;
//import ro.allevo.fintpws.model.QMovePrivMapEntity;
import ro.allevo.fintpws.model.messagesViews.MtFitoficstmrcdttrfView;
import ro.allevo.fintpws.resources.ApiResource;
import ro.allevo.fintpws.security.RolesUtils;
import ro.allevo.fintpws.util.ResourcesUtils;

/**
 * Resource class implementing /queues/{name} path methods.
 * 
 * @author horia
 * @version $Revision: 1.0 $
 */
public class MtFitoficstmrcdttrfViewResource {
	/**
	 * Field logger.
	 */
	private static Logger logger = LogManager.getLogger(MtFitoficstmrcdttrfViewResource.class
			.getName());

	/**
	 * Field ERROR_MESSAGE_GET_QUEUE. (value is ""Error returning queue : "")
	 */
	static final String ERROR_MESSAGE_GET_QUEUE = "Error returning queue : ";
	/**
	 * Field ERROR_MESSAGE_PUT_QUEUE. (value is ""Error updating queue : "")
	 */
	static final String ERROR_MESSAGE_PUT_QUEUE = "Error updating queue : ";
	/**
	 * Field ERROR_MESSAGE_Q_NOT_FOUND. (value is ""Queue with name [%s] not
	 * found"")
	 */
	static final String ERROR_MESSAGE_Q_NOT_FOUND = "Queue with name [%s] not found";
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

	// actual uri info provided by parent resource
	/**
	 * Field uriInfo.
	 */
	private UriInfo uriInfo;
	/**
	 * Field entityManagerConfig.
	 */
	private EntityManager entityManagerConfig;
	/**
	 * Field entityManagerData.
	 */
	private EntityManager entityManagerData;

	/**
	 * the JPA entity
	 */
	private MtFitoficstmrcdttrfView mtFitoficstmrcdttrfView;

	/**
	 * queue name
	 */
	private String queueName;

	/**
	 * Creates a new instance of MtFitoficstmrcdttrfViewResource
	 * 
	 * @param uriInfo
	 *            UriInfo actual uri passed by parent resource
	 * 
	 * @param queueName
	 *            String Queue name
	 * @param entityManagerConfig
	 *            EntityManager
	 * @param entityManagerData
	 *            EntityManager
	 */
	public MtFitoficstmrcdttrfViewResource(UriInfo uriInfo, EntityManager entityManagerConfig,
			EntityManager entityManagerData, String queueName) {
		this.uriInfo = uriInfo;
		this.entityManagerConfig = entityManagerConfig;
		this.entityManagerData = entityManagerData;
		this.queueName = queueName;
		this.mtFitoficstmrcdttrfView = null;

		mtFitoficstmrcdttrfView = MtFitoficstmrcdttrfViewResource.findByName(entityManagerConfig, queueName);
	}

	/**
	 * Method findByName. Looks for a MtFitoficstmrcdttrfView based on it's name.
	 * 
	 * @param entityManager
	 *            EntityManager
	 * @param name
	 *            String
	 * @return MtFitoficstmrcdttrfView
	 */
	public static MtFitoficstmrcdttrfView findByName(EntityManager entityManager,
			String name) {
		// entityManager.find is much faster than a query, but name is char (
		// not varchar ) and can't be used as ID
		// mtFitoficstmrcdttrfView = entityManager.find(MtFitoficstmrcdttrfView.class, queueName);
		// TODO : change datatype to varchar and mark name as @ID in the entity
		// Also, not using getSingleResult because unchecked exception
		// NoResultException can actually be recovered from
		TypedQuery<MtFitoficstmrcdttrfView> query = entityManager.createNamedQuery(
				"MtFitoficstmrcdttrfView.findByName", MtFitoficstmrcdttrfView.class);
		List<MtFitoficstmrcdttrfView> results = query.setParameter("name", name)
				.getResultList();
		if (!results.isEmpty()) {
			return results.get(0);
		}
		return null;
	}
	
	
	/**
	 * GET method : returns an application/json formatted queue
	 * 
	 * @return JSONObject the queue
	 */
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public JSONObject getQueue() {
		if(!RolesUtils.hasUserOrAdministratorRole()){
			throw new AccessDeniedException("Access denied");
		}
		if (null == mtFitoficstmrcdttrfView) {
			logger.error(String.format(ERROR_MESSAGE_Q_NOT_FOUND, queueName));
			throw new EntityNotFoundException(String.format(
					ERROR_MESSAGE_Q_NOT_FOUND, queueName));
		}
		try {
			return MtFitoficstmrcdttrfViewResource.asJson(mtFitoficstmrcdttrfView, uriInfo.getPath());
		} catch (JSONException je) {
			logger.error(ERROR_MESSAGE_GET_QUEUE + ERROR_REASON_JSON, je);
			throw new ApplicationJsonException(je, ERROR_MESSAGE_GET_QUEUE
					+ ERROR_REASON_JSON,
					Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}



	/**
	 * Returns the resource formatted as json
	 * 
	 * @param mtFitoficstmrcdttrfView
	 *            MtFitoficstmrcdttrfView
	 * @param path
	 *            String
	 * @return JSONObject
	 * @throws JSONException
	 * @throws ParseException 
	 */
	public static JSONObject asJson(MtFitoficstmrcdttrfView mtFitoficstmrcdttrfView, String path)
			throws JSONException, ParseException {
		JSONObject queueAsJson = ApiResource.getMetaResource(path,
				MtFitoficstmrcdttrfViewResource.class);

		// fill data
		queueAsJson.put("guid", mtFitoficstmrcdttrfView.getGuid())
		.put("valuedate", ResourcesUtils.getISODate(mtFitoficstmrcdttrfView.getValuedate()))
		.put("sender", mtFitoficstmrcdttrfView.getSender())
		.put("receiver", mtFitoficstmrcdttrfView.getReceiver())
		.put("trn", mtFitoficstmrcdttrfView.getTrn())
		.put("amount", mtFitoficstmrcdttrfView.getAmount())
		.put("currency", mtFitoficstmrcdttrfView.getCurrency());
				
		
		return queueAsJson;
	}
	

	/**
	 * Method toString.
	 * 
	 * @return String
	 */
	public String toString() {
		return mtFitoficstmrcdttrfView.getGuid();
	}
}
