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

import java.util.Random;

import javax.rmi.CORBA.Tie;
import javax.ws.rs.core.MediaType;

import junit.framework.TestListener;

import org.apache.commons.lang3.RandomStringUtils;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import ro.allevo.fintpws.model.TimeLimitEntity;
import ro.allevo.fintpws.resources.TimeLimitResource;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;

@RunWith(JUnit4.class)
public class TestTimeLimitResource {

	static final String TIME_LIMITS_PATH = "api/timelimits";
	static final String TIME_LIMITS_PATH_WITH_SLASH = TIME_LIMITS_PATH + "/";
	static WebResource wr;

	@BeforeClass
	public static void startMethod() throws JSONException {
		final Client c = Client.create();
		c.addFilter(new HTTPBasicAuthFilter("admin", "admin"));
		wr = c.resource(TestUtils.getUrlBase());
	}

	@Before
	public void cleanupBefore() throws JSONException {
		cleanup();
	}

	@After
	public void cleanupAfter() throws JSONException {
		cleanup();
	}

	public static void cleanup() throws JSONException {

		int page = 1;
		boolean findRecords = false;
		JSONObject timeLimits = null;
		do {
			findRecords = false;
			timeLimits = wr.path(TIME_LIMITS_PATH)
					.queryParam("page", String.valueOf(page))
					.accept(MediaType.APPLICATION_JSON).get(JSONObject.class);
			JSONArray timeLimitsArray = timeLimits.getJSONArray("timelimits");
			for (int i = 0; i < timeLimitsArray.length(); i++) {

				String limitName = timeLimitsArray.getJSONObject(i).optString(
						"limitname");
				if (limitName.startsWith("unit")) {
					TestTimeLimitResource.deleteTestTimeLimit(wr, limitName);
					findRecords = true;
				}
			}
			if (!findRecords) {
				page++;
			}
		} while (timeLimits.has("has_more"));

	}

	public static JSONObject createTestTimeLimit(WebResource wr,
			String limitName) throws JSONException {
		JSONObject timeLimit = new JSONObject().put("limitname", limitName)
				.put("limittime", "23:59:00");

		return TestHelper.assertResponseCreated(wr, "POST", TIME_LIMITS_PATH,
				timeLimit);
	}

	public static JSONObject createTestTimeLimit(WebResource wr, String guid,
			String limitName) throws JSONException {
		JSONObject timeLimit = new JSONObject().put("guid", guid)
				.put("limitname", limitName)
				.put("limittime", "23:59:00");

		return TestHelper.assertResponseCreated(wr, "POST", TIME_LIMITS_PATH,
				timeLimit);
	}

	public static JSONObject findTestTimeLimit(WebResource wr, String name)
			throws JSONException {
		ClientResponse clientResponse = wr.path(TIME_LIMITS_PATH)
				.queryParam("filter_limitname_exact", name)
				.accept(MediaType.APPLICATION_JSON)
				.type(MediaType.APPLICATION_JSON).get(ClientResponse.class);
		return (clientResponse.getClientResponseStatus()
				.equals(ClientResponse.Status.OK))? clientResponse
				.getEntity(JSONObject.class) : null;
	}

	public static JSONObject findRandomTimeLimit(WebResource wr)
			throws JSONException {

		JSONObject timeLimits = wr.path(TIME_LIMITS_PATH)
				.queryParam("filter", "tb").accept(MediaType.APPLICATION_JSON)
				.get(JSONObject.class);
		JSONArray timeLimitsArray = timeLimits.getJSONArray("timelimits");
		if (timeLimitsArray.length() == 0) {
			return null;
		}

		JSONObject timeLimit = null;
		do {
			timeLimit = timeLimitsArray.getJSONObject(new Random()
					.nextInt(timeLimitsArray.length()));
		} while (!timeLimit.getString("description").startsWith("unit"));

		return timeLimit;
	}

	public static void deleteTestTimeLimit(WebResource wr, String limitname) {
		wr.path(TIME_LIMITS_PATH_WITH_SLASH).path(limitname)
				.delete(ClientResponse.class);
	}

	public static JSONObject readTestTimeLimit(WebResource wr, String limitname)
			throws JSONException {
		return TestHelper.assertResponseOK(wr, "GET",
				TIME_LIMITS_PATH_WITH_SLASH + limitname, null);
	}

	@Test
	public void createTimeLimit() throws JSONException {
		TestHelper.assertResponseMethodNotAllowed(wr, "POST",
				TIME_LIMITS_PATH_WITH_SLASH + "20", null);
	}

