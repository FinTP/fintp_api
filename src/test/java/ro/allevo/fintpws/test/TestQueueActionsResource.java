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

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import ro.allevo.fintpws.model.QueueActionEntity;
import ro.allevo.fintpws.resources.QueueActionsResource;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;

/**
 * Tests for {@link QueueActionsResource}.
 * 
 * @author anda
 * 
 */

public class TestQueueActionsResource {

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

				String action = queueActionsArray.getJSONObject(i).optString(
						"action");
				if (action.startsWith("unit")) {
					TestQueueActionResource.deleteTestQueueAction(wr, action);
					findRecords = true;
				}
			}
			if (!findRecords) {
				page++;
			}
		} while (queueActions.has("has_more"));

	}
	
	@Test
	public void createQueueActions() throws JSONException {
		// create a queue action with a random action
		String action = "unit test action" + new Random().nextInt();
		
		JSONObject response = TestQueueActionResource.createTestQueueAction(wr,
				action);
		Assert.assertTrue("uri missing", response.getString("uri").length() > 0);

		// make sure it is created
		JSONObject queueAction = TestQueueActionResource.readTestQueueAction(wr,
				action);
		Assert.assertEquals("queue action",action,queueAction.getString("action"));

		//TODO : testam cand avem conditie de unicitate in baza de date 
		// make sure we get a conflict if we created it again
		//TestHelper.assertResponseConflict(wr, "POST", QUEUE_ACTIONS_PATH,
		//		queueAction);
	}
	
	@Test
	public void insertQueueActionsAllFields() throws JSONException {
		// generate a queue action (JSONObject format)
		JSONObject jsonEntity = TestUtils.fillResourceData(new JSONObject(),
				new QueueActionEntity());
		
		jsonEntity.put("action", "unit"
				+ jsonEntity.getString("action").substring(4));
		jsonEntity.remove("actionid");
	
		// create a queue
		TestHelper.assertResponseCreated(wr, "POST", QUEUE_ACTIONS_PATH, jsonEntity);
		
		// now read it
		JSONObject queueAction = TestQueueActionResource.readTestQueueAction(wr, jsonEntity.getString("action"));

		// assert it worked
		Assert.assertEquals("queue action filed not correct", TestUtils
				.compareJSONObjects(new QueueActionEntity(), jsonEntity, queueAction,
						"action,actionid"), true);
	}


	@Test
	public void readQueueActions() throws JSONException {
		// make sure it makes sense ( >0 queue is available )
		TestQueueActionResource.createTestQueueAction(wr, UNIT_TEST_ACTIONS);
		
		JSONObject queueActions = wr.path(QUEUE_ACTIONS_PATH)
				.accept(MediaType.APPLICATION_JSON).get(JSONObject.class);

		// make sure the metadata is returned
		Assert.assertEquals("href not correct", QUEUE_ACTIONS_PATH,
				queueActions.getString("href"));
		Assert.assertEquals("_type not correct",
				QueueActionsResource.class.getName(), queueActions.getString("_type"));

		JSONArray queueActionsList = queueActions.getJSONArray("queueactions");
		Assert.assertTrue("queue actions list empty", queueActionsList.length() > 0);

		// get first queue, see if has a name
		Assert.assertTrue("queue actions list empty", queueActionsList.getJSONObject(0)
				.getString("action").length() > 0);
	}
	
	@Test
	public void updateQueueActions() throws JSONException {
		TestHelper.assertResponseMethodNotAllowed(wr, "PUT", QUEUE_ACTIONS_PATH_WITH_SLASH,
				null);
	}

	@Test
	public void deleteQueueActions() throws JSONException {
		TestHelper.assertResponseMethodNotAllowed(wr, "DELETE", QUEUE_ACTIONS_PATH_WITH_SLASH,
				null);
	}

	@Test
	public void totalTests() throws JSONException {
		// requst t among some garbage
		JSONObject queueActions = wr.path(QUEUE_ACTIONS_PATH_WITH_SLASH).queryParam("filter", "stu")
				.accept(MediaType.APPLICATION_JSON).get(JSONObject.class);
		int total1 = queueActions.getInt("total");

		TestQueueActionResource.createTestQueueAction(wr, "unit_total");
		
		// requst t among some garbage
		queueActions = wr.path(QUEUE_ACTIONS_PATH_WITH_SLASH)
				.queryParam("filter", "adste").accept(MediaType.APPLICATION_JSON_TYPE)
				.get(JSONObject.class);
		
		int total2 = queueActions.getInt("total");

		Assert.assertEquals("total not +1", total1 + 1, total2);
		
		// request a small page, look for total to be the same
		queueActions = wr.path(QUEUE_ACTIONS_PATH_WITH_SLASH)
				.queryParam("filter", "stu").queryParam("page_size", "1")
				.accept(MediaType.APPLICATION_JSON).get(JSONObject.class);
		
		int total3 = queueActions.getInt("total");
		
		Assert.assertEquals("total wrong for page", total1 + 1, total3);
	}

	@Test
	public void pagingTests() throws JSONException {
		// TODO: test last page
		// create 101 queueActions
		for (int i = 0; i < 101; i++) {
			TestQueueActionResource.createTestQueueAction(wr, "unit_page_test" + i);
		}

		// test get default page (1), default page_size
		JSONObject queueActions = wr.path(QUEUE_ACTIONS_PATH)
				.accept(MediaType.APPLICATION_JSON).get(JSONObject.class);

		// we should have at least a full page and at least 1 more item
		Assert.assertEquals("default paging items", 100, queueActions
				.getJSONArray("queueactions").length());
		Assert.assertEquals("default paging has_more", true,
				queueActions.getBoolean("has_more"));

		// test get default page (1), custom size : 42
		// 42 = between[1,100] => we should get 42 time limits per page
		queueActions = wr.path(QUEUE_ACTIONS_PATH)
				.queryParam("page_size", "42")
				.accept(MediaType.APPLICATION_JSON).get(JSONObject.class);
		// we should have at least a full page and at least 1 more item
		Assert.assertEquals("custom page size items", 42, queueActions
				.getJSONArray("queueactions").length());
		Assert.assertEquals("custom page size has_more", true,
				queueActions.getBoolean("has_more"));

		// test get default page (1), custom invalid size : -1
		// -1 < 0 => size = DEFAULT_SIZE(100)
		queueActions = wr.path(QUEUE_ACTIONS_PATH)
				.queryParam("page_size", "-1")
				.accept(MediaType.APPLICATION_JSON).get(JSONObject.class);
		// we should have at least a full page and at least 1 more item
		Assert.assertEquals("custom invalid page size -1 items", 100,
				queueActions.getJSONArray("queueactions").length());
		Assert.assertEquals("custom invalid page size -1 items", true,
				queueActions.getBoolean("has_more"));

		// test get default page (1), custom invalid size : 101
		// 101 > 100 => size = DEFAULT_SIZE(100)
		queueActions = wr.path(QUEUE_ACTIONS_PATH)
				.queryParam("page_size", "101")
				.accept(MediaType.APPLICATION_JSON).get(JSONObject.class);
		// we should have at least a full page and at least 1 more item
		Assert.assertEquals("custom invalid page size 100 items", 100,
				queueActions.getJSONArray("queueactions").length());
		Assert.assertEquals("custom invalid page size 100 items", true,
				queueActions.getBoolean("has_more"));

		// test get default page (1), custom invalid size : aaa
		// aaa is not int > size= DEFAULT_SIZE(100)
		queueActions = wr.path(QUEUE_ACTIONS_PATH)
				.queryParam("page_size", "aaa")
				.accept(MediaType.APPLICATION_JSON).get(JSONObject.class);
		// we should have at least a full page and at least 1 more item
		Assert.assertEquals("custom invalid page size aaa items", 100,
				queueActions.getJSONArray("queueactions").length());
		Assert.assertEquals("custom invalid page size aaa items", true,
				queueActions.getBoolean("has_more"));

		// test get custom page(2), custom size: 2
		queueActions = wr.path(QUEUE_ACTIONS_PATH).queryParam("page", "2")
				.queryParam("page_size", "2")
				.accept(MediaType.APPLICATION_JSON).get(JSONObject.class);

		// we should have at least a full page and at least 1 more item
		Assert.assertEquals("custom page 2 custom page size 2 items", 2,
				queueActions.getJSONArray("queueactions").length());
		Assert.assertEquals("custom page 2 custom page size 2 has_more", true,
				queueActions.getBoolean("has_more"));
		String action1 = queueActions.getJSONArray("queueactions")
				.getJSONObject(1).getString("action");

		// repeat with next page
		queueActions = wr.path(QUEUE_ACTIONS_PATH).queryParam("page", "3")
				.queryParam("page_size", "2")
				.accept(MediaType.APPLICATION_JSON).get(JSONObject.class);

		// we should have at least a full page and at least 1 more item
		Assert.assertEquals("custom page 3 custom page size 2 items", 2,
				queueActions.getJSONArray("queueactions").length());
		Assert.assertEquals("custom page 3 custom page size 2 has_more", true,
				queueActions.getBoolean("has_more"));
		String action2 = queueActions.getJSONArray("queueactions")
				.getJSONObject(1).getString("action");

		// time limits guid should be different
		Assert.assertNotEquals("different actions on pages", action1, action2);

	}
}
