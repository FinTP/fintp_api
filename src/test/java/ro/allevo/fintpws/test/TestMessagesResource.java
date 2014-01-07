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
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import ro.allevo.fintpws.model.EntryQueueEntity;
import ro.allevo.fintpws.resources.MessagesResource;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;

/**
 * Tests for {@link MessagesResource}.
 * 
 * @author costi
 * @version $Revision: 1.0 $
 */
@RunWith(JUnit4.class)
public class TestMessagesResource {
	/**
	 * Field logger.
	 */
	// private static Logger logger = LogManager
	// .getLogger(TestMessagesResource.class.getName());

	/**
	 * Field UNIT_TEST_M_ID. (value is ""unittest e id"")
	 */
	static final String UNIT_TEST_M_ID = TestMessageResource.UNIT_TEST_M_ID;
	/**
	 * Field MESSAGES_PATH. (value is ""api/messages"")
	 */
	static final String MESSAGES_PATH_QUEUE = TestMessageResource.MESSAGES_PATH_QUEUE;
	/**
	 * Field MESSAGES_PATH_QUEUE_WITH_SLASH. (value is ""api/messages/"")
	 */
	static final String MESSAGES_PATH_QUEUE_WITH_SLASH = TestMessageResource.MESSAGES_PATH_QUEUE_WITH_SLASH;

	/**
	 * Field MESSAGES_PATH. (value is "TestMessageResource.MESSAGES_PATH")
	 */
	static final String MESSAGES_PATH = TestMessageResource.MESSAGES_PATH;
	/**
	 * Field MESSAGES_PATH_WITH_SLASH. (value is "MESSAGES_PATH + "/"")
	 */
	static final String MESSAGES_PATH_WITH_SLASH = MESSAGES_PATH + "/";

	static final String IMPOSSIBLE_QUEUE_NAME = "unit_impossible001_name";

	
	/**
	 * Field wr.
	 */
	static WebResource wr;

	/**
	 * Method startMethod.
	 * 
	 * @throws JSONException
	 */
	@BeforeClass
	public static void startMethod() throws JSONException {
		// create a client:
		final ClientConfig cc = new DefaultClientConfig();
		final Client c = Client.create(cc);
		c.addFilter(new HTTPBasicAuthFilter("admin", "admin"));
		wr = c.resource(TestUtils.getUrlBase());
		TestAuthorizationUtils.giveAdministratorReportsRole(wr);
	}
	
	@AfterClass
	public static void stopMethod() throws JSONException{
		TestAuthorizationUtils.removeAdministratorReportsRole(wr);
	}

	/**
	 * Method cleanup.
	 * 
	 * @throws JSONException
	 */
	@Before
	public void cleanupBefore() throws JSONException {
		cleanup();
		// create 10 queues
		for (int i = 0; i < 10; i++) {
			TestQueueResource.createTestQueue(wr,
					TestQueueResource.UNIT_TEST_Q_NAME + i, "desc");
			TestAuthorizationUtils.allowAdminWriteAuthorityOnQueue(wr,
					TestQueueResource.UNIT_TEST_Q_NAME + i);
		}
	}

	/**
	 * Method cleanupAfter.
	 * 
	 * @throws JSONException
	 */
	@After
	public void cleanupAfter() throws JSONException {
		cleanup();
	}

