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

/**
 * 
 */
package ro.allevo.fintpws.resources;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import javax.persistence.EntityManager;
import javax.persistence.EntityNotFoundException;
import javax.persistence.RollbackException;
import javax.persistence.TypedQuery;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import ro.allevo.fintpws.exceptions.ApplicationJsonException;
import ro.allevo.fintpws.model.TimeLimitEntity;
import ro.allevo.fintpws.util.JsonResponseWrapper;
import ro.allevo.fintpws.util.ResourcesUtils;

/**
 * @author remus
 * @version $Revision: 1.0 $
 */
public class TimeLimitResource {

	/**
	 * Field logger
	 */
	private static final Logger logger = LogManager
			.getLogger(TimeLimitResource.class);

	/**
	 * Field ERROR_MESSAGE_GET_TIME_LIMIT. (value is ""Error returning time
	 * limit : "")
	 */
	static final String ERROR_MESSAGE_GET_TIME_LIMIT = "Error returning time limit : ";
	/**
	 * Field ERROR_MESSAGE_PUT_TIME_LIMIT. (value is ""Error updating time limit
	 * : "")
	 */
	static final String ERROR_MESSAGE_PUT_TIME_LIMIT = "Error updating time limit : ";
	/**
	 * Field ERROR_MESSAGE_DELETE_TIME_LIMIT. (value is ""Error deleting time limit
	 * : "")
	 */
	static final String ERROR_MESSAGE_DELETE_TIME_LIMIT = "Error deleting time limit : ";
	/**
	 * Field ERROR_MESSAGE_TIME_LIMIT_NOT_FOUND. (value is ""Time limit with
	 * name [%s] not found"")
	 */
	static final String ERROR_MESSAGE_TIME_LIMIT_NOT_FOUND = "Time limit with limitname [%s] not found";
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
	 * Field ERROR_REASON_PARSE. (value is ""parse"")
	 */
	static final String ERROR_REASON_PARSE = "parse";

	/**
	 * Field uriInfo
	 */
	private UriInfo uriInfo;
	/**
	 * Field entityManagerConfig.
	 */
	private EntityManager entityManagerConfig;

	/**
	 * the JPA entity
	 */
	private TimeLimitEntity timeLimitEntity;

	/**
	 * default constructor
	 */
	public TimeLimitResource() {

	}

	/**
	 * guid
	 */
	private String guid;

	/**
	 * Constructor for TimeLimitResource.
	 * 
	 * @param uriInfo
	 *            UriInfo
	 * @param entityManagerConfig
	 *            EntityManager
	 * @param guid
	 *            String
	 */
	public TimeLimitResource(UriInfo uriInfo,
			EntityManager entityManagerConfig, String limitname) {
		this.uriInfo = uriInfo;
		this.entityManagerConfig = entityManagerConfig;
		timeLimitEntity = findByName(entityManagerConfig, limitname);
	}

	/**
	 * 
	 * @param entityManager
	 *            EntityManager
	 * @param guid
	 *            String
	 * @return TimeLimitEntity
	 */

	public static TimeLimitEntity findByName(EntityManager entityManager,
			String name) {

		final TypedQuery<TimeLimitEntity> query = entityManager
				.createNamedQuery("TimeLimitEntity.findByName",
						TimeLimitEntity.class);

		final java.util.List<TimeLimitEntity> results = query.setParameter(
				"limitname", name).getResultList();
		if (!results.isEmpty()) {
			return results.get(0);
		}
		return null;
	}

