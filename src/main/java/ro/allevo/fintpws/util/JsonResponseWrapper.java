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

package ro.allevo.fintpws.util;

import java.net.URI;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

public final class JsonResponseWrapper {
	static final int METHOD_NOT_ALLOWED = 405;

	/**
	 * Field logger.
	 */
	private static Logger logger = LogManager
			.getLogger(JsonResponseWrapper.class.getName());

	private JsonResponseWrapper() {

	}

	public static Response getResponse(Response.Status code, URI uri) {
		return getResponse(code, "", uri, null);
	}

	public static Response getResponse(Response.Status code, String message) {
		return getResponse(code, message, null, null);
	}

	public static Response getResponse(int code, String message) {
		return getResponse(code, message, null, null);
	}
	
	public static Response getResponse(String id, Response.Status code, URI uri) {
		return getResponse(code, "", uri, id);
	}

	public static Response getResponse(int code, String message, URI uri, String id) {
		try {
			return Response.status(code)
					.entity(JsonResponseWrapper.getEntity(code, message, uri, id))
					.type(MediaType.APPLICATION_JSON).build();
		} catch (JSONException je) {
			logger.error("Error formatting response as JSON", je);

			// we are potentially already in an exception block, so don't
			// attempt to throw some other error
			return null;
		}
	}

	public static Response getResponse(Response.Status code, String message,
			URI uri, String id) {
		try {
			switch (code) {
			case OK:
				return Response
						.ok()
						.entity(JsonResponseWrapper.getEntity(code, message,
								uri, id)).type(MediaType.APPLICATION_JSON).build();
			case CREATED:
				return Response
						.created(uri)
						.entity(JsonResponseWrapper.getEntity(code, message,
								uri, id)).type(MediaType.APPLICATION_JSON).build();
			default:
				return Response
						.status(code)
						.entity(JsonResponseWrapper.getEntity(code, message,
								uri, id)).type(MediaType.APPLICATION_JSON).build();
			}
		} catch (JSONException je) {
			logger.error("Error formatting response as JSON", je);

			// we are potentially already in an exception block, so don't
			// attempt to throw some other error
			return null;
		}
	}

	public static JSONObject getEntity(int code, String message, URI uri, String id)
			throws JSONException {
		String finalMessage = message;
		String finalURIPath = (null == uri) ? null : uri.getPath();

		// build a common message based on code if the message is missing42
		switch (code) {
		case METHOD_NOT_ALLOWED:
			finalMessage = "method not allowed";
			break;
		default:
			finalMessage = message;
		}
		return new JSONObject().put("code", code).put("message", finalMessage)
				.put("id", id)
				.put("uri", finalURIPath);
	}

	public static JSONObject getEntity(Response.Status code, String message,
			URI uri, String id) throws JSONException {
		String finalMessage = message;
		String finalURIPath = (null == uri) ? null : uri.getPath();

		// build a common message based on code if the message is missing
		if (message.length() == 0) {
			switch (code) {
			case OK:
				finalMessage = "ok";
				break;
			case CREATED:
				finalMessage = "created";
				break;
			case NOT_FOUND:
				finalMessage = "not found";
				break;
			default:
				return getEntity(code.getStatusCode(), message, uri, id);
			}
		}

		return new JSONObject().put("code", code.getStatusCode())
				.put("message", finalMessage).put("id", id).put("uri", finalURIPath);
	}
}