	/**
	 * Method finalMethod.
	 * 
	 * @throws JSONException
	 */
	public static void cleanup() throws JSONException {
		// delete all queues and all messages that start with unit*
		JSONObject queues;
		JSONObject messages;
		boolean findRecords = false;
		int page = 1;
		do {
			findRecords = false;
			queues = wr.path(TestQueueResource.QUEUES_PATH)
					.queryParam("page", String.valueOf(page))
					.accept(MediaType.APPLICATION_JSON).get(JSONObject.class);
			JSONArray queuesArray = queues.getJSONArray("queues");
			for (int i = 0; i < queuesArray.length(); i++) {
				String qname = queuesArray.getJSONObject(i).getString("name");
				if (qname.startsWith("unit")) {
					cleanupMessages(qname);
					TestQueueResource.deleteTestQueue(wr, qname);
					findRecords = true;
				}
			}
			if (!findRecords) {
				page++;
			}
		} while (queues.has("has_more"));
		page = 1;
		do {
			findRecords = false;
			messages = wr.path(TestMessageResource.MESSAGES_PATH)
					.queryParam("page", String.valueOf(page))
					.accept(MediaType.APPLICATION_JSON).get(JSONObject.class);
			JSONArray messagesArray = messages.getJSONArray("messages");
			for (int i = 0; i < messagesArray.length(); i++) {
				String messageId = messagesArray.getJSONObject(i).getString(
						"guid");
				if (messageId.startsWith("unit")) {
					TestMessageResource.deleteTestMessage(wr, messageId);
					findRecords = true;
				}
			}
			if (!findRecords) {
				page++;
			}
		} while (queues.has("has_more"));

	}

	/**
	 * Method cleanupMessages.
	 * 
	 * @param queueName
	 *            String
	 * @throws JSONException
	 */
	public static void cleanupMessages(String queueName) throws JSONException {
		// delete all messages that start with unit*
		JSONObject messages;
		int page = 1;
		boolean findRecords = false;
		TestAuthorizationUtils.allowAdminWriteAuthorityOnQueue(wr, queueName);
		do {
			messages = wr.path(TestQueueResource.QUEUES_PATH).path(queueName)
					.path("messages").queryParam("page", String.valueOf(page))
					.accept(MediaType.APPLICATION_JSON).get(JSONObject.class);
			JSONArray messagesArray = messages.getJSONArray("messages");
			for (int i = 0; i < messagesArray.length(); i++) {
				String messageId = messagesArray.getJSONObject(i).getString(
						"guid");
				if (messageId.startsWith("unit")) {
					TestMessageResource.deleteTestMessageFromQueue(wr,
							messageId, queueName);
					findRecords = true;
				}
			}
			if (!findRecords) {
				page++;
			}
		} while (messages.has("has_more"));
	}

	/**
	 * Method createMessages.
	 * 
	 * @throws JSONException
	 */
	@Test
	public void createMessages() throws JSONException {
		// delete the message if it already exists
		TestMessageResource.deleteTestMessage(wr, UNIT_TEST_M_ID);
		// make sure it is deleted
		TestHelper.assertResponseNotFound(wr, "GET",
				MESSAGES_PATH_QUEUE_WITH_SLASH + UNIT_TEST_M_ID, null);
		// create it with a random priority
		final int messagePriority = new Random().nextInt();
		JSONObject rj = TestMessageResource.createTestMessageQueue(wr,
				UNIT_TEST_M_ID, messagePriority,
				TestQueueResource.UNIT_TEST_Q_NAME);
		Assert.assertTrue("uri missing", rj.getString("uri").length() > 0);
		// make sure it is created
		rj = wr.path(MESSAGES_PATH_QUEUE_WITH_SLASH).path(UNIT_TEST_M_ID)
				.queryParam("filter", "b").accept(MediaType.APPLICATION_JSON)
				.get(JSONObject.class);
	}

