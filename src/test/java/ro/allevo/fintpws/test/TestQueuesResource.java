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

import ro.allevo.fintpws.model.QueueEntity;
import ro.allevo.fintpws.resources.QueuesResource;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;

/**
 * Tests for {@link QueuesResource}.
 * 
 * @author horia
 */
@RunWith(JUnit4.class)
public class TestQueuesResource {
	/**
	 * Field logger.
	 */
	//private static Logger logger = LogManager
	//		.getLogger(TestQueuesResource.class.getName());

	static final String UNIT_TEST_Q_NAME = "unittest q name";
	static final String QUEUES_PATH = "api/queues";
	static final String QUEUES_PATH_WITH_SLASH = QUEUES_PATH + "/";
	static final String IMPOSSIBLE_SERVICE_MAP_NAME = "unit_impossible001_name";
	

	static WebResource wr;
	
	@BeforeClass
	public static void startMethod() throws JSONException{
		// create a client:
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
		// delete all queues that start with unit* 
		JSONObject queues;
		boolean findRecords = false;
		int page = 1;
		do{ 
			findRecords = false;
			queues = wr.path(QUEUES_PATH).queryParam("page", String.valueOf(page))
					.accept(MediaType.APPLICATION_JSON)
					.get(JSONObject.class);
			JSONArray queuesArray = queues.getJSONArray("queues");
			for (int i = 0; i < queuesArray.length(); i++) {
				String qname = queuesArray.getJSONObject(i).getString("name");
				if (qname.startsWith("unit")) {
					TestQueueResource.deleteTestQueue(wr, qname);
					findRecords = true;
				}
			}
			if(!findRecords){
				page++;
			}
		}while(queues.has("has_more"));
		TestQueueTypeResource.deleteTestQueueType(wr, "unit_test");
	}

	@Test
	public void createQueues() throws JSONException {
		// delete the queue if it already exists
		TestQueueResource.deleteTestQueue(wr, UNIT_TEST_Q_NAME);

		// make sure it is deleted
		TestHelper.assertResponseNotFound(wr, "GET", QUEUES_PATH_WITH_SLASH
				+ UNIT_TEST_Q_NAME, null);

		// create it with a random desc
		String qdesc = "unit test q desc" + new Random().nextInt();
		JSONObject rj = TestQueueResource.createTestQueue(wr, UNIT_TEST_Q_NAME,
				qdesc);
		Assert.assertTrue("uri missing", rj.getString("uri").length() > 0);

		// make sure it is created
		rj = TestQueueResource.readTestQueue(wr, UNIT_TEST_Q_NAME);
		Assert.assertEquals("q description not correct", qdesc,
				rj.getString("description"));

		// make sure we get a conflict if we create it again
		TestHelper.assertResponseConflict(wr, "POST", QUEUES_PATH, rj);
	}
	
	@Test
	public void insertQueueAllFields() throws JSONException {
		// generate a queue(JSONObject format)
		JSONObject jsonEntity = TestUtils.fillResourceData(new JSONObject(),
				new QueueEntity());
		jsonEntity.put("name", "unit"
				+ jsonEntity.getString("name").substring(4));
		if( null == TestQueueTypeResource.findTestQueueType(wr,"unit_test")){
			TestQueueTypeResource.createTestQueueType(wr, "unit_test", "lvl");
		}
		if (null == TestServiceMapResource.findTestServiceMap(wr,
				TestServiceMapResource.UNIT_TEST_SERVICE_MAP_NAME)) {
			TestServiceMapResource.createTestServiceMap(wr,
					TestServiceMapResource.UNIT_TEST_SERVICE_MAP_NAME);
		}
		JSONObject queueType = TestQueueTypeResource.readTestQueueType(wr,
				"unit_test");
		JSONObject serviceMap = TestServiceMapResource.readTestServiceMap(wr,
				TestServiceMapResource.UNIT_TEST_SERVICE_MAP_NAME);
		
		jsonEntity.put("type", queueType.get("typename"));
		jsonEntity.put("connector",serviceMap.get("friendlyname"));
		// create a queue
		TestHelper.assertResponseCreated(wr, "POST", QUEUES_PATH, jsonEntity);
		// now read it
		JSONObject queue = TestQueueResource.readTestQueue(wr, jsonEntity.getString("name"));

		// TODO:temporarily disable connector comparison (it is string, not
		
		// assert it worked
		Assert.assertEquals("q filed not correct", TestUtils
				.compareJSONObjects(new QueueEntity(), jsonEntity, queue,
						"guid,type,queuetypeentity,servicemapentity,connector"), true);
	}

	@Test
	public void readQueues() throws JSONException {
		// make sure it makes sense ( >0 queue is available )
		TestQueueResource.createTestQueue(wr, UNIT_TEST_Q_NAME, "description");

		JSONObject queues = wr.path(QUEUES_PATH)
				.accept(MediaType.APPLICATION_JSON).get(JSONObject.class);

		// make sure the metadata is returned
		Assert.assertEquals("href not correct", QUEUES_PATH,
				queues.getString("href"));
		Assert.assertEquals("_type not correct",
				QueuesResource.class.getName(), queues.getString("_type"));

		JSONArray queuesList = queues.getJSONArray("queues");
		Assert.assertTrue("queue list empty", queuesList.length() > 0);

		// get first queue, see if has a name
		Assert.assertTrue("queue list empty", queuesList.getJSONObject(0)
				.getString("name").length() > 0);
	}

