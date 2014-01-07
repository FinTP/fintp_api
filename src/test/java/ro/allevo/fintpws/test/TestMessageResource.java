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

import java.math.BigDecimal;
import java.util.Random;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
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
import ro.allevo.fintpws.model.RoutedMessageEntity;
import ro.allevo.fintpws.resources.MessageResource;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;

/**
 * Tests for {@link MessageResource}.
 * 
 * @author costi
 * @version $Revision: 1.0 $
 */
@RunWith(JUnit4.class)
public class TestMessageResource {
	/**
	 * Field logger.
	 */
	// private static Logger logger = LogManager
	// .getLogger(TestMessageResource.class.getName());

	/**
	 * Field UNIT_TEST_M_ID. (value is ""unittest e guid"")
	 */
	static final String UNIT_TEST_M_ID = "unittest e guid";
	/**
	 * Field MESSAGES_PATH. (value is ""api/messages"")
	 */
	static final String MESSAGES_PATH = "api/messages";

	/**
	 * Field MESSAGES_PATH_WITH_SLASH. (value is "MESSAGES_PATH + "/"")
	 */
	static final String MESSAGES_PATH_WITH_SLASH = MESSAGES_PATH + "/";

	/**
	 * Field MESSAGES_PATH_QUEUE. (value is ""api/queues/" +
	 * TestQueueResource.UNIT_TEST_Q_NAME+ "/messages"")
	 */
	static final String MESSAGES_PATH_QUEUE = "api/queues/"
			+ TestQueueResource.UNIT_TEST_Q_NAME + "/messages";