	/**
	 * Method createMessage.
	 * 
	 * @throws JSONException
	 * */
	@Test
	public void createMessage() throws JSONException {
		// generate a message(JSONObject format)
		JSONObject jsonEntity = new JSONObject();
		jsonEntity = TestUtils.fillResourceData(jsonEntity,
				new EntryQueueEntity());
		jsonEntity.put("guid",
				"unit" + jsonEntity.getString("guid").substring(4));
		jsonEntity.put("queuename", TestQueueResource.UNIT_TEST_Q_NAME);

		JSONObject queue = TestQueueResource.findTestQueue(wr,
				TestQueueResource.UNIT_TEST_Q_NAME);
		if (null == queue) {
			TestQueueResource.createTestQueue(wr,
					TestQueueResource.UNIT_TEST_Q_NAME, "desc");
			queue = TestQueueResource.readTestQueue(wr,
					TestQueueResource.UNIT_TEST_Q_NAME);
		}
		
		TestAuthorizationUtils.allowAdminWriteAuthorityOnQueue(wr,
				TestQueueResource.UNIT_TEST_Q_NAME);
		// create a message
		TestHelper.assertResponseCreated(wr, "POST", MESSAGES_PATH_QUEUE,
				jsonEntity);
		// read message
		JSONObject message = TestMessageResource.readTestMessageFromQueue(wr,
				jsonEntity.getString("guid"),
				TestQueueResource.UNIT_TEST_Q_NAME);

		Assert.assertEquals("m fileds not correct", TestUtils
				.compareJSONObjects(new EntryQueueEntity(), jsonEntity,
						message, "correlationid,queuename,insertdate,payload"),
				true);
		TestHelper.assertResponseMethodNotAllowed(wr, "POST",
				MESSAGES_PATH_QUEUE_WITH_SLASH + UNIT_TEST_M_ID, null);
	}

	/**
	 * Method readMessages.
	 * 
	 * @throws JSONException
	 */
	@Test
	public void readMessagesFromQueue() throws JSONException {
		// make sure it makes sense ( >0 message is available )
		TestMessageResource.createTestMessageQueue(wr, UNIT_TEST_M_ID, 6,
				TestQueueResource.UNIT_TEST_Q_NAME);

		final JSONObject messages = wr
				.path(TestMessageResource.MESSAGES_PATH_QUEUE)
				.accept(MediaType.APPLICATION_JSON).get(JSONObject.class);

		// make sure the metadata is returned
		Assert.assertEquals("href not correct", MESSAGES_PATH_QUEUE,
				messages.getString("href"));
		Assert.assertEquals("_type not correct",
				MessagesResource.class.getName(), messages.getString("_type"));

		final JSONArray messagesList = messages.getJSONArray("messages");
		Assert.assertTrue("message list empty", messagesList.length() > 0);

		// get first message, see if has a guid
		Assert.assertTrue("message list empty", messagesList.getJSONObject(0)
				.getString("guid").length() > 0);
	}

	/**
	 * Method readMessages.
	 * 
	 * @throws JSONException
	 */
	@Test
	public void readMessages() throws JSONException {
		// make sure it makes sense ( >0 message is available )
		TestMessageResource.createMessage(wr, UNIT_TEST_M_ID,
				TestQueueResource.UNIT_TEST_Q_NAME);

		final JSONObject messages = wr.path(TestMessageResource.MESSAGES_PATH)
				.accept(MediaType.APPLICATION_JSON).get(JSONObject.class);

		final JSONArray messagesList = messages.getJSONArray("messages");
		Assert.assertTrue("message list empty", messagesList.length() > 0);

		// get first message, see if has a guid
		Assert.assertTrue("message list empty", messagesList.getJSONObject(0)
				.getString("guid").length() > 0);
	}

	/**
	 * Method deleteMessages.
	 * 
	 * @throws JSONException
	 */
	@Test
	public void deleteMessages() throws JSONException {
		TestHelper.assertResponseMethodNotAllowed(wr, "DELETE",
				MESSAGES_PATH_WITH_SLASH, null);
	}

	/**
	 * Method deleteMessagesFromQueue.
	 * 
	 * @throws JSONException
	 */
	@Test
	public void deleteMessagesFromQueue() throws JSONException {
		// make sure it makes sense ( queue is available )
		TestQueueResource.createTestQueue(wr,
				TestQueueResource.UNIT_TEST_Q_NAME, "");
		TestHelper.assertResponseMethodNotAllowed(wr, "DELETE",
				MESSAGES_PATH_QUEUE_WITH_SLASH, null);
	}

