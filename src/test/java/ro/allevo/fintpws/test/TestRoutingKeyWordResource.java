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

import ro.allevo.fintpws.model.RoutingKeyWordEntity;
import ro.allevo.fintpws.resources.RoutingKeyWordResource;

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
public class TestRoutingKeyWordResource {

	static final String ROUTING_KEY_WORDS_PATH = "api/routingkeywords";
	static final String ROUTING_KEY_WORDS_PATH_WITH_SLASH = ROUTING_KEY_WORDS_PATH
			+ "/";
	static WebResource wr;
	static final String UNIT_TEST_RKW_NAME = "unittest rkw name";

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
				routingKeyWordsArray.getJSONObject(i).getString("keyword");
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

	public static JSONObject createTestRoutingKeyWord(WebResource wr,
			String routingKeyWordName) throws JSONException {
		JSONObject keyword = new JSONObject()
				.put("keyword", routingKeyWordName)
				.put("comparer","2013-01-01 23:59:00.0");

		return TestHelper.assertResponseCreated(wr, "POST",
				ROUTING_KEY_WORDS_PATH, keyword);
	}

	// public static JSONObject
	// createTestRoutingKeyWordWithKeyWordMapKeywordid(WebResource wr,
	// String keywordid) throws JSONException{
	// JSONObject routingKeyWord = new JSONObject()
	// .put("keywordid", keywordid)
	// .put("comparer", "action_unit_test");
	//
	// return TestHelper.assertResponseCreated(wr, "POST",
	// ROUTING_KEY_WORDS_PATH,
	// routingKeyWord);
	// }

	public static JSONObject createTestRoutingKeyWord(WebResource wr,
			String guid, String keyword) throws JSONException {
		JSONObject routingKeyWord = new JSONObject().put("guid", guid)
				.put("keyword", keyword)
				.put("comparer", "2013-01-01 23:59:00.0");

		return TestHelper.assertResponseCreated(wr, "POST",
				ROUTING_KEY_WORDS_PATH, routingKeyWord);
	}

	public static JSONObject findTestRoutingKeyWord(WebResource wr,
			String keyword) {
		ClientResponse clientResponse = wr.path(ROUTING_KEY_WORDS_PATH)
				.path(keyword).accept(MediaType.APPLICATION_JSON)
				.type(MediaType.APPLICATION_JSON).get(ClientResponse.class);
		return (clientResponse.getClientResponseStatus()
				.equals(ClientResponse.Status.OK)) ? clientResponse
				.getEntity(JSONObject.class) : null;
	}

	public static JSONObject findRandomRoutingKeyWord(WebResource wr)
			throws JSONException {

		JSONObject routingKeyWords = wr.path(ROUTING_KEY_WORDS_PATH)
				.queryParam("filter", "tb").accept(MediaType.APPLICATION_JSON)
				.get(JSONObject.class);
		JSONArray routingKeyWordsArray = routingKeyWords
				.getJSONArray("routingkeywords");
		if (routingKeyWordsArray.length() == 0) {
			return null;
		}

		JSONObject routingKeyWord = null;
		do {
			routingKeyWord = routingKeyWordsArray.getJSONObject(new Random()
					.nextInt(routingKeyWordsArray.length()));
		} while (!routingKeyWord.getString("description").startsWith("unit"));

		return routingKeyWord;
	}

	public static void deleteTestRoutingKeyWord(WebResource wr, String keyword) {
		wr.path(ROUTING_KEY_WORDS_PATH_WITH_SLASH).path(keyword)
				.delete(ClientResponse.class);
	}

	public static JSONObject readTestRoutingKeyWord(WebResource wr,
			String keyword) throws JSONException {
		return TestHelper.assertResponseOK(wr, "GET",
				ROUTING_KEY_WORDS_PATH_WITH_SLASH + keyword, null);
	}

	@Test
	public void createRoutingKeyWord() throws JSONException {
		TestHelper.assertResponseMethodNotAllowed(wr, "POST",
				ROUTING_KEY_WORDS_PATH_WITH_SLASH + "20", null);
	}

