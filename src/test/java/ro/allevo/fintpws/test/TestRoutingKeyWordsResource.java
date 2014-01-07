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

import ro.allevo.fintpws.model.RoutingKeyWordEntity;
import ro.allevo.fintpws.resources.RoutingKeyWordsResource;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;

/**
 * Tests for {@link RoutingKeyWordResource}.
 * 
 * @author Edi
 * 
 */

@RunWith(JUnit4.class)
public class TestRoutingKeyWordsResource {

	static final String ROUTING_KEY_WORDS_PATH = "api/routingkeywords";
	static final String ROUTING_KEY_WORDS_PATH_WITH_SLASH = ROUTING_KEY_WORDS_PATH
			+ "/";
	static final String IMPOSSIBLE_RKWM_MAPID = "ABC";

	
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
		JSONObject routingKeyWords = null;
		do {
			findRecords = false;
			routingKeyWords = wr.path(ROUTING_KEY_WORDS_PATH)
					.queryParam("page", String.valueOf(page))
					.accept(MediaType.APPLICATION_JSON).get(JSONObject.class);
			JSONArray routingKeyWordsArray = routingKeyWords
					.getJSONArray("routingkeywords");
			for (int i = 0; i < routingKeyWordsArray.length(); i++) {

				String keyword = routingKeyWordsArray.getJSONObject(i)
						.optString("keyword");
				if (keyword.startsWith("unit")) {
					TestRoutingKeyWordResource.deleteTestRoutingKeyWord(wr,
							keyword);
					findRecords = true;
				}
			}
			if (!findRecords) {
				page++;
			}
		} while (routingKeyWords.has("has_more"));

	}

	@Test
	public void createRoutingKeyWords() throws JSONException {
		// create a routing key word with a random name
		String name = "unit test name" + new Random().nextInt();
		JSONObject response = TestRoutingKeyWordResource
				.createTestRoutingKeyWord(wr, name);
		Assert.assertTrue("uri missing", response.getString("uri").length() > 0);

		// make sure it is created
		JSONObject routingKeyWord = TestRoutingKeyWordResource
				.readTestRoutingKeyWord(wr, name);
		Assert.assertEquals("routing key word keyword ", name,
				routingKeyWord.getString("keyword"));

		// make sure we get a conflict if we created it again
		TestHelper.assertResponseConflict(wr, "POST", ROUTING_KEY_WORDS_PATH,
				routingKeyWord);
	}

	@Test
	public void insertRoutingKeyWordsAllFields() throws JSONException {
		// generate a routing key word (JSONObject)
		JSONObject jsonEntity = TestUtils.fillResourceData(new JSONObject(),
				new RoutingKeyWordEntity());

		jsonEntity.remove("guid");
		jsonEntity.put(
				"keyword",
				"unit"
						+ jsonEntity.getString("keyword").substring(0,
								jsonEntity.getString("keyword").length() - 4));

		// create a routing key word
		TestHelper.assertResponseCreated(wr, "POST", ROUTING_KEY_WORDS_PATH,
				jsonEntity);

		// now read it
		JSONObject routingKeyWord = TestRoutingKeyWordResource
				.readTestRoutingKeyWord(wr, jsonEntity.getString("keyword"));

		// assert it worked
		Assert.assertEquals("routing key word not filled correct", TestUtils
				.compareJSONObjects(new RoutingKeyWordEntity(), jsonEntity,
						routingKeyWord, "guid,keyword"), true);
	}

	@Test
	public void readRoutingKeyWordFromRoutingKeyWordMap() throws JSONException {

		// create routing k referring routingschema(make sure it makes sense)
		// TO DO string MACRO path

		String keyword = "unit" + new Random().nextInt();
		TestRoutingKeyWordResource.createTestRoutingKeyWord(wr, keyword);
		JSONObject testRoutingKeyword = TestRoutingKeyWordResource.readTestRoutingKeyWord(wr, keyword);

		
		JSONObject responseCreateingRoutingKeyWordMap = TestRoutingKeyWordMapResource
				.createTestRoutingKeyWordMap(wr, "unit",
						testRoutingKeyword.getString("keyword"));

		JSONObject routingKeyWordMap = TestRoutingKeyWordMapResource
				.readTestRoutingKeyWordMap(wr, new BigDecimal(
						responseCreateingRoutingKeyWordMap.getString("id")));

		final String path = "api/routingkeywordmaps/"
				+ responseCreateingRoutingKeyWordMap.getString("id")
				+ "/routingkeywords";

		final JSONObject routingKeyWord = wr.path(path)
				.accept(MediaType.APPLICATION_JSON).get(JSONObject.class);

		// make sure the metadata is returned
		final JSONArray routingKeyWordsList = routingKeyWord
				.getJSONArray("routingkeywords");
		Assert.assertTrue("routingkeywords list empty",
				routingKeyWordsList.length() > 0);

		// // get first routing rule, see if has a guid
		Assert.assertTrue("routingkeywords list empty", routingKeyWordsList
				.getJSONObject(0).getString("keyword").length() > 0);

		// delete routing rule created (in order to be able to delete rs)
		TestRoutingKeyWordResource.deleteTestRoutingKeyWord(wr,
				testRoutingKeyword.getString("keyword"));

		TestRoutingKeyWordMapResource.deleteTestRoutingKeyWordMap(wr,
				responseCreateingRoutingKeyWordMap.getString("id"));
	}

	@Test
	public void readRoutingKeyWordsAllFields() throws JSONException {
		// make sure it makes sense (>0 routing key word available)
		TestRoutingKeyWordResource.createTestRoutingKeyWord(wr, "unit_desc");

		JSONObject routingKeyWords = wr.path(ROUTING_KEY_WORDS_PATH)
				.accept(MediaType.APPLICATION_JSON).get(JSONObject.class);

		// make sure the metadata is returned
		Assert.assertEquals("href not correct", ROUTING_KEY_WORDS_PATH,
				routingKeyWords.getString("href"));
		Assert.assertEquals("_type not correct",
				RoutingKeyWordsResource.class.getName(),
				routingKeyWords.getString("_type"));

		JSONArray tlList = routingKeyWords.getJSONArray("routingkeywords");
		Assert.assertTrue("routing key words list empty", tlList.length() > 0);

		// get first routing key word, see if has guid
		Assert.assertTrue("routingkeywords list empty", tlList.getJSONObject(0)
				.getString("keyword").length() > 0);

	}

	@Test
	public void readRoutingKeyWords() throws JSONException {
		// make sure it makes sense (>0 routing key words available)
		TestRoutingKeyWordResource.createTestRoutingKeyWord(wr, "unit_name");

		JSONObject routingKeyWords = wr.path(ROUTING_KEY_WORDS_PATH)
				.accept(MediaType.APPLICATION_JSON).get(JSONObject.class);

		// make sure the metadata is returned
		Assert.assertEquals("href not correct", ROUTING_KEY_WORDS_PATH,
				routingKeyWords.getString("href"));
		Assert.assertEquals("_type not correct",
				RoutingKeyWordsResource.class.getName(),
				routingKeyWords.getString("_type"));

		JSONArray tlList = routingKeyWords.getJSONArray("routingkeywords");
		Assert.assertTrue("routing key words list empty", tlList.length() > 0);

		// get first routing key words, see if has guid
		Assert.assertTrue("routing key words list empty",
				tlList.getJSONObject(0).getString("keyword").length() > 0);
	}

	@Test
	public void updateRoutingKeyWords() throws JSONException {
		TestHelper.assertResponseMethodNotAllowed(wr, "PUT",
				ROUTING_KEY_WORDS_PATH_WITH_SLASH, null);
	}

	@Test
	public void deleteRoutingKeyWords() throws JSONException {
		TestHelper.assertResponseMethodNotAllowed(wr, "DELETE",
				ROUTING_KEY_WORDS_PATH_WITH_SLASH, null);
	}
	
	@Test
	public void getRoutingKeyWordsWithoutParent() throws JSONException {
		TestHelper
				.assertResponseNotFound(
						wr,
						"GET",
						TestRoutingKeyWordMapsResource.ROUTING_KEY_WORD_MAPS_PATH_WITH_SLASH
								+ IMPOSSIBLE_RKWM_MAPID + "/routingkeywords",
						null);
	}

	@Test
	public void totalTests() throws JSONException {
		// request t among some other garbage
		JSONObject routingKeyWords = wr.path(ROUTING_KEY_WORDS_PATH_WITH_SLASH)
				.queryParam("filter", "adste")
				.accept(MediaType.APPLICATION_JSON_TYPE).get(JSONObject.class);

		int total1 = routingKeyWords.getInt("total");

		TestRoutingKeyWordResource.createTestRoutingKeyWord(wr, "unit_total");

		// request t among some other garbage
		routingKeyWords = wr.path(ROUTING_KEY_WORDS_PATH_WITH_SLASH)
				.queryParam("filter", "adste")
				.accept(MediaType.APPLICATION_JSON_TYPE).get(JSONObject.class);

		int total2 = routingKeyWords.getInt("total");

		Assert.assertEquals("total not +1", total1 + 1, total2);

		// request a small page, look for total to be the same
		routingKeyWords = wr.path(ROUTING_KEY_WORDS_PATH_WITH_SLASH)
				.queryParam("filter", "stu").queryParam("page_size", "1")
				.accept(MediaType.APPLICATION_JSON).get(JSONObject.class);

		int total3 = routingKeyWords.getInt("total");

		Assert.assertEquals("total wrong for page", total1 + 1, total3);
	}

	@Test
	public void pagingTests() throws JSONException {
		// TODO: test last page

		// create 101 routingKeyWords
		for (int i = 0; i < 101; i++) {
			TestRoutingKeyWordResource.createTestRoutingKeyWord(wr,
					"unit_page_test" + i);
		}

		// test get default page (1), default page_size
		JSONObject routingKeyWords = wr.path(ROUTING_KEY_WORDS_PATH)
				.accept(MediaType.APPLICATION_JSON).get(JSONObject.class);

		// we should have at least a full page and at least 1 more item
		Assert.assertEquals("default paging items", 100, routingKeyWords
				.getJSONArray("routingkeywords").length());
		Assert.assertEquals("default paging has_more", true,
				routingKeyWords.getBoolean("has_more"));

		// test get default page (1), custom size : 42
		// 42 = between[1,100] => we should get 42 routing key words per page
		routingKeyWords = wr.path(ROUTING_KEY_WORDS_PATH)
				.queryParam("page_size", "42")
				.accept(MediaType.APPLICATION_JSON).get(JSONObject.class);
		// we should have at least a full page and at least 1 more item
		Assert.assertEquals("custom page size items", 42, routingKeyWords
				.getJSONArray("routingkeywords").length());
		Assert.assertEquals("custom page size has_more", true,
				routingKeyWords.getBoolean("has_more"));

		// test get default page (1), custom invalid size : -1
		// -1 < 0 => size = DEFAULT_SIZE(100)
		routingKeyWords = wr.path(ROUTING_KEY_WORDS_PATH)
				.queryParam("page_size", "-1")
				.accept(MediaType.APPLICATION_JSON).get(JSONObject.class);
		// we should have at least a full page and at least 1 more item
		Assert.assertEquals("custom invalid page size -1 items", 100,
				routingKeyWords.getJSONArray("routingkeywords").length());
		Assert.assertEquals("custom invalid page size -1 items", true,
				routingKeyWords.getBoolean("has_more"));

		// test get default page (1), custom invalid size : 101
		// 101 > 100 => size = DEFAULT_SIZE(100)
		routingKeyWords = wr.path(ROUTING_KEY_WORDS_PATH)
				.queryParam("page_size", "101")
				.accept(MediaType.APPLICATION_JSON).get(JSONObject.class);
		// we should have at least a full page and at least 1 more item
		Assert.assertEquals("custom invalid page size 100 items", 100,
				routingKeyWords.getJSONArray("routingkeywords").length());
		Assert.assertEquals("custom invalid page size 100 items", true,
				routingKeyWords.getBoolean("has_more"));

		// test get default page (1), custom invalid size : aaa
		// aaa is not int > size= DEFAULT_SIZE(100)
		routingKeyWords = wr.path(ROUTING_KEY_WORDS_PATH)
				.queryParam("page_size", "aaa")
				.accept(MediaType.APPLICATION_JSON).get(JSONObject.class);
		// we should have at least a full page and at least 1 more item
		Assert.assertEquals("custom invalid page size aaa items", 100,
				routingKeyWords.getJSONArray("routingkeywords").length());
		Assert.assertEquals("custom invalid page size aaa items", true,
				routingKeyWords.getBoolean("has_more"));

		// test get custom page(2), custom size: 2
		routingKeyWords = wr.path(ROUTING_KEY_WORDS_PATH)
				.queryParam("page", "2").queryParam("page_size", "2")
				.accept(MediaType.APPLICATION_JSON).get(JSONObject.class);

		// we should have at least a full page and at least 1 more item
		Assert.assertEquals("custom page 2 custom page size 2 items", 2,
				routingKeyWords.getJSONArray("routingkeywords").length());
		Assert.assertEquals("custom page 2 custom page size 2 has_more", true,
				routingKeyWords.getBoolean("has_more"));
		String guid1 = routingKeyWords.getJSONArray("routingkeywords")
				.getJSONObject(1).getString("keyword");

		// repeat with next page
		routingKeyWords = wr.path(ROUTING_KEY_WORDS_PATH)
				.queryParam("page", "3").queryParam("page_size", "2")
				.accept(MediaType.APPLICATION_JSON).get(JSONObject.class);

		// we should have at least a full page and at least 1 more item
		Assert.assertEquals("custom page 3 custom page size 2 items", 2,
				routingKeyWords.getJSONArray("routingkeywords").length());
		Assert.assertEquals("custom page 3 custom page size 2 has_more", true,
				routingKeyWords.getBoolean("has_more"));
		String guid2 = routingKeyWords.getJSONArray("routingkeywords")
				.getJSONObject(1).getString("keyword");

		// routing key words guid should be different
		Assert.assertNotEquals("different guids on pages", guid1, guid2);

	}

}
