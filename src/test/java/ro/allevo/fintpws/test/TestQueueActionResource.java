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
import org.junit.Ignore;
import org.junit.Test;

import ro.allevo.fintpws.model.QueueActionEntity;
import ro.allevo.fintpws.resources.QueueActionResource;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;

public class TestQueueActionResource {

	static final String UNIT_TEST_ACTIONS = "unit HelloWorld";
	static final String QUEUE_ACTIONS_PATH = "api/queueactions";
	static final String QUEUE_ACTIONS_PATH_WITH_SLASH = QUEUE_ACTIONS_PATH + "/";
	static WebResource wr;

	@BeforeClass
	public static void startMethod() throws JSONException {
		final ClientConfig cc = new DefaultClientConfig();
		final Client c = Client.create(cc);
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
		JSONObject queueActions = null;
		do {
			findRecords = false;
			queueActions = wr.path(QUEUE_ACTIONS_PATH)
					.queryParam("page", String.valueOf(page))
					.accept(MediaType.APPLICATION_JSON).get(JSONObject.class);
			JSONArray queueActionsArray = queueActions.getJSONArray("queueactions");
			for (int i = 0; i < queueActionsArray.length(); i++) {

				String actionName = queueActionsArray.getJSONObject(i).optString(
						"action");
				if (actionName.startsWith("unit")) {
					TestQueueActionResource.deleteTestQueueAction(wr, actionName);
					findRecords = true;
				}
			}
			if (!findRecords) {
				page++;
			}
		} while (queueActions.has("has_more"));

	}
	
	public static JSONObject createTestQueueAction(WebResource wr, 
			String action) throws JSONException {
		JSONObject queueAction = new JSONObject().put("action", action);
		
		return TestHelper
				.assertResponseCreated(wr, "POST", QUEUE_ACTIONS_PATH, queueAction);
	}
	
	public static JSONObject createTestQueueAction(WebResource wr, 
			String action, String descr) throws JSONException {
		JSONObject queueAction = new JSONObject().put("action", action).put("description", descr);
		
		return TestHelper
				.assertResponseCreated(wr, "POST", QUEUE_ACTIONS_PATH, queueAction);
	}

	public static JSONObject findTestQueueAction(WebResource wr, String action)
			throws JSONException {
		ClientResponse clientResponse = wr.path(QUEUE_ACTIONS_PATH).path(action)
				.accept(MediaType.APPLICATION_JSON)
				.type(MediaType.APPLICATION_JSON).get(ClientResponse.class);
		return (clientResponse.getClientResponseStatus()
				.equals(ClientResponse.Status.OK)) ? clientResponse
				.getEntity(JSONObject.class) : null;
	}
	
	public static JSONObject findRandomQueueAction(WebResource wr)
			throws JSONException {

		JSONObject queueActions = wr.path(QUEUE_ACTIONS_PATH).queryParam("filter", "tb")
				.accept(MediaType.APPLICATION_JSON).get(JSONObject.class);

		JSONArray queueActionsArray = queueActions.getJSONArray("queueactions");
		if(queueActionsArray.length() == 0){
			return null;
		}
		
		JSONObject queueAction = null;
		do{
			 queueAction = queueActionsArray.getJSONObject(
					new Random().nextInt(queueActionsArray.length()));
		}while (!queueAction.getString("action").startsWith("unit"));
		
		return queueAction;
	}
	
	public static JSONObject readTestQueueAction(WebResource wr, String action)
			throws JSONException {
		return TestHelper.assertResponseOK(wr, "GET", QUEUE_ACTIONS_PATH_WITH_SLASH + action,
				null);
	}

	public static void deleteTestQueueAction(WebResource wr, String action) {
		wr.path(QUEUE_ACTIONS_PATH_WITH_SLASH).path(action).delete(ClientResponse.class);
	}

	@Test
	public void createQueueAction() throws JSONException {
		TestHelper.assertResponseMethodNotAllowed(wr, "POST", QUEUE_ACTIONS_PATH_WITH_SLASH
				+ UNIT_TEST_ACTIONS, null);
	}
	
	@Test
	public void readQueueAction() throws JSONException {
		// create a queue action with a random desc
		String qdesc = "unit test queue action desc" + new Random().nextInt();
		createTestQueueAction(wr, UNIT_TEST_ACTIONS,qdesc);

		// now read it
		JSONObject rj = readTestQueueAction(wr, UNIT_TEST_ACTIONS);
		Assert.assertEquals("queue action description not correct", qdesc,
				rj.getString("description"));

		// make sure the metadata is returned
		Assert.assertEquals("href not correct", QUEUE_ACTIONS_PATH_WITH_SLASH
				+ UNIT_TEST_ACTIONS, rj.getString("href"));
		Assert.assertEquals("_type not correct", QueueActionResource.class.getName(),
				rj.getString("_type"));

		// check 404 if the queue action with the stupid name is requested
		TestHelper.assertResponseNotFound(wr, "GET", QUEUE_ACTIONS_PATH_WITH_SLASH
				+ new Random().nextInt(), null);
	}

