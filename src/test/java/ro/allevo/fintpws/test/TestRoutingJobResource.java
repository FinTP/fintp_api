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

import ro.allevo.fintpws.model.RoutingJobEntity;
import ro.allevo.fintpws.resources.RoutingJobResource;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;

public class TestRoutingJobResource {
	static final String UNIT_TEST_ROUTING_JOBS = "unit HelloWorld";
	static final String ROUTING_JOBS_PATH = "api/routingjobs";
	static final String ROUTING_JOBS_PATH_WITH_SLASH = ROUTING_JOBS_PATH + "/";
	static WebResource wr;

	@BeforeClass
	public static void startMethod() throws JSONException {
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
		JSONObject routingJobs = null;
		do {
			findRecords = false;
			routingJobs = wr.path(ROUTING_JOBS_PATH)
					.queryParam("page", String.valueOf(page))
					.accept(MediaType.APPLICATION_JSON).get(JSONObject.class);
			JSONArray routingJobsArray = routingJobs.getJSONArray("routingjobs");
			for (int i = 0; i < routingJobsArray.length(); i++) {

				String guid = routingJobsArray.getJSONObject(i).getString(
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
	
	public static JSONObject createTestRoutingJob(WebResource wr, 
			String guid) throws JSONException {
		JSONObject routingJob = new JSONObject()
				.put("guid", guid)
				.put("status", new Random().nextInt())
				.put("backout", new Random().nextInt())
				.put("priority",new Random().nextInt())
				.put("function",UNIT_TEST_ROUTING_JOBS);
		
		return TestHelper
				.assertResponseCreated(wr, "POST", ROUTING_JOBS_PATH, routingJob);
	}
	
	public static JSONObject createTestRoutingJob(WebResource wr, 
			String guid, String status) throws JSONException {
		JSONObject routingJob = new JSONObject()
			.put("guid", guid)
			.put("status", status)
			.put("backout", new Random().nextInt())
			.put("priority",new Random().nextInt())
			.put("function",UNIT_TEST_ROUTING_JOBS);
		
		return TestHelper
				.assertResponseCreated(wr, "POST", ROUTING_JOBS_PATH, routingJob);
	}
	
	public static JSONObject createTestRoutingJob(WebResource wr, 
			String guid, String status, String function) throws JSONException {
		JSONObject routingJob = new JSONObject()
			.put("guid", guid)
			.put("status", status)
			.put("backout", new Random().nextInt())
			.put("priority",new Random().nextInt())
			.put("function",function);
		
		return TestHelper
				.assertResponseCreated(wr, "POST", ROUTING_JOBS_PATH, routingJob);
	}

	public static JSONObject findTestRoutingJob(WebResource wr, String guid)
			throws JSONException {
		ClientResponse clientResponse = wr.path(ROUTING_JOBS_PATH).path(guid)
				.accept(MediaType.APPLICATION_JSON)
				.type(MediaType.APPLICATION_JSON).get(ClientResponse.class);
		return (clientResponse.getClientResponseStatus()
				.equals(ClientResponse.Status.OK)) ? clientResponse
				.getEntity(JSONObject.class) : null;
	}
	
	public static JSONObject findRandomRoutingJob(WebResource wr)
			throws JSONException {

		JSONObject routingJobs = wr.path(ROUTING_JOBS_PATH).queryParam("filter", "tb")
				.accept(MediaType.APPLICATION_JSON).get(JSONObject.class);

		JSONArray routingJobsArray = routingJobs.getJSONArray("routingjobs");
		if(routingJobsArray.length() == 0){
			return null;
		}
		
		JSONObject routingJob = null;
		do{
			routingJob = routingJobsArray.getJSONObject(
					new Random().nextInt(routingJobsArray.length()));
		}while (!routingJob.getString("guid").startsWith("unit"));
		
		return routingJob;
	}
	
	public static JSONObject readTestRoutingJob(WebResource wr, String guid)
			throws JSONException {
		return TestHelper.assertResponseOK(wr, "GET", ROUTING_JOBS_PATH_WITH_SLASH + guid,
				null);
	}

	public static void deleteTestRoutingJob(WebResource wr, String guid) {
		wr.path(ROUTING_JOBS_PATH_WITH_SLASH).path(guid).delete(ClientResponse.class);
	}

	@Test
	public void createRoutingJob() throws JSONException {
		TestHelper.assertResponseMethodNotAllowed(wr, "POST", ROUTING_JOBS_PATH_WITH_SLASH
				+ UNIT_TEST_ROUTING_JOBS, null);
	}
	
	@Test
	public void readRoutingJob() throws JSONException {
		// create a routing job with a random status
		String guid = UNIT_TEST_ROUTING_JOBS;
		String status = new Random().nextInt() + "";
		createTestRoutingJob(wr, guid, status);

		// now read it
		JSONObject rj = readTestRoutingJob(wr, UNIT_TEST_ROUTING_JOBS);
		Assert.assertEquals("routing job staus not correct", status,
				rj.getString("status"));

		// make sure the metadata is returned
		Assert.assertEquals("href not correct", ROUTING_JOBS_PATH_WITH_SLASH
				+ UNIT_TEST_ROUTING_JOBS, rj.getString("href"));
		Assert.assertEquals("_type not correct", RoutingJobResource.class.getName(),
				rj.getString("_type"));

		// check 404 if the routing job with the stupid status is requested
		TestHelper.assertResponseNotFound(wr, "GET", ROUTING_JOBS_PATH_WITH_SLASH
				+ new Random().nextInt(), null);
	}

	@Test
	public void updateRoutingJobOptionalFields() throws JSONException {
		// test update status
		// create a routing job with a random status
		String guid = UNIT_TEST_ROUTING_JOBS + new Random().nextInt();
		String status = new Random().nextInt() + "";
		createTestRoutingJob(wr, guid, status);

		// change status to a new random one
		String status2 = new Random().nextInt() + "";
		JSONObject routingJob = readTestRoutingJob(wr, guid);
		routingJob.remove("guid");
		routingJob.put("status", status2);
		TestHelper.assertResponseOK(wr, "PUT",
				ROUTING_JOBS_PATH_WITH_SLASH + guid, routingJob);

		// assert it worked
		JSONObject routingJob2 = readTestRoutingJob(wr, guid);
		Assert.assertEquals("routing job status not correct", status2,
				routingJob2.getString("status"));

		// test singular field update
		String status3 = new Random().nextInt() + "";
		JSONObject routingJobnewstatus = new JSONObject().put("status", status3);
		TestHelper.assertResponseOK(wr, "PUT",
				ROUTING_JOBS_PATH_WITH_SLASH + guid, routingJobnewstatus);

		// assert it worked
		JSONObject routingJob3 = readTestRoutingJob(wr, guid);
		Assert.assertEquals("routing job status not correct", status3,
				routingJob3.getString("status"));
	}
	
	@Test
	public void updateRoutingJobLongName() throws JSONException {
		// create a routing job with a random guid and function
		String guid = UNIT_TEST_ROUTING_JOBS + new Random().nextInt();
		String function = UNIT_TEST_ROUTING_JOBS + new Random().nextInt();
		createTestRoutingJob(wr, guid, new Random().nextInt()+"", function);

		// update function
		JSONObject routingJob = readTestRoutingJob(wr, guid);
		routingJob.remove("function");
		routingJob.put("function", RandomStringUtils
				.randomAlphanumeric(201));
		TestHelper.assertResponseBadRequest(wr, "PUT", ROUTING_JOBS_PATH_WITH_SLASH + guid,
				routingJob);
	}
	
	@Test
	public void updateRoutingJobAllFields() throws JSONException {
		// generate a routing job (JSONObject format)
		JSONObject jsonEntity = TestUtils.fillResourceData(new JSONObject(),
				new RoutingJobEntity());
		jsonEntity.remove("status");
		jsonEntity.remove("guid");
		
		// create a routing job
		createTestRoutingJob(wr, UNIT_TEST_ROUTING_JOBS);

		// update all fields
		TestHelper.assertResponseOK(wr, "PUT", ROUTING_JOBS_PATH_WITH_SLASH
				+ UNIT_TEST_ROUTING_JOBS, jsonEntity);

		// now read it
		JSONObject routingJob = readTestRoutingJob(wr, UNIT_TEST_ROUTING_JOBS);

		// assert it worked
		Assert.assertEquals("routing job fields not correct", TestUtils
				.compareJSONObjects(new RoutingJobEntity(), jsonEntity, routingJob,
						"status,guid"), true);
	}

	@Test
	public void deleteRoutingJob() throws JSONException {
		// create a routing job with a random status
		createTestRoutingJob(wr, UNIT_TEST_ROUTING_JOBS);

		readTestRoutingJob(wr, UNIT_TEST_ROUTING_JOBS);

		// delete it
		TestHelper.assertResponseOK(wr, "DELETE", ROUTING_JOBS_PATH_WITH_SLASH
				+ UNIT_TEST_ROUTING_JOBS, null);

		// read it again to make sure it's gone
		TestHelper.assertResponseNotFound(wr, "GET", ROUTING_JOBS_PATH_WITH_SLASH
				+ UNIT_TEST_ROUTING_JOBS, null);

		// check 404 if the routing job with the random status is requested
		TestHelper.assertResponseNotFound(wr, "DELETE", ROUTING_JOBS_PATH_WITH_SLASH
				+ new Random().nextInt(), null);
	}
	
}
