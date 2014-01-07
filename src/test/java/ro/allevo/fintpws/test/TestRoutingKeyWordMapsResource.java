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

/**
 * 
 */
package ro.allevo.fintpws.test;

import java.math.BigDecimal;
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

import ro.allevo.fintpws.model.RoutingKeyWordMapEntity;
import ro.allevo.fintpws.resources.RoutingKeyWordMapsResource;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;

/**
 * Tests for {@link RoutingKeyWordMapResource}.
 * 
 * @author Edi
 * 
 */

@RunWith(JUnit4.class)
public class TestRoutingKeyWordMapsResource {

	static final String ROUTING_KEY_WORD_MAPS_PATH = "api/routingkeywordmaps";
	static final String ROUTING_KEY_WORD_MAPS_PATH_WITH_SLASH = ROUTING_KEY_WORD_MAPS_PATH
			+ "/";
	static final String ROUTING_KEY_WORD_MAPS_PATH_ROUTINGKEYWORD = "api/routingkeywords/"
			+ TestRoutingKeyWordResource.UNIT_TEST_RKW_NAME
			+ "/routigkeywordmaps";
	static final String ROUTING_KEY_WORD_MAPS_PATH_ROUTINGKEYWORD_WITH_SLASH = ROUTING_KEY_WORD_MAPS_PATH_ROUTINGKEYWORD
			+ "/";

	
	

	static WebResource wr;

	@BeforeClass
	public static void startMethod() {
		final ClientConfig cc = new DefaultClientConfig();
		final Client c = Client.create(cc);
		c.addFilter(new HTTPBasicAuthFilter("admin", "admin"));
		wr = c.resource(TestUtils.getUrlBase());
	}

	@Before
	public void cleanupBefore() throws JSONException {
		cleanup();
		//create routing key word we need for FK
		JSONObject response1 = TestRoutingKeyWordResource
				.createTestRoutingKeyWord(
						wr,
						TestRoutingKeyWordMapResource.UNIT_TEST_R_KEYWORD);
	}

	@After
	public void cleanupAfter() throws JSONException {
		cleanup();
		//delete routing key word we need for FK
		TestRoutingKeyWordResource.deleteTestRoutingKeyWord(wr,
				TestRoutingKeyWordMapResource.UNIT_TEST_R_KEYWORD);
	}

