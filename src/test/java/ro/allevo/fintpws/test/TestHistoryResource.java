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

import javax.ws.rs.core.MediaType;

import org.apache.commons.lang3.RandomStringUtils;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import ro.allevo.fintpws.model.HistoryEntity;
import ro.allevo.fintpws.resources.HistoryResource;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;

public class TestHistoryResource {

	static final String UNIT_TEST_HISTORIES = "unit HelloWorld";
	static final String HISTORIES_PATH = "api/histories";
	static final String HISTORIES_PATH_WITH_SLASH = HISTORIES_PATH + "/";
	static WebResource wr;

	@BeforeClass
	public static void startMethod() throws JSONException {
		final ClientConfig cc = new DefaultClientConfig();
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
		JSONObject histories = null;
		do {
			findRecords = false;
			histories = wr.path(HISTORIES_PATH)
					.queryParam("page", String.valueOf(page))
					.accept(MediaType.APPLICATION_JSON).get(JSONObject.class);
			JSONArray historiesArray = histories.getJSONArray("histories");
			for (int i = 0; i < historiesArray.length(); i++) {

				String payload = historiesArray.getJSONObject(i).getString(
						"payload");
				if (payload.startsWith("unit")) {
					TestHistoryResource.deleteTestHistory(wr, payload);
					findRecords = true;
				}
			}
			if (!findRecords) {
				page++;
			}
		} while (histories.has("has_more"));

	}
	
	public static JSONObject createTestHistory(WebResource wr, 
			String payload) throws JSONException {
		JSONObject history = new JSONObject()
				.put("guid", UNIT_TEST_HISTORIES)
				.put("payload", payload)
				.put("correlationid", UNIT_TEST_HISTORIES)
				.put("requestorservice",UNIT_TEST_HISTORIES)
				.put("requesttype",UNIT_TEST_HISTORIES)
				.put("priority", "1")
				.put("holdstatus", new Random().nextInt());;
		
		return TestHelper
				.assertResponseCreated(wr, "POST", HISTORIES_PATH, history);
	}
	
	public static JSONObject createTestHistory(WebResource wr, 
			String guid, String correlationid) throws JSONException {
		JSONObject history = new JSONObject()
				.put("guid", guid)
				.put("payload", UNIT_TEST_HISTORIES)
				.put("correlationid", correlationid)
				.put("requestorservice",UNIT_TEST_HISTORIES)
				.put("requesttype",UNIT_TEST_HISTORIES)
				.put("priority", "1")
				.put("holdstatus", new Random().nextInt());;
		
		return TestHelper
				.assertResponseCreated(wr, "POST", HISTORIES_PATH, history);
	}
	
	public static JSONObject createTestHistory(WebResource wr) throws JSONException {
		JSONObject history = new JSONObject()
				.put("payload", UNIT_TEST_HISTORIES)
				.put("correlationid", UNIT_TEST_HISTORIES)
				.put("requestorservice",UNIT_TEST_HISTORIES)
				.put("requesttype",UNIT_TEST_HISTORIES)
				.put("priority", "1")
				.put("holdstatus", new Random().nextInt());
		
		return TestHelper
				.assertResponseCreated(wr, "POST", HISTORIES_PATH, history);
	}

	public static JSONObject findTestHistory(WebResource wr, String guid)
			throws JSONException {
		ClientResponse clientResponse = wr.path(HISTORIES_PATH).path(guid)
				.accept(MediaType.APPLICATION_JSON)
				.type(MediaType.APPLICATION_JSON).get(ClientResponse.class);
		return (clientResponse.getClientResponseStatus()
				.equals(ClientResponse.Status.OK)) ? clientResponse
				.getEntity(JSONObject.class) : null;
	}
	
	public static JSONObject findRandomHistory(WebResource wr)
			throws JSONException {

		JSONObject histories = wr.path(HISTORIES_PATH).queryParam("filter", "tb")
				.accept(MediaType.APPLICATION_JSON).get(JSONObject.class);

		JSONArray historiesArray = histories.getJSONArray("histories");
		if(historiesArray.length() == 0){
			return null;
		}
		
		JSONObject history = null;
		do{
			 history = historiesArray.getJSONObject(
					new Random().nextInt(historiesArray.length()));
		}while (!history.getString("guid").startsWith("unit"));
		
		return history;
	}
	
	public static JSONObject readTestHistory(WebResource wr, String guid)
			throws JSONException {
		return TestHelper.assertResponseOK(wr, "GET", HISTORIES_PATH_WITH_SLASH + guid,
				null);
	}

	public static void deleteTestHistory(WebResource wr, String guid) {
		wr.path(HISTORIES_PATH_WITH_SLASH).path(guid).delete(ClientResponse.class);
	}