	/**
	 * GET Method : returns an application/json formatted time limit
	 * 
	 * @return JSONObject the time limit
	 */

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public JSONObject getTimeLimit() {

		if (null == timeLimitEntity) {
			logger.error(String
					.format(ERROR_MESSAGE_TIME_LIMIT_NOT_FOUND, guid));
			throw new EntityNotFoundException(String.format(
					ERROR_MESSAGE_TIME_LIMIT_NOT_FOUND, guid));
		}
		try {
			return TimeLimitResource.asJson(timeLimitEntity, uriInfo.getPath());
		} catch (JSONException je) {
			logger.error(ERROR_MESSAGE_GET_TIME_LIMIT + ERROR_REASON_JSON, je);
			throw new ApplicationJsonException(je, ERROR_MESSAGE_GET_TIME_LIMIT
					+ ERROR_REASON_JSON,
					Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
		}
	}

	/**
	 * PUT method: updates the time limit
	 * 
	 * @param jsonEntity
	 *            JSONObject the time limit holding new values
	 * @return Response
	 */
	@PUT
	@Consumes(MediaType.APPLICATION_JSON)
	public Response updateTimeLimit(JSONObject jsonEntity) {
		if (null == timeLimitEntity) {
			logger.error(String
					.format(ERROR_MESSAGE_TIME_LIMIT_NOT_FOUND, guid));
			throw new EntityNotFoundException(String.format(
					ERROR_MESSAGE_TIME_LIMIT_NOT_FOUND, guid));
		}

		try {
			if (jsonEntity.has("limitname")) {
				timeLimitEntity.setLimitname(jsonEntity.optString("limitname"));
			}
			if (jsonEntity.has("limittime")) {
				timeLimitEntity.setLimittime(ResourcesUtils
						.getTime(jsonEntity.optString("limittime")));
			}

			entityManagerConfig.getTransaction().begin();
			entityManagerConfig.merge(timeLimitEntity);
			entityManagerConfig.getTransaction().commit();
		}
		/*
		 * catch (JSONException je) { logger.error(ERROR_MESSAGE_PUT_TIME_LIMIT+
		 * ERROR_REASON_JSON, je); throw new ApplicationJsonException(je,
		 * ERROR_MESSAGE_PUT_TIME_LIMIT+ ERROR_REASON_JSON,
		 * Response.Status.BAD_REQUEST.getStatusCode()); }
		 */
		catch (NumberFormatException nfe) {
			logger.error(ERROR_MESSAGE_PUT_TIME_LIMIT
					+ ERROR_REASON_NUMBER_FORMAT, nfe);
			throw new ApplicationJsonException(nfe,
					ERROR_MESSAGE_PUT_TIME_LIMIT + ERROR_REASON_JSON,
					Response.Status.BAD_REQUEST.getStatusCode());
		} catch (RollbackException re) {
			ApplicationJsonException.handleSQLException(re,
					ERROR_MESSAGE_PUT_TIME_LIMIT, logger);
			logger.error(ERROR_MESSAGE_PUT_TIME_LIMIT + ERROR_REASON_ROLLBACK,
					re);
			throw re;
		} catch (ParseException pe) {
			logger.error(ERROR_MESSAGE_PUT_TIME_LIMIT + ERROR_REASON_PARSE, pe);
			throw new ApplicationJsonException(pe, ERROR_MESSAGE_PUT_TIME_LIMIT
					+ ERROR_REASON_PARSE,
					Response.Status.BAD_REQUEST.getStatusCode());
		} finally {
			if (null != entityManagerConfig) {
				entityManagerConfig.close();
			}
		}

		return JsonResponseWrapper.getResponse(Response.Status.OK,
				"time limit updated");
	}

	/**
	 * DELETE method : deletes the time limit
	 * 
	 * @return Response
	 */

	@DELETE
	public Response deleteTimeLimit() {
		if (null == timeLimitEntity) {
			logger.error(String
					.format(ERROR_MESSAGE_TIME_LIMIT_NOT_FOUND, guid));
			throw new EntityNotFoundException(String.format(
					ERROR_MESSAGE_TIME_LIMIT_NOT_FOUND, guid));
		}
		try {
			entityManagerConfig.getTransaction().begin();
			entityManagerConfig.remove(timeLimitEntity);
			entityManagerConfig.getTransaction().commit();
		}catch (RollbackException re) {
			ApplicationJsonException.handleSQLException(re,
					ERROR_MESSAGE_DELETE_TIME_LIMIT, logger);
					logger.error(ERROR_MESSAGE_PUT_TIME_LIMIT + ERROR_REASON_ROLLBACK,
					re);
					throw re;
					}
		finally {
			if (null != entityManagerConfig) {
				entityManagerConfig.close();
			}
		}

		return JsonResponseWrapper.getResponse(Response.Status.OK,
				"time limit deleted");
	}

	/**
	 * Returns the resource formatted as json
	 * 
	 * @param timeLimitEntity
	 *            TimeLimitEntity
	 * @param path
	 *            String
	 * @return JSONObject
	 * @throws JSONException
	 */
	public static JSONObject asJson(TimeLimitEntity timeLimitEntity, String path)
			throws JSONException {
		final JSONObject timeLimitAsJson = ApiResource.getMetaResource(path,
				TimeLimitResource.class);

		timeLimitAsJson.put("limitname", timeLimitEntity.getLimitname())
				.put("limittime", new SimpleDateFormat("hh:mm:ss").format(timeLimitEntity.getLimittime().getTime()));

		return timeLimitAsJson;
	}

	/**
	 * Method toString.
	 * 
	 * @return String
	 */
	public String toString() {
		String res = "";
		res += timeLimitEntity.getGuid();
		return res;
	}
}