	public static void cleanup() throws JSONException {

		int page = 1;
		boolean findRecords = false;
		JSONObject routingKeyWordMaps = null;
		do {
			findRecords = false;
			routingKeyWordMaps = wr.path(ROUTING_KEY_WORD_MAPS_PATH)
					.queryParam("page", String.valueOf(page))
					.accept(MediaType.APPLICATION_JSON).get(JSONObject.class);
			JSONArray routingKeyWordsArray = routingKeyWordMaps
					.getJSONArray("routingkeywordmaps");
			for (int i = 0; i < routingKeyWordsArray.length(); i++) {

				String tag = routingKeyWordsArray.getJSONObject(i).getString(
						"tag");

				String mapid = routingKeyWordsArray
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
	
	

	@Test
	public void createRoutingKeyWordMaps() throws JSONException {
		
		// create a routing key word map with a random name
		String name = "unit test name" + new Random().nextInt();
		JSONObject response = TestRoutingKeyWordMapResource
				.createTestRoutingKeyWordMap(wr, name, TestRoutingKeyWordMapResource.UNIT_TEST_R_KEYWORD);
		Assert.assertTrue("uri missing", response.getString("uri").length() > 0);

		// make sure it is created
		JSONObject routingKeyWordMap = TestRoutingKeyWordMapResource
				.readTestRoutingKeyWordMap(wr,
						new BigDecimal(response.getInt("id")));
		Assert.assertEquals("routing key word map tag ", name,
				routingKeyWordMap.getString("tag"));

		// make sure we get a conflict if we created it again
		TestHelper.assertResponseConflict(wr, "POST",
				ROUTING_KEY_WORD_MAPS_PATH, routingKeyWordMap);
	}

	@Test
	public void insertRoutingKeyWordMapsAllFields() throws JSONException {
		// generate a routing key word map (JSONObject)
		JSONObject jsonEntity = TestUtils.fillResourceData(new JSONObject(),
				new RoutingKeyWordMapEntity());

		jsonEntity.remove("keyword");
		jsonEntity.remove("mapid");
		jsonEntity.put("keyword", TestRoutingKeyWordMapResource.UNIT_TEST_R_KEYWORD);
		jsonEntity
				.put("tag", "unit" + jsonEntity.getString("tag").substring(4));

		// create a routing key word map
		JSONObject response = TestHelper.assertResponseCreated(wr, "POST",
				ROUTING_KEY_WORD_MAPS_PATH, jsonEntity);

		// now read it
		JSONObject routingKeyWordMap = TestRoutingKeyWordMapResource
				.readTestRoutingKeyWordMap(wr,
						new BigDecimal(response.getInt("id")));

		// assert it worked
		Assert.assertEquals("routing key word map not filled correct",
				TestUtils.compareJSONObjects(new RoutingKeyWordMapEntity(),
						jsonEntity, routingKeyWordMap, "mapid,keywordid,routingkeywordentity"), true);
	}

	@Test
	public void readRoutingKeyWordMapsAllFields() throws JSONException {
		// make sure it makes sense (>0 routing key word map available)
		TestRoutingKeyWordMapResource.createTestRoutingKeyWordMap(wr,
				"unit_desc",TestRoutingKeyWordMapResource.UNIT_TEST_R_KEYWORD);

		JSONObject routingKeyWordMaps = wr.path(ROUTING_KEY_WORD_MAPS_PATH)
				.accept(MediaType.APPLICATION_JSON).get(JSONObject.class);

		// make sure the metadata is returned
		Assert.assertEquals("href not correct", ROUTING_KEY_WORD_MAPS_PATH,
				routingKeyWordMaps.getString("href"));
		Assert.assertEquals("_type not correct",
				RoutingKeyWordMapsResource.class.getName(),
				routingKeyWordMaps.getString("_type"));

		JSONArray tlList = routingKeyWordMaps
				.getJSONArray("routingkeywordmaps");
		Assert.assertTrue("routing key word maps list empty",
				tlList.length() > 0);

		// get first routing key word map, see if has guid
		Assert.assertTrue("routingkeywordmaps list empty", tlList
				.getJSONObject(0).getString("mapid").length() > 0);

	}

	@Test
	public void readRoutingKeyWordMaps() throws JSONException {
		// make sure it makes sense (>0 routing key word maps available)
		TestRoutingKeyWordMapResource.createTestRoutingKeyWordMap(wr,
				"unit_name",TestRoutingKeyWordMapResource.UNIT_TEST_R_KEYWORD);

		JSONObject routingKeyWordMaps = wr.path(ROUTING_KEY_WORD_MAPS_PATH)
				.accept(MediaType.APPLICATION_JSON).get(JSONObject.class);

		// make sure the metadata is returned
		Assert.assertEquals("href not correct", ROUTING_KEY_WORD_MAPS_PATH,
				routingKeyWordMaps.getString("href"));
		Assert.assertEquals("_type not correct",
				RoutingKeyWordMapsResource.class.getName(),
				routingKeyWordMaps.getString("_type"));

		JSONArray tlList = routingKeyWordMaps
				.getJSONArray("routingkeywordmaps");
		Assert.assertTrue("routing key word maps list empty",
				tlList.length() > 0);

		// get first routing key word maps, see if has mapid
		Assert.assertTrue("routing key word maps list empty", tlList
				.getJSONObject(0).getString("mapid").length() > 0);
	}

	@Test
	public void updateRoutingKeyWordMaps() throws JSONException {
		TestHelper.assertResponseMethodNotAllowed(wr, "PUT",
				ROUTING_KEY_WORD_MAPS_PATH_WITH_SLASH, null);
	}

	@Test
	public void deleteRoutingKeyWordMaps() throws JSONException {
		TestHelper.assertResponseMethodNotAllowed(wr, "DELETE",
				ROUTING_KEY_WORD_MAPS_PATH_WITH_SLASH, null);
	}

	@Test
	public void totalTests() throws JSONException {
		// request t among some other garbage
		JSONObject routingKeyWordMaps = wr
				.path(ROUTING_KEY_WORD_MAPS_PATH_WITH_SLASH)
				.queryParam("filter", "adste")
				.accept(MediaType.APPLICATION_JSON_TYPE).get(JSONObject.class);

		int total1 = routingKeyWordMaps.getInt("total");

		TestRoutingKeyWordMapResource.createTestRoutingKeyWordMap(wr,
				"unit_total", TestRoutingKeyWordMapResource.UNIT_TEST_R_KEYWORD);

		// request t among some other garbage
		routingKeyWordMaps = wr.path(ROUTING_KEY_WORD_MAPS_PATH_WITH_SLASH)
				.queryParam("filter", "adste")
				.accept(MediaType.APPLICATION_JSON_TYPE).get(JSONObject.class);

		int total2 = routingKeyWordMaps.getInt("total");

		Assert.assertEquals("total not +1", total1 + 1, total2);

		// request a small page, look for total to be the same
		routingKeyWordMaps = wr.path(ROUTING_KEY_WORD_MAPS_PATH_WITH_SLASH)
				.queryParam("filter", "stu").queryParam("page_size", "1")
				.accept(MediaType.APPLICATION_JSON).get(JSONObject.class);

		int total3 = routingKeyWordMaps.getInt("total");

		Assert.assertEquals("total wrong for page", total1 + 1, total3);
	}

	@Test
	public void pagingTests() throws JSONException {
		// TODO: test last page

		// create 101 routingKeyWordMaps
		for (int i = 0; i < 101; i++) {
			TestRoutingKeyWordMapResource.createTestRoutingKeyWordMap(wr,
					"unit_page_test" + i,TestRoutingKeyWordMapResource.UNIT_TEST_R_KEYWORD);
		}

		// test get default page (1), default page_size
		JSONObject routingKeyWordMaps = wr.path(ROUTING_KEY_WORD_MAPS_PATH)
				.accept(MediaType.APPLICATION_JSON).get(JSONObject.class);

		// we should have at least a full page and at least 1 more item
		Assert.assertEquals("default paging items", 100, routingKeyWordMaps
				.getJSONArray("routingkeywordmaps").length());
		Assert.assertEquals("default paging has_more", true,
				routingKeyWordMaps.getBoolean("has_more"));

		// test get default page (1), custom size : 42
		// 42 = between[1,100] => we should get 42 routing key words per page
		routingKeyWordMaps = wr.path(ROUTING_KEY_WORD_MAPS_PATH)
				.queryParam("page_size", "42")
				.accept(MediaType.APPLICATION_JSON).get(JSONObject.class);
		// we should have at least a full page and at least 1 more item
		Assert.assertEquals("custom page size items", 42, routingKeyWordMaps
				.getJSONArray("routingkeywordmaps").length());
		Assert.assertEquals("custom page size has_more", true,
				routingKeyWordMaps.getBoolean("has_more"));

		// test get default page (1), custom invalid size : -1
		// -1 < 0 => size = DEFAULT_SIZE(100)
		routingKeyWordMaps = wr.path(ROUTING_KEY_WORD_MAPS_PATH)
				.queryParam("page_size", "-1")
				.accept(MediaType.APPLICATION_JSON).get(JSONObject.class);
		// we should have at least a full page and at least 1 more item
		Assert.assertEquals("custom invalid page size -1 items", 100,
				routingKeyWordMaps.getJSONArray("routingkeywordmaps").length());
		Assert.assertEquals("custom invalid page size -1 items", true,
				routingKeyWordMaps.getBoolean("has_more"));

		// test get default page (1), custom invalid size : 101
		// 101 > 100 => size = DEFAULT_SIZE(100)
		routingKeyWordMaps = wr.path(ROUTING_KEY_WORD_MAPS_PATH)
				.queryParam("page_size", "101")
				.accept(MediaType.APPLICATION_JSON).get(JSONObject.class);
		// we should have at least a full page and at least 1 more item
		Assert.assertEquals("custom invalid page size 100 items", 100,
				routingKeyWordMaps.getJSONArray("routingkeywordmaps").length());
		Assert.assertEquals("custom invalid page size 100 items", true,
				routingKeyWordMaps.getBoolean("has_more"));

		// test get default page (1), custom invalid size : aaa
		// aaa is not int > size= DEFAULT_SIZE(100)
		routingKeyWordMaps = wr.path(ROUTING_KEY_WORD_MAPS_PATH)
				.queryParam("page_size", "aaa")
				.accept(MediaType.APPLICATION_JSON).get(JSONObject.class);
		// we should have at least a full page and at least 1 more item
		Assert.assertEquals("custom invalid page size aaa items", 100,
				routingKeyWordMaps.getJSONArray("routingkeywordmaps").length());
		Assert.assertEquals("custom invalid page size aaa items", true,
				routingKeyWordMaps.getBoolean("has_more"));

		// test get custom page(2), custom size: 2
		routingKeyWordMaps = wr.path(ROUTING_KEY_WORD_MAPS_PATH)
				.queryParam("page", "2").queryParam("page_size", "2")
				.accept(MediaType.APPLICATION_JSON).get(JSONObject.class);

		// we should have at least a full page and at least 1 more item
		Assert.assertEquals("custom page 2 custom page size 2 items", 2,
				routingKeyWordMaps.getJSONArray("routingkeywordmaps").length());
		Assert.assertEquals("custom page 2 custom page size 2 has_more", true,
				routingKeyWordMaps.getBoolean("has_more"));
		String guid1 = routingKeyWordMaps.getJSONArray("routingkeywordmaps")
				.getJSONObject(1).getString("mapid");

		// repeat with next page
		routingKeyWordMaps = wr.path(ROUTING_KEY_WORD_MAPS_PATH)
				.queryParam("page", "3").queryParam("page_size", "2")
				.accept(MediaType.APPLICATION_JSON).get(JSONObject.class);

		// we should have at least a full page and at least 1 more item
		Assert.assertEquals("custom page 3 custom page size 2 items", 2,
				routingKeyWordMaps.getJSONArray("routingkeywordmaps").length());
		Assert.assertEquals("custom page 3 custom page size 2 has_more", true,
				routingKeyWordMaps.getBoolean("has_more"));
		String guid2 = routingKeyWordMaps.getJSONArray("routingkeywordmaps")
				.getJSONObject(1).getString("mapid");

		// routing key word maps mapid should be different
		Assert.assertNotEquals("different mapid on pages", guid1, guid2);

	}

}
