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

package ro.allevo.fintpws.test;

import javax.ws.rs.core.MediaType;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.junit.Assert;

import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

public final class TestHelper {
	/**
	 * Field logger.
	 */
	private static Logger logger = LogManager.getLogger(TestHelper.class
			.getName());

	private TestHelper() {
	}

	public static JSONObject assertResponse(WebResource wr, String method,
			String path, int status, Object entity) throws JSONException {
		ClientResponse r = wr.path(path).accept(MediaType.APPLICATION_JSON)
				.type(MediaType.APPLICATION_JSON)
				.method(method, ClientResponse.class, entity);
		JSONObject e = r.getEntity(JSONObject.class);

		if ((null != e) && (e.has("message"))) {
			logger.debug(method + " " + path + " expecting [" + status
					+ "] got [" + r.getStatus() + "] - "
					+ e.getString("message"));
		}

		Assert.assertEquals(method + " " + path + " wrong response", status,
				r.getStatus());

		return e;
	}

	public static JSONObject assertResponseMethodNotAllowed(WebResource wr,
			String method, String path, Object entity) throws JSONException {
		JSONObject e = assertResponse(wr, method, path,
				ClientResponse.Status.METHOD_NOT_ALLOWED.getStatusCode(),
				entity);
		Assert.assertEquals("code",
				ClientResponse.Status.METHOD_NOT_ALLOWED.getStatusCode(),
				e.getInt("code"));
		return e;
	}

	public static JSONObject assertResponseConflict(WebResource wr,
			String method, String path, Object entity) throws JSONException {
		JSONObject e = assertResponse(wr, method, path,
				ClientResponse.Status.CONFLICT.getStatusCode(), entity);
		Assert.assertEquals("code",
				ClientResponse.Status.CONFLICT.getStatusCode(),
				e.getInt("code"));
		return e;
	}

	public static JSONObject assertResponseCreated(WebResource wr,
			String method, String path, Object entity) throws JSONException {
		return assertResponse(wr, method, path,
				ClientResponse.Status.CREATED.getStatusCode(), entity);
	}

	public static JSONObject assertResponseOK(WebResource wr, String method,
			String path, Object entity) throws JSONException {
		return assertResponse(wr, method, path,
				ClientResponse.Status.OK.getStatusCode(), entity);
	}

	public static JSONObject assertResponseNotFound(WebResource wr,
			String method, String path, Object entity) throws JSONException {
		JSONObject e = assertResponse(wr, method, path,
				ClientResponse.Status.NOT_FOUND.getStatusCode(), entity);
		Assert.assertEquals("code",
				ClientResponse.Status.NOT_FOUND.getStatusCode(),
				e.getInt("code"));
		return e;
	}

	public static JSONObject assertResponseBadRequest(WebResource wr,
			String method, String path, Object entity) throws JSONException {
		JSONObject e = assertResponse(wr, method, path,
				ClientResponse.Status.BAD_REQUEST.getStatusCode(), entity);
		Assert.assertEquals("code",
				ClientResponse.Status.BAD_REQUEST.getStatusCode(),
				e.getInt("code"));
		return e;
	}
}