	/**
	 * Method getMessagesWithoutParent
	 * 
	 * @throws JSONException
	 */
	@Test
	public void getMessagesWithoutParent() throws JSONException {
		TestHelper.assertResponseNotFound(wr, "GET",
				TestQueueResource.QUEUES_PATH_WITH_SLASH
						+ IMPOSSIBLE_QUEUE_NAME + "/messages", null);
	}
	
	
	/**
	 * Method totalTests.
	 * 
	 * @throws JSONException
	 */
	@Test
	public void totalTests() throws JSONException {
		JSONObject queue = TestQueueResource.findTestQueue(wr,
				TestQueueResource.UNIT_TEST_Q_NAME);
		if (null == queue) {
			TestQueueResource.createTestQueue(wr,
					TestQueueResource.UNIT_TEST_Q_NAME, "desc");
			queue = TestQueueResource.readTestQueue(wr,
					TestQueueResource.UNIT_TEST_Q_NAME);
		}
		// insert random number of messages in queues
		for (int i = 0; i < 10; i++) {
			for (int j = 0; j < (new Random().nextInt(i + 1) + 1); j++) {
				TestMessageResource.createMessage(wr, UNIT_TEST_M_ID + i + "."
						+ j, TestQueueResource.UNIT_TEST_Q_NAME + i);
			}
		}
		queue = TestQueueResource.findRandomQueue(wr);

		JSONObject messages = wr.path(MESSAGES_PATH)
				.queryParam("filter", "stu").accept(MediaType.APPLICATION_JSON)
				.get(JSONObject.class);
		// number of messages in a queue random
		final int noMessages1 = messages.getInt("total");

		// create message in queue
		TestMessageResource.createMessage(wr, UNIT_TEST_M_ID,
				queue.getString("name"));

		messages = wr.path(MESSAGES_PATH).queryParam("filter", "stu")
				.accept(MediaType.APPLICATION_JSON).get(JSONObject.class);
		final int noMessages2 = messages.getInt("total");

		Assert.assertEquals("total not +1", noMessages1 + 1, noMessages2);

		JSONObject queue1 = null;
		do {
			queue1 = TestQueueResource.findRandomQueue(wr);
		} while (queue1.getString("name").equals(queue.getString("name")));

		// create a message to another queue
		TestMessageResource.createMessage(wr, UNIT_TEST_M_ID + "new",
				queue1.getString("name"));

		messages = wr.path(MESSAGES_PATH).queryParam("filter", "stu")
				.accept(MediaType.APPLICATION_JSON).get(JSONObject.class);
		final int noMessages3 = messages.getInt("total");

		Assert.assertEquals("total not +1", noMessages2 + 1, noMessages3);

		// request a small page, look for total to be the same ( unoptimized
		// method )
		messages = wr.path(MESSAGES_PATH).queryParam("filter", "stu")
				.queryParam("page_size", "1")
				.accept(MediaType.APPLICATION_JSON).get(JSONObject.class);
		int noMessages4 = messages.getInt("total");

		Assert.assertEquals("total wrong for page", noMessages2 + 1,
				noMessages4);

	}

