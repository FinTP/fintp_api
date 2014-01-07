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

import java.math.BigDecimal;
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

import ro.allevo.fintpws.model.RoutingKeyWordMapEntity;
import ro.allevo.fintpws.resources.RoutingKeyWordMapResource;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;

/**
 * 
 * @author Edi
 * 
 */

@RunWith(JUnit4.class)
public class TestRoutingKeyWordMapResource {

	static final String ROUTING_KEY_WORDS_MAPS_PATH = "api/routingkeywordmaps";
	static final String ROUTING_KEY_WORDS_MAPS_PATH_WITH_SLASH = ROUTING_KEY_WORDS_MAPS_PATH
			+ "/";
	static String UNIT_TEST_R_KEYWORD = "unit test keyword";
	
	static WebResource wr;
	
	@BeforeClass
	public static void startMethod() {
		final ClientConfig cc = new DefaultClientConfig();
		final Client c = Client.create();
		c.addFilter(new HTTPBasicAuthFilter("admin", "admin"));
		wr = c.resource(TestUtils.getUrlBase());
	}

	@Before
	public void cleanupBefore() throws JSONException {
		cleanup();
		//create routing key word we need for FK
		JSONObject response1 = TestRoutingKeyWordResource
				.createTestRoutingKeyWord(wr, UNIT_TEST_R_KEYWORD);
	}

	@After
	public void cleanupAfter() throws JSONException {
		cleanup();
		// delete routing key word we need for FK
		TestRoutingKeyWordResource.deleteTestRoutingKeyWord(wr,
				UNIT_TEST_R_KEYWORD);
	}


	
	public static void cleanup() throws JSONException {

		int page = 1;
		boolean findRecords = false;
		JSONObject routingKeyWordMaps = null;
		do {
			findRecords = false;
			routingKeyWordMaps = wr.path(ROUTING_KEY_WORDS_MAPS_PATH)
					.queryParam("page", String.valueOf(page))
					.accept(MediaType.APPLICATION_JSON).get(JSONObject.class);
			JSONArray routingKeyWordMapsArray = routingKeyWordMaps
					.getJSONArray("routingkeywordmaps");
			for (int i = 0; i < routingKeyWordMapsArray.length(); i++) {

				String tag = routingKeyWordMapsArray.getJSONObject(i)
						.getString("tag");

				String mapid = routingKeyWordMapsArray
						.getJSONObject(i).getString("mapid");

				if (tag.startsWith("unit")) {
					TestRoutingKeyWordMapResource.deleteTestRoutingKeyWordMap(
							wr, mapid);
					findRecords = true;
				}
			}
			if (!findRecords) {
				page++;
			}
		} while (routingKeyWordMaps.has("has_more"));

	}

	
	public static JSONObject createTestRoutingKeyWordMap(WebResource wr,
			String tag) throws JSONException {
		JSONObject routingKeyWordMap = new JSONObject()
				.put("keyword", UNIT_TEST_R_KEYWORD)
				.put("tag", tag).put("selector", "2013")
				.put("mt", "1254mt");

		return TestHelper.assertResponseCreated(wr, "POST",
				ROUTING_KEY_WORDS_MAPS_PATH, routingKeyWordMap);
	}
	

	

	public static JSONObject createTestRoutingKeyWordMap(WebResource wr,
			String tag, String keyword) throws JSONException {
		JSONObject routingKeyWordMap = new JSONObject()
				.put("keyword", keyword).put("tag", tag)
				.put("selector", "2013").put("mt", "1254mt");

		return TestHelper.assertResponseCreated(wr, "POST",
				ROUTING_KEY_WORDS_MAPS_PATH, routingKeyWordMap);
	}

	public static JSONObject findTestRoutingKeyWordMap(WebResource wr,
			String tag) {
		ClientResponse clientResponse = wr.path(ROUTING_KEY_WORDS_MAPS_PATH)
				.path(tag).accept(MediaType.APPLICATION_JSON)
				.type(MediaType.APPLICATION_JSON).get(ClientResponse.class);
		return (clientResponse.getClientResponseStatus()
				.equals(ClientResponse.Status.OK)) ? clientResponse
				.getEntity(JSONObject.class) : null;
	}

	public static JSONObject findRandomRoutingKeyWordMap(WebResource wr)
			throws JSONException {

		JSONObject routingKeyWordMaps = wr.path(ROUTING_KEY_WORDS_MAPS_PATH)
				.queryParam("filter", "tb").accept(MediaType.APPLICATION_JSON)
				.get(JSONObject.class);
		JSONArray routingKeyWordsArray = routingKeyWordMaps
				.getJSONArray("routingkeywordmaps");
		if (routingKeyWordsArray.length() == 0) {
			return null;
		}

		JSONObject routingKeyWordMap = null;
		do {
			routingKeyWordMap = routingKeyWordsArray.getJSONObject(new Random()
					.nextInt(routingKeyWordsArray.length()));
		} while (!routingKeyWordMap.getString("tag").startsWith("unit"));

		return routingKeyWordMap;
	}

	public static void deleteTestRoutingKeyWordMap(WebResource wr,
			String mapid) {
		wr.path(ROUTING_KEY_WORDS_MAPS_PATH_WITH_SLASH).path(mapid)
				.delete(ClientResponse.class);
	}

	public static JSONObject readTestRoutingKeyWordMap(WebResource wr,
			BigDecimal mapid) throws JSONException {
		return TestHelper.assertResponseOK(wr, "GET",
				ROUTING_KEY_WORDS_MAPS_PATH_WITH_SLASH + mapid, null);
	}

	@Test
	public void createRoutingKeyWordMap() throws JSONException {
		TestHelper.assertResponseMethodNotAllowed(wr, "POST",
				ROUTING_KEY_WORDS_MAPS_PATH_WITH_SLASH + "20", null);
	}

