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

import ro.allevo.fintpws.model.RoutingSchemaEntity;
import ro.allevo.fintpws.resources.RoutingSchemasResource;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;

/**
 * Tests for {@link RoutingSchemasResource}.
 * 
 * @author remus
 * 
 */
@RunWith(JUnit4.class)
public class TestRoutingSchemasResource {
	static final String UNIT_TEST_ROUTINGSCHEMA_NAME = "unitrs";
	static final String ROUTING_SCHEMAS_PATH = "api/routingschemas";
	static final String ROUTING_SCHEMAS_WITH_SLASH = ROUTING_SCHEMAS_PATH + "/";
	
	static WebResource wr;
	//static String UNIT_TEST_START_LIMIT, UNIT_TEST_STOP_LIMIT;

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
		JSONObject routingSchemas = null;
		do {
			findRecords = false;
			routingSchemas = wr.path(ROUTING_SCHEMAS_PATH)
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
		page = 1;
		findRecords = false;
		JSONObject timeLimits = null;
		do {
			findRecords = false;
			timeLimits = wr.path(TestTimeLimitResource.TIME_LIMITS_PATH)
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

	@Test
	public void createRoutingSchemas() throws JSONException {
		// delete the rs if it already exists
		TestRoutingSchemaResource.deleteTestRoutingSchema(wr,
				UNIT_TEST_ROUTINGSCHEMA_NAME);

		// make sure it is deleted
		TestHelper.assertResponseNotFound(wr, "GET", ROUTING_SCHEMAS_WITH_SLASH
				+ UNIT_TEST_ROUTINGSCHEMA_NAME, null);

		// create it with a random desc
		String desc = "unit test rs desc" + new Random().nextInt();
		JSONObject response = TestRoutingSchemaResource
				.createTestRoutingSchema(wr, UNIT_TEST_ROUTINGSCHEMA_NAME, desc);
		Assert.assertTrue("uri missing", response.getString("uri").length() > 0);

		// make sure it is created
		response = TestRoutingSchemaResource.readTestRoutingSchema(wr,
				UNIT_TEST_ROUTINGSCHEMA_NAME);
		Assert.assertEquals("rs description not correct", desc,
				response.getString("description"));

		// make sure we get a conflict if we create it again
		TestHelper.assertResponseConflict(wr, "POST", ROUTING_SCHEMAS_PATH,
				response);

	}

	@Test
	public void insertRoutingSchemaAllFields() throws JSONException {
		// generate a rs(JSONObject format)
		JSONObject jsonEntity = TestUtils.fillResourceData(new JSONObject(),
				new RoutingSchemaEntity());
		JSONObject response1 = TestTimeLimitResource.createTestTimeLimit(wr,
					"unit_test_sch");
		String startLimit = response1.getString("id");
		JSONObject response2 = TestTimeLimitResource.createTestTimeLimit(wr,
				"unit_test_sc" );
		String stopLimit = response2.getString("id");

		jsonEntity.put("name",
				"unit" + jsonEntity.getString("name").substring(4));
		jsonEntity.put("startlimit", startLimit);
		jsonEntity.put("stoplimit", stopLimit);
		jsonEntity.remove("guid");
		// create a routing schema
		TestHelper.assertResponseCreated(wr, "POST", ROUTING_SCHEMAS_PATH,
				jsonEntity);
		// now read it
		JSONObject routingSchema = TestRoutingSchemaResource
				.readTestRoutingSchema(wr, jsonEntity.getString("name"));

		// assert it worked
		Assert.assertEquals("rs filled not correct", TestUtils
						.compareJSONObjects(new RoutingSchemaEntity(),
								jsonEntity, routingSchema,
								"guid,startlimit,stoplimit,startlimitentity,stoplimitentity"),
				true);

	}

	@Test
	public void readRoutingSchemas() throws JSONException {
		// make sure it makes sense (>0 rs available)
		TestRoutingSchemaResource.createTestRoutingSchema(wr,
				UNIT_TEST_ROUTINGSCHEMA_NAME, null);

		JSONObject routingSchemas = wr.path(ROUTING_SCHEMAS_PATH)
				.accept(MediaType.APPLICATION_JSON).get(JSONObject.class);

		// make sure the metadata is returned
		Assert.assertEquals("href not correct", ROUTING_SCHEMAS_PATH,
				routingSchemas.getString("href"));
		Assert.assertEquals("_type not correct",
				RoutingSchemasResource.class.getName(),
				routingSchemas.getString("_type"));

		JSONArray routingSchemasList = routingSchemas
				.getJSONArray("routingschemas");
		Assert.assertTrue("rs list empty", routingSchemasList.length() > 0);

		// get first rs, see if has a name
		Assert.assertTrue("rs list empty", routingSchemasList.getJSONObject(0)
				.getString("name").length() > 0);
	}

	@Test
	public void updateRoutingSchemas() throws JSONException {
		TestHelper.assertResponseMethodNotAllowed(wr, "PUT",
				ROUTING_SCHEMAS_WITH_SLASH, null);
	}

	@Test
	public void deleteRoutingSchemas() throws JSONException {
		TestHelper.assertResponseMethodNotAllowed(wr, "DELETE",
				ROUTING_SCHEMAS_WITH_SLASH, null);
	}

	@Test
	public void totalTests() throws JSONException {
		// request t among some other garbage
		JSONObject routingSchemas = wr.path(ROUTING_SCHEMAS_WITH_SLASH)
				.queryParam("filter", "stu").accept(MediaType.APPLICATION_JSON)
				.get(JSONObject.class);
		int nb1 = routingSchemas.getInt("total");

		TestRoutingSchemaResource.createTestRoutingSchema(wr,
				UNIT_TEST_ROUTINGSCHEMA_NAME, null);

		// request t among some other garbage
		routingSchemas = wr.path(ROUTING_SCHEMAS_WITH_SLASH)
				.queryParam("filter", "stu").accept(MediaType.APPLICATION_JSON)
				.get(JSONObject.class);
		int nb2 = routingSchemas.getInt("total");

		Assert.assertEquals("total not + 1", nb1 + 1, nb2);

		// request a small page. look for total to be the same
		routingSchemas = wr.path(ROUTING_SCHEMAS_WITH_SLASH)
				.queryParam("filter", "stu").queryParam("page_size", "1")
				.accept(MediaType.APPLICATION_JSON).get(JSONObject.class);
		int nb3 = routingSchemas.getInt("total");

		Assert.assertEquals("total wrong for page", nb1 + 1, nb3);

	}

	@Test
	public void pagingTests() throws JSONException {
		// create 101 routing schemas
		for (int i = 0; i < 101; i++) {
			TestRoutingSchemaResource.createTestRoutingSchema(wr,
					UNIT_TEST_ROUTINGSCHEMA_NAME + i, null);
		}

		// test get default page (1), default page_size
		JSONObject routingSchemas = wr.path(ROUTING_SCHEMAS_PATH)
				.accept(MediaType.APPLICATION_JSON).get(JSONObject.class);

		// we should have at least a full page and at least 1 more item
		Assert.assertEquals("default paging items", 100, routingSchemas
				.getJSONArray("routingschemas").length());
		Assert.assertEquals("default paging has_more", true,
				routingSchemas.getBoolean("has_more"));

		// test get default page (1), custom size : 42
		routingSchemas = wr.path(ROUTING_SCHEMAS_PATH)
				.queryParam("page_size", "42")
				.accept(MediaType.APPLICATION_JSON).get(JSONObject.class);

		// we should have at least a full page and at least 1 more item
		Assert.assertEquals("custom page size items", 42, routingSchemas
				.getJSONArray("routingschemas").length());
		Assert.assertEquals("custom page size has_more", true,
				routingSchemas.getBoolean("has_more"));

		// test get default page (1), custom invalid size : -1
		routingSchemas = wr.path(ROUTING_SCHEMAS_PATH)
				.queryParam("page_size", "-1")
				.accept(MediaType.APPLICATION_JSON).get(JSONObject.class);

		// we should have at least a full page and at least 1 more item
		Assert.assertEquals("custom invalid page size -1 items", 100,
				routingSchemas.getJSONArray("routingschemas").length());
		Assert.assertEquals("custom invalid page size -1 has_more", true,
				routingSchemas.getBoolean("has_more"));

		// test get default page (1), custom invalid size : 101
		routingSchemas = wr.path(ROUTING_SCHEMAS_PATH)
				.queryParam("page_size", "101")
				.accept(MediaType.APPLICATION_JSON).get(JSONObject.class);

		// we should have at least a full page and at least 1 more item
		Assert.assertEquals("custom invalid page size 101 items", 100,
				routingSchemas.getJSONArray("routingschemas").length());
		Assert.assertEquals("custom invalid page size 101 has_more", true,
				routingSchemas.getBoolean("has_more"));

		// test get default page (1), custom invalid size : aaa
		routingSchemas = wr.path(ROUTING_SCHEMAS_PATH)
				.queryParam("page_size", "aaa")
				.accept(MediaType.APPLICATION_JSON).get(JSONObject.class);

		// we should have at least a full page and at least 1 more item
		Assert.assertEquals("custom invalid page size aaa items", 100,
				routingSchemas.getJSONArray("routingschemas").length());
		Assert.assertEquals("custom invalid page size aaa paging has_more",
				true, routingSchemas.getBoolean("has_more"));

		// test get custom page (2), custom size : 2
		routingSchemas = wr.path(ROUTING_SCHEMAS_PATH).queryParam("page", "2")
				.queryParam("page_size", "2")
				.accept(MediaType.APPLICATION_JSON).get(JSONObject.class);

		// we should have at least a full page and at least 1 more item
		Assert.assertEquals("custom page 2 custom page size 2 items", 2,
				routingSchemas.getJSONArray("routingschemas").length());
		Assert.assertEquals("custom page 2 custom page size 2 has_more", true,
				routingSchemas.getBoolean("has_more"));
		String rs1name = routingSchemas.getJSONArray("routingschemas")
				.getJSONObject(1).getString("name");

		// repeat with next page
		routingSchemas = wr.path(ROUTING_SCHEMAS_PATH).queryParam("page", "3")
				.queryParam("page_size", "2")
				.accept(MediaType.APPLICATION_JSON).get(JSONObject.class);

		// we should have at least a full page and at least 1 more item
		Assert.assertEquals("custom page 3 custom page size 2 items", 2,
				routingSchemas.getJSONArray("routingschemas").length());
		Assert.assertEquals("custom page 3 custom page size 2 paging has_more",
				true, routingSchemas.getBoolean("has_more"));
		String rs2name = routingSchemas.getJSONArray("routingschemas")
				.getJSONObject(1).getString("name");

		// routing schema names should differ ( sloppy test to check another
		// page is
		// returned )
		Assert.assertNotEquals("different routing schemas on pages", rs1name,
				rs2name);
	}
}
