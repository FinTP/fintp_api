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
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import ro.allevo.fintpws.model.RoutingRuleEntity;
import ro.allevo.fintpws.resources.RoutingRuleResource;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;


@RunWith(JUnit4.class)
public class TestRoutingRuleResource {
		
	static final String ROUTING_RULES_PATH = "api/routingrules";
	static final String ROUTING_RULES_PATH_WITH_SLASH = ROUTING_RULES_PATH
			+ "/";
		
	
	static final String UNIT_TEST_DESCRIPTION = "unit test";
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
		TestRoutingSchemaResource.deleteTestRoutingSchema(wr, TestRoutingSchemaResource.UNIT_TEST_ROUTINGSCHEMA_NAME);
		TestTimeLimitResource.deleteTestTimeLimit(wr, "unit_test");
		TestTimeLimitResource.deleteTestTimeLimit(wr, "unit_test1");
		TestQueueResource.deleteTestQueue(wr, TestQueueResource.UNIT_TEST_Q_NAME);
	}

	public static void cleanup() throws JSONException {

		int page = 1;
		boolean findRecords = false;
		JSONObject routingRules = null;
		JSONObject routingSchemas = null;
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

	public static JSONObject createTestRoutingRule(WebResource wr,
			String description) throws JSONException {
		JSONObject rSchema = TestRoutingSchemaResource.findTestRoutingSchema(wr, TestRoutingSchemaResource.UNIT_TEST_ROUTINGSCHEMA_NAME);
		if (null == rSchema) {
			TestRoutingSchemaResource.createTestRoutingSchema(wr, 
					TestRoutingSchemaResource.UNIT_TEST_ROUTINGSCHEMA_NAME, null);
		}
		JSONObject routingSchema = TestRoutingSchemaResource.readTestRoutingSchema(wr, TestRoutingSchemaResource.UNIT_TEST_ROUTINGSCHEMA_NAME);
		
		JSONObject queue = TestQueueResource.findTestQueue(wr, TestQueueResource.UNIT_TEST_Q_NAME);
		if (null == queue) {
			TestQueueResource.createTestQueue(wr,
					TestQueueResource.UNIT_TEST_Q_NAME, "desc");
			queue = TestQueueResource.readTestQueue(wr,
					TestQueueResource.UNIT_TEST_Q_NAME);
		}
		JSONObject routingRule = new JSONObject()
				.put("guid", new Random().nextInt())
				.put("description", description)
				.put("schema", routingSchema.getString("name"))
				.put("queue", queue.getString("name"))
				.put("action", "action_unit_test").put("sequence", "100")
				.put("ruletype", "0");

		return TestHelper.assertResponseCreated(wr, "POST", ROUTING_RULES_PATH,
				routingRule);
	}

	public static JSONObject findTestRoutingRule(WebResource wr, String guid)
			throws JSONException {
		ClientResponse clientResponse = wr.path(ROUTING_RULES_PATH).path(guid)
				.accept(MediaType.APPLICATION_JSON)
				.type(MediaType.APPLICATION_JSON).get(ClientResponse.class);
		return (clientResponse.getClientResponseStatus()
				.equals(ClientResponse.Status.OK)) ? clientResponse
				.getEntity(JSONObject.class) : null;
	}

	public static JSONObject findRandomRoutingRule(WebResource wr)
			throws JSONException {

		JSONObject routingRules = wr.path(ROUTING_RULES_PATH)
				.queryParam("filter", "tb").accept(MediaType.APPLICATION_JSON)
				.get(JSONObject.class);
		JSONArray routingRulesArray = routingRules.getJSONArray("routingrules");
		if (routingRulesArray.length() == 0) {
			return null;
		}

		JSONObject routingRule = null;
		do {
			routingRule = routingRulesArray.getJSONObject(new Random()
					.nextInt(routingRulesArray.length()));
		} while (!routingRule.getString("description").startsWith("unit"));

		return routingRule;
	}

	public static void deleteTestRoutingRule(WebResource wr, String guid) {
		wr.path(ROUTING_RULES_PATH_WITH_SLASH).path(guid)
				.delete(ClientResponse.class);
	}
	
	public static void deleteTestRoutingRuleFromRSchema(WebResource wr, String guid,
			String routingSchemaName){
		wr.path(TestRoutingSchemaResource.ROUTING_SCHEMAS_PATH)
		.path(routingSchemaName).path("routingrules").path(guid)
		.delete(ClientResponse.class);
	}

	public static JSONObject readTestRoutingRule(WebResource wr, String guid)
			throws JSONException {
		return TestHelper.assertResponseOK(wr, "GET",
				ROUTING_RULES_PATH_WITH_SLASH + guid, null);
	}

	@Test
	public void createRoutingRule() throws JSONException {
		TestHelper.assertResponseMethodNotAllowed(wr, "POST",
				ROUTING_RULES_PATH_WITH_SLASH + new Random().nextInt(),
				null);
	}

	@Test
	public void readRoutingRule() throws JSONException {
		// create a routing rule with unit+random description
		String description = UNIT_TEST_DESCRIPTION + new Random().nextInt();
		JSONObject response = createTestRoutingRule(wr, description);

		// now read it
		JSONObject routingRule = readTestRoutingRule(wr,
				response.getString("id"));
		Assert.assertEquals("routing rule description not correct",
				description, routingRule.getString("description"));

		// make sure the metadata is returned
		Assert.assertEquals("href not correct", ROUTING_RULES_PATH_WITH_SLASH
				+ response.getString("id"), routingRule.getString("href"));
		Assert.assertEquals("_type not correct",
				RoutingRuleResource.class.getName(),
				routingRule.getString("_type"));

		// check 404 if the routing rule with the stupid name is requested
		TestHelper.assertResponseNotFound(wr, "GET",
				ROUTING_RULES_PATH_WITH_SLASH + "text", null);

	}
	
	@Test
	public void updateRoutingRuleOptionalFields() throws JSONException {
		// test update description
		// create a routing rule with a random description

		String desc1 = UNIT_TEST_DESCRIPTION + new Random().nextInt();
		JSONObject response = createTestRoutingRule(wr, desc1);

		// change description to a new random one
		String desc2 = UNIT_TEST_DESCRIPTION+ new Random().nextInt();
		JSONObject routingRule = readTestRoutingRule(wr,
				response.getString("id"));
		routingRule.remove("description");
		routingRule.put("description", desc2);
		TestHelper.assertResponseOK(wr, "PUT", ROUTING_RULES_PATH_WITH_SLASH
				+ response.getString("id"), routingRule);

		// assert it worked
		JSONObject routingRule2 = readTestRoutingRule(wr,
				response.getString("id"));
		Assert.assertEquals("description not correct", desc2,
				routingRule2.getString("description"));

		// test singular field update
		String desc3 = UNIT_TEST_DESCRIPTION + new Random().nextInt();
		JSONObject rrNewDesc = new JSONObject().put("description", desc3);
		TestHelper.assertResponseOK(wr, "PUT", ROUTING_RULES_PATH_WITH_SLASH
				+ response.getInt("id"), rrNewDesc);

		// assert it worked
		JSONObject routingRule3 = readTestRoutingRule(wr,
				response.getString("id"));
		Assert.assertEquals("description not correct", desc3,
				routingRule3.getString("description"));

	}

	@Test
	public void updateRoutingRuleAllFields() throws JSONException {
		JSONObject jsonEntity = TestUtils.fillResourceData(new JSONObject(),
				new RoutingRuleEntity());
		jsonEntity.remove("guid");
		jsonEntity.remove("schema");
		jsonEntity.remove("queue");
		jsonEntity.remove("description");

		// create a routing rule
		JSONObject response = createTestRoutingRule(wr, "unit1");

		// update all fields
		TestHelper.assertResponseOK(wr, "PUT", ROUTING_RULES_PATH_WITH_SLASH
				+ response.getString("id"), jsonEntity);

		// now read it
		JSONObject routingRule = readTestRoutingRule(wr,
				response.getString("id"));

		// assert it worked
		Assert.assertEquals(
				"roruting rules fields not correct",
				TestUtils
						.compareJSONObjects(new RoutingRuleEntity(),
								jsonEntity, routingRule,
								"guid,queueid,schemaguid,description,queueentity," +
								"routingschemaentity"),
				true);
	}

	@Test
	public void updateRoutingRuleLongDescription() throws JSONException {
		// create a rr with a random desc
		String desc = UNIT_TEST_DESCRIPTION + new Random().nextInt();
		JSONObject response = createTestRoutingRule(wr, desc);

		// update desc
		JSONObject routingRule = readTestRoutingRule(wr,
				response.getString("id"));
		routingRule.remove("description");
		routingRule
				.put("description", RandomStringUtils.randomAlphanumeric(71));
		TestHelper.assertResponseBadRequest(wr, "PUT",
				ROUTING_RULES_PATH_WITH_SLASH + response.getString("id"),
				routingRule);
	}

	@Ignore
	@Test
	public void updateRoutingRuleKeyFields() throws JSONException {
		// test update guid
		// create a rr with a random guid
		String desc = UNIT_TEST_DESCRIPTION+ new Random().nextInt();
		JSONObject response = createTestRoutingRule(wr, desc);

		/*
		 * TODO: update guid with existing returns code 405??
		 */

		/*
		 * TODO: insert rr with the same guid returns 409 (ok??)
		 * 
		 * 
		 */

		// create a second routing rule
		// update it's guid to guid of first rr

		JSONObject response2 = createTestRoutingRule(wr, desc);

		JSONObject routingRule2asJson = readTestRoutingRule(wr,
				response2.getString("id"));
		routingRule2asJson.remove("guid");
		routingRule2asJson.put("guid", response.getString("id"));

		TestHelper.assertResponseConflict(wr, "PUT",
				ROUTING_RULES_PATH_WITH_SLASH + response2.getString("id"),
				routingRule2asJson);

	}

	@Test
	public void deleteRoutingRule() throws JSONException {

		// create a rr with a random desc
		String desc = UNIT_TEST_DESCRIPTION + new Random().nextInt();
		JSONObject response = createTestRoutingRule(wr, desc);

		JSONObject routingRule = readTestRoutingRule(wr,
				response.getString("id"));

		// delete it
		TestHelper.assertResponseOK(wr, "DELETE", ROUTING_RULES_PATH_WITH_SLASH
				+ response.getString("id"), null);

		// read it again to make sure it's gone
		TestHelper.assertResponseNotFound(wr, "GET",
				ROUTING_RULES_PATH_WITH_SLASH + response.getString("id"), null);

		// check 404 if the routing rule with stupid guid requested
		TestHelper.assertResponseNotFound(wr, "DELETE",
				ROUTING_RULES_PATH_WITH_SLASH + "text", null);

	}
}
