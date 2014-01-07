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

import ro.allevo.fintpws.model.ServicePerformanceEntity;
import ro.allevo.fintpws.resources.ServicePerformancesResource;

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
public class TestServicePerformancesResource {

	static final String SERVICE_PERFORMANCES_PATH = "api/serviceperformances";
	static final String SERVICE_PERFORMANCES_PATH_WITH_SLASH = SERVICE_PERFORMANCES_PATH
			+ "/";
	static final String UNIT_TEST_SERVICE_PERFOEMANCE_SEQUENCENO = "9999999";

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
		JSONObject servicePerformances = null;
		do {
			findRecords = false;
			servicePerformances = wr.path(SERVICE_PERFORMANCES_PATH)
					.queryParam("page", String.valueOf(page))
					.accept(MediaType.APPLICATION_JSON).get(JSONObject.class);
			JSONArray servicePerformancesArray = servicePerformances
					.getJSONArray("serviceperformances");
			for (int i = 0; i < servicePerformancesArray.length(); i++) {

				String sequenceNo = servicePerformancesArray.getJSONObject(i)
						.getString("sequenceno");

				String serviceId = servicePerformancesArray.getJSONObject(i)
						.getString("serviceid");

				if (sequenceNo
						.startsWith(UNIT_TEST_SERVICE_PERFOEMANCE_SEQUENCENO)) {
					TestServicePerformanceResource
							.deleteTestServicePerformance(wr, serviceId);
					findRecords = true;
				}
			}
			if (!findRecords) {
				page++;
			}
		} while (servicePerformances.has("has_more"));

	}

	@Test
	public void createServicePerformances() throws JSONException {
		// create a service performance with a sequenceno
		// String friendlyName = "unit test name" + new Random().nextInt();

		JSONObject response = TestServicePerformanceResource
				.createTestServicePerformance(wr,
						UNIT_TEST_SERVICE_PERFOEMANCE_SEQUENCENO);

		Assert.assertTrue("uri missing", response.getString("uri").length() > 0);

		// make sure it is created
		JSONObject servicePerformance = TestServicePerformanceResource
				.readTestServicePerformance(wr, response.getString("id"));
		Assert.assertEquals("service performnace sequenceno ",
				UNIT_TEST_SERVICE_PERFOEMANCE_SEQUENCENO,
				servicePerformance.getString("sequenceno"));

		// make sure we get a conflict if we created it again
		TestHelper.assertResponseConflict(wr, "POST",
				SERVICE_PERFORMANCES_PATH, servicePerformance);
	}

	@Test
	public void insertServicePerformancesAllFields() throws JSONException {
		// generate a service performance (JSONObject)
		JSONObject jsonEntity = TestUtils.fillResourceData(new JSONObject(),
				new ServicePerformanceEntity());

		jsonEntity.remove("serviceid");
		jsonEntity.put("sequenceno", UNIT_TEST_SERVICE_PERFOEMANCE_SEQUENCENO);

		// create a service performance
		JSONObject response = TestHelper.assertResponseCreated(wr, "POST",
				SERVICE_PERFORMANCES_PATH, jsonEntity);

		// now read it
		JSONObject servicePerfromance = TestServicePerformanceResource
				.readTestServicePerformance(wr, response.getString("id"));

		// assert it worked
		Assert.assertEquals("service performance not filled correct", TestUtils
				.compareJSONObjects(new ServicePerformanceEntity(), jsonEntity,
						servicePerfromance, "serviceid"), true);
	}

	@Test
	public void readServicePerformancesAllFields() throws JSONException {
		// make sure it makes sense (>0 service performance available)
		JSONObject response = TestServicePerformanceResource
				.createTestServicePerformance(wr,
						UNIT_TEST_SERVICE_PERFOEMANCE_SEQUENCENO);

		JSONObject servicePerformances = wr.path(SERVICE_PERFORMANCES_PATH)
				.accept(MediaType.APPLICATION_JSON).get(JSONObject.class);

		// make sure the metadata is returned
		Assert.assertEquals("href not correct", SERVICE_PERFORMANCES_PATH,
				servicePerformances.getString("href"));
		Assert.assertEquals("_type not correct",
				ServicePerformancesResource.class.getName(),
				servicePerformances.getString("_type"));

		JSONArray tlList = servicePerformances
				.getJSONArray("serviceperformances");
		Assert.assertTrue("service performances list empty",
				tlList.length() > 0);

		// get first service performance, see if has serviceid
		Assert.assertTrue("serviceperformances list empty", tlList
				.getJSONObject(0).getString("serviceid").length() > 0);

	}

	@Test
	public void readServicePerformances() throws JSONException {
		// make sure it makes sense (>0 service performances available)
		JSONObject response = TestServicePerformanceResource
				.createTestServicePerformance(wr,
						UNIT_TEST_SERVICE_PERFOEMANCE_SEQUENCENO);

		JSONObject servicePerformances = wr.path(SERVICE_PERFORMANCES_PATH)
				.accept(MediaType.APPLICATION_JSON).get(JSONObject.class);

		// make sure the metadata is returned
		Assert.assertEquals("href not correct", SERVICE_PERFORMANCES_PATH,
				servicePerformances.getString("href"));
		Assert.assertEquals("_type not correct",
				ServicePerformancesResource.class.getName(),
				servicePerformances.getString("_type"));

		JSONArray tlList = servicePerformances
				.getJSONArray("serviceperformances");
		Assert.assertTrue("service performances list empty",
				tlList.length() > 0);

		// get first service performances, see if has serviceid
		Assert.assertTrue("service performances list empty", tlList
				.getJSONObject(0).getString("serviceid").length() > 0);
	}

	@Test
	public void updateServicePerformances() throws JSONException {
		TestHelper.assertResponseMethodNotAllowed(wr, "PUT",
				SERVICE_PERFORMANCES_PATH_WITH_SLASH, null);
	}

	@Test
	public void deleteServicePerformances() throws JSONException {
		TestHelper.assertResponseMethodNotAllowed(wr, "DELETE",
				SERVICE_PERFORMANCES_PATH_WITH_SLASH, null);
	}

	@Test
	public void totalTests() throws JSONException {
		// request t among some other garbage
		JSONObject servicePerformances = wr.path(SERVICE_PERFORMANCES_PATH)
				.queryParam("filter", "adste")
				.accept(MediaType.APPLICATION_JSON_TYPE).get(JSONObject.class);

		int total1 = servicePerformances.getInt("total");

		TestServicePerformanceResource.createTestServicePerformance(wr,
				UNIT_TEST_SERVICE_PERFOEMANCE_SEQUENCENO);

		// request t among some other garbage
		servicePerformances = wr.path(SERVICE_PERFORMANCES_PATH_WITH_SLASH)
				.queryParam("filter", "adste")
				.accept(MediaType.APPLICATION_JSON_TYPE).get(JSONObject.class);

		int total2 = servicePerformances.getInt("total");

		Assert.assertEquals("total not +1", total1 + 1, total2);

		// request a small page, look for total to be the same
		servicePerformances = wr.path(SERVICE_PERFORMANCES_PATH_WITH_SLASH)
				.queryParam("filter", "stu").queryParam("page_size", "1")
				.accept(MediaType.APPLICATION_JSON).get(JSONObject.class);

		int total3 = servicePerformances.getInt("total");

		Assert.assertEquals("total wrong for page", total1 + 1, total3);
	}

	@Test
	public void pagingTests() throws JSONException {

		// create 101 serviceMaps
		for (int i = 0; i < 101; i++) {
			TestServicePerformanceResource.createTestServicePerformance(wr,
					UNIT_TEST_SERVICE_PERFOEMANCE_SEQUENCENO + i);
		}

		// test get default page (1), default page_size
		JSONObject servicePerformances = wr.path(SERVICE_PERFORMANCES_PATH)
				.accept(MediaType.APPLICATION_JSON).get(JSONObject.class);

		// we should have at least a full page and at least 1 more item
		Assert.assertEquals("default paging items", 100, servicePerformances
				.getJSONArray("serviceperformances").length());
		Assert.assertEquals("default paging has_more", true,
				servicePerformances.getBoolean("has_more"));

		// test get default page (1), custom size : 42
		// 42 = between[1,100] => we should get 42 service maps per page
		servicePerformances = wr.path(SERVICE_PERFORMANCES_PATH).queryParam("page_size", "42")
				.accept(MediaType.APPLICATION_JSON).get(JSONObject.class);
		// we should have at least a full page and at least 1 more item
		Assert.assertEquals("custom page size items", 42, servicePerformances
				.getJSONArray("serviceperformances").length());
		Assert.assertEquals("custom page size has_more", true,
				servicePerformances.getBoolean("has_more"));

		// test get default page (1), custom invalid size : -1
		// -1 < 0 => size = DEFAULT_SIZE(100)
		servicePerformances = wr.path(SERVICE_PERFORMANCES_PATH).queryParam("page_size", "-1")
				.accept(MediaType.APPLICATION_JSON).get(JSONObject.class);
		// we should have at least a full page and at least 1 more item
		Assert.assertEquals("custom invalid page size -1 items", 100,
				servicePerformances.getJSONArray("serviceperformances").length());
		Assert.assertEquals("custom invalid page size -1 items", true,
				servicePerformances.getBoolean("has_more"));

		// test get default page (1), custom invalid size : 101
		// 101 > 100 => size = DEFAULT_SIZE(100)
		servicePerformances = wr.path(SERVICE_PERFORMANCES_PATH).queryParam("page_size", "101")
				.accept(MediaType.APPLICATION_JSON).get(JSONObject.class);
		// we should have at least a full page and at least 1 more item
		Assert.assertEquals("custom invalid page size 100 items", 100,
				servicePerformances.getJSONArray("serviceperformances").length());
		Assert.assertEquals("custom invalid page size 100 items", true,
				servicePerformances.getBoolean("has_more"));

		// test get default page (1), custom invalid size : aaa
		// aaa is not int > size= DEFAULT_SIZE(100)
		servicePerformances = wr.path(SERVICE_PERFORMANCES_PATH).queryParam("page_size", "aaa")
				.accept(MediaType.APPLICATION_JSON).get(JSONObject.class);
		// we should have at least a full page and at least 1 more item
		Assert.assertEquals("custom invalid page size aaa items", 100,
				servicePerformances.getJSONArray("serviceperformances").length());
		Assert.assertEquals("custom invalid page size aaa items", true,
				servicePerformances.getBoolean("has_more"));

		// test get custom page(2), custom size: 2
		servicePerformances = wr.path(SERVICE_PERFORMANCES_PATH).queryParam("page", "2")
				.queryParam("page_size", "2")
				.accept(MediaType.APPLICATION_JSON).get(JSONObject.class);

		// we should have at least a full page and at least 1 more item
		Assert.assertEquals("custom page 2 custom page size 2 items", 2,
				servicePerformances.getJSONArray("serviceperformances").length());
		Assert.assertEquals("custom page 2 custom page size 2 has_more", true,
				servicePerformances.getBoolean("has_more"));
		String serviceid1 = servicePerformances.getJSONArray("serviceperformances")
				.getJSONObject(1).getString("serviceid");

		// repeat with next page
		servicePerformances = wr.path(SERVICE_PERFORMANCES_PATH).queryParam("page", "3")
				.queryParam("page_size", "2")
				.accept(MediaType.APPLICATION_JSON).get(JSONObject.class);

		// we should have at least a full page and at least 1 more item
		Assert.assertEquals("custom page 3 custom page size 2 items", 2,
				servicePerformances.getJSONArray("serviceperformances").length());
		Assert.assertEquals("custom page 3 custom page size 2 has_more", true,
				servicePerformances.getBoolean("has_more"));
		String serviceid2 = servicePerformances.getJSONArray("serviceperformances")
				.getJSONObject(1).getString("serviceid");

		// service maps serviceid should be different
		Assert.assertNotEquals("different guids on pages", serviceid1,
				serviceid2);

	}
}
