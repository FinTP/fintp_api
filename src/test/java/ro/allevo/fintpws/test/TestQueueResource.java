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
import ro.allevo.fintpws.resources.QueueResource;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;

/**
 * Tests for {@link QueueResource}.
 * 
 * @author horia
 */
@RunWith(JUnit4.class)
public class TestQueueResource {
	/**
	 * Field logger.
	 */
	// private static Logger logger =
	// LogManager.getLogger(TestQueueResource.class
	// .getName());

	static final String UNIT_TEST_Q_NAME = "unittest q name";
	static final String QUEUES_PATH = "api/queues";
	static final String QUEUES_PATH_WITH_SLASH = QUEUES_PATH + "/";

	static WebResource wr;

	@BeforeClass
	public static void startMethod() throws JSONException {
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
		int page = 1;
		boolean findRecords = false;
		JSONObject queues = null;
		do {
			findRecords = false;
			queues = wr.path(QUEUES_PATH)
					.queryParam("page", String.valueOf(page))
					.accept(MediaType.APPLICATION_JSON).get(JSONObject.class);
			JSONArray queuesArray = queues.getJSONArray("queues");
			for (int i = 0; i < queuesArray.length(); i++) {
				String qname = queuesArray.getJSONObject(i).getString("name");
				if (qname.startsWith("unit")) {
					TestQueueResource.deleteTestQueue(wr, qname);
					findRecords = true;
				}
			}
			if (!findRecords) {
				page++;
			}
		} while (queues.has("has_more"));
		TestQueueTypeResource.deleteTestQueueType(wr, "unit_test");
	}

	public static JSONObject createTestQueue(WebResource wr, String name,
			String desc) throws JSONException {

		JSONObject queue = new JSONObject().put("name", name).put(
				"description", desc);
		return TestHelper.assertResponseCreated(wr, "POST", QUEUES_PATH, queue);
	}

	public static JSONObject createTestQueue(WebResource wr, String name,
			String desc, String connector, String typeId) throws JSONException {
		if (null == TestQueueTypeResource.findTestQueueType(wr, "unit_test")) {
			TestQueueTypeResource.createTestQueueType(wr, "unit_test", "lvl");
		}
		JSONObject queueType = TestQueueTypeResource.readTestQueueType(wr,
				"unit_test");

		JSONObject queue = new JSONObject().put("name", name)
				.put("description", desc).put("connector", connector)
				.put("type", queueType.get("typeid"));

		return TestHelper.assertResponseCreated(wr, "POST", QUEUES_PATH, queue);
	}

	public static JSONObject findTestQueue(WebResource wr, String name)
			throws JSONException {
		ClientResponse clientResponse = wr.path(QUEUES_PATH).path(name)
				.accept(MediaType.APPLICATION_JSON)
				.type(MediaType.APPLICATION_JSON).get(ClientResponse.class);
		return (clientResponse.getClientResponseStatus()
				.equals(ClientResponse.Status.OK)) ? clientResponse
				.getEntity(JSONObject.class) : null;
	}

	public static JSONObject findRandomQueue(WebResource wr)
			throws JSONException {

		JSONObject queues = wr.path(QUEUES_PATH).queryParam("filter", "tb")
				.accept(MediaType.APPLICATION_JSON).get(JSONObject.class);

		JSONArray queuesArray = queues.getJSONArray("queues");
		if (queuesArray.length() == 0) {
			return null;
		}

		JSONObject queue = null;
		do {
			queue = queuesArray.getJSONObject(new Random().nextInt(queuesArray
					.length()));
		} while (!queue.getString("name").startsWith("unit"));

		return queue;

	}

	public static JSONObject readTestQueue(WebResource wr, String name)
			throws JSONException {
		return TestHelper.assertResponseOK(wr, "GET", QUEUES_PATH_WITH_SLASH
				+ name, null);
	}

	public static void deleteTestQueue(WebResource wr, String name) {
		wr.path(QUEUES_PATH_WITH_SLASH).path(name).delete(ClientResponse.class);
	}