	/**
	 * Field MESSAGES_PATH_QUEUE_WITH_SLASH. (value is
	 * "MESSAGES_PATH_QUEUE + "/"")
	 */
	static final String MESSAGES_PATH_QUEUE_WITH_SLASH = MESSAGES_PATH_QUEUE
			+ "/";

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
		boolean findRecords = false;
		JSONObject messages;
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
			if (!findRecords){
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
					deleteTestMessage(wr, messageId);
					findRecords = true;
				}
			}
			if (!findRecords){
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
	 * Method createTestMessage.
	 * 
	 * @param wr
	 *            WebResource
	 * 
	 * @param priority
	 *            String
	 * @param queueName
	 *            String
	 * 
	 * @param messageId
	 *            String
	 * @return JSONObject
	 * @throws JSONException
	 */
	public static JSONObject createTestMessageQueue(WebResource wr,
			String messageId, int priority, String queueName)
			throws JSONException {
		String correlationid = java.util.UUID.randomUUID().toString()
				.substring(1, 31);

		final JSONObject message = new JSONObject().put("guid", messageId)
				.put("priority", priority).put("correlationid", correlationid)
				.put("currencyamount", "938.").put("currencydate", "130125")
				.put("currencytype", "RON").put("currentmessage", 1)
				.put("payload", "xml message payload")
				.put("iban", "RO12BNKA6612417018171XXX")
				.put("queuename", queueName)
				.put("ibanpl", "RO98BNKB5419017010101XXX")
				.put("insertdate", "2013-03-20 11:07:36")
				.put("messagetype", "103").put("msginfo", "ROL")
				.put("mur", "701").put("receiver", "BNKBROB0XXXX")
				.put("receivercorresp", "BNKBROB0XXXX")
				.put("sender", "BNKAROB0AXXX").put("senderapp", "BOACHOut")
				.put("sendercorresp", "BNKAROB0AXXX").put("trn", "998221")
				.put("batchid", "BATCH01").put("correlationid", correlationid)
				.put("holdstatus", 4).put("qid", "4")
				.put("requestorservice", "4").put("requesttype", "4")
				.put("responderservice", "4").put("sequence", 4)
				.put("requestor", "BOACHIn");
		if (null != queueName) {
			JSONObject queue = TestQueueResource.findTestQueue(wr, queueName);
			if (null == queue) {
				TestQueueResource.createTestQueue(wr, queueName, "desc");
				queue = TestQueueResource.readTestQueue(wr, queueName);
			}
			message.put("currentqueue", queue.getString("name"));
		}
		TestAuthorizationUtils.allowAdminWriteAuthorityOnQueue(wr, queueName);
		return TestHelper.assertResponseCreated(wr, "POST",
				TestQueueResource.QUEUES_PATH_WITH_SLASH + queueName + "/messages", message);
	}

	/**
	 * Method createMessage.
	 * 
	 * @param wr
	 *            WebResource
	 * @param id
	 *            String
	 * @param queueName
	 *            String
	 * @throws JSONException
	 */
	public static void createMessage(WebResource wr, String id, String queueName)
			throws JSONException {
		createTestMessageQueue(wr, id, 4, queueName);
		JSONObject message = readTestMessageFromQueue(wr, id, queueName);
		message.remove("queuename");
		wr.path(TestQueueResource.QUEUES_PATH).path(queueName).path("messages")
				.path(id).accept(MediaType.APPLICATION_JSON).put(message);

		EntityManagerFactory dataEntityManagerFactory = Persistence
				.createEntityManagerFactory("fintpDATA");
		EntityManager emd = dataEntityManagerFactory.createEntityManager();

		try {
			RoutedMessageEntity routedMessageEntity = new RoutedMessageEntity();
			routedMessageEntity.setGuid(id);
			routedMessageEntity.setCorrelationid(message
					.getString("correlationid"));
			routedMessageEntity.setTrn("Transaction");
			routedMessageEntity.setAmount(new BigDecimal(1));

			emd.getTransaction().begin();
			emd.persist(routedMessageEntity);
			emd.getTransaction().commit();

		} finally {
			if (null != emd) {
				emd.close();
			}
		}
	}

	public static JSONObject readTestMessageFromQueue(WebResource wr,
			String id, String queueName) throws JSONException {
		return TestHelper.assertResponseOK(wr, "GET",
				TestQueueResource.QUEUES_PATH_WITH_SLASH + queueName
						+ "/messages/" + id, null);
	}

	/**
	 * Method deleteTestMessage.
	 * 
	 * @param wr
	 *            WebResource
	 * @param name
	 *            String
	 */
	public static void deleteTestMessage(WebResource wr, String name) {
		wr.path(MESSAGES_PATH_QUEUE).path(name).delete(ClientResponse.class);
	}

	/**
	 * Method deleteTestMessageFromQueue.
	 * 
	 * @param wr
	 *            WebResource
	 * @param name
	 *            String
	 * @param queueName
	 *            String
	 */
	public static void deleteTestMessageFromQueue(WebResource wr, String name,
			String queueName) {
		wr.path(TestQueueResource.QUEUES_PATH).path(queueName).path("messages")
				.path(name).delete(ClientResponse.class);
	}

	/**
	 * Method createMessage.
	 * 
	 * @throws JSONException
	 * */
	@Test
	public void createMessage() throws JSONException {
		TestHelper.assertResponseMethodNotAllowed(wr, "POST",
				MESSAGES_PATH_WITH_SLASH + UNIT_TEST_M_ID, null);
	}

	/**
	 * Method createMessageQueue.
	 * 
	 * @throws JSONException
	 */
	@Test
	public void createMessageQueue() throws JSONException {
		
		//make sure you have the message where you want to call post from
		TestMessageResource.createTestMessageQueue(wr,
				UNIT_TEST_M_ID, new Random().nextInt(),
				TestQueueResource.UNIT_TEST_Q_NAME);
		TestHelper.assertResponseMethodNotAllowed(wr, "POST",
				MESSAGES_PATH_QUEUE_WITH_SLASH + UNIT_TEST_M_ID, null);
	}

	/**
	 * Method updateMessageOptionalFields.
	 * 
	 * @throws JSONException
	 */
	@Test
	public void updateMessageOptionalFields() throws JSONException {
		JSONObject queue = TestQueueResource.findTestQueue(wr,
				TestQueueResource.UNIT_TEST_Q_NAME);
		if (null == queue) {
			TestQueueResource.createTestQueue(wr,
					TestQueueResource.UNIT_TEST_Q_NAME, "desc");
			queue = TestQueueResource.readTestQueue(wr,
					TestQueueResource.UNIT_TEST_Q_NAME);
		}
		// test update priority
		// create a m with a random desc
		final int priority = new Random().nextInt();
		createTestMessageQueue(wr, UNIT_TEST_M_ID, priority,
				TestQueueResource.UNIT_TEST_Q_NAME);
		// change priority to a new random one
		final int priority2 = new Random().nextInt();
		final JSONObject message = readTestMessageFromQueue(wr, UNIT_TEST_M_ID,
				TestQueueResource.UNIT_TEST_Q_NAME);
		message.remove("priority");
		message.put("priority", priority2);
		message.put("payload", "xml message");
		TestHelper.assertResponseOK(wr, "PUT", MESSAGES_PATH_QUEUE_WITH_SLASH
				+ UNIT_TEST_M_ID, message);

		// assert it worked
		final JSONObject message2 = readTestMessageFromQueue(wr,
				UNIT_TEST_M_ID, TestQueueResource.UNIT_TEST_Q_NAME);
		Assert.assertEquals("message priority not correct", priority2,
				message2.getInt("priority"));
	}

	/**
	 * Method updateMessageKeyFields.
	 * 
	 * @throws JSONException
	 */
	@Test
	public void updateMessageKeyFields() throws JSONException {
		// test update name
		JSONObject queue = TestQueueResource.findTestQueue(wr,
				TestQueueResource.UNIT_TEST_Q_NAME);
		if (null == queue) {
			TestQueueResource.createTestQueue(wr,
					TestQueueResource.UNIT_TEST_Q_NAME, "desc");
			queue = TestQueueResource.readTestQueue(wr,
					TestQueueResource.UNIT_TEST_Q_NAME);
		}
		// create a message with a random id
		final String newId = UNIT_TEST_M_ID + new Random().nextInt();
		createTestMessageQueue(wr, newId, 4, TestQueueResource.UNIT_TEST_Q_NAME);

		// update id
		final JSONObject message = readTestMessageFromQueue(wr, newId,
				TestQueueResource.UNIT_TEST_Q_NAME);
		message.remove("guid");
		message.put("guid", UNIT_TEST_M_ID);

		// test conflict (409)
		// check 409 if the id is updated
		TestHelper.assertResponseConflict(wr, "PUT",
				MESSAGES_PATH_QUEUE_WITH_SLASH + newId, message);

	}

	/**
	 * Method updateMessageAllFields.
	 * 
	 * @throws JSONException
	 */
	@Test
	public void updateMessageAllFields() throws JSONException {
		// test update
		JSONObject queue = TestQueueResource.findTestQueue(wr,
				TestQueueResource.UNIT_TEST_Q_NAME);
		if (null == queue) {
			TestQueueResource.createTestQueue(wr,
					TestQueueResource.UNIT_TEST_Q_NAME, "desc");
			queue = TestQueueResource.readTestQueue(wr,
					TestQueueResource.UNIT_TEST_Q_NAME);
		}
		// create an message
		createTestMessageQueue(wr, UNIT_TEST_M_ID, 6,
				TestQueueResource.UNIT_TEST_Q_NAME);

		// generate a message(JSONObject format)
		JSONObject jsonEntity = TestUtils.fillResourceData(new JSONObject(),
				new RoutedMessageEntity());
		jsonEntity = TestUtils.fillResourceData(jsonEntity,
				new EntryQueueEntity());
		jsonEntity.put("queuename", TestQueueResource.UNIT_TEST_Q_NAME);
		jsonEntity.put("guid", UNIT_TEST_M_ID);

		TestHelper.assertResponseOK(wr, "PUT", MESSAGES_PATH_QUEUE_WITH_SLASH
				+ UNIT_TEST_M_ID, jsonEntity);
		JSONObject message = readTestMessageFromQueue(wr, jsonEntity
				.get("guid").toString(), TestQueueResource.UNIT_TEST_Q_NAME);

		Assert.assertEquals("m fileds not correct", TestUtils
				.compareJSONObjects(new EntryQueueEntity(), jsonEntity,
						message, "correlationid,insertdate,payload"), true);

	}

	/**
	 * Method deleteMessage.
	 * 
	 * @throws JSONException
	 */
	@Test
	public void deleteMessageFromQueue() throws JSONException {
		JSONObject queue = TestQueueResource.findTestQueue(wr,
				TestQueueResource.UNIT_TEST_Q_NAME);
		if (null == queue) {
			TestQueueResource.createTestQueue(wr,
					TestQueueResource.UNIT_TEST_Q_NAME, "desc");
			queue = TestQueueResource.readTestQueue(wr,
					TestQueueResource.UNIT_TEST_Q_NAME);
		}
		// create a m with a random desc
		final int priority = new Random().nextInt();
		createTestMessageQueue(wr, UNIT_TEST_M_ID, priority,
				TestQueueResource.UNIT_TEST_Q_NAME);

		final JSONObject message = readTestMessageFromQueue(wr, UNIT_TEST_M_ID,
				TestQueueResource.UNIT_TEST_Q_NAME);

		// delete it
		TestHelper.assertResponseOK(wr, "DELETE",
				MESSAGES_PATH_QUEUE_WITH_SLASH + UNIT_TEST_M_ID, null);

		// read it again to make sure it's gone
		TestHelper.assertResponseNotFound(wr, "GET",
				MESSAGES_PATH_QUEUE_WITH_SLASH + UNIT_TEST_M_ID, null);

		// check 404 if the m with the stupid Id is requested
		TestHelper.assertResponseNotFound(wr, "DELETE",
				MESSAGES_PATH_QUEUE_WITH_SLASH + new Random().nextInt(), null);
	}

	/**
	 * Method deleteMessage.
	 * 
	 * @throws JSONException
	 */
	@Test
	public void deleteMessage() throws JSONException {
		JSONObject queue = TestQueueResource.findTestQueue(wr,
				TestQueueResource.UNIT_TEST_Q_NAME);
		if (null == queue) {
			TestQueueResource.createTestQueue(wr,
					TestQueueResource.UNIT_TEST_Q_NAME, "desc");
			queue = TestQueueResource.readTestQueue(wr,
					TestQueueResource.UNIT_TEST_Q_NAME);
		}
		createMessage(wr, UNIT_TEST_M_ID, TestQueueResource.UNIT_TEST_Q_NAME);

		final JSONObject message = readTestMessageFromQueue(wr, UNIT_TEST_M_ID,
				TestQueueResource.UNIT_TEST_Q_NAME);

		// delete it
		TestHelper.assertResponseOK(wr, "DELETE", MESSAGES_PATH_WITH_SLASH
				+ UNIT_TEST_M_ID, null);

		// read it again to make sure it's gone
		TestHelper.assertResponseNotFound(wr, "GET", MESSAGES_PATH_WITH_SLASH
				+ UNIT_TEST_M_ID, null);

		// check 404 if the m with the stupid Id is requested
		TestHelper.assertResponseNotFound(wr, "DELETE",
				MESSAGES_PATH_WITH_SLASH + new Random().nextInt(), null);
	}

	public static JSONObject findRandomMessage(WebResource wr)
		throws JSONException {
	
		JSONObject messages = wr.path(MESSAGES_PATH)
				.accept(MediaType.APPLICATION_JSON).get(JSONObject.class);
		
		JSONArray messagesArray = messages.getJSONArray("messages");
		if(messagesArray.length() == 0){
			return null;
		}
		
		JSONObject message = null;
		do{
			 message = messagesArray.getJSONObject(
					new Random().nextInt(messagesArray.length()));
		}while (!message.getString("guid").startsWith("unit"));
		
		return message;
	
	}
	
	/**
	 * Method readMessage.
	 * 
	 * @throws JSONException
	 */
	@Test
	public void readMessageFromQueue() throws JSONException {
		JSONObject queue = TestQueueResource.findTestQueue(wr,
				TestQueueResource.UNIT_TEST_Q_NAME);
		if (null == queue) {
			TestQueueResource.createTestQueue(wr,
					TestQueueResource.UNIT_TEST_Q_NAME, "desc");
			queue = TestQueueResource.readTestQueue(wr,
					TestQueueResource.UNIT_TEST_Q_NAME);
		}
		// create an message with a random priority
		final int priority = new Random().nextInt();
		createTestMessageQueue(wr, UNIT_TEST_M_ID, priority,
				TestQueueResource.UNIT_TEST_Q_NAME);

		// now read it
		final JSONObject messageObject = readTestMessageFromQueue(wr,
				UNIT_TEST_M_ID, TestQueueResource.UNIT_TEST_Q_NAME);
		Assert.assertEquals("priority not correct", priority,
				messageObject.getInt("priority"));

		// make sure the metadata is returned
		Assert.assertEquals("href not correct", MESSAGES_PATH_QUEUE_WITH_SLASH
				+ UNIT_TEST_M_ID, messageObject.getString("href"));
		Assert.assertEquals("_type not correct",
				MessageResource.class.getName(),
				messageObject.getString("_type"));

		// check 404 if the message with the stupid id is requested
		TestHelper.assertResponseNotFound(wr, "GET",
				MESSAGES_PATH_QUEUE_WITH_SLASH + +new Random().nextInt(), null);
	}

	/**
	 * Method readMessage.
	 * 
	 * @throws JSONException
	 */
	@Test
	public void readMessage() throws JSONException {

		// create an message
		createMessage(wr, UNIT_TEST_M_ID, TestQueueResource.UNIT_TEST_Q_NAME);

		// now read it
		TestHelper.assertResponseOK(wr, "GET", MESSAGES_PATH_WITH_SLASH
				+ UNIT_TEST_M_ID, null);
	}

}
