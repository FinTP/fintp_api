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

import ro.allevo.fintpws.model.RoutingRuleEntity;
import ro.allevo.fintpws.resources.RoutingRulesResource;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;

/**
 * Tests for {@link RoutingRulesResource}.
 * 
 * @author remus
 * 
 */
@RunWith(JUnit4.class)
public class TestRoutingRulesResource {

	static final String ROUTING_RULES_PATH = "api/routingrules";
	static final String ROUTING_RULES_PATH_WITH_SLASH = ROUTING_RULES_PATH
			+ "/";

	static final String IMPOSSIBLE_ROUTING_RULES_NAME = "abcde12";

	
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
		TestRoutingSchemaResource.deleteTestRoutingSchema(wr, TestRoutingSchemaResource.UNIT_TEST_ROUTINGSCHEMA_NAME);
		TestTimeLimitResource.deleteTestTimeLimit(wr, "unit_test");
		TestTimeLimitResource.deleteTestTimeLimit(wr, "unit_test1");
		
		TestQueueResource.deleteTestQueue(wr, TestQueueResource.UNIT_TEST_Q_NAME);
	}

	public static void cleanup() throws JSONException {

		int page = 1;
		boolean findRecords = false;
		JSONObject routingRules = null;
		do {
			findRecords = false;
			routingRules = wr.path(ROUTING_RULES_PATH)
					.queryParam("page", String.valueOf(page))
					.accept(MediaType.APPLICATION_JSON).get(JSONObject.class);
			JSONArray routingRulesArray = routingRules
					.getJSONArray("routingrules");
			for (int i = 0; i < routingRulesArray.length(); i++) {

				String description = routingRulesArray.getJSONObject(i)
						.optString("description");
				String guid = routingRulesArray.getJSONObject(i).getString(
						"guid");
				if (description.startsWith("unit")) {
					TestRoutingRuleResource.deleteTestRoutingRule(wr, guid);
					findRecords = true;
				}
			}
			if (!findRecords) {
				page++;
			}
		} while (routingRules.has("has_more"));
		
		page = 1;
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
		} while (routingRules.has("has_more"));
	}

	@Test
	public void createRoutingRules() throws JSONException {
		// create a routing rule with a random description
		String desc = TestRoutingRuleResource.UNIT_TEST_DESCRIPTION + new Random().nextInt();
		JSONObject response = TestRoutingRuleResource.createTestRoutingRule(wr, desc);
		Assert.assertTrue("uri missing", response.getString("uri").length() > 0);

		// make sure it is created
		JSONObject routingRule = TestRoutingRuleResource.readTestRoutingRule(
				wr, response.getString("id"));
		Assert.assertEquals("routing rule description not correct", desc,
				routingRule.getString("description"));

		// make sure we get a conflict if we create it again
		TestHelper.assertResponseConflict(wr, "POST", ROUTING_RULES_PATH,
				routingRule);
	}

	@Test
	public void insertRoutingRulesAllFields() throws JSONException {
		// generate a routing rule (JSONObject format)
		JSONObject jsonEntity = TestUtils.fillResourceData(new JSONObject(),
				new RoutingRuleEntity());

		JSONObject rSchema = TestRoutingSchemaResource.findTestRoutingSchema(wr, TestRoutingSchemaResource.UNIT_TEST_ROUTINGSCHEMA_NAME);
		if (null == rSchema) {
			TestRoutingSchemaResource.createTestRoutingSchema(wr, 
					TestRoutingSchemaResource.UNIT_TEST_ROUTINGSCHEMA_NAME, null);
		}
		JSONObject routingSchema = TestRoutingSchemaResource.readTestRoutingSchema(wr, TestRoutingSchemaResource.UNIT_TEST_ROUTINGSCHEMA_NAME);
		
		
		jsonEntity.remove("guid");
		jsonEntity.put("description",
				"unit" + jsonEntity.getString("description").substring(4));
		jsonEntity.put("schema", routingSchema.getString("name"));
		

		JSONObject queue = TestQueueResource.findTestQueue(wr, TestQueueResource.UNIT_TEST_Q_NAME);
		if (null == queue) {
			TestQueueResource.createTestQueue(wr,
					TestQueueResource.UNIT_TEST_Q_NAME, "desc");
			queue = TestQueueResource.readTestQueue(wr,
					TestQueueResource.UNIT_TEST_Q_NAME);
		}
		
		jsonEntity.put("queue", queue.getString("name"));

		// create a routing rule
		JSONObject response = TestHelper.assertResponseCreated(wr, "POST",
				ROUTING_RULES_PATH, jsonEntity);
		// now read it
		JSONObject routingRule = TestRoutingRuleResource.readTestRoutingRule(
				wr, response.getString("id"));

		// assert it worked
		Assert.assertEquals("routing rule not filled correct", TestUtils
				.compareJSONObjects(new RoutingRuleEntity(), jsonEntity,
						routingRule, "guid,schemaguid,routingschemaentity,queueentity,queueid"), true);
	}

	@Test
	public void readRoutingRules() throws JSONException {
		// make sure it makes sense ( >0 routing rule available)
		TestRoutingRuleResource.createTestRoutingRule(wr,
				TestRoutingRuleResource.UNIT_TEST_DESCRIPTION);
		
		JSONObject routingRules = wr.path(ROUTING_RULES_PATH)
				.accept(MediaType.APPLICATION_JSON).get(JSONObject.class);

		// make sure the metadata is returned
		Assert.assertEquals("href not correct", ROUTING_RULES_PATH,
				routingRules.getString("href"));
		Assert.assertEquals("_type not correct",
				RoutingRulesResource.class.getName(),
				routingRules.getString("_type"));

		JSONArray rrList = routingRules.getJSONArray("routingrules");
		Assert.assertTrue("routing rules list empty", rrList.length() > 0);

		// get first routing rule, see if has guid
		Assert.assertTrue("routing rules list empty", rrList.getJSONObject(0)
				.getString("guid").length() > 0);

	}
	
	@Test
	public void readRoutingRulesFromRoutingSchema() throws JSONException{
		
		// create routing rule referring routingschema(make sure it makes sense)
		JSONObject response = TestRoutingRuleResource
				.createTestRoutingRule(wr, TestRoutingRuleResource.UNIT_TEST_DESCRIPTION);

		final JSONObject routingRules = wr.path(
				TestRoutingSchemaResource.ROUTING_SCHEMAS_PATH_WITH_SLASH + TestRoutingSchemaResource.UNIT_TEST_ROUTINGSCHEMA_NAME 
				+ "/routingrules" )
				.accept(MediaType.APPLICATION_JSON).get(JSONObject.class);

		// make sure the metadata is returned
		final JSONArray routingRulesList = routingRules
				.getJSONArray("routingrules");
		Assert.assertTrue("routingrules list empty",
				routingRulesList.length() > 0);

		// get first routing rule, see if has a guid
		Assert.assertTrue("routingrules list empty", routingRulesList
				.getJSONObject(0).getString("guid").length() > 0);

		// delete routing rule created (in order to be able to delete rs)
		TestRoutingRuleResource.deleteTestRoutingRule(wr,
				response.getString("id"));

	}

	@Test
	public void updateRoutingRules() throws JSONException {
		TestHelper.assertResponseMethodNotAllowed(wr, "PUT",
				ROUTING_RULES_PATH_WITH_SLASH, null);
	}

	@Test
	public void deleteRoutingRules() throws JSONException {
		TestHelper.assertResponseMethodNotAllowed(wr, "DELETE",
				ROUTING_RULES_PATH_WITH_SLASH, null);
	}

	@Test
	public void deleteRoutingRulesFromRoutingSchema() throws JSONException{
			TestHelper.assertResponseMethodNotAllowed(wr, "DELETE",
					 ROUTING_RULES_PATH_WITH_SLASH , null);
	}
	
	@Test
	public void getRoutingRulesWithoutParent() throws JSONException {
		TestHelper
				.assertResponseNotFound(wr, "GET",
						TestRoutingSchemasResource.ROUTING_SCHEMAS_WITH_SLASH
								+ IMPOSSIBLE_ROUTING_RULES_NAME
								+ "/routingrules", null);
	}
	
	@Test
	public void totalTests() throws JSONException {
		// request t among some garbage
		JSONObject routingRules = wr.path(ROUTING_RULES_PATH_WITH_SLASH)
				.queryParam("filter", "stu").accept(MediaType.APPLICATION_JSON)
				.get(JSONObject.class);

		int nbRoutingRules = routingRules.getInt("total");

		TestRoutingRuleResource.createTestRoutingRule(wr, TestRoutingRuleResource.UNIT_TEST_DESCRIPTION);

		// request t among other garbage
		routingRules = wr.path(ROUTING_RULES_PATH_WITH_SLASH)
				.queryParam("filter", "stu").accept(MediaType.APPLICATION_JSON)
				.get(JSONObject.class);
		int nbRoutingRueles2 = routingRules.getInt("total");

		Assert.assertEquals("total not +1", nbRoutingRules + 1,
				nbRoutingRueles2);

		// request a small page, look for total to be the same
		routingRules = wr.path(ROUTING_RULES_PATH_WITH_SLASH)
				.queryParam("filter", "stu").queryParam("page_size", "1")
				.accept(MediaType.APPLICATION_JSON).get(JSONObject.class);
		int nbRoutingRueles3 = routingRules.getInt("total");

		Assert.assertEquals("total wrong for page", nbRoutingRules + 1,
				nbRoutingRueles3);
	}
	
	//TODO : totalTestsRoutingSchema

	@Test
	public void pagingTests() throws JSONException {
	

		// create 101 routingRules
		for (int i = 0; i < 101; i++) {
			TestRoutingRuleResource.createTestRoutingRule(wr, TestRoutingRuleResource.UNIT_TEST_DESCRIPTION + i);
		}

		// test get default page (1), default page_size
		JSONObject routingRules = wr.path(ROUTING_RULES_PATH)
				.accept(MediaType.APPLICATION_JSON).get(JSONObject.class);

		// we should have at least a full page and at least 1 more item
		Assert.assertEquals("default paging items", 100, routingRules
				.getJSONArray("routingrules").length());
		Assert.assertEquals("default paging has_more", true,
				routingRules.getBoolean("has_more"));

		// test get default page (1), custom size : 42
		// 42 = between[1,100] => we should get 42 routing rules per page
		routingRules = wr.path(ROUTING_RULES_PATH)
				.queryParam("page_size", "42")
				.accept(MediaType.APPLICATION_JSON).get(JSONObject.class);
		// we should have at least a full page and at least 1 more item
		Assert.assertEquals("custom page size items", 42, routingRules
				.getJSONArray("routingrules").length());
		Assert.assertEquals("custom page size has_more", true,
				routingRules.getBoolean("has_more"));

		// test get default page (1), custom invalid size : -1
		// -1 < 0 => size = DEFAULT_SIZE(100)
		routingRules = wr.path(ROUTING_RULES_PATH)
				.queryParam("page_size", "-1")
				.accept(MediaType.APPLICATION_JSON).get(JSONObject.class);
		// we should have at least a full page and at least 1 more item
		Assert.assertEquals("custom invalid page size -1 items", 100,
				routingRules.getJSONArray("routingrules").length());
		Assert.assertEquals("custom invalid page size -1 items", true,
				routingRules.getBoolean("has_more"));

		// test get default page (1), custom invalid size : 101
		// 101 > 100 => size = DEFAULT_SIZE(100)
		routingRules = wr.path(ROUTING_RULES_PATH)
				.queryParam("page_size", "101")
				.accept(MediaType.APPLICATION_JSON).get(JSONObject.class);
		// we should have at least a full page and at least 1 more item
		Assert.assertEquals("custom invalid page size 100 items", 100,
				routingRules.getJSONArray("routingrules").length());
		Assert.assertEquals("custom invalid page size 100 items", true,
				routingRules.getBoolean("has_more"));

		// test get default page (1), custom invalid size : aaa
		// aaa is not int > size= DEFAULT_SIZE(100)
		routingRules = wr.path(ROUTING_RULES_PATH)
				.queryParam("page_size", "aaa")
				.accept(MediaType.APPLICATION_JSON).get(JSONObject.class);
		// we should have at least a full page and at least 1 more item
		Assert.assertEquals("custom invalid page size aaa items", 100,
				routingRules.getJSONArray("routingrules").length());
		Assert.assertEquals("custom invalid page size aaa items", true,
				routingRules.getBoolean("has_more"));

		// test get custom page(2), custom size: 2
		routingRules = wr.path(ROUTING_RULES_PATH).queryParam("page", "2")
				.queryParam("page_size", "2")
				.accept(MediaType.APPLICATION_JSON).get(JSONObject.class);

		// we should have at least a full page and at least 1 more item
		Assert.assertEquals("custom page 2 custom page size 2 items", 2,
				routingRules.getJSONArray("routingrules").length());
		Assert.assertEquals("custom page 2 custom page size 2 has_more", true,
				routingRules.getBoolean("has_more"));
		String guid1 = routingRules.getJSONArray("routingrules")
				.getJSONObject(1).getString("guid");

		// repeat with next page
		routingRules = wr.path(ROUTING_RULES_PATH).queryParam("page", "3")
				.queryParam("page_size", "2")
				.accept(MediaType.APPLICATION_JSON).get(JSONObject.class);

		// we should have at least a full page and at least 1 more item
		Assert.assertEquals("custom page 3 custom page size 2 items", 2,
				routingRules.getJSONArray("routingrules").length());
		Assert.assertEquals("custom page 3 custom page size 2 has_more", true,
				routingRules.getBoolean("has_more"));
		String guid2 = routingRules.getJSONArray("routingrules")
				.getJSONObject(1).getString("guid");

		// routing rules guid should be different
		Assert.assertNotEquals("different guids on pages", guid1, guid2);

	}

	@Test
	public void pagingTestsRoutingSchema() throws JSONException {
		

		// create 101 routingRules
		for (int i = 0; i < 101; i++) {
			TestRoutingRuleResource.createTestRoutingRule(wr, TestRoutingRuleResource.UNIT_TEST_DESCRIPTION);
		}

		// test get default page (1), default page_size
		JSONObject routingRules = wr.path(ROUTING_RULES_PATH)
				.accept(MediaType.APPLICATION_JSON).get(JSONObject.class);

		// we should have at least a full page and at least 1 more item
		Assert.assertEquals("default paging items", 100, routingRules
				.getJSONArray("routingrules").length());
		Assert.assertEquals("default paging has_more", true,
				routingRules.getBoolean("has_more"));

		// test get default page (1), custom size : 42
		// 42 = between[1,100] => we should get 42 routing rules per page
		routingRules = wr.path(ROUTING_RULES_PATH)
				.queryParam("page_size", "42")
				.accept(MediaType.APPLICATION_JSON).get(JSONObject.class);
		// we should have at least a full page and at least 1 more item
		Assert.assertEquals("custom page size items", 42, routingRules
				.getJSONArray("routingrules").length());
		Assert.assertEquals("custom page size has_more", true,
				routingRules.getBoolean("has_more"));

		// test get default page (1), custom invalid size : -1
		// -1 < 0 => size = DEFAULT_SIZE(100)
		routingRules = wr.path(ROUTING_RULES_PATH)
				.queryParam("page_size", "-1")
				.accept(MediaType.APPLICATION_JSON).get(JSONObject.class);
		// we should have at least a full page and at least 1 more item
		Assert.assertEquals("custom invalid page size -1 items", 100,
				routingRules.getJSONArray("routingrules").length());
		Assert.assertEquals("custom invalid page size -1 items", true,
				routingRules.getBoolean("has_more"));

		// test get default page (1), custom invalid size : 101
		// 101 > 100 => size = DEFAULT_SIZE(100)
		routingRules = wr.path(ROUTING_RULES_PATH)
				.queryParam("page_size", "101")
				.accept(MediaType.APPLICATION_JSON).get(JSONObject.class);
		// we should have at least a full page and at least 1 more item
		Assert.assertEquals("custom invalid page size 100 items", 100,
				routingRules.getJSONArray("routingrules").length());
		Assert.assertEquals("custom invalid page size 100 items", true,
				routingRules.getBoolean("has_more"));

		// test get default page (1), custom invalid size : aaa
		// aaa is not int > size= DEFAULT_SIZE(100)
		routingRules = wr.path(ROUTING_RULES_PATH)
				.queryParam("page_size", "aaa")
				.accept(MediaType.APPLICATION_JSON).get(JSONObject.class);
		// we should have at least a full page and at least 1 more item
		Assert.assertEquals("custom invalid page size aaa items", 100,
				routingRules.getJSONArray("routingrules").length());
		Assert.assertEquals("custom invalid page size aaa items", true,
				routingRules.getBoolean("has_more"));

		// test get custom page(2), custom size: 2
		routingRules = wr.path(ROUTING_RULES_PATH).queryParam("page", "2")
				.queryParam("page_size", "2")
				.accept(MediaType.APPLICATION_JSON).get(JSONObject.class);

		// we should have at least a full page and at least 1 more item
		Assert.assertEquals("custom page 2 custom page size 2 items", 2,
				routingRules.getJSONArray("routingrules").length());
		Assert.assertEquals("custom page 2 custom page size 2 has_more", true,
				routingRules.getBoolean("has_more"));
		String guid1 = routingRules.getJSONArray("routingrules")
				.getJSONObject(1).getString("guid");

		// repeat with next page
		routingRules = wr.path(ROUTING_RULES_PATH).queryParam("page", "3")
				.queryParam("page_size", "2")
				.accept(MediaType.APPLICATION_JSON).get(JSONObject.class);

		// we should have at least a full page and at least 1 more item
		Assert.assertEquals("custom page 3 custom page size 2 items", 2,
				routingRules.getJSONArray("routingrules").length());
		Assert.assertEquals("custom page 3 custom page size 2 has_more", true,
				routingRules.getBoolean("has_more"));
		String guid2 = routingRules.getJSONArray("routingrules")
				.getJSONObject(1).getString("guid");

		// routing rules guid should be different
		Assert.assertNotEquals("different guids on pages", guid1, guid2);
	}
}
