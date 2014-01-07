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

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;

import ro.allevo.fintpws.model.QueueActionEntity;
import ro.allevo.fintpws.model.QueueTypeEntity;
import ro.allevo.fintpws.resources.QueueActionsResource;
import ro.allevo.fintpws.resources.QueueTypesResource;
import ro.allevo.fintpws.resources.TimeLimitsResource;

/**
 * Tests for {@link QueueTypesResource}.
 * 
 * @author anda
 * 
 */

public class TestQueueTypesResource {
	
	static final String UNIT_TEST_TYPES = "unit HelloWorld";
	static final String QUEUE_TYPES_PATH = "api/queuetypes";
	static final String QUEUE_TYPES_PATH_WITH_SLASH = QUEUE_TYPES_PATH + "/";
	
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
		JSONObject queueTypes = null;
		do {
			findRecords = false;
			queueTypes = wr.path(QUEUE_TYPES_PATH)
					.queryParam("page", String.valueOf(page))
					.accept(MediaType.APPLICATION_JSON).get(JSONObject.class);
			JSONArray queueTypesArray = queueTypes.getJSONArray("queuetypes");
			for (int i = 0; i < queueTypesArray.length(); i++) {

				String typename = queueTypesArray.getJSONObject(i).optString(
						"typename");
				if (typename.startsWith("unit")) {
					TestQueueTypeResource.deleteTestQueueType(wr, typename);
					findRecords = true;
				}
			}
			if (!findRecords) {
				page++;
			}
		} while (queueTypes.has("has_more"));
	}
	
	@Test
	public void createQueueTypes() throws JSONException {
		// create a queue type with a random typename
		String typename = "unit test typename" + new Random().nextInt();
		
		JSONObject response = TestQueueTypeResource.createTestQueueType(wr,
				typename);
		Assert.assertTrue("uri missing", response.getString("uri").length() > 0);

		// make sure it is created
		JSONObject queueType = TestQueueTypeResource.readTestQueueType(wr,
				typename);
		Assert.assertEquals("queue typename",typename,queueType.getString("typename"));
	}
	
	@Test
	public void insertQueueTypesAllFields() throws JSONException {
		// generate a queue type (JSONObject format)
		JSONObject jsonEntity = TestUtils.fillResourceData(new JSONObject(),
				new QueueTypeEntity());
		
		jsonEntity.put("typename", "unit"
				+ jsonEntity.getString("typename").substring(4));
		jsonEntity.remove("typeid");
	
		// create a queue
		TestHelper.assertResponseCreated(wr, "POST", QUEUE_TYPES_PATH, jsonEntity);
		
		// now read it
		JSONObject queueType = TestQueueTypeResource.readTestQueueType(wr, jsonEntity.getString("typename"));

		// assert it worked
		Assert.assertEquals("queue type filed not correct", TestUtils
				.compareJSONObjects(new QueueTypeEntity(), jsonEntity, queueType,
						"typename,typeid"), true);
	}
	
	@Test
	public void readQueueTypes() throws JSONException {
		// make sure it makes sense ( >0 queue is available )
		TestQueueTypeResource.createTestQueueType(wr, UNIT_TEST_TYPES);
		
		JSONObject queueTypes = wr.path(QUEUE_TYPES_PATH)
				.accept(MediaType.APPLICATION_JSON).get(JSONObject.class);

		// make sure the metadata is returned
		Assert.assertEquals("href not correct", QUEUE_TYPES_PATH,
				queueTypes.getString("href"));
		Assert.assertEquals("_type not correct",
				QueueTypesResource.class.getName(), queueTypes.getString("_type"));

		JSONArray queueTypesList = queueTypes.getJSONArray("queuetypes");
		Assert.assertTrue("queue types list empty", queueTypesList.length() > 0);

		// get first queue type, see if has a name
		Assert.assertTrue("queue type list empty", queueTypesList.getJSONObject(0)
				.getString("typename").length() > 0);
	}
	
	@Test
	public void updateQueueTypes() throws JSONException {
		TestHelper.assertResponseMethodNotAllowed(wr, "PUT", QUEUE_TYPES_PATH_WITH_SLASH,
				null);
	}

	@Test
	public void deleteQueueTypes() throws JSONException {
		TestHelper.assertResponseMethodNotAllowed(wr, "DELETE", QUEUE_TYPES_PATH_WITH_SLASH,
				null);
	}
	
	@Test
	public void totalTests() throws JSONException {
		// requst t among some garbage
		JSONObject queueTypes = wr.path(QUEUE_TYPES_PATH_WITH_SLASH).queryParam("filter", "stu")
				.accept(MediaType.APPLICATION_JSON).get(JSONObject.class);
		int total1 = queueTypes.getInt("total");

		TestQueueTypeResource.createTestQueueType(wr, "unit_total");
		
		// requst t among some garbage
		queueTypes = wr.path(QUEUE_TYPES_PATH_WITH_SLASH)
				.queryParam("filter", "adste").accept(MediaType.APPLICATION_JSON_TYPE)
				.get(JSONObject.class);
		
		int total2 = queueTypes.getInt("total");

		Assert.assertEquals("total not +1", total1 + 1, total2);
		
		// request a small page, look for total to be the same
		queueTypes = wr.path(QUEUE_TYPES_PATH_WITH_SLASH)
				.queryParam("filter", "stu").queryParam("page_size", "1")
				.accept(MediaType.APPLICATION_JSON).get(JSONObject.class);
		
		int total3 = queueTypes.getInt("total");
		
		Assert.assertEquals("total wrong for page", total1 + 1, total3);
	}
	
	@Test
	public void pagingTests() throws JSONException {
		// TODO: test last page
		// create 101 queueTypes
		for (int i = 0; i < 101; i++) {
			TestQueueTypeResource.createTestQueueType(wr, "unit_page_test" + i);
		}

		// test get default page (1), default page_size
		JSONObject queueTypes = wr.path(QUEUE_TYPES_PATH)
				.accept(MediaType.APPLICATION_JSON).get(JSONObject.class);

		// we should have at least a full page and at least 1 more item
		Assert.assertEquals("default paging items", 100, queueTypes
				.getJSONArray("queuetypes").length());
		Assert.assertEquals("default paging has_more", true,
				queueTypes.getBoolean("has_more"));

		// test get default page (1), custom size : 42
		// 42 = between[1,100] => we should get 42 time limits per page
		queueTypes = wr.path(QUEUE_TYPES_PATH)
				.queryParam("page_size", "42")
				.accept(MediaType.APPLICATION_JSON).get(JSONObject.class);
		// we should have at least a full page and at least 1 more item
		Assert.assertEquals("custom page size items", 42, queueTypes
				.getJSONArray("queuetypes").length());
		Assert.assertEquals("custom page size has_more", true,
				queueTypes.getBoolean("has_more"));

		// test get default page (1), custom invalid size : -1
		// -1 < 0 => size = DEFAULT_SIZE(100)
		queueTypes = wr.path(QUEUE_TYPES_PATH)
				.queryParam("page_size", "-1")
				.accept(MediaType.APPLICATION_JSON).get(JSONObject.class);
		// we should have at least a full page and at least 1 more item
		Assert.assertEquals("custom invalid page size -1 items", 100,
				queueTypes.getJSONArray("queuetypes").length());
		Assert.assertEquals("custom invalid page size -1 items", true,
				queueTypes.getBoolean("has_more"));

		// test get default page (1), custom invalid size : 101
		// 101 > 100 => size = DEFAULT_SIZE(100)
		queueTypes = wr.path(QUEUE_TYPES_PATH)
				.queryParam("page_size", "101")
				.accept(MediaType.APPLICATION_JSON).get(JSONObject.class);
		// we should have at least a full page and at least 1 more item
		Assert.assertEquals("custom invalid page size 100 items", 100,
				queueTypes.getJSONArray("queuetypes").length());
		Assert.assertEquals("custom invalid page size 100 items", true,
				queueTypes.getBoolean("has_more"));

		// test get default page (1), custom invalid size : aaa
		// aaa is not int > size= DEFAULT_SIZE(100)
		queueTypes = wr.path(QUEUE_TYPES_PATH)
				.queryParam("page_size", "aaa")
				.accept(MediaType.APPLICATION_JSON).get(JSONObject.class);
		// we should have at least a full page and at least 1 more item
		Assert.assertEquals("custom invalid page size aaa items", 100,
				queueTypes.getJSONArray("queuetypes").length());
		Assert.assertEquals("custom invalid page size aaa items", true,
				queueTypes.getBoolean("has_more"));

		// test get custom page(2), custom size: 2
		queueTypes = wr.path(QUEUE_TYPES_PATH).queryParam("page", "2")
				.queryParam("page_size", "2")
				.accept(MediaType.APPLICATION_JSON).get(JSONObject.class);

		// we should have at least a full page and at least 1 more item
		Assert.assertEquals("custom page 2 custom page size 2 items", 2,
				queueTypes.getJSONArray("queuetypes").length());
		Assert.assertEquals("custom page 2 custom page size 2 has_more", true,
				queueTypes.getBoolean("has_more"));
		String type1 = queueTypes.getJSONArray("queuetypes")
				.getJSONObject(1).getString("typename");

		// repeat with next page
		queueTypes = wr.path(QUEUE_TYPES_PATH).queryParam("page", "3")
				.queryParam("page_size", "2")
				.accept(MediaType.APPLICATION_JSON).get(JSONObject.class);

		// we should have at least a full page and at least 1 more item
		Assert.assertEquals("custom page 3 custom page size 2 items", 2,
				queueTypes.getJSONArray("queuetypes").length());
		Assert.assertEquals("custom page 3 custom page size 2 has_more", true,
				queueTypes.getBoolean("has_more"));
		String type2 = queueTypes.getJSONArray("queuetypes")
				.getJSONObject(1).getString("typename");

		// queue types typeid should be different
		Assert.assertNotEquals("different typeid on pages", type1, type2);

	}

	
}
