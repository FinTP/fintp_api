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

import ro.allevo.fintpws.model.ServiceMapEntity;
import ro.allevo.fintpws.resources.ServiceMapsResource;

import com.sun.jersey.api.client.Client;
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
public class TestServiceMapsResource {

	static final String SERVICE_MAPS_PATH = "api/servicemaps";
	static final String SERVICE_MAPS_PATH_WITH_SLASH = SERVICE_MAPS_PATH + "/";
	static final String UNIT_TEST_Q_NAME = "unittest q name";

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
		JSONObject serviceMaps = null;
		do {
			findRecords = false;
			serviceMaps = wr.path(SERVICE_MAPS_PATH)
					.queryParam("page", String.valueOf(page))
					.accept(MediaType.APPLICATION_JSON).get(JSONObject.class);
			JSONArray serviceMapsArray = serviceMaps
					.getJSONArray("servicemaps");
			for (int i = 0; i < serviceMapsArray.length(); i++) {

				String friendlyName = serviceMapsArray.getJSONObject(i)
						.getString("friendlyname");

				if (friendlyName.startsWith("unit")) {
					TestServiceMapResource.deleteTestServiceMap(wr,
							friendlyName);
					findRecords = true;
				}
			}
			if (!findRecords) {
				page++;
			}
		} while (serviceMaps.has("has_more"));

	}

	@Test
	public void createServiceMaps() throws JSONException {
		// create a service map with a random name
		String friendlyName = "unit test name" + new Random().nextInt();
		JSONObject response = TestServiceMapResource.createTestServiceMap(wr,
				friendlyName);
		Assert.assertTrue("uri missing", response.getString("uri").length() > 0);

		// make sure it is created
		JSONObject serviceMap = TestServiceMapResource.readTestServiceMap(wr,
				friendlyName);
		Assert.assertEquals("service map friendlyname ", friendlyName,
				serviceMap.getString("friendlyname"));

		// make sure we get a conflict if we created it again
		TestHelper.assertResponseConflict(wr, "POST", SERVICE_MAPS_PATH,
				serviceMap);
	}

	@Test
	public void insertServiceMapsAllFields() throws JSONException {
		// generate a service map (JSONObject)
		JSONObject jsonEntity = TestUtils.fillResourceData(new JSONObject(),
				new ServiceMapEntity());

		jsonEntity.remove("serviceid");
		jsonEntity.put(
				"friendlyname",
				"unit"
						+ jsonEntity.getString("friendlyname")
								.substring(
										0,
										jsonEntity.getString("friendlyname")
												.length() - 4));

		// create a service map
		TestHelper.assertResponseCreated(wr, "POST", SERVICE_MAPS_PATH,
				jsonEntity);

		// now read it
		JSONObject serviceMap = TestServiceMapResource.readTestServiceMap(wr,
				jsonEntity.getString("friendlyname"));

		// assert it worked
		Assert.assertEquals("service map not filled correct", TestUtils
				.compareJSONObjects(new ServiceMapEntity(), jsonEntity,
						serviceMap, "serviceid,friendlyname"), true);
	}

	@Test
	public void readServiceMapsAllFields() throws JSONException {
		// make sure it makes sense (>0 service map available)
		TestServiceMapResource.createTestServiceMap(wr, "unit_desc");

		JSONObject serviceMaps = wr.path(SERVICE_MAPS_PATH)
				.accept(MediaType.APPLICATION_JSON).get(JSONObject.class);

		// make sure the metadata is returned
		Assert.assertEquals("href not correct", SERVICE_MAPS_PATH,
				serviceMaps.getString("href"));
		Assert.assertEquals("_type not correct",
				ServiceMapsResource.class.getName(),
				serviceMaps.getString("_type"));

		JSONArray tlList = serviceMaps.getJSONArray("servicemaps");
		Assert.assertTrue("service maps list empty", tlList.length() > 0);

		// get first service map, see if has serviceid
		Assert.assertTrue("servicemaps list empty", tlList.getJSONObject(0)
				.getString("friendlyname").length() > 0);

	}

	@Test
	public void readServiceMaps() throws JSONException {
		// make sure it makes sense (>0 service maps available)
		TestServiceMapResource.createTestServiceMap(wr, "unit_name");

		JSONObject serviceMaps = wr.path(SERVICE_MAPS_PATH)
				.accept(MediaType.APPLICATION_JSON).get(JSONObject.class);

		// make sure the metadata is returned
		Assert.assertEquals("href not correct", SERVICE_MAPS_PATH,
				serviceMaps.getString("href"));
		Assert.assertEquals("_type not correct",
				ServiceMapsResource.class.getName(),
				serviceMaps.getString("_type"));

		JSONArray tlList = serviceMaps.getJSONArray("servicemaps");
		Assert.assertTrue("service maps list empty", tlList.length() > 0);

		// get first service map, see if has friendly name
		Assert.assertTrue("service maps list empty", tlList.getJSONObject(0)
				.getString("friendlyname").length() > 0);
	}

	@Test
	public void updateServiceMaps() throws JSONException {
		TestHelper.assertResponseMethodNotAllowed(wr, "PUT",
				SERVICE_MAPS_PATH_WITH_SLASH, null);
	}

	@Test
	public void deleteServiceMaps() throws JSONException {
		TestHelper.assertResponseMethodNotAllowed(wr, "DELETE",
				SERVICE_MAPS_PATH_WITH_SLASH, null);
	}

	@Test
	public void totalTests() throws JSONException {
		// request t among some other garbage
		JSONObject serviceMaps = wr.path(SERVICE_MAPS_PATH_WITH_SLASH)
				.queryParam("filter", "adste")
				.accept(MediaType.APPLICATION_JSON_TYPE).get(JSONObject.class);

		int total1 = serviceMaps.getInt("total");

		TestServiceMapResource.createTestServiceMap(wr, "unit_total");

		// request t among some other garbage
		serviceMaps = wr.path(SERVICE_MAPS_PATH_WITH_SLASH)
				.queryParam("filter", "adste")
				.accept(MediaType.APPLICATION_JSON_TYPE).get(JSONObject.class);

		int total2 = serviceMaps.getInt("total");

		Assert.assertEquals("total not +1", total1 + 1, total2);

		// request a small page, look for total to be the same
		serviceMaps = wr.path(SERVICE_MAPS_PATH_WITH_SLASH)
				.queryParam("filter", "stu").queryParam("page_size", "1")
				.accept(MediaType.APPLICATION_JSON).get(JSONObject.class);

		int total3 = serviceMaps.getInt("total");

		Assert.assertEquals("total wrong for page", total1 + 1, total3);
	}


	@Test
	public void pagingTests() throws JSONException {
		// TODO: test last page

		// create 101 serviceMaps
		for (int i = 0; i < 101; i++) {
			TestServiceMapResource.createTestServiceMap(wr, "unit_page_test"
					+ i);
		}

		// test get default page (1), default page_size
		JSONObject serviceMaps = wr.path(SERVICE_MAPS_PATH)
				.accept(MediaType.APPLICATION_JSON).get(JSONObject.class);

		// we should have at least a full page and at least 1 more item
		Assert.assertEquals("default paging items", 100, serviceMaps
				.getJSONArray("servicemaps").length());
		Assert.assertEquals("default paging has_more", true,
				serviceMaps.getBoolean("has_more"));

		// test get default page (1), custom size : 42
		// 42 = between[1,100] => we should get 42 service maps per page
		serviceMaps = wr.path(SERVICE_MAPS_PATH).queryParam("page_size", "42")
				.accept(MediaType.APPLICATION_JSON).get(JSONObject.class);
		// we should have at least a full page and at least 1 more item
		Assert.assertEquals("custom page size items", 42, serviceMaps
				.getJSONArray("servicemaps").length());
		Assert.assertEquals("custom page size has_more", true,
				serviceMaps.getBoolean("has_more"));

		// test get default page (1), custom invalid size : -1
		// -1 < 0 => size = DEFAULT_SIZE(100)
		serviceMaps = wr.path(SERVICE_MAPS_PATH).queryParam("page_size", "-1")
				.accept(MediaType.APPLICATION_JSON).get(JSONObject.class);
		// we should have at least a full page and at least 1 more item
		Assert.assertEquals("custom invalid page size -1 items", 100,
				serviceMaps.getJSONArray("servicemaps").length());
		Assert.assertEquals("custom invalid page size -1 items", true,
				serviceMaps.getBoolean("has_more"));

		// test get default page (1), custom invalid size : 101
		// 101 > 100 => size = DEFAULT_SIZE(100)
		serviceMaps = wr.path(SERVICE_MAPS_PATH).queryParam("page_size", "101")
				.accept(MediaType.APPLICATION_JSON).get(JSONObject.class);
		// we should have at least a full page and at least 1 more item
		Assert.assertEquals("custom invalid page size 100 items", 100,
				serviceMaps.getJSONArray("servicemaps").length());
		Assert.assertEquals("custom invalid page size 100 items", true,
				serviceMaps.getBoolean("has_more"));

		// test get default page (1), custom invalid size : aaa
		// aaa is not int > size= DEFAULT_SIZE(100)
		serviceMaps = wr.path(SERVICE_MAPS_PATH).queryParam("page_size", "aaa")
				.accept(MediaType.APPLICATION_JSON).get(JSONObject.class);
		// we should have at least a full page and at least 1 more item
		Assert.assertEquals("custom invalid page size aaa items", 100,
				serviceMaps.getJSONArray("servicemaps").length());
		Assert.assertEquals("custom invalid page size aaa items", true,
				serviceMaps.getBoolean("has_more"));

		// test get custom page(2), custom size: 2
		serviceMaps = wr.path(SERVICE_MAPS_PATH).queryParam("page", "2")
				.queryParam("page_size", "2")
				.accept(MediaType.APPLICATION_JSON).get(JSONObject.class);

		// we should have at least a full page and at least 1 more item
		Assert.assertEquals("custom page 2 custom page size 2 items", 2,
				serviceMaps.getJSONArray("servicemaps").length());
		Assert.assertEquals("custom page 2 custom page size 2 has_more", true,
				serviceMaps.getBoolean("has_more"));
		String serviceName1 = serviceMaps.getJSONArray("servicemaps")
				.getJSONObject(1).getString("friendlyname");

		// repeat with next page
		serviceMaps = wr.path(SERVICE_MAPS_PATH).queryParam("page", "3")
				.queryParam("page_size", "2")
				.accept(MediaType.APPLICATION_JSON).get(JSONObject.class);

		// we should have at least a full page and at least 1 more item
		Assert.assertEquals("custom page 3 custom page size 2 items", 2,
				serviceMaps.getJSONArray("servicemaps").length());
		Assert.assertEquals("custom page 3 custom page size 2 has_more", true,
				serviceMaps.getBoolean("has_more"));
		String serviceName2 = serviceMaps.getJSONArray("servicemaps")
				.getJSONObject(1).getString("friendlyname");

		// service maps serviceid should be different
		Assert.assertNotEquals("different guids on pages", serviceName1,
				serviceName2);

	}
}