	/**
	 * Method totalTestsQueue.
	 * 
	 * @throws JSONException
	 */
	@Test
	public void totalTestsQueue() throws JSONException {
		JSONObject queue = TestQueueResource.findTestQueue(wr,
				TestQueueResource.UNIT_TEST_Q_NAME);
		if (null == queue) {
			TestQueueResource.createTestQueue(wr,
					TestQueueResource.UNIT_TEST_Q_NAME, "desc");
			queue = TestQueueResource.readTestQueue(wr,
					TestQueueResource.UNIT_TEST_Q_NAME);
		}
		// insert random number of messages in queues
		for (int i = 0; i < 10; i++) {
			for (int j = 0; j < (new Random().nextInt(i + 1) + 1); j++) {
				TestMessageResource.createTestMessageQueue(wr, UNIT_TEST_M_ID
						+ i + "." + j, 7, TestQueueResource.UNIT_TEST_Q_NAME
						+ i);
			}
		}
		queue = TestQueueResource.findRandomQueue(wr);

		JSONObject messages = wr.path(TestQueueResource.QUEUES_PATH)
				.path(queue.getString("name")).path("messages")
				.queryParam("filter", "stu").accept(MediaType.APPLICATION_JSON)
				.get(JSONObject.class);
		// number of messages in a queue random
		final int noMessages1 = messages.getInt("total");

		// create message in queue
		TestMessageResource.createTestMessageQueue(wr, UNIT_TEST_M_ID, 7,
				queue.getString("name"));

		messages = wr.path(TestQueueResource.QUEUES_PATH)
				.path(queue.getString("name")).path("messages")
				.queryParam("filter", "stu").accept(MediaType.APPLICATION_JSON)
				.get(JSONObject.class);
		final int noMessages2 = messages.getInt("total");

		Assert.assertEquals("total not +1", noMessages1 + 1, noMessages2);

		JSONObject queue1 = null;
		do {
			queue1 = TestQueueResource.findRandomQueue(wr);
		} while (queue1.getString("name").equals(queue.getString("name")));

		// create a message to another queue
		TestMessageResource.createTestMessageQueue(wr, UNIT_TEST_M_ID + "new",
				7, queue1.getString("name"));

		messages = wr.path(TestQueueResource.QUEUES_PATH)
				.path(queue.getString("name")).path("messages")
				.queryParam("filter", "stu").accept(MediaType.APPLICATION_JSON)
				.get(JSONObject.class);
		final int noMessages3 = messages.getInt("total");

		Assert.assertEquals("total wrong for page", noMessages2, noMessages3);
	}

