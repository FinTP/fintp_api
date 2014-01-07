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

import ro.allevo.fintpws.model.HistoryEntity;
import ro.allevo.fintpws.resources.HistoriesResource;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;

/**
 * Tests for {@link HistoriesResource}.
 * 
 * @author anda
 * 
 */

@RunWith(JUnit4.class)
public class TestHistoriesResource {

	static final String UNIT_TEST_HISTORIES = "unit ";
	static final String HISTORIES_PATH = "api/histories";
	static final String HISTORIES_PATH_WITH_SLASH = HISTORIES_PATH + "/";

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
		JSONObject histories = null;
		do {
			findRecords = false;
			histories = wr.path(HISTORIES_PATH)
					.queryParam("page", String.valueOf(page))
					.accept(MediaType.APPLICATION_JSON).get(JSONObject.class);
			JSONArray historiesArray = histories.getJSONArray("histories");
			for (int i = 0; i < historiesArray.length(); i++) {

				String guid = historiesArray.getJSONObject(i).optString("guid");
				if (guid.startsWith("unit")) {
					TestHistoryResource.deleteTestHistory(wr, guid);
					findRecords = true;
				}
			}
			if (!findRecords) {
				page++;
			}
		} while (histories.has("has_more"));
	}

	@Test
	public void createHistories() throws JSONException {
		// create a history with a random guid
		String guid = "unit test" + new Random().nextInt();

		JSONObject response = TestHistoryResource.createTestHistory(wr, guid,
				UNIT_TEST_HISTORIES);
		Assert.assertTrue("uri missing", response.getString("uri").length() > 0);

		// make sure it is created
		JSONObject history = TestHistoryResource.readTestHistory(wr, guid);
		Assert.assertEquals("history guid", guid, history.getString("guid"));
	}

	@Test
	public void insertHistoriesAllFields() throws JSONException {
		// generate a history guid (JSONObject format)
		JSONObject jsonEntity = TestUtils.fillResourceData(new JSONObject(),
				new HistoryEntity());

		jsonEntity.put("guid",
				"unit" + jsonEntity.getString("guid").substring(4));

		// create a history
		TestHelper
				.assertResponseCreated(wr, "POST", HISTORIES_PATH, jsonEntity);

		// now read it
		JSONObject history = TestHistoryResource.readTestHistory(wr,
				jsonEntity.getString("guid"));

		// assert it worked
		Assert.assertEquals("history guid filed not correct", TestUtils
				.compareJSONObjects(new HistoryEntity(), jsonEntity, history,
						"guid"), true);
	}

	@Test
	public void readHistories() throws JSONException {
		// make sure it makes sense ( >0 history is available )
		TestHistoryResource.createTestHistory(wr, UNIT_TEST_HISTORIES);

		JSONObject histories = wr.path(HISTORIES_PATH)
				.accept(MediaType.APPLICATION_JSON).get(JSONObject.class);

		// make sure the metadata is returned
		Assert.assertEquals("href not correct", HISTORIES_PATH,
				histories.getString("href"));
		Assert.assertEquals("_type not correct",
				HistoriesResource.class.getName(), histories.getString("_type"));

		JSONArray historiesList = histories.getJSONArray("histories");
		Assert.assertTrue("histories list empty", historiesList.length() > 0);

		// get first history, see if has a guid
		Assert.assertTrue("histories list empty", historiesList
				.getJSONObject(0).getString("guid").length() > 0);
	}

	@Test
	public void updateHistories() throws JSONException {
		TestHelper.assertResponseMethodNotAllowed(wr, "PUT",
				HISTORIES_PATH_WITH_SLASH, null);
	}

	@Test
	public void deleteHistories() throws JSONException {
		TestHelper.assertResponseMethodNotAllowed(wr, "DELETE",
				HISTORIES_PATH_WITH_SLASH, null);
	}

	@Test
	public void totalTests() throws JSONException {
		// requst t among some garbage
		JSONObject histories = wr.path(HISTORIES_PATH_WITH_SLASH)
				.queryParam("filter", "stu").accept(MediaType.APPLICATION_JSON)
				.get(JSONObject.class);
		int total1 = histories.getInt("total");

		TestHistoryResource.createTestHistory(wr, "unit_total");

		// requst t among some garbage
		histories = wr.path(HISTORIES_PATH_WITH_SLASH)
				.queryParam("filter", "adste")
				.accept(MediaType.APPLICATION_JSON_TYPE).get(JSONObject.class);

		int total2 = histories.getInt("total");

		Assert.assertEquals("total not +1", total1 + 1, total2);

		// request a small page, look for total to be the same
		histories = wr.path(HISTORIES_PATH_WITH_SLASH)
				.queryParam("filter", "stu").queryParam("page_size", "1")
				.accept(MediaType.APPLICATION_JSON).get(JSONObject.class);

		int total3 = histories.getInt("total");

		Assert.assertEquals("total wrong for page", total1 + 1, total3);
	}

	@Test
	public void pagingTests() throws JSONException {
		// TODO: test last page
		// create 101 histories
		for (int i = 0; i < 101; i++) {
			TestHistoryResource.createTestHistory(wr, "unit_page_test" + i,
					UNIT_TEST_HISTORIES);
		}

		// test get default page (1), default page_size
		JSONObject histories = wr.path(HISTORIES_PATH)
				.accept(MediaType.APPLICATION_JSON).get(JSONObject.class);

		// we should have at least a full page and at least 1 more item
		Assert.assertEquals("default paging items", 100, histories
				.getJSONArray("histories").length());
		Assert.assertEquals("default paging has_more", true,
				histories.getBoolean("has_more"));

		// test get default page (1), custom size : 42
		// 42 = between[1,100] => we should get 42 histories per page
		histories = wr.path(HISTORIES_PATH).queryParam("page_size", "42")
				.accept(MediaType.APPLICATION_JSON).get(JSONObject.class);
		// we should have at least a full page and at least 1 more item
		Assert.assertEquals("custom page size items", 42, histories
				.getJSONArray("histories").length());
		Assert.assertEquals("custom page size has_more", true,
				histories.getBoolean("has_more"));

		// test get default page (1), custom invalid size : -1
		// -1 < 0 => size = DEFAULT_SIZE(100)
		histories = wr.path(HISTORIES_PATH).queryParam("page_size", "-1")
				.accept(MediaType.APPLICATION_JSON).get(JSONObject.class);
		// we should have at least a full page and at least 1 more item
		Assert.assertEquals("custom invalid page size -1 items", 100, histories
				.getJSONArray("histories").length());
		Assert.assertEquals("custom invalid page size -1 items", true,
				histories.getBoolean("has_more"));

		// test get default page (1), custom invalid size : 101
		// 101 > 100 => size = DEFAULT_SIZE(100)
		histories = wr.path(HISTORIES_PATH).queryParam("page_size", "101")
				.accept(MediaType.APPLICATION_JSON).get(JSONObject.class);
		// we should have at least a full page and at least 1 more item
		Assert.assertEquals("custom invalid page size 100 items", 100,
				histories.getJSONArray("histories").length());
		Assert.assertEquals("custom invalid page size 100 items", true,
				histories.getBoolean("has_more"));

		// test get default page (1), custom invalid size : aaa
		// aaa is not int > size= DEFAULT_SIZE(100)
		histories = wr.path(HISTORIES_PATH).queryParam("page_size", "aaa")
				.accept(MediaType.APPLICATION_JSON).get(JSONObject.class);
		// we should have at least a full page and at least 1 more item
		Assert.assertEquals("custom invalid page size aaa items", 100,
				histories.getJSONArray("histories").length());
		Assert.assertEquals("custom invalid page size aaa items", true,
				histories.getBoolean("has_more"));

		// test get custom page(2), custom size: 2
		histories = wr.path(HISTORIES_PATH).queryParam("page", "2")
				.queryParam("page_size", "2")
				.accept(MediaType.APPLICATION_JSON).get(JSONObject.class);

		// we should have at least a full page and at least 1 more item
		Assert.assertEquals("custom page 2 custom page size 2 items", 2,
				histories.getJSONArray("histories").length());
		Assert.assertEquals("custom page 2 custom page size 2 has_more", true,
				histories.getBoolean("has_more"));
		String guid1 = histories.getJSONArray("histories").getJSONObject(1)
				.getString("guid");

		// repeat with next page
		histories = wr.path(HISTORIES_PATH).queryParam("page", "3")
				.queryParam("page_size", "2")
				.accept(MediaType.APPLICATION_JSON).get(JSONObject.class);

		// we should have at least a full page and at least 1 more item
		Assert.assertEquals("custom page 3 custom page size 2 items", 2,
				histories.getJSONArray("histories").length());
		Assert.assertEquals("custom page 3 custom page size 2 has_more", true,
				histories.getBoolean("has_more"));
		String guid2 = histories.getJSONArray("histories").getJSONObject(1)
				.getString("guid");

		// histories guid should be different
		Assert.assertNotEquals("different histories on pages", guid1, guid2);

	}
}
