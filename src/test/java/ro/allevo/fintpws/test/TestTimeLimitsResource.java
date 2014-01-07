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
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import ro.allevo.fintpws.model.TimeLimitEntity;
import ro.allevo.fintpws.resources.TimeLimitsResource;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;

/**
 * Tests for {@link TimeLimitsResource}.
 * 
 * @author remus
 * 
 */

@RunWith(JUnit4.class)
public class TestTimeLimitsResource {

	static final String TIME_LIMITS_PATH = "api/timelimits";
	static final String TIME_LIMITS_PATH_WITH_SLASH = TIME_LIMITS_PATH + "/";
	static final String TIME_LIMITS_PATH_ROUTINGSCHEMA = "api/routingschemas/"
			+ TestRoutingSchemaResource.UNIT_TEST_ROUTINGSCHEMA_NAME
			+"/timelimits";
	static final String TIME_LIMITS_PATH_ROUTINGSCHEMA_WITH_SLASH = 
			TIME_LIMITS_PATH_ROUTINGSCHEMA + "/";
	static final String IMPOSSIBLE_ROUTING_SCHEMAS_NAME = "2541imp";
	
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

		page = 1;
		findRecords = false;
		JSONObject routingSchemas = null;
		do {
			findRecords = false;
			routingSchemas = wr.path(TestRoutingSchemaResource.ROUTING_SCHEMAS_PATH)
					.queryParam("page", String.valueOf(page))
					.accept(MediaType.APPLICATION_JSON).get(JSONObject.class);
			JSONArray routingSchemasArray = routingSchemas
					.getJSONArray("routingschemas");
			for (int i = 0; i < routingSchemasArray.length(); i++) {

				String name = routingSchemasArray.getJSONObject(i).getString(
						"name");
				if (name.startsWith("unit")) {
					TestRoutingSchemaResource.deleteTestRoutingSchema(wr, name);
					findRecords = true;
				}
			}
			if (!findRecords) {
				page++;
			}
		} while (routingSchemas.has("has_more"));
		
	}
	
	@Test
	public void createTimeLimits() throws JSONException {
		// create a time limit with a random name
		String name = "unit test name" + new Random().nextInt();
		JSONObject response = TestTimeLimitResource.createTestTimeLimit(wr,
				name);
		Assert.assertTrue("uri missing", response.getString("uri").length() > 0);

		// make sure it is created
		JSONObject timeLimit = TestTimeLimitResource.readTestTimeLimit(wr,
				response.getString("id"));
		Assert.assertEquals("time limit limitname ", name,
				timeLimit.getString("limitname"));

		// make sure we get a conflict if we created it again
		TestHelper.assertResponseConflict(wr, "POST", TIME_LIMITS_PATH,
				timeLimit);
	}

	@Test
	public void insertTimeLimitsAllFields() throws JSONException {
		// generate a time limit (JSONObject)
		JSONObject jsonEntity = TestUtils.fillResourceData(new JSONObject(),
				new TimeLimitEntity());

		jsonEntity.remove("guid");
		jsonEntity.put("limitname", "unit"+jsonEntity.getString("limitname").substring(4));

		// create a time limit
		JSONObject response = TestHelper.assertResponseCreated(wr, "POST",
				TIME_LIMITS_PATH, jsonEntity);

		// now read it
		JSONObject timeLimit = TestTimeLimitResource.readTestTimeLimit(wr,
				response.getString("id"));

		// assert it worked
		Assert.assertEquals("time limit not filled correct", TestUtils
				.compareJSONObjects(new TimeLimitEntity(), jsonEntity,
						timeLimit, "guid,limittime"), true);
	}

	@Test
	public void readTimeLimitsAllFields() throws JSONException {
		// make sure it makes sense (>0 time limit available)
		TestTimeLimitResource.createTestTimeLimit(wr, "unit_desc");

		JSONObject timeLimits = wr.path(TIME_LIMITS_PATH)
				.accept(MediaType.APPLICATION_JSON).get(JSONObject.class);

		// make sure the metadata is returned
		Assert.assertEquals("href not correct", TIME_LIMITS_PATH,
				timeLimits.getString("href"));
		Assert.assertEquals("_type not correct",
				TimeLimitsResource.class.getName(),
				timeLimits.getString("_type"));

		JSONArray tlList = timeLimits.getJSONArray("timelimits");
		Assert.assertTrue("time limits list empty", tlList.length() > 0);

		// get first time limit, see if has limitname
		Assert.assertTrue("timelimits list empty", tlList.getJSONObject(0)
				.getString("limitname").length() > 0);

	}
	
	@Test
	public void readTimeLimits() throws JSONException{
		//make sure it makes sense (>0 time limits available)
		TestTimeLimitResource.createTestTimeLimit(wr, "unit_name");
		
		JSONObject timeLimits = wr.path(TIME_LIMITS_PATH)
				.accept(MediaType.APPLICATION_JSON).get(JSONObject.class);
		
		//make sure the metadata is returned
		Assert.assertEquals("href not correct", TIME_LIMITS_PATH, timeLimits.getString("href"));
		Assert.assertEquals("_type not correct", TimeLimitsResource.class.getName(), timeLimits.getString("_type"));
		
		JSONArray tlList = timeLimits.getJSONArray("timelimits");
		Assert.assertTrue("time limits list empty", tlList.length() > 0);
		
		// get first time limit, see if has limitname
		Assert.assertTrue("time limits list empty", tlList.getJSONObject(0).getString("limitname").length() > 0);
	}

	
	@Test
	public void readTimeLimitsFromRoutingSchemas() throws JSONException{
		//one routing schema will always have 2 timelimits
		
		TestRoutingSchemaResource.createTestRoutingSchema(wr,
				TestRoutingSchemaResource.UNIT_TEST_ROUTINGSCHEMA_NAME, null);
		
		final JSONObject timelimits = wr.path(TIME_LIMITS_PATH_ROUTINGSCHEMA)
				.accept(MediaType.APPLICATION_JSON).get(JSONObject.class);
		//make sure metadata is correct
		Assert.assertEquals("href not correct", TIME_LIMITS_PATH_ROUTINGSCHEMA,
				timelimits.getString("href"));
		Assert.assertEquals("_type not correct", TimeLimitsResource.class.getName(),
				timelimits.getString("_type"));
		
		//make sure there are 2 timelimits
		final JSONArray timelimitsList = timelimits.getJSONArray("timelimits");
		Assert.assertTrue("timelimts array length is 2", 
				timelimitsList.length() == 2 || timelimitsList.length() == 1);
		
		//get first time limit, see if it has limitname
		Assert.assertTrue("timelimits list empty", timelimitsList.getJSONObject(0)
				.getString("limitname").length() > 0);
		
		//delete routing schema and 2 timelimits refered
		
		//delete routing schema
		TestRoutingSchemaResource.deleteTestRoutingSchema(wr,
				TestRoutingSchemaResource.UNIT_TEST_ROUTINGSCHEMA_NAME);
		
		// read the routing schema to make sure it's gone
		TestHelper.assertResponseNotFound(wr, "GET",
						TestRoutingSchemaResource.ROUTING_SCHEMAS_PATH_WITH_SLASH + 
						TestRoutingSchemaResource.UNIT_TEST_ROUTINGSCHEMA_NAME,
						null);
		
		//delete time limits
		TestRoutingSchemaResource.deleteTestRoutingSchema(wr, "unit_start");
		TestRoutingSchemaResource.deleteTestRoutingSchema(wr, "unit_stop");
		
		//read the time limits to make sure they are gone
		TestHelper.assertResponseNotFound(wr, "GET", 
				TIME_LIMITS_PATH_ROUTINGSCHEMA_WITH_SLASH + "unit_start", null);
		TestHelper.assertResponseNotFound(wr, "GET", 
				TIME_LIMITS_PATH_ROUTINGSCHEMA_WITH_SLASH + "unit_stop", null);
		
		
	}
	
	@Test
	public void updateTimeLimits() throws JSONException{
		TestHelper.assertResponseMethodNotAllowed(wr, "PUT", TIME_LIMITS_PATH_WITH_SLASH, null);
	}
	
	@Test
	public void deleteTimeLimits() throws JSONException{
		TestHelper.assertResponseMethodNotAllowed(wr, "DELETE", TIME_LIMITS_PATH_WITH_SLASH, null);
	}
	
	@Test
	public void getTimeLimitsWithoutParent() throws JSONException {
		TestHelper
				.assertResponseNotFound(wr, "GET",
						TestRoutingSchemasResource.ROUTING_SCHEMAS_WITH_SLASH
								+ IMPOSSIBLE_ROUTING_SCHEMAS_NAME
								+ "/timelimits", null);
	}

	
	@Test
	public void totalTests() throws JSONException{
		// request t among some other garbage
		JSONObject timeLimits = wr.path(TIME_LIMITS_PATH_WITH_SLASH)
				.queryParam("filter", "adste").accept(MediaType.APPLICATION_JSON_TYPE)
				.get(JSONObject.class);
		
		int total1 = timeLimits.getInt("total");
		
		TestTimeLimitResource.createTestTimeLimit(wr, "unit_total");
		
		// request t among some other garbage
		timeLimits = wr.path(TIME_LIMITS_PATH_WITH_SLASH)
				.queryParam("filter", "adste").accept(MediaType.APPLICATION_JSON_TYPE)
				.get(JSONObject.class);
		
		int total2 = timeLimits.getInt("total");
		
		Assert.assertEquals("total not +1", total1 + 1, total2);
		
		// request a small page, look for total to be the same
		timeLimits = wr.path(TIME_LIMITS_PATH_WITH_SLASH)
				.queryParam("filter", "stu").queryParam("page_size", "1")
				.accept(MediaType.APPLICATION_JSON).get(JSONObject.class);
		
		int total3 = timeLimits.getInt("total");
		
		Assert.assertEquals("total wrong for page", total1 + 1, total3);
	}
	
	@Test
	public void pagingTests() throws JSONException {
		// TODO: test last page

		// create 101 timeLimits
		for (int i = 0; i < 101; i++) {
			TestTimeLimitResource.createTestTimeLimit(wr, "unit_page_test" + i);
		}

		// test get default page (1), default page_size
		JSONObject timeLimits = wr.path(TIME_LIMITS_PATH)
				.accept(MediaType.APPLICATION_JSON).get(JSONObject.class);

		// we should have at least a full page and at least 1 more item
		Assert.assertEquals("default paging items", 100, timeLimits
				.getJSONArray("timelimits").length());
		Assert.assertEquals("default paging has_more", true,
				timeLimits.getBoolean("has_more"));

		// test get default page (1), custom size : 42
		// 42 = between[1,100] => we should get 42 time limits per page
		timeLimits = wr.path(TIME_LIMITS_PATH)
				.queryParam("page_size", "42")
				.accept(MediaType.APPLICATION_JSON).get(JSONObject.class);
		// we should have at least a full page and at least 1 more item
		Assert.assertEquals("custom page size items", 42, timeLimits
				.getJSONArray("timelimits").length());
		Assert.assertEquals("custom page size has_more", true,
				timeLimits.getBoolean("has_more"));

		// test get default page (1), custom invalid size : -1
		// -1 < 0 => size = DEFAULT_SIZE(100)
		timeLimits = wr.path(TIME_LIMITS_PATH)
				.queryParam("page_size", "-1")
				.accept(MediaType.APPLICATION_JSON).get(JSONObject.class);
		// we should have at least a full page and at least 1 more item
		Assert.assertEquals("custom invalid page size -1 items", 100,
				timeLimits.getJSONArray("timelimits").length());
		Assert.assertEquals("custom invalid page size -1 items", true,
				timeLimits.getBoolean("has_more"));

		// test get default page (1), custom invalid size : 101
		// 101 > 100 => size = DEFAULT_SIZE(100)
		timeLimits = wr.path(TIME_LIMITS_PATH)
				.queryParam("page_size", "101")
				.accept(MediaType.APPLICATION_JSON).get(JSONObject.class);
		// we should have at least a full page and at least 1 more item
		Assert.assertEquals("custom invalid page size 100 items", 100,
				timeLimits.getJSONArray("timelimits").length());
		Assert.assertEquals("custom invalid page size 100 items", true,
				timeLimits.getBoolean("has_more"));

		// test get default page (1), custom invalid size : aaa
		// aaa is not int > size= DEFAULT_SIZE(100)
		timeLimits = wr.path(TIME_LIMITS_PATH)
				.queryParam("page_size", "aaa")
				.accept(MediaType.APPLICATION_JSON).get(JSONObject.class);
		// we should have at least a full page and at least 1 more item
		Assert.assertEquals("custom invalid page size aaa items", 100,
				timeLimits.getJSONArray("timelimits").length());
		Assert.assertEquals("custom invalid page size aaa items", true,
				timeLimits.getBoolean("has_more"));

		// test get custom page(2), custom size: 2
		timeLimits = wr.path(TIME_LIMITS_PATH).queryParam("page", "2")
				.queryParam("page_size", "2")
				.accept(MediaType.APPLICATION_JSON).get(JSONObject.class);

		// we should have at least a full page and at least 1 more item
		Assert.assertEquals("custom page 2 custom page size 2 items", 2,
				timeLimits.getJSONArray("timelimits").length());
		Assert.assertEquals("custom page 2 custom page size 2 has_more", true,
				timeLimits.getBoolean("has_more"));
		String limitname1 = timeLimits.getJSONArray("timelimits")
				.getJSONObject(1).getString("limitname");

		// repeat with next page
		timeLimits = wr.path(TIME_LIMITS_PATH).queryParam("page", "3")
				.queryParam("page_size", "2")
				.accept(MediaType.APPLICATION_JSON).get(JSONObject.class);

		// we should have at least a full page and at least 1 more item
		Assert.assertEquals("custom page 3 custom page size 2 items", 2,
				timeLimits.getJSONArray("timelimits").length());
		Assert.assertEquals("custom page 3 custom page size 2 has_more", true,
				timeLimits.getBoolean("has_more"));
		String limitname2 = timeLimits.getJSONArray("timelimits")
				.getJSONObject(1).getString("limitname");

		// time limits limitname should be different
		Assert.assertNotEquals("different limitnames on pages", limitname1, limitname2);

	}
}