	/**
	 * Method pagingTests.
	 * 
	 * @throws JSONException
	 */
	@Test
	public void pagingTests() throws JSONException {
		// create 101 messages
		for (int i = 0; i < 101; i++) {
			TestMessageResource.createMessage(wr, UNIT_TEST_M_ID + i,
					TestQueueResource.UNIT_TEST_Q_NAME);
		}

		// test get default page (1), default page_size
		JSONObject messages = wr.path(MESSAGES_PATH)
				.accept(MediaType.APPLICATION_JSON).get(JSONObject.class);

		// we should have at least a full page and at least 1 more item
		Assert.assertEquals("default paging items", 100,
				messages.getJSONArray("messages").length());
		Assert.assertEquals("default paging has_more", true,
				messages.getBoolean("has_more"));

		// test get last page
		messages = wr.path(MESSAGES_PATH).queryParam("filter", "t")
				.accept(MediaType.APPLICATION_JSON).get(JSONObject.class);
		final int noMessages = messages.getInt("total");
		int pageNo = noMessages / 100;
		if (((noMessages % 100) != 0) || (noMessages == 0)) {
			pageNo++;
		}
		messages = wr.path(MESSAGES_PATH)
				.queryParam("page", String.valueOf(pageNo))
				.accept(MediaType.APPLICATION_JSON).get(JSONObject.class);
		Assert.assertEquals("custom last page", noMessages % 100, messages
				.getJSONArray("messages").length());

		// test get default page (1), custom size : 42
		messages = wr.path(MESSAGES_PATH).queryParam("page_size", "42")
				.accept(MediaType.APPLICATION_JSON).get(JSONObject.class);

		// we should have at least a full page and at least 1 more item
		Assert.assertEquals("custom page size items", 42, messages
				.getJSONArray("messages").length());
		Assert.assertEquals("custom page size has_more", true,
				messages.getBoolean("has_more"));

		// test get default page (1), custom invalid size : -1
		messages = wr.path(MESSAGES_PATH).queryParam("page_size", "-1")
				.accept(MediaType.APPLICATION_JSON).get(JSONObject.class);

		// we should have at least a full page and at least 1 more item
		Assert.assertEquals("custom invalid page size -1 items", 100, messages
				.getJSONArray("messages").length());
		Assert.assertEquals("custom invalid page size -1 has_more", true,
				messages.getBoolean("has_more"));

		// test get default page (1), custom invalid size : 101
		messages = wr.path(MESSAGES_PATH).queryParam("page_size", "101")
				.accept(MediaType.APPLICATION_JSON).get(JSONObject.class);

		// we should have at least a full page and at least 1 more item
		Assert.assertEquals("custom invalid page size 101 items", 100, messages
				.getJSONArray("messages").length());
		Assert.assertEquals("custom invalid page size 101 has_more", true,
				messages.getBoolean("has_more"));

		// test get default page (1), custom invalid size : aaa
		messages = wr.path(MESSAGES_PATH).queryParam("page_size", "aaa")
				.accept(MediaType.APPLICATION_JSON).get(JSONObject.class);

		// we should have at least a full page and at least 1 more item
		Assert.assertEquals("custom invalid page size aaa items", 100, messages
				.getJSONArray("messages").length());
		Assert.assertEquals("custom invalid page size aaa paging has_more",
				true, messages.getBoolean("has_more"));

		// test get custom page (2), custom size : 2
		messages = wr.path(MESSAGES_PATH).queryParam("page", "2")
				.queryParam("page_size", "2")
				.accept(MediaType.APPLICATION_JSON).get(JSONObject.class);

		// we should have at least a full page and at least 1 more item
		Assert.assertEquals("custom page 2 custom page size 2 items", 2,
				messages.getJSONArray("messages").length());
		Assert.assertEquals("custom page 2 custom page size 2 has_more", true,
				messages.getBoolean("has_more"));
		final String message1ID = messages.getJSONArray("messages")
				.getJSONObject(1).getString("guid");

		// repeat with next page
		messages = wr.path(MESSAGES_PATH).queryParam("page", "3")
				.queryParam("page_size", "2")
				.accept(MediaType.APPLICATION_JSON).get(JSONObject.class);

		// we should have at least a full page and at least 1 more item
		Assert.assertEquals("custom page 3 custom page size 2 items", 2,
				messages.getJSONArray("messages").length());
		Assert.assertEquals("custom page 3 custom page size 2 paging has_more",
				true, messages.getBoolean("has_more"));
		final String message2Id = messages.getJSONArray("messages")
				.getJSONObject(1).getString("guid");

		// message ids should differ ( sloppy test to check another page is
		// returned )
		Assert.assertNotEquals("different messages on pages", message1ID,
				message2Id);
	}

