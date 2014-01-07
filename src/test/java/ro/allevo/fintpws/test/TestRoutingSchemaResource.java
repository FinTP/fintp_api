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
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import ro.allevo.fintpws.model.RoutingSchemaEntity;
import ro.allevo.fintpws.resources.RoutingSchemaResource;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;

@RunWith(JUnit4.class)
public class TestRoutingSchemaResource {
	static final String UNIT_TEST_ROUTINGSCHEMA_NAME = "unit_test";
	static final String ROUTING_SCHEMAS_PATH = "api/routingschemas";
	static final String ROUTING_SCHEMAS_PATH_WITH_SLASH = ROUTING_SCHEMAS_PATH
			+ "/";
	
	static WebResource wr;

	/*
	 * TODO: create startlimit, stoplimit, agter delte them
	 */

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
		// create timelimits we need for FK
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

	public static void deleteTestRoutingSchema(WebResource wr, String name) {
		wr.path(ROUTING_SCHEMAS_PATH_WITH_SLASH).path(name)
				.delete(ClientResponse.class);
	}

	public static JSONObject createTestRoutingSchema(WebResource wr,
			String name, String desc) throws JSONException {

		// TODO : test behaves odd because the filtering is disabled for the
		// moment: check later
		JSONObject timeLimitsFiltered = TestTimeLimitResource
				.findTestTimeLimit(wr, "unit_test");
		JSONObject response1;
		JSONObject firstTimeLimit;
		if (timeLimitsFiltered.getJSONArray("timelimits").length() == 0) {
			firstTimeLimit = TestTimeLimitResource.createTestTimeLimit(wr,
					"unit_test");
			response1 = TestTimeLimitResource.readTestTimeLimit(wr,
					firstTimeLimit.getString("id"));
		} else {
			response1 = TestTimeLimitResource.readTestTimeLimit(wr,
					timeLimitsFiltered.getJSONArray("timelimits")
							.getJSONObject(0).getString("limitname"));
		}
		String startLimitName = response1.getString("limitname");

		JSONObject timeLimitsFiltered2 = TestTimeLimitResource
				.findTestTimeLimit(wr, "unit_test1");
		JSONObject response2;
		JSONObject secondTimeLimit;

		if (timeLimitsFiltered2.getJSONArray("timelimits").length() == 0) {
			secondTimeLimit = TestTimeLimitResource.createTestTimeLimit(wr,
					"unit_test1");
			response2 = TestTimeLimitResource.readTestTimeLimit(wr,
					secondTimeLimit.getString("id"));
		} else {
			response2 = TestTimeLimitResource.readTestTimeLimit(wr,
					timeLimitsFiltered2.getJSONArray("timelimits")
							.getJSONObject(0).getString("limitname"));
		}

		String stopLimitName = response2.getString("limitname");
		JSONObject routingSchema = new JSONObject().put("name", name)
				.put("startlimit", startLimitName)
				.put("stoplimit", stopLimitName)
				.put("active", "1")
				.put("description", desc);
		return TestHelper.assertResponseCreated(wr, "POST",
				ROUTING_SCHEMAS_PATH, routingSchema);
	}
	
	public static JSONObject findTestRoutingSchema(WebResource wr, String name)
			throws JSONException {

		ClientResponse clientResponse = wr.path(ROUTING_SCHEMAS_PATH)
				.path(name).accept(MediaType.APPLICATION_JSON)
				.type(MediaType.APPLICATION_JSON).get(ClientResponse.class);
		return (clientResponse.getClientResponseStatus()
				.equals(ClientResponse.Status.OK)) ? clientResponse
				.getEntity(JSONObject.class) : null;
	}

	public static JSONObject findRandomRoutingSchema(WebResource wr)
			throws JSONException {

		JSONObject routingSchemas = wr.path(ROUTING_SCHEMAS_PATH)
				.queryParam("filter", "tb").accept(MediaType.APPLICATION_JSON)
				.get(JSONObject.class);
		JSONArray routingSchemasArray = routingSchemas
				.getJSONArray("routingschemas");
		if (routingSchemasArray.length() == 0) {
			return null;
		}

		JSONObject routingSchema = null;
		do {
			routingSchema = routingSchemasArray.getJSONObject(new Random()
					.nextInt(routingSchemasArray.length()));
		} while (!routingSchema.getString("name").startsWith("unit"));

		return routingSchema;
	}

	public static JSONObject readTestRoutingSchema(WebResource wr, String name)
			throws JSONException {

		return TestHelper.assertResponseOK(wr, "GET",
				ROUTING_SCHEMAS_PATH_WITH_SLASH + name, null);
	}

	@Test
	public void createRoutingSchema() throws JSONException {
		TestHelper.assertResponseMethodNotAllowed(wr, "POST",
				ROUTING_SCHEMAS_PATH_WITH_SLASH + UNIT_TEST_ROUTINGSCHEMA_NAME,
				null);
	}