	@Test
	public void createQueue() throws JSONException {
		TestHelper.assertResponseMethodNotAllowed(wr, "POST",
				QUEUES_PATH_WITH_SLASH + UNIT_TEST_Q_NAME, null);
	}

	@Test
	public void readQueue() throws JSONException {
		// create a q with a random desc
		String qdesc = "unit test q desc" + new Random().nextInt();
		createTestQueue(wr, UNIT_TEST_Q_NAME, qdesc);

		// now read it
		JSONObject rj = readTestQueue(wr, UNIT_TEST_Q_NAME);
		Assert.assertEquals("q description not correct", qdesc,
				rj.getString("description"));

		// make sure the metadata is returned
		Assert.assertEquals("href not correct", QUEUES_PATH_WITH_SLASH
				+ UNIT_TEST_Q_NAME, rj.getString("href"));
		Assert.assertEquals("_type not correct", QueueResource.class.getName(),
				rj.getString("_type"));

		// check 404 if the q with the stupid name is requested
		TestHelper.assertResponseNotFound(wr, "GET", QUEUES_PATH_WITH_SLASH
				+ +new Random().nextInt(), null);
	}

	@Test
	public void updateQueueOptionalFields() throws JSONException {
		// test update description
		// create a q with a random desc
		String qdesc = "unit test q desc" + new Random().nextInt();
		createTestQueue(wr, UNIT_TEST_Q_NAME, qdesc);

		// change description to a new random one
		String qdesc2 = "unit test q desc" + new Random().nextInt();
		JSONObject queue = readTestQueue(wr, UNIT_TEST_Q_NAME);
		queue.remove("description");
		queue.put("description", qdesc2);
		TestHelper.assertResponseOK(wr, "PUT", QUEUES_PATH_WITH_SLASH
				+ UNIT_TEST_Q_NAME, queue);

		// assert it worked
		JSONObject queue2 = readTestQueue(wr, UNIT_TEST_Q_NAME);
		Assert.assertEquals("q description not correct", qdesc2,
				queue2.getString("description"));

		// test singular field update
		String qdesc3 = "unit test q desc" + new Random().nextInt();
		JSONObject queuenewdesc = new JSONObject().put("description", qdesc3);
		TestHelper.assertResponseOK(wr, "PUT", QUEUES_PATH_WITH_SLASH
				+ UNIT_TEST_Q_NAME, queuenewdesc);

		// assert it worked
		JSONObject queue3 = readTestQueue(wr, UNIT_TEST_Q_NAME);
		Assert.assertEquals("q description not correct", qdesc3,
				queue3.getString("description"));
	}

	@Test
	public void updateQueueLongName() throws JSONException {
		// create a q with a random desc
		String qdesc = "unit test q desc" + new Random().nextInt();
		createTestQueue(wr, UNIT_TEST_Q_NAME, qdesc);

		// update name
		JSONObject queue = readTestQueue(wr, UNIT_TEST_Q_NAME);
		queue.remove("name");
		queue.put("name", "123456789012345678901234567890123456789012345678904");
		TestHelper.assertResponseBadRequest(wr, "PUT", QUEUES_PATH_WITH_SLASH
				+ UNIT_TEST_Q_NAME, queue);
	}