	@Test
	public void updateQueueActionOptionalFields() throws JSONException {
		// test update description
		// create a queue action with a random desc
		String qdesc = "unit test queue action desc" + new Random().nextInt();
		createTestQueueAction(wr, UNIT_TEST_ACTIONS);

		// change description to a new random one
		String qdesc2 = "unit test queue action desc" + new Random().nextInt();
		JSONObject queueAction = readTestQueueAction(wr, UNIT_TEST_ACTIONS);
		queueAction.remove("action");
		queueAction.put("description", qdesc2);
		TestHelper.assertResponseOK(wr, "PUT",
				QUEUE_ACTIONS_PATH_WITH_SLASH + UNIT_TEST_ACTIONS, queueAction);

		// assert it worked
		JSONObject queueAction2 = readTestQueueAction(wr, UNIT_TEST_ACTIONS);
		Assert.assertEquals("queue action description not correct", qdesc2,
				queueAction2.getString("description"));

		// test singular field update
		String qdesc3 = "unit test queue action desc" + new Random().nextInt();
		JSONObject queueActionnewdesc = new JSONObject().put("description", qdesc3);
		TestHelper.assertResponseOK(wr, "PUT",
				QUEUE_ACTIONS_PATH_WITH_SLASH + UNIT_TEST_ACTIONS, queueActionnewdesc);

		// assert it worked
		JSONObject queueAction3 = readTestQueueAction(wr, UNIT_TEST_ACTIONS);
		Assert.assertEquals("queue action description not correct", qdesc3,
				queueAction3.getString("description"));
	}
	
	@Test
	public void updateQueueActionLongName() throws JSONException {
		// create a q with a random desc
		String qdesc = "unit test q desc" + new Random().nextInt();
		createTestQueueAction(wr, UNIT_TEST_ACTIONS);

		// update name
		JSONObject queueAction = readTestQueueAction(wr, UNIT_TEST_ACTIONS);
		queueAction.remove("action");
		queueAction.put("action", RandomStringUtils
				.randomAlphanumeric(101));
		TestHelper.assertResponseBadRequest(wr, "PUT", QUEUE_ACTIONS_PATH_WITH_SLASH + UNIT_TEST_ACTIONS,
				queueAction);
	}
	
	@Test
	public void updateQueueActionAllFields() throws JSONException {
		// generate a queue action (JSONObject format)
		JSONObject jsonEntity = TestUtils.fillResourceData(new JSONObject(),
				new QueueActionEntity());
		jsonEntity.remove("action");
		jsonEntity.remove("actionid");

		// create a queue action
		createTestQueueAction(wr, UNIT_TEST_ACTIONS);

		// update all fields
		TestHelper.assertResponseOK(wr, "PUT", QUEUE_ACTIONS_PATH_WITH_SLASH
				+ UNIT_TEST_ACTIONS, jsonEntity);

		// now read it
		JSONObject queueAction = readTestQueueAction(wr, UNIT_TEST_ACTIONS);

		// assert it worked
		Assert.assertEquals("queue action fileds not correct", TestUtils
				.compareJSONObjects(new QueueActionEntity(), jsonEntity, queueAction,
						"action,actionid"), true);

	}

	@Ignore
	@Test
	public void updateQueueActionKeyFields() throws JSONException {
		// test update name
		// create a queue action with a random name
		String newaction = UNIT_TEST_ACTIONS + new Random().nextInt();
		createTestQueueAction(wr, newaction);

		// update name
		JSONObject queueAction = readTestQueueAction(wr, newaction);
		queueAction.remove("action");
		queueAction.put("action", UNIT_TEST_ACTIONS);
		TestHelper.assertResponseOK(wr, "PUT", QUEUE_ACTIONS_PATH_WITH_SLASH + newaction, queueAction);

		// assert it worked
		JSONObject queueAction2 = readTestQueueAction(wr, UNIT_TEST_ACTIONS);
		Assert.assertEquals("queue action action not correct", UNIT_TEST_ACTIONS,
				queueAction2.getString("action"));

		// test name conflict (409)
		// create a second queue
		String queueAction2action = UNIT_TEST_ACTIONS + new Random().nextInt();
		createTestQueueAction(wr, queueAction2action);

		//TODO : testam cand action e unique
		
		// update to action of the 1st queue action
		queueAction2 = readTestQueueAction(wr, queueAction2action);
		queueAction2.remove("action");
		queueAction2.put("action", UNIT_TEST_ACTIONS);
		TestHelper.assertResponseConflict(wr, "PUT",
				QUEUE_ACTIONS_PATH_WITH_SLASH + queueAction2action, queueAction2);
	}

	@Test
	public void deleteQueueAction() throws JSONException {
		// create a queue action with a random desc
		String qdesc = "unit test queue action desc" + new Random().nextInt();
		createTestQueueAction(wr, UNIT_TEST_ACTIONS);

		JSONObject queueAction = readTestQueueAction(wr, UNIT_TEST_ACTIONS);

		// delete it
		TestHelper.assertResponseOK(wr, "DELETE", QUEUE_ACTIONS_PATH_WITH_SLASH
				+ UNIT_TEST_ACTIONS, null);

		// read it again to make sure it's gone
		TestHelper.assertResponseNotFound(wr, "GET", QUEUE_ACTIONS_PATH_WITH_SLASH
				+ UNIT_TEST_ACTIONS, null);

		// check 404 if the queue action with the stupid name is requested
		TestHelper.assertResponseNotFound(wr, "DELETE", QUEUE_ACTIONS_PATH_WITH_SLASH
				+ new Random().nextInt(), null);
	}
	
}
