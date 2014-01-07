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
import javax.ws.rs.core.MediaType;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;

/**
 * Simplistic unit-testing main class. Runs all implemented methods against a
 * deployed API. Use as select in Project Explorer -> Run as application
 */
public final class Main {

	private static Logger logger = LogManager.getLogger(Main.class.getName());

	static final String URL_BASE = "http://localhost:8080/fintpWebServices/";
	static final double MILLIS_IN_A_SEC = 1000.0;
	static final String TESTCASE_DELIMITER = "-----";
	static final String ACHOUTQUEUE_PATH = "api/queues/ACHOutQueue";
	static final String MESSAGES_PATH = "api/messages";
	static final String QUEUES_PATH = "api/queues";
	
	static final String QUEUE_FIELD_DESCRIPTION = "description";
	static final String QUEUE_FIELD_HOLDSTATUS = "holdstatus";
	private static ClientResponse clientResponse = null;

	private Main() {
	}

	private static void testApis( WebResource wr ) {
		logger.debug("Getting list of apis:");
		JSONArray users = wr.path("api").accept(MediaType.APPLICATION_JSON)
				.get(JSONArray.class);
		logger.debug(String.format("List of apis found:\n%s",
				users.toString()));
		logger.debug(TESTCASE_DELIMITER);
	}
	
//Queue section
	private static void testQueues( WebResource wr ) throws JSONException {
		logger.debug("Getting list of queues:");
		JSONObject queues = wr.path(QUEUES_PATH)
				.accept(MediaType.APPLICATION_JSON).get(JSONObject.class);
		JSONArray queuesList = queues.getJSONArray("queues");
		logger.debug(String.format("List of queues found:\n%s",
				queuesList.toString()));
		logger.debug(TESTCASE_DELIMITER);
	}
	
	private static void testACHOutQueue( WebResource wr ) throws JSONException {
		logger.debug("Getting queue ACHOutQueue:");
		clientResponse = wr.path(ACHOUTQUEUE_PATH).type(MediaType.APPLICATION_JSON)
			.get(ClientResponse.class);
		JSONObject queue = clientResponse.getEntity(JSONObject.class);
		logger.debug(String.format("ACHOutQueue found:\n%s",
				queue.toString()));
		logger.debug(TESTCASE_DELIMITER);
	}
	
	private static void testPostQueue( WebResource wr) throws JSONException {
		logger.debug("Add new queue:");
		/*JSONObject queue = wr.path(QUEUES_PATH).path("test1")
				.accept(MediaType.APPLICATION_JSON).get(JSONObject.class);
		queue.put("name", "newName");*/
		JSONObject queueAsJson = new JSONObject();
		queueAsJson.put("guid", "guid")
				.put("name", "QueueName")
				.put("description", "queue description")
				.put("holdstatus", 1)
				.put("connector", 2)
				.put("actions", 3)
				.put("batchno", 4)
				.put("typeid", 5)
				.put("moveto", 6);
		clientResponse = wr.path(QUEUES_PATH).type(MediaType.APPLICATION_JSON)
			.post(ClientResponse.class, queueAsJson);
		if (ClientResponse.Status.CREATED == clientResponse.getClientResponseStatus()) 
			logger.debug(String.format("Queue is created:\n%s", clientResponse.getEntity(JSONObject.class)));
		else
			logger.debug(String.format("Client response:\n%s", clientResponse.getEntity(JSONObject.class)));
		logger.debug(TESTCASE_DELIMITER);
	}
	
	private static void testPutQueue( WebResource wr) throws JSONException {
		logger.debug("Update queue:");
		clientResponse = wr.path(QUEUES_PATH).path("QueueName")
				.accept(MediaType.APPLICATION_JSON).get(ClientResponse.class);
		JSONObject queue = clientResponse.getEntity(JSONObject.class);
		queue.put("description", "newDescription");
				
		clientResponse = wr.path(QUEUES_PATH).path("QueueName").type(MediaType.APPLICATION_JSON)
			.put(ClientResponse.class, queue);
		if (ClientResponse.Status.OK == clientResponse.getClientResponseStatus()) 
			logger.debug(String.format("Queue updated:\n%s", clientResponse.getEntity(JSONObject.class)));
		else
			logger.debug(String.format("Client response:\n%s", clientResponse.getEntity(JSONObject.class)));
		logger.debug(TESTCASE_DELIMITER);
	}
	