	@Test
	public void updateQueueAllFields() throws JSONException {
		// generate a queue(JSONObject format)
		JSONObject jsonEntity = TestUtils.fillResourceData(new JSONObject(),
				new QueueEntity());
		jsonEntity.remove("name");
		jsonEntity.remove("guid");

		// create a queue
		createTestQueue(wr, UNIT_TEST_Q_NAME, "qdesc");

		// complete 'type' attribute with a valid queue type
		if (null == TestQueueTypeResource.findTestQueueType(wr, "unit_test")) {
			TestQueueTypeResource.createTestQueueType(wr, "unit_test", "lvl");
		}
		if (null == TestServiceMapResource.findTestServiceMap(wr,
				"unittest sm name")) {
			TestServiceMapResource.createTestServiceMap(wr, "unittest sm name");
		}

		JSONObject queueType = TestQueueTypeResource.readTestQueueType(wr,
				"unit_test");
		JSONObject serviceMap = TestServiceMapResource.readTestServiceMap(wr,
				"unittest sm name");
		jsonEntity.put("type", queueType.get("typename"));
		jsonEntity.put("connector", serviceMap.get("friendlyname"));

		// update all fields
		TestHelper.assertResponseOK(wr, "PUT", QUEUES_PATH_WITH_SLASH
				+ UNIT_TEST_Q_NAME, jsonEntity);

		// now read it
		JSONObject queue = readTestQueue(wr, UNIT_TEST_Q_NAME);

		// TODO:temporarily disable connector comparison (it is string, not
		// bigDecimal seen likewise because of reflection

		// assert it worked
		Assert.assertEquals(
				"q fileds not correct",
				TestUtils
						.compareJSONObjects(new QueueEntity(), jsonEntity,
								queue,
								"guid,name,typeid,queuetypeentity,servicemapentity,connector"),
				true);
	}

	@Test
	public void testDefaults() throws JSONException {
		// create a q with a random desc
		String qdesc = "unit test q desc" + new Random().nextInt();
		createTestQueue(wr, UNIT_TEST_Q_NAME, qdesc);

		// now read it
		JSONObject queue = readTestQueue(wr, UNIT_TEST_Q_NAME);

		Assert.assertEquals("holdstatus default incorrect", 0,
				queue.getInt("holdstatus"));
		Assert.assertEquals("type default incorrect", "Ordinary",
				queue.getString("type"));
	}

	@Test
	public void updateQueueKeyFields() throws JSONException {
		// test update name
		// create a q with a random name
		String newname = UNIT_TEST_Q_NAME + new Random().nextInt();
		createTestQueue(wr, newname, "description");

		// update name
		JSONObject queue = readTestQueue(wr, newname);
		queue.remove("name");
		queue.put("name", UNIT_TEST_Q_NAME);
		TestHelper.assertResponseOK(wr, "PUT",
				QUEUES_PATH_WITH_SLASH + newname, queue);

		// assert it worked
		JSONObject queue2 = readTestQueue(wr, UNIT_TEST_Q_NAME);
		Assert.assertEquals("q name not correct", UNIT_TEST_Q_NAME,
				queue2.getString("name"));

		// test name conflict (409)
		// create a second queue
		String queue2name = UNIT_TEST_Q_NAME + new Random().nextInt();
		createTestQueue(wr, queue2name, "description");

		// update to name of the 1st q
		queue2 = readTestQueue(wr, queue2name);
		queue2.remove("name");
		queue2.put("name", UNIT_TEST_Q_NAME);
		TestHelper.assertResponseConflict(wr, "PUT", QUEUES_PATH_WITH_SLASH
				+ queue2name, queue2);
	}

	@Test
	public void deleteQueue() throws JSONException {
		// create a q with a random desc
		String qdesc = "unit test q desc" + new Random().nextInt();
		createTestQueue(wr, UNIT_TEST_Q_NAME, qdesc);

		JSONObject queue = readTestQueue(wr, UNIT_TEST_Q_NAME);

		// delete it
		TestHelper.assertResponseOK(wr, "DELETE", QUEUES_PATH_WITH_SLASH
				+ UNIT_TEST_Q_NAME, null);

		// read it again to make sure it's gone
		TestHelper.assertResponseNotFound(wr, "GET", QUEUES_PATH_WITH_SLASH
				+ UNIT_TEST_Q_NAME, null);

		// check 404 if the q with the stupid name is requested
		TestHelper.assertResponseNotFound(wr, "DELETE", QUEUES_PATH_WITH_SLASH
				+ new Random().nextInt(), null);
	}
}