	@Test
	public void createHistory() throws JSONException {
		TestHelper.assertResponseMethodNotAllowed(wr, "POST", HISTORIES_PATH_WITH_SLASH
				+ UNIT_TEST_HISTORIES, null);
	}
	
	@Test
	public void readHistory() throws JSONException {
		// create a history with a random payload
		String payload = "unit test history" + new Random().nextInt();
		createTestHistory(wr, payload);

		// now read it
		JSONObject rj = readTestHistory(wr, UNIT_TEST_HISTORIES);
		Assert.assertEquals("history payload not correct", payload,
				rj.getString("payload"));

		// make sure the metadata is returned
		Assert.assertEquals("href not correct", HISTORIES_PATH_WITH_SLASH
				+ UNIT_TEST_HISTORIES, rj.getString("href"));
		Assert.assertEquals("_type not correct", HistoryResource.class.getName(),
				rj.getString("_type"));

		// check 404 if the history with the stupid name is requested
		TestHelper.assertResponseNotFound(wr, "GET", HISTORIES_PATH_WITH_SLASH
				+ new Random().nextInt(), null);
	}

	@Test
	public void updateHistoryOptionalFields() throws JSONException {
		// test update payload
		// create a history with a random payload
		createTestHistory(wr, UNIT_TEST_HISTORIES);

		// change payload to a new random one
		String payload2 = "unit test payload desc" + new Random().nextInt();
		JSONObject history = readTestHistory(wr, UNIT_TEST_HISTORIES);
		history.remove("guid");
		history.put("payload", payload2);
		TestHelper.assertResponseOK(wr, "PUT",
				HISTORIES_PATH_WITH_SLASH + UNIT_TEST_HISTORIES, history);

		// assert it worked
		JSONObject history2 = readTestHistory(wr, UNIT_TEST_HISTORIES);
		Assert.assertEquals("history payload not correct", payload2,
				history2.getString("payload"));

		// test singular field update
		String payload3 = "unit test history payload" + new Random().nextInt();
		JSONObject historynewpayload = new JSONObject().put("payload", payload3);
		TestHelper.assertResponseOK(wr, "PUT",
				HISTORIES_PATH_WITH_SLASH + UNIT_TEST_HISTORIES, historynewpayload);

		// assert it worked
		JSONObject history3 = readTestHistory(wr, UNIT_TEST_HISTORIES);
		Assert.assertEquals("history payload not correct", payload3,
				history3.getString("payload"));
	}
	
	@Test
	public void updateHistoryLongName() throws JSONException {
		// create a history with a random guid and correlationid
		String guid = UNIT_TEST_HISTORIES + new Random().nextInt();
		createTestHistory(wr, guid, UNIT_TEST_HISTORIES + new Random().nextInt());

		// update payload
		JSONObject history = readTestHistory(wr, guid);
		history.remove("correlationid");
		history.put("correlationid", RandomStringUtils
				.randomAlphanumeric(31));
		TestHelper.assertResponseBadRequest(wr, "PUT", HISTORIES_PATH_WITH_SLASH + guid,
				history);
	}
	
	@Test
	public void updateHistoryAllFields() throws JSONException {
		// generate a history (JSONObject format)
		JSONObject jsonEntity = TestUtils.fillResourceData(new JSONObject(),
				new HistoryEntity());
		jsonEntity.remove("payload");
		jsonEntity.remove("guid");
		
		// create a history
		createTestHistory(wr, UNIT_TEST_HISTORIES);

		// update all fields
		TestHelper.assertResponseOK(wr, "PUT", HISTORIES_PATH_WITH_SLASH
				+ UNIT_TEST_HISTORIES, jsonEntity);

		// now read it
		JSONObject history = readTestHistory(wr, UNIT_TEST_HISTORIES);

		// assert it worked
		Assert.assertEquals("history fields not correct", TestUtils
				.compareJSONObjects(new HistoryEntity(), jsonEntity, history,
						"payload,guid"), true);
	}

	@Test
	public void deleteHistory() throws JSONException {
		// create a history with a random payload
		createTestHistory(wr, UNIT_TEST_HISTORIES);

		readTestHistory(wr, UNIT_TEST_HISTORIES);

		// delete it
		TestHelper.assertResponseOK(wr, "DELETE", HISTORIES_PATH_WITH_SLASH
				+ UNIT_TEST_HISTORIES, null);

		// read it again to make sure it's gone
		TestHelper.assertResponseNotFound(wr, "GET", HISTORIES_PATH_WITH_SLASH
				+ UNIT_TEST_HISTORIES, null);

		// check 404 if the history with the random payload is requested
		TestHelper.assertResponseNotFound(wr, "DELETE", HISTORIES_PATH_WITH_SLASH
				+ new Random().nextInt(), null);
	}
	
}