	private static void testDeleteQueue( WebResource wr ) throws JSONException {
		logger.debug("Delete queue:");
		clientResponse = wr.path(QUEUES_PATH).path("QueueName").delete(ClientResponse.class);
		if (ClientResponse.Status.OK == clientResponse.getClientResponseStatus()) 
			logger.debug(String.format("Queue deleted:\n%s",  clientResponse.getEntity(JSONObject.class)));
		else
			logger.debug(String.format("Client response:\n%s", clientResponse.getEntity(JSONObject.class)));
		logger.debug(TESTCASE_DELIMITER);
	}
	
//Message section
	private static void testMessages( WebResource wr ) throws JSONException {
		logger.debug("Getting list of messages:");
		JSONObject messages = wr.path(MESSAGES_PATH)
				.accept(MediaType.APPLICATION_JSON).get(JSONObject.class);
		JSONArray messagesList = messages.getJSONArray("messages");
		logger.debug(String.format("List of messages found:\n%s",
				messagesList.toString()));
		logger.debug(TESTCASE_DELIMITER);
	}
	
	private static void testMessageWithGuid( WebResource wr, String messageGuid) throws JSONException {
		logger.debug("Getting message with guid " + messageGuid + ":");
		JSONObject message = wr.path(MESSAGES_PATH).path(messageGuid)
				.accept(MediaType.APPLICATION_JSON).get(JSONObject.class);
		logger.debug(String.format("Message found:\n%s",
				message.toString()));
		logger.debug(TESTCASE_DELIMITER);
	}
	
	private static void testPUTPerformance( WebResource wr ) throws JSONException {
		logger.debug("Test performance with java reflection and java static:");
		// to test comment (uncomment) @PUT and asJson methods 

		long startLong = 0;
		long endLong = 0;

		// Test with JSONObject
		// prepare test ( 1 run to allow runtime to compile whatever is needed)
		clientResponse = wr.path(ACHOUTQUEUE_PATH).type(MediaType.APPLICATION_JSON)
						.get(ClientResponse.class);
		JSONObject queue = clientResponse.getEntity(JSONObject.class);
		
		clientResponse = wr.path(ACHOUTQUEUE_PATH).type(MediaType.APPLICATION_JSON).put(ClientResponse.class, queue);
		
		// run the test
		startLong = System.currentTimeMillis();
		for (int i = 0; i < 1; i++) {
			queue.put(QUEUE_FIELD_DESCRIPTION, "description" + i);
			queue.put(QUEUE_FIELD_HOLDSTATUS, new BigDecimal(i));
			clientResponse = wr.path(ACHOUTQUEUE_PATH).type(MediaType.APPLICATION_JSON).put(ClientResponse.class, queue);
		}
		endLong = System.currentTimeMillis();
		logger.debug("time .... " + (endLong - startLong) / MILLIS_IN_A_SEC + " seconds");
		logger.debug(TESTCASE_DELIMITER);
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {

		try {
			// create a client:
			ClientConfig cc = new DefaultClientConfig();
			Client c = Client.create(cc);

			String argUrl = System.getProperty("webAppUrl");
			if ( argUrl == null )
				argUrl = URL_BASE;
			WebResource wr = c.resource(argUrl);
			
			// get the api list
			testApis(wr);
			
			// get the queues list
			testQueues(wr);
			
			// get the ACHOutQueue
			testACHOutQueue(wr);
			
			// add new queue
			testPostQueue(wr);
			
			//update queue
			testPutQueue(wr);
			
			// delete queue
			testDeleteQueue(wr);
			
			// get the messages list
			testMessages(wr);

			// test PUT performance
			testPUTPerformance(wr);
			
			// get the message
			testMessageWithGuid(wr, "8");
			
		} catch (JSONException je) {
			logger.error("JSONException : ", je);
		}
	}
}