	@Test
	public void readRoutingKeyWord() throws JSONException {
		// create a routing key word with unit + random keyword
		String keyword = "unit" + new Random().nextInt(100);
		createTestRoutingKeyWord(wr, keyword);

		// now read it
		JSONObject routingKeyWord = readTestRoutingKeyWord(wr, keyword);
		Assert.assertEquals("routing key word keyword not correct", keyword,
				routingKeyWord.getString("keyword"));

		// make sure the metadata is returned
		Assert.assertEquals("href not correct",
				ROUTING_KEY_WORDS_PATH_WITH_SLASH + keyword,
				routingKeyWord.getString("href"));
		Assert.assertEquals("_type not correct",
				RoutingKeyWordResource.class.getName(),
				routingKeyWord.getString("_type"));

		// check 404 if the routing key word with the stupid name is requested
		TestHelper.assertResponseNotFound(wr, "GET", ROUTING_KEY_WORDS_PATH_WITH_SLASH
				+ "text", null);
	}

	@Test
	public void updateRoutingKeyWordOptionalFields() throws JSONException {
		// test update keyword
		// create a routingkeyword with a random keyword

		String keyword1 = "unit" + new Random().nextInt();
		createTestRoutingKeyWord(wr, keyword1);

		// change description to a new random one
		String comparer = "random comparer" + new Random().nextInt();
		JSONObject routingKeyWord = readTestRoutingKeyWord(wr, keyword1);
		routingKeyWord.remove("comparer");
		routingKeyWord.put("comparer", comparer);
		TestHelper.assertResponseOK(wr, "PUT",
				ROUTING_KEY_WORDS_PATH_WITH_SLASH + keyword1, routingKeyWord);

		// assert it worked
		JSONObject routingKeyWord2 = readTestRoutingKeyWord(wr, keyword1);
		Assert.assertEquals("routing key word comparer not correct", comparer,
				routingKeyWord2.getString("comparer"));

		// test singular field update
		String comparer2 = "random comparer" + new Random().nextInt();
		JSONObject tlNew = new JSONObject().put("comparer", comparer2);
		TestHelper.assertResponseOK(wr, "PUT",
				ROUTING_KEY_WORDS_PATH_WITH_SLASH + keyword1, tlNew);

		// assert it worked
		JSONObject routingKeyWord3 = readTestRoutingKeyWord(wr, keyword1);
		Assert.assertEquals("routing key word comparer not correct", comparer2,
				routingKeyWord3.getString("comparer"));

	}

	@Test
	public void updateRoutingKeyWordAllFields() throws JSONException {
		JSONObject jsonEntity = TestUtils.fillResourceData(new JSONObject(),
				new RoutingKeyWordEntity());
		jsonEntity.remove("guid");
		jsonEntity.remove("keyword");

		// create a routing key word
		createTestRoutingKeyWord(wr, "unit3");

		// update all fields
		TestHelper.assertResponseOK(wr, "PUT",
				ROUTING_KEY_WORDS_PATH_WITH_SLASH + "unit3", jsonEntity);

		// now read it
		JSONObject routingKeyWord = readTestRoutingKeyWord(wr, "unit3");

		// assert it worked
		Assert.assertEquals("routing key word fields not correct", TestUtils
				.compareJSONObjects(new RoutingKeyWordEntity(), jsonEntity,
						routingKeyWord, "guid,keyword"), true);

	}

	@Test
	public void updateRoutingKeyWordKeyword() throws JSONException {
		// create routing key word with random name

		createTestRoutingKeyWord(wr, UNIT_TEST_RKW_NAME);

		// update name
		JSONObject routingKeyWord = readTestRoutingKeyWord(wr,
				UNIT_TEST_RKW_NAME);
		routingKeyWord.remove("keyword");
		routingKeyWord.put("keyword", RandomStringUtils.randomAlphanumeric(51));
		TestHelper.assertResponseBadRequest(wr, "PUT",
				ROUTING_KEY_WORDS_PATH_WITH_SLASH + UNIT_TEST_RKW_NAME,
				routingKeyWord);

	}

	@Test
	public void deleteRoutingKeyWord() throws JSONException {

		// create a routing key word with a random name
		String name = "unit test " + new Random().nextInt();
		createTestRoutingKeyWord(wr, name);

		// delete it
		TestHelper.assertResponseOK(wr, "DELETE",
				ROUTING_KEY_WORDS_PATH_WITH_SLASH + name, null);

		// read it again to make sure it's gone
		TestHelper.assertResponseNotFound(wr, "GET",
				ROUTING_KEY_WORDS_PATH_WITH_SLASH + name, null);

		// check 404 if the routing key word
		TestHelper.assertResponseNotFound(wr, "DELETE",
				ROUTING_KEY_WORDS_PATH_WITH_SLASH + "text", null);

	}

}