	@Test
	public void readRoutingSchema() throws JSONException {
		// create a routing schema with unit+random name
		String name = "unit" + RandomStringUtils.randomAlphanumeric(6);

		createTestRoutingSchema(wr, name, null);

		// now read it
		JSONObject routingSchema = readTestRoutingSchema(wr, name);
		Assert.assertEquals("routing schema name not correct", name,
				routingSchema.getString("name"));

		// make sure the metadata is returned
		Assert.assertEquals("href not correct", ROUTING_SCHEMAS_PATH_WITH_SLASH
				+ name, routingSchema.getString("href"));
		Assert.assertEquals("_type not correct",
				RoutingSchemaResource.class.getName(),
				routingSchema.getString("_type"));

		// check 404 if the routing schema with the stupid name is requested
		TestHelper.assertResponseNotFound(wr, "GET",
				ROUTING_SCHEMAS_PATH_WITH_SLASH + new Random().nextDouble(),
				null);
	}

	@Test
	public void updateRoutingSchemaOptionalFields() throws JSONException {
		// test update description
		// create a routing schema with a random description
		String desc1 = "unit test rs desc" + new Random().nextInt();
		createTestRoutingSchema(wr, UNIT_TEST_ROUTINGSCHEMA_NAME, desc1);

		// change description to a new random one
		String desc2 = "unit test rs desc" + new Random().nextInt();
		JSONObject routingSchema = readTestRoutingSchema(wr,
				UNIT_TEST_ROUTINGSCHEMA_NAME);
		routingSchema.remove("description");
		routingSchema.put("description", desc2);
		TestHelper.assertResponseOK(wr, "PUT", ROUTING_SCHEMAS_PATH_WITH_SLASH
				+ UNIT_TEST_ROUTINGSCHEMA_NAME, routingSchema);

		// assert it worked
		JSONObject routingSchema2 = readTestRoutingSchema(wr,
				UNIT_TEST_ROUTINGSCHEMA_NAME);
		Assert.assertEquals("rs description not corrent", desc2,
				routingSchema2.getString("description"));

		// test singular field update
		String desc3 = "unit test rs desc" + new Random().nextInt();
		JSONObject rsNewDesc = new JSONObject().put("description", desc3);
		TestHelper.assertResponseOK(wr, "PUT", ROUTING_SCHEMAS_PATH_WITH_SLASH
				+ UNIT_TEST_ROUTINGSCHEMA_NAME, rsNewDesc);

		// assert it worked
		JSONObject routingSchema3 = readTestRoutingSchema(wr,
				UNIT_TEST_ROUTINGSCHEMA_NAME);
		Assert.assertEquals("rs description not corrent", desc3,
				routingSchema3.getString("description"));

	}

	@Test
	public void updateRoutingSchemaLongName() throws JSONException {
		// create a rs with a random desc
		String desc = "unit test rs desc" + new Random().nextInt();
		createTestRoutingSchema(wr, UNIT_TEST_ROUTINGSCHEMA_NAME, desc);

		// update name
		JSONObject routingSchema = readTestRoutingSchema(wr,
				UNIT_TEST_ROUTINGSCHEMA_NAME);
		routingSchema.remove("name");
		routingSchema.put("name", "01234567891");
		TestHelper.assertResponseBadRequest(wr, "PUT",
				ROUTING_SCHEMAS_PATH_WITH_SLASH + UNIT_TEST_ROUTINGSCHEMA_NAME,
				routingSchema);
	}

	@Test
	public void updateRoutingSchemaAllFields() throws JSONException {
		// generate a routingSchema (JSONObject format)
		JSONObject jsonEntity = TestUtils.fillResourceData(new JSONObject(),
				new RoutingSchemaEntity());
		jsonEntity.remove("name");
		jsonEntity.remove("guid");
		jsonEntity.remove("startlimit");
		jsonEntity.remove("stoplimit");

		// create a routing schema
		createTestRoutingSchema(wr, UNIT_TEST_ROUTINGSCHEMA_NAME, "desc");

		// update all fields
		TestHelper.assertResponseOK(wr, "PUT", ROUTING_SCHEMAS_PATH_WITH_SLASH
				+ UNIT_TEST_ROUTINGSCHEMA_NAME, jsonEntity);

		// now read it
		JSONObject routingSchema = readTestRoutingSchema(wr,
				UNIT_TEST_ROUTINGSCHEMA_NAME);

		// assert it worked
		Assert.assertEquals("rs fields not correct", TestUtils
				.compareJSONObjects(new RoutingSchemaEntity(), jsonEntity,
						routingSchema, "guid,name,startlimit,stoplimit,startlimitentity,stoplimitentity"), true);

	}

	@Test
	public void deleteRoutingSchema() throws JSONException {
		// create a rs with a random description
		String desc = "unit test rs desc" + new Random().nextInt();
		createTestRoutingSchema(wr, UNIT_TEST_ROUTINGSCHEMA_NAME, desc);

		readTestRoutingSchema(wr, UNIT_TEST_ROUTINGSCHEMA_NAME);

		// delete it
		TestHelper.assertResponseOK(wr, "DELETE",
				ROUTING_SCHEMAS_PATH_WITH_SLASH + UNIT_TEST_ROUTINGSCHEMA_NAME,
				null);

		// read it again to make sure it's gone
		TestHelper.assertResponseNotFound(wr, "GET",
				ROUTING_SCHEMAS_PATH_WITH_SLASH + UNIT_TEST_ROUTINGSCHEMA_NAME,
				null);

		// check 404 if the rs with stupid name is requested
		TestHelper.assertResponseNotFound(wr, "DELETE",
				ROUTING_SCHEMAS_PATH_WITH_SLASH + new Random().nextInt(), null);

	}

}