	@Test
	public void updateQueues() throws JSONException {
		TestHelper.assertResponseMethodNotAllowed(wr, "PUT", QUEUES_PATH_WITH_SLASH,
				null);
	}

	@Test
	public void deleteQueues() throws JSONException {
		TestHelper.assertResponseMethodNotAllowed(wr, "DELETE", QUEUES_PATH_WITH_SLASH,
				null);
	}

	@Test
	public void totalTests() throws JSONException {
		// requst t among some garbage
		JSONObject queues = wr.path(QUEUES_PATH_WITH_SLASH).queryParam("filter", "stu")
				.accept(MediaType.APPLICATION_JSON).get(JSONObject.class);
		int noQueues = queues.getInt("total");

		TestQueueResource.createTestQueue(wr, UNIT_TEST_Q_NAME, "desc");
		
		// requst t among some garbage
		queues = wr.path(QUEUES_PATH_WITH_SLASH).queryParam("filter", "stu")
				.accept(MediaType.APPLICATION_JSON).get(JSONObject.class);
		int noQueues2 = queues.getInt("total");

		Assert.assertEquals("total not +1", noQueues + 1, noQueues2);
		
		// request a small page, look for total to be the same ( unoptimized method )
		queues = wr.path(QUEUES_PATH_WITH_SLASH).queryParam("filter", "stu").queryParam("page_size", "1")
				.accept(MediaType.APPLICATION_JSON).get(JSONObject.class);
		int noQueues3 = queues.getInt("total");
		
		Assert.assertEquals("total wrong for page", noQueues2, noQueues3);
	}

	@Test
	public void pagingTests() throws JSONException {
		// TODO : after total is implemented, test the first, last page
		// create 101 queues
		for (int i = 0; i < 101; i++){
			TestQueueResource.createTestQueue(wr, UNIT_TEST_Q_NAME + i, "desc");
		}

		// test get default page (1), default page_size
		JSONObject queues = wr.path(QUEUES_PATH)
				.accept(MediaType.APPLICATION_JSON).get(JSONObject.class);

		// we should have at least a full page and at least 1 more item
		Assert.assertEquals("default paging items", 100,
				queues.getJSONArray("queues").length());
		Assert.assertEquals("default paging has_more", true,
				queues.getBoolean("has_more"));

		// test get default page (1), custom size : 42
		queues = wr.path(QUEUES_PATH).queryParam("page_size", "42")
				.accept(MediaType.APPLICATION_JSON).get(JSONObject.class);

		// we should have at least a full page and at least 1 more item
		Assert.assertEquals("custom page size items", 42,
				queues.getJSONArray("queues").length());
		Assert.assertEquals("custom page size has_more", true,
				queues.getBoolean("has_more"));

		// test get default page (1), custom invalid size : -1
		queues = wr.path(QUEUES_PATH).queryParam("page_size", "-1")
				.accept(MediaType.APPLICATION_JSON).get(JSONObject.class);

		// we should have at least a full page and at least 1 more item
		Assert.assertEquals("custom invalid page size -1 items", 100, queues
				.getJSONArray("queues").length());
		Assert.assertEquals("custom invalid page size -1 has_more", true,
				queues.getBoolean("has_more"));

		// test get default page (1), custom invalid size : 101
		queues = wr.path(QUEUES_PATH).queryParam("page_size", "101")
				.accept(MediaType.APPLICATION_JSON).get(JSONObject.class);

		// we should have at least a full page and at least 1 more item
		Assert.assertEquals("custom invalid page size 101 items", 100, queues
				.getJSONArray("queues").length());
		Assert.assertEquals("custom invalid page size 101 has_more", true,
				queues.getBoolean("has_more"));

		// test get default page (1), custom invalid size : aaa
		queues = wr.path(QUEUES_PATH).queryParam("page_size", "aaa")
				.accept(MediaType.APPLICATION_JSON).get(JSONObject.class);

		// we should have at least a full page and at least 1 more item
		Assert.assertEquals("custom invalid page size aaa items", 100, queues
				.getJSONArray("queues").length());
		Assert.assertEquals("custom invalid page size aaa paging has_more",
				true, queues.getBoolean("has_more"));

		// test get custom page (2), custom size : 2
		queues = wr.path(QUEUES_PATH).queryParam("page", "2")
				.queryParam("page_size", "2")
				.accept(MediaType.APPLICATION_JSON).get(JSONObject.class);

		// we should have at least a full page and at least 1 more item
		Assert.assertEquals("custom page 2 custom page size 2 items", 2, queues
				.getJSONArray("queues").length());
		Assert.assertEquals("custom page 2 custom page size 2 has_more", true,
				queues.getBoolean("has_more"));
		String q1name = queues.getJSONArray("queues").getJSONObject(1)
				.getString("name");

		// repeat with next page
		queues = wr.path(QUEUES_PATH).queryParam("page", "3")
				.queryParam("page_size", "2")
				.accept(MediaType.APPLICATION_JSON).get(JSONObject.class);

		// we should have at least a full page and at least 1 more item
		Assert.assertEquals("custom page 3 custom page size 2 items", 2, queues
				.getJSONArray("queues").length());
		Assert.assertEquals("custom page 3 custom page size 2 paging has_more",
				true, queues.getBoolean("has_more"));
		String q2name = queues.getJSONArray("queues").getJSONObject(1)
				.getString("name");

		// queue names should differ ( sloppy test to check another page is
		// returned )
		Assert.assertNotEquals("different queues on pages", q1name, q2name);
	}
}