	/**
	 * Method pagingTestsQueue.
	 * 
	 * @throws JSONException
	 */
	@Test
	public void pagingTestsQueue() throws JSONException {
		// create 101 messages
		for (int i = 0; i < 101; i++) {
			TestMessageResource.createTestMessageQueue(wr, UNIT_TEST_M_ID + i,
					6, TestQueueResource.UNIT_TEST_Q_NAME);
		}

		// test get default page (1), default page_size
		JSONObject messages = wr.path(MESSAGES_PATH_QUEUE)
				.accept(MediaType.APPLICATION_JSON).get(JSONObject.class);

		// we should have at least a full page and at least 1 more item
		Assert.assertEquals("default paging items", 100,
				messages.getJSONArray("messages").length());
		Assert.assertEquals("default paging has_more", true,
				messages.getBoolean("has_more"));

		// test get last page
		messages = wr.path(MESSAGES_PATH_QUEUE).queryParam("filter", "t")
				.accept(MediaType.APPLICATION_JSON).get(JSONObject.class);
		final int noMessages = messages.getInt("total");
		int pageNo = noMessages / 100;
		if (((noMessages % 100) != 0) || (noMessages == 0)) {
			pageNo++;
		}
		messages = wr.path(MESSAGES_PATH_QUEUE)
				.queryParam("page", String.valueOf(pageNo))
				.accept(MediaType.APPLICATION_JSON).get(JSONObject.class);
		Assert.assertEquals("custom last page", noMessages % 100, messages
				.getJSONArray("messages").length());

		// test get default page (1), custom size : 42
		messages = wr.path(MESSAGES_PATH_QUEUE).queryParam("page_size", "42")
				.accept(MediaType.APPLICATION_JSON).get(JSONObject.class);

		// we should have at least a full page and at least 1 more item
		Assert.assertEquals("custom page size items", 42, messages
				.getJSONArray("messages").length());
		Assert.assertEquals("custom page size has_more", true,
				messages.getBoolean("has_more"));

		// test get default page (1), custom invalid size : -1
		messages = wr.path(MESSAGES_PATH_QUEUE).queryParam("page_size", "-1")
				.accept(MediaType.APPLICATION_JSON).get(JSONObject.class);

		// we should have at least a full page and at least 1 more item
		Assert.assertEquals("custom invalid page size -1 items", 100, messages
				.getJSONArray("messages").length());
		Assert.assertEquals("custom invalid page size -1 has_more", true,
				messages.getBoolean("has_more"));

		// test get default page (1), custom invalid size : 101
		messages = wr.path(MESSAGES_PATH_QUEUE).queryParam("page_size", "101")
				.accept(MediaType.APPLICATION_JSON).get(JSONObject.class);

		// we should have at least a full page and at least 1 more item
		Assert.assertEquals("custom invalid page size 101 items", 100, messages
				.getJSONArray("messages").length());
		Assert.assertEquals("custom invalid page size 101 has_more", true,
				messages.getBoolean("has_more"));

		// test get default page (1), custom invalid size : aaa
		messages = wr.path(MESSAGES_PATH_QUEUE).queryParam("page_size", "aaa")
				.accept(MediaType.APPLICATION_JSON).get(JSONObject.class);

		// we should have at least a full page and at least 1 more item
		Assert.assertEquals("custom invalid page size aaa items", 100, messages
				.getJSONArray("messages").length());
		Assert.assertEquals("custom invalid page size aaa paging has_more",
				true, messages.getBoolean("has_more"));

		// test get custom page (2), custom size : 2
		messages = wr.path(MESSAGES_PATH_QUEUE).queryParam("page", "2")
				.queryParam("page_size", "2")
				.accept(MediaType.APPLICATION_JSON).get(JSONObject.class);

		// we should have at least a full page and at least 1 more item
		Assert.assertEquals("custom page 2 custom page size 2 items", 2,
				messages.getJSONArray("messages").length());
		Assert.assertEquals("custom page 2 custom page size 2 has_more", true,
				messages.getBoolean("has_more"));
		final String message1ID = messages.getJSONArray("messages")
				.getJSONObject(1).getString("guid");

		// repeat with next page
		messages = wr.path(MESSAGES_PATH_QUEUE).queryParam("page", "3")
				.queryParam("page_size", "2")
				.accept(MediaType.APPLICATION_JSON).get(JSONObject.class);

		// we should have at least a full page and at least 1 more item
		Assert.assertEquals("custom page 3 custom page size 2 items", 2,
				messages.getJSONArray("messages").length());
		Assert.assertEquals("custom page 3 custom page size 2 paging has_more",
				true, messages.getBoolean("has_more"));
		final String message2Id = messages.getJSONArray("messages")
				.getJSONObject(1).getString("guid");

		// message ids should differ ( sloppy test to check another page is
		// returned )
		Assert.assertNotEquals("different messages on pages", message1ID,
				message2Id);
	}

}