	@Test
	public void readTimeLimit() throws JSONException {
		// create a time limit with unit+random limit name
		String limitName = "unit" + new Random().nextInt();
		JSONObject response = createTestTimeLimit(wr, limitName);

		// now read it
		JSONObject timeLimit = readTestTimeLimit(wr, response.getString("id"));
		Assert.assertEquals("time limit limitname not correct", limitName,
				timeLimit.getString("limitname"));

		// make sure the metadata is returned
		Assert.assertEquals("href not correct", TIME_LIMITS_PATH_WITH_SLASH
				+ response.getString("id"), timeLimit.getString("href"));
		Assert.assertEquals("_type not correct",
				TimeLimitResource.class.getName(), timeLimit.getString("_type"));

		// check 404 if the time limit with the stupid name is requested
		TestHelper.assertResponseNotFound(wr, "GET", TIME_LIMITS_PATH_WITH_SLASH + "text",
				null);
	}

	@Test
	public void updateTimeLimitOptionalFields() throws JSONException {
		// test update limitname
		// create a timelimit with a random limitname

		String limitName1 = "unit test limitname" + new Random().nextInt();
		JSONObject response = createTestTimeLimit(wr, limitName1);

		// change description to a new random one
		String limitName2 = "unit test limitname" + new Random().nextInt();
		JSONObject timeLimit = readTestTimeLimit(wr, response.getString("id"));
		timeLimit.remove("limitname");
		timeLimit.put("limitname", limitName2);
		TestHelper.assertResponseOK(wr, "PUT", TIME_LIMITS_PATH_WITH_SLASH
				+ response.getString("id"), timeLimit);

		// assert it worked
		JSONObject timeLimit2 = readTestTimeLimit(wr, response.getString("id"));
		Assert.assertEquals("time limit name not correct", limitName2,
				timeLimit2.getString("limitname"));

		// test singular field update
		String limitName3 = "unit test limitname" + new Random().nextInt();
		JSONObject tlNew = new JSONObject().put("limitname", limitName3);
		TestHelper.assertResponseOK(wr, "PUT", TIME_LIMITS_PATH_WITH_SLASH
				+ response.getInt("id"), tlNew);

		// assert it worked
		JSONObject timeLimit3 = readTestTimeLimit(wr, response.getString("id"));
		Assert.assertEquals("timelimit name not correct", limitName3,
				timeLimit3.getString("limitname"));

	}

	@Test
	public void updateTimeLimitAllFields() throws JSONException {
		JSONObject jsonEntity = TestUtils.fillResourceData(new JSONObject(),
				new TimeLimitEntity());
		jsonEntity.remove("guid");
		jsonEntity.put("limitname", "unit"+jsonEntity.getString("limitname").substring(4));

		// create a time limit
		JSONObject response = createTestTimeLimit(wr, "unit1");

		// update all fields
		TestHelper.assertResponseOK(wr, "PUT", TIME_LIMITS_PATH_WITH_SLASH
				+ response.getString("id"), jsonEntity);

		// now read it
		JSONObject timeLimit = readTestTimeLimit(wr, response.getString("id"));

		// assert it worked
		Assert.assertEquals("time limits fields not correct", TestUtils
				.compareJSONObjects(new TimeLimitEntity(), jsonEntity,
						timeLimit, "guid,limittime"), true);

	}

	@Test
	public void updateTimeLimitLongName() throws JSONException {
		// create time limit with random name
		String name = "unit test " + new Random().nextInt();
		JSONObject response = createTestTimeLimit(wr, name);

		// update name
		JSONObject timeLimit = readTestTimeLimit(wr, response.getString("id"));
		timeLimit.remove("limitname");
		timeLimit.put("limitname", RandomStringUtils.randomAlphanumeric(101));
		TestHelper.assertResponseBadRequest(wr, "PUT",
				TIME_LIMITS_PATH_WITH_SLASH + response.getString("id"),
				timeLimit);

	}

	/*
	 * TODO: update keys (similar routng rules)
	 */

	@Test
	public void deleteTimeLimit() throws JSONException {

		// create a time limit with a random name
		String name = "unit test " + new Random().nextInt();
		JSONObject response = createTestTimeLimit(wr, name);

		// delete it
		TestHelper.assertResponseOK(wr, "DELETE", TIME_LIMITS_PATH_WITH_SLASH
				+ response.getString("id"), null);
		
		// read it again to make sure it's gone
		TestHelper.assertResponseNotFound(wr, "GET",
				TIME_LIMITS_PATH_WITH_SLASH + response.getString("id"), null);
		
		//check 404 if the time limit 
		TestHelper.assertResponseNotFound(wr, "DELETE",
				TIME_LIMITS_PATH_WITH_SLASH + "text", null);

	}
}
