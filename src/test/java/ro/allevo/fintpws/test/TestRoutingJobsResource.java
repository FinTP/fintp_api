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

import ro.allevo.fintpws.model.RoutingJobEntity;
import ro.allevo.fintpws.resources.RoutingJobsResource;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;

/**
 * Tests for {@link RoutingJobsResource}.
 * 
 * @author anda
 * 
 */

@RunWith(JUnit4.class)
public class TestRoutingJobsResource {
	static final String UNIT_TEST_ROUTING_JOBS = "unit HelloWorld";
	static final String ROUTING_JOBS_PATH = "api/routingjobs";
	static final String ROUTING_JOBS_PATH_WITH_SLASH = ROUTING_JOBS_PATH + "/";
	
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
		JSONObject routingJobs = null;
		do {
			findRecords = false;
			routingJobs = wr.path(ROUTING_JOBS_PATH)
					.queryParam("page", String.valueOf(page))
					.accept(MediaType.APPLICATION_JSON).get(JSONObject.class);
			JSONArray routingJobsArray = routingJobs.getJSONArray("routingjobs");
			for (int i = 0; i < routingJobsArray.length(); i++) {

				String guid = routingJobsArray.getJSONObject(i).optString(
						"guid");
				if (guid.startsWith("unit")) {
					TestRoutingJobResource.deleteTestRoutingJob(wr, guid);
					findRecords = true;
				}
			}
			if (!findRecords) {
				page++;
			}
		} while (routingJobs.has("has_more"));
	}
	
	@Test
	public void createRoutingJobs() throws JSONException {
		// create a routing job with a random guid
		String guid = "unit test" + new Random().nextInt();
		
		JSONObject response = TestRoutingJobResource.createTestRoutingJob(wr,
				guid);
		Assert.assertTrue("uri missing", response.getString("uri").length() > 0);

		// make sure it is created
		JSONObject routingJob = TestRoutingJobResource.readTestRoutingJob(wr,
				guid);
		Assert.assertEquals("routing job guid",guid,routingJob.getString("guid"));
	}
	
	@Test
	public void insertRoutingJobsAllFields() throws JSONException {
		// generate a routing job guid (JSONObject format)
		JSONObject jsonEntity = TestUtils.fillResourceData(new JSONObject(),
				new RoutingJobEntity());
		
		jsonEntity.put("guid", "unit"
				+ jsonEntity.getString("guid").substring(4));
		
		// create a routing job
		TestHelper.assertResponseCreated(wr, "POST", ROUTING_JOBS_PATH, jsonEntity);
		
		// now read it
		JSONObject routingJob = TestRoutingJobResource.readTestRoutingJob(wr, jsonEntity.getString("guid"));

		// assert it worked
		Assert.assertEquals("routing job guid filed not correct", TestUtils
				.compareJSONObjects(new RoutingJobEntity(), jsonEntity, routingJob,
						"guid"), true);
	}

	@Test
	public void readRoutingJobs() throws JSONException {
		// make sure it makes sense ( >0 routing job is available )
		TestRoutingJobResource.createTestRoutingJob(wr, UNIT_TEST_ROUTING_JOBS);
		
		JSONObject routingJobs = wr.path(ROUTING_JOBS_PATH)
				.accept(MediaType.APPLICATION_JSON).get(JSONObject.class);

		// make sure the metadata is returned
		Assert.assertEquals("href not correct", ROUTING_JOBS_PATH,
				routingJobs.getString("href"));
		Assert.assertEquals("_type not correct",
				RoutingJobsResource.class.getName(), routingJobs.getString("_type"));

		JSONArray routingJobsList = routingJobs.getJSONArray("routingjobs");
		Assert.assertTrue("routingjobs list empty", routingJobsList.length() > 0);

		// get first routing job, see if has a guid
		Assert.assertTrue("routing jobs list empty", routingJobsList.getJSONObject(0)
				.getString("guid").length() > 0);
	}
	
	@Test
	public void updateRoutingJobs() throws JSONException {
		TestHelper.assertResponseMethodNotAllowed(wr, "PUT", ROUTING_JOBS_PATH_WITH_SLASH,
				null);
	}

	@Test
	public void deleteRoutingJobs() throws JSONException {
		TestHelper.assertResponseMethodNotAllowed(wr, "DELETE", ROUTING_JOBS_PATH_WITH_SLASH,
				null);
	}

	@Test
	public void totalTests() throws JSONException {
		// requst t among some garbage
		JSONObject routingJobs = wr.path(ROUTING_JOBS_PATH_WITH_SLASH).queryParam("filter", "stu")
				.accept(MediaType.APPLICATION_JSON).get(JSONObject.class);
		int total1 = routingJobs.getInt("total");

		TestRoutingJobResource.createTestRoutingJob(wr, "unit_total");
		
		// requst t among some garbage
		routingJobs = wr.path(ROUTING_JOBS_PATH_WITH_SLASH)
				.queryParam("filter", "adste").accept(MediaType.APPLICATION_JSON_TYPE)
				.get(JSONObject.class);
		
		int total2 = routingJobs.getInt("total");

		Assert.assertEquals("total not +1", total1 + 1, total2);
		
		// request a small page, look for total to be the same
		routingJobs = wr.path(ROUTING_JOBS_PATH_WITH_SLASH)
				.queryParam("filter", "stu").queryParam("page_size", "1")
				.accept(MediaType.APPLICATION_JSON).get(JSONObject.class);
		
		int total3 = routingJobs.getInt("total");
		
		Assert.assertEquals("total wrong for page", total1 + 1, total3);
	}

	@Test
	public void pagingTests() throws JSONException {
		// TODO: test last page
		// create 101 routing jobs
		for (int i = 0; i < 101; i++) {
			TestRoutingJobResource.createTestRoutingJob(wr, "unit_page_test" + i);
		}

		// test get default page (1), default page_size
		JSONObject routingJobs = wr.path(ROUTING_JOBS_PATH)
				.accept(MediaType.APPLICATION_JSON).get(JSONObject.class);

		// we should have at least a full page and at least 1 more item
		Assert.assertEquals("default paging items", 100, routingJobs
				.getJSONArray("routingjobs").length());
		Assert.assertEquals("default paging has_more", true,
				routingJobs.getBoolean("has_more"));

		// test get default page (1), custom size : 42
		// 42 = between[1,100] => we should get 42 routing jobs per page
		routingJobs = wr.path(ROUTING_JOBS_PATH)
				.queryParam("page_size", "42")
				.accept(MediaType.APPLICATION_JSON).get(JSONObject.class);
		// we should have at least a full page and at least 1 more item
		Assert.assertEquals("custom page size items", 42, routingJobs
				.getJSONArray("routingjobs").length());
		Assert.assertEquals("custom page size has_more", true,
				routingJobs.getBoolean("has_more"));

		// test get default page (1), custom invalid size : -1
		// -1 < 0 => size = DEFAULT_SIZE(100)
		routingJobs = wr.path(ROUTING_JOBS_PATH)
				.queryParam("page_size", "-1")
				.accept(MediaType.APPLICATION_JSON).get(JSONObject.class);
		// we should have at least a full page and at least 1 more item
		Assert.assertEquals("custom invalid page size -1 items", 100,
				routingJobs.getJSONArray("routingjobs").length());
		Assert.assertEquals("custom invalid page size -1 items", true,
				routingJobs.getBoolean("has_more"));

		// test get default page (1), custom invalid size : 101
		// 101 > 100 => size = DEFAULT_SIZE(100)
		routingJobs = wr.path(ROUTING_JOBS_PATH)
				.queryParam("page_size", "101")
				.accept(MediaType.APPLICATION_JSON).get(JSONObject.class);
		// we should have at least a full page and at least 1 more item
		Assert.assertEquals("custom invalid page size 100 items", 100,
				routingJobs.getJSONArray("routingjobs").length());
		Assert.assertEquals("custom invalid page size 100 items", true,
				routingJobs.getBoolean("has_more"));

		// test get default page (1), custom invalid size : aaa
		// aaa is not int > size= DEFAULT_SIZE(100)
		routingJobs = wr.path(ROUTING_JOBS_PATH)
				.queryParam("page_size", "aaa")
				.accept(MediaType.APPLICATION_JSON).get(JSONObject.class);
		// we should have at least a full page and at least 1 more item
		Assert.assertEquals("custom invalid page size aaa items", 100,
				routingJobs.getJSONArray("routingjobs").length());
		Assert.assertEquals("custom invalid page size aaa items", true,
				routingJobs.getBoolean("has_more"));

		// test get custom page(2), custom size: 2
		routingJobs = wr.path(ROUTING_JOBS_PATH).queryParam("page", "2")
				.queryParam("page_size", "2")
				.accept(MediaType.APPLICATION_JSON).get(JSONObject.class);

		// we should have at least a full page and at least 1 more item
		Assert.assertEquals("custom page 2 custom page size 2 items", 2,
				routingJobs.getJSONArray("routingjobs").length());
		Assert.assertEquals("custom page 2 custom page size 2 has_more", true,
				routingJobs.getBoolean("has_more"));
		String guid1 = routingJobs.getJSONArray("routingjobs")
				.getJSONObject(1).getString("guid");

		// repeat with next page
		routingJobs = wr.path(ROUTING_JOBS_PATH).queryParam("page", "3")
				.queryParam("page_size", "2")
				.accept(MediaType.APPLICATION_JSON).get(JSONObject.class);

		// we should have at least a full page and at least 1 more item
		Assert.assertEquals("custom page 3 custom page size 2 items", 2,
				routingJobs.getJSONArray("routingjobs").length());
		Assert.assertEquals("custom page 3 custom page size 2 has_more", true,
				routingJobs.getBoolean("has_more"));
		String guid2 = routingJobs.getJSONArray("routingjobs")
				.getJSONObject(1).getString("guid");

		// routing jobs guid should be different
		Assert.assertNotEquals("different routing jobs on pages", guid1, guid2);

	}
}
