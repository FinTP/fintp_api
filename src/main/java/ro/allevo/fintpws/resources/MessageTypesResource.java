package ro.allevo.fintpws.resources;

import java.util.List;

import javax.persistence.EntityManager;
import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.springframework.security.access.AccessDeniedException;

import ro.allevo.fintpws.exceptions.ApplicationJsonException;
import ro.allevo.fintpws.model.EntryQueueEntity;
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
	static final String ERROR_MESSAGE_GET_MESSAGES = "Error returning messages : ";
	/**
	 * Field ERROR_REASON_JSON. (value is ""json"")
	 */
	static final String ERROR_REASON_JSON = "json";

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
	public MessageTypesResource(UriInfo uriInfo,
			EntityManager entityManagerData, EntityManager entityManagerConfig,
			QueueEntity queueEntity) {
		super(uriInfo, entityManagerData.createNamedQuery(
				"EntryQueueEntity.findDistinctMessagesQueue",
				EntryQueueEntity.class).setParameter("queuename",
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
		List<?> items = getItems();

		if (items.size() > 0) {
			messagesAsJson.put("messagetypes", (List<String>) items);
		}

		return messagesAsJson;
	}
}