	@Test
	public void readRoutingKeyWordMap() throws JSONException {
		// create a routing key word with unit + random keyword
		String tag = "unit" + new Random().nextInt();
		JSONObject response = createTestRoutingKeyWordMap(wr, tag);

		// now read it
		JSONObject routingKeyWordMap = readTestRoutingKeyWordMap(wr,
				new BigDecimal(response.getInt("id")));
		Assert.assertEquals("routing key word keyword not correct", tag,
				routingKeyWordMap.getString("tag"));

		// make sure the metadata is returned
		Assert.assertEquals("href not correct",
				ROUTING_KEY_WORDS_MAPS_PATH_WITH_SLASH + response.getInt("id"),
				routingKeyWordMap.getString("href"));
		Assert.assertEquals("_type not correct",
				RoutingKeyWordMapResource.class.getName(),
				routingKeyWordMap.getString("_type"));

		// check 404 if the routing key word with the stupid name is requested
		TestHelper.assertResponseNotFound(wr, "GET",
				ROUTING_KEY_WORDS_MAPS_PATH_WITH_SLASH + "text", null);
	}

	@Test
	public void updateRoutingKeyWordMapOptionalFields() throws JSONException {
		// test update keyword
		// create a routingkeyword with a random keyword

		String tag = "unit" + new Random().nextInt();
		JSONObject response = createTestRoutingKeyWordMap(wr, tag);

		// change description to a new random one
		String selector = new Random().nextInt() + "";
		JSONObject routingKeyWordMap = readTestRoutingKeyWordMap(wr,
				new BigDecimal(response.getInt("id")));
		routingKeyWordMap.remove("selector");
		routingKeyWordMap.put("selector", selector);
		TestHelper.assertResponseOK(wr, "PUT",
				ROUTING_KEY_WORDS_MAPS_PATH_WITH_SLASH
						+ new BigDecimal(response.getInt("id")),
				routingKeyWordMap);

		// assert it worked
		JSONObject routingKeyWordMap2 = readTestRoutingKeyWordMap(wr,
				new BigDecimal(response.getInt("id")));
		Assert.assertEquals("routing key word selector not correct", selector,
				routingKeyWordMap2.getString("selector"));

		// test singular field update
		String selector2 = new Random().nextInt() + "";
		JSONObject tlNew = new JSONObject().put("selector", selector2);
		TestHelper.assertResponseOK(wr, "PUT",
				ROUTING_KEY_WORDS_MAPS_PATH_WITH_SLASH
						+ new BigDecimal(response.getInt("id")), tlNew);

		// assert it worked
		JSONObject routingKeyWordMap3 = readTestRoutingKeyWordMap(wr,
				new BigDecimal(response.getInt("id")));
		Assert.assertEquals("routing key word selector not correct", selector2,
				routingKeyWordMap3.getString("selector"));

	}

	@Test
	public void updateRoutingKeyWordMapAllFields() throws JSONException {
		JSONObject jsonEntity = TestUtils.fillResourceData(new JSONObject(),
				new RoutingKeyWordMapEntity());
		jsonEntity.remove("mapid");
		jsonEntity.remove("keyword");
		jsonEntity
			.put("tag", "unit" + jsonEntity.getString("tag").substring(4));

		
		// create a routing key word
		JSONObject response = createTestRoutingKeyWordMap(wr, "unit3");

		// update all fields
		TestHelper.assertResponseOK(wr, "PUT",
				ROUTING_KEY_WORDS_MAPS_PATH_WITH_SLASH
						+ new BigDecimal(response.getInt("id")), jsonEntity);

		// now read it
		JSONObject routingKeyWordMap = readTestRoutingKeyWordMap(wr,
				new BigDecimal(response.getInt("id")));

		// assert it worked
		Assert.assertEquals("routing key word fields not correct", TestUtils
				.compareJSONObjects(new RoutingKeyWordMapEntity(), jsonEntity,
						routingKeyWordMap, "mapid,keywordid,routingkeywordentity"), true);

	}

	@Test
	public void updateRoutingKeyWordMapKeywordid() throws JSONException {
		// create routing key word with random name

		JSONObject response = createTestRoutingKeyWordMap(wr,
				UNIT_TEST_R_KEYWORD);

		// update name
		JSONObject routingKeyWordMap = readTestRoutingKeyWordMap(wr,
				new BigDecimal(response.getInt("id")));
		routingKeyWordMap.remove("keyword");
		routingKeyWordMap.put("keyword",
				RandomStringUtils.randomAlphanumeric(11));
		TestHelper.assertResponseBadRequest(wr, "PUT",
				ROUTING_KEY_WORDS_MAPS_PATH_WITH_SLASH
						+ new BigDecimal(response.getInt("id")),
				routingKeyWordMap);

	}

	@Test
	public void deleteRoutingKeyWordMap() throws JSONException {

		// create a routing key word map with a random name
		String tag = "unit test " + new Random().nextInt();
		JSONObject response = createTestRoutingKeyWordMap(wr, tag);

		// delete it
		TestHelper.assertResponseOK(wr, "DELETE",
				ROUTING_KEY_WORDS_MAPS_PATH_WITH_SLASH
						+ response.getString("id"), null);

		// read it again to make sure it's gone
		TestHelper.assertResponseNotFound(wr, "GET",
				ROUTING_KEY_WORDS_MAPS_PATH_WITH_SLASH
						+ response.getString("id"), null);

		// check 404 if the routing key word
		TestHelper.assertResponseNotFound(wr, "DELETE",
				ROUTING_KEY_WORDS_MAPS_PATH_WITH_SLASH + "text", null);
	}

}
