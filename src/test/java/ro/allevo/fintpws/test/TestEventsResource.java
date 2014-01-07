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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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

import ro.allevo.fintpws.model.StatusEntity;
import ro.allevo.fintpws.resources.EventsResource;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;

/**
 * Tests for {@link EventsResource}.
 * 
 * @author horia
 * @version $Revision: 1.0 $
 */
@RunWith(JUnit4.class)
public class TestEventsResource {
	/**
	 * Field logger.
	 */
	private static Logger logger = LogManager
			.getLogger(TestEventsResource.class.getName());

	/**
	 * Field UNIT_TEST_E_ID.
	 * (value is ""unittest e id"")
	 */
	static final String UNIT_TEST_E_ID = "unittest e id";
	/**
	 * Field EVENTS_PATH.
	 * (value is ""api/events"")
	 */
	static final String EVENTS_PATH = "api/events";
	/**
	 * Field EVENTS_PATH_SLASH.
	 * (value is ""api/events/"")
	 */
	static final String EVENTS_PATH_SLASH = "api/events/";
	
	/**
	 * Field wr.
	 */
	static WebResource wr;

	/**
	 * Method startMethod.
	 * @throws JSONException
	 */
	@BeforeClass
	public static void startMethod() throws JSONException{
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
	 * Method cleanupBefore.
	 * @throws JSONException
	 */
	@Before
	public void cleanupBefore() throws JSONException {
		cleanup();
	}
	
	/**
	 * Method cleanupAfter.
	 * @throws JSONException
	 */
	@After
	public void cleanupAfter() throws JSONException {
		cleanup();
	}
	
	/**
	 * Method cleanup.
	 * @throws JSONException
	 */
	public static void cleanup() throws JSONException {
		// delete all events that start with unit*
		JSONObject events;
		int page = 1;
		boolean findRecords = false;
		do {
			findRecords = false;
			events = wr.path(EVENTS_PATH).queryParam("page", String.valueOf(page))
					.accept(MediaType.APPLICATION_JSON)
					.get(JSONObject.class);
			JSONArray eventsArray = events.getJSONArray("events");
			for (int i = 0; i < eventsArray.length(); i++) {
				String eventId = eventsArray.getJSONObject(i).getString("guid");
				if (eventId.startsWith("unit")) {
					TestEventResource.deleteTestEvent(wr, eventId);
					findRecords = true;
				}
			}
			if (!findRecords){
				page++;
			}
		} while (events.has("has_more"));
		// delete test messages from previous runs
		cleanupMessages();
		// delete test queue
		TestQueueResource.deleteTestQueue(wr, TestQueueResource.UNIT_TEST_Q_NAME);
	}

	
	public static void cleanupMessages() throws JSONException {
		JSONObject messages;
		int page = 1;
		boolean findRecords = false;
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
			if (!findRecords){
				page++;
			}
		} while (messages.has("has_more"));
	}
	/**
	 * Method createEvents.
	 * @throws JSONException
	 */
	@Test
	public void createEvents() throws JSONException {
		
		// delete the event if it already exists
		TestEventResource.deleteTestEvent(wr, UNIT_TEST_E_ID);

		// make sure it is deleted
		TestHelper.assertResponseNotFound(wr, "GET", EVENTS_PATH_SLASH
				+ UNIT_TEST_E_ID, null);

		// create it with a random additional info(related to previous message)
		final String eventAdditionalInfo = "unit test event additional info"
				+ new Random().nextInt();
		JSONObject rj = TestEventResource.createTestEvent(wr, UNIT_TEST_E_ID,
				eventAdditionalInfo, "000");
		Assert.assertTrue("uri missing", rj.getString("uri").length() > 0);

		// make sure it is created
		rj = TestEventResource.readTestEvent(wr, UNIT_TEST_E_ID);
		Assert.assertEquals("event  additional info not correct",
				eventAdditionalInfo, rj.getString("additionalinfo"));

		// make sure we get a conflict if we create it again
		TestHelper.assertResponseConflict(wr, "POST", EVENTS_PATH, rj);
		
		JSONObject jsonEntity = TestUtils.fillResourceData(new JSONObject(), new StatusEntity());
		jsonEntity.put("guid", "unit"
				+ jsonEntity.getString("guid").substring(4));
		
		TestHelper.assertResponseCreated(wr, "POST", "api/events", jsonEntity);
		JSONObject event = TestEventResource.readTestEvent(wr, jsonEntity.getString("guid"));
		
		Assert.assertEquals("e filed not correct", TestUtils.compareJSONObjects(new StatusEntity(),
						jsonEntity, event, "insertdate"), true);
	}

	/**
	 * Method readEvents.
	 * @throws JSONException
	 */
	@Test
	public void readEvents() throws JSONException {
		
		// make sure it makes sense ( >0 event is available )
		TestEventResource.createTestEvent(wr, UNIT_TEST_E_ID, "additionalinfo", "000");
				
		JSONObject events = wr.path(EVENTS_PATH)
				.accept(MediaType.APPLICATION_JSON).get(JSONObject.class);

		// make sure the metadata is returned
		Assert.assertEquals("href not correct", EVENTS_PATH,
				events.getString("href"));
		Assert.assertEquals("_type not correct",
				EventsResource.class.getName(), events.getString("_type"));

		JSONArray eventsList = events.getJSONArray("events");
		Assert.assertTrue("event list empty", eventsList.length() > 0);

		// get first event, see if has a guid
		Assert.assertTrue("event list empty", eventsList.getJSONObject(0)
				.getString("guid").length() > 0);
	}

	@Test
	public void readEventsCorrelatedMessage() throws JSONException {
		JSONObject message = null;
		// create 10 message
		// create a random number of events related to message
		for (int i = 0; i < 10; i++) {
			TestMessageResource.createMessage(wr,
					TestMessageResource.UNIT_TEST_M_ID + "." + i,
					TestQueueResource.UNIT_TEST_Q_NAME);

			message = wr.path(TestMessageResource.MESSAGES_PATH)
					.path(TestMessageResource.UNIT_TEST_M_ID + "." + i)
					.accept(MediaType.APPLICATION_JSON).get(JSONObject.class);

			for (int j = 0; j < (new Random().nextInt(i + 1) + 1); j++) {
				TestEventResource.createTestEvent(wr, UNIT_TEST_E_ID + i + "."
						+ j, "additionalinfo",
						message.getString("correlationid"));
			}
		}

		message = TestMessageResource.findRandomMessage(wr);
		JSONObject events = wr.path(TestMessageResource.MESSAGES_PATH)
				.path(message.getString("guid")).path("events")
				.accept(MediaType.APPLICATION_JSON).get(JSONObject.class);

		// read events related to message
		JSONArray eventsList = events.getJSONArray("events");
		TestHelper.assertResponseOK(
				wr,
				"GET",
				TestMessageResource.MESSAGES_PATH_WITH_SLASH
						+ message.getString("guid") + "/events", null);
		// get first event, see if has a guid
		Assert.assertTrue("event list empty", eventsList.getJSONObject(0)
				.getString("guid").length() > 0);
	}

	@Test
	public void totalTestsEventsCorrelatedMessage() throws JSONException {
		JSONObject message = null;
		for (int i = 0; i < 10; i++) {
			TestMessageResource.createMessage(wr,
					TestMessageResource.UNIT_TEST_M_ID + "." + i,
					TestQueueResource.UNIT_TEST_Q_NAME);

			message = wr.path(TestMessageResource.MESSAGES_PATH)
					.path(TestMessageResource.UNIT_TEST_M_ID + "." + i)
					.accept(MediaType.APPLICATION_JSON).get(JSONObject.class);

			for (int j = 0; j < (new Random().nextInt(i + 1) + 1); j++) {
				TestEventResource.createTestEvent(wr, UNIT_TEST_E_ID + i + "."
						+ j, "additionalinfo",
						message.getString("correlationid"));
			}
		}

		message = TestMessageResource.findRandomMessage(wr);
		// find events correlated to message
		JSONObject events = wr.path(TestMessageResource.MESSAGES_PATH)
				.path(message.getString("guid")).path("events")
				.queryParam("filter", "t").accept(MediaType.APPLICATION_JSON)
				.get(JSONObject.class);

		// number of events
		final int noEvents1 = events.getInt("total");

		TestEventResource.createTestEvent(wr, UNIT_TEST_E_ID + "new",
				"additionalinfo", message.getString("correlationid"));

		// read events
		events = wr.path(TestMessageResource.MESSAGES_PATH)
				.path(message.getString("guid")).path("events")
				.queryParam("filter", "t").accept(MediaType.APPLICATION_JSON)
				.get(JSONObject.class);
		final int noEvents2 = events.getInt("total");

		Assert.assertEquals("total not +1", noEvents1 + 1, noEvents2);

		// find a different message from the first message
		JSONObject message1 = TestMessageResource.findRandomMessage(wr);
		do {
			message1 = TestMessageResource.findRandomMessage(wr);
		} while (message1.getString("guid").equals(message.getString("guid")));

		// create an event correlated with the message
		TestEventResource.createTestEvent(wr, UNIT_TEST_E_ID + "new1",
				"additionalinfo", message1.getString("correlationid"));

		// read events correlated with first message
		events = wr.path(TestMessageResource.MESSAGES_PATH)
				.path(message.getString("guid")).path("events")
				.queryParam("filter", "t").accept(MediaType.APPLICATION_JSON)
				.get(JSONObject.class);
		final int noEvents3 = events.getInt("total");

		Assert.assertEquals("total wrong", noEvents2, noEvents3);

	}

	/**
	 * Method deleteEvents.
	 * @throws JSONException
	 */
	@Test
	public void deleteEvents() throws JSONException {
		TestHelper.assertResponseMethodNotAllowed(wr, "DELETE",
				EVENTS_PATH_SLASH, null);
	}

	/**
	 * Method totalTests.
	 * @throws JSONException
	 */
	@Test
	public void totalTests() throws JSONException {
		
		// requst t among some garbage
		JSONObject events = wr.path(EVENTS_PATH_SLASH)
				.queryParam("filter", "stu").accept(MediaType.APPLICATION_JSON)
				.get(JSONObject.class);
		int noEvents = events.getInt("total");

		TestEventResource.createTestEvent(wr, UNIT_TEST_E_ID, "desc",
				"000");

		// requst t among some garbage
		events = wr.path(EVENTS_PATH_SLASH).queryParam("filter", "stu")
				.accept(MediaType.APPLICATION_JSON).get(JSONObject.class);
		int noEvents2 = events.getInt("total");

		Assert.assertEquals("total not +1", noEvents + 1, noEvents2);

		// request a small page, look for total to be the same ( unoptimized
		// method )
		events = wr.path(EVENTS_PATH_SLASH).queryParam("filter", "stu")
				.queryParam("page_size", "1")
				.accept(MediaType.APPLICATION_JSON).get(JSONObject.class);
		int noEvents3 = events.getInt("total");

		Assert.assertEquals("total wrong for page", noEvents2, noEvents3);
	}

	/**
	 * Method pagingTests.
	 * @throws JSONException
	 */
	@Test
	public void pagingTests() throws JSONException {
		
		// create 101 events
		for (int i = 0; i < 101; i++) {
			TestEventResource.createTestEvent(wr, UNIT_TEST_E_ID + i, "desc",
					 "000");
		}

		// test get default page (1), default page_size
		JSONObject events = wr.path(EVENTS_PATH)
				.accept(MediaType.APPLICATION_JSON).get(JSONObject.class);

		// we should have at least a full page and at least 1 more item
		Assert.assertEquals("default paging items", 100,
				events.getJSONArray("events").length());
		Assert.assertEquals("default paging has_more", true,
				events.getBoolean("has_more"));

		// test get last page
		events = wr.path(EVENTS_PATH).queryParam("filter", "t")
				.accept(MediaType.APPLICATION_JSON).get(JSONObject.class);
		int noEvents = events.getInt("total");
		int pageNo = noEvents / 100;
		if(((noEvents % 100) != 0) || (noEvents == 0)){
			pageNo++;
		}
		events = wr.path(EVENTS_PATH)
				.queryParam("page", String.valueOf(pageNo))
				.accept(MediaType.APPLICATION_JSON).get(JSONObject.class);
		Assert.assertEquals("custom last page", noEvents % 100, events
				.getJSONArray("events").length());

		// test get default page (1), custom size : 42
		events = wr.path(EVENTS_PATH).queryParam("page_size", "42")
				.accept(MediaType.APPLICATION_JSON).get(JSONObject.class);

		// we should have at least a full page and at least 1 more item
		Assert.assertEquals("custom page size items", 42,
				events.getJSONArray("events").length());
		Assert.assertEquals("custom page size has_more", true,
				events.getBoolean("has_more"));

		// test get default page (1), custom invalid size : -1
		events = wr.path(EVENTS_PATH).queryParam("page_size", "-1")
				.accept(MediaType.APPLICATION_JSON).get(JSONObject.class);

		// we should have at least a full page and at least 1 more item
		Assert.assertEquals("custom invalid page size -1 items", 100, events
				.getJSONArray("events").length());
		Assert.assertEquals("custom invalid page size -1 has_more", true,
				events.getBoolean("has_more"));

		// test get default page (1), custom invalid size : 101
		events = wr.path(EVENTS_PATH).queryParam("page_size", "101")
				.accept(MediaType.APPLICATION_JSON).get(JSONObject.class);

		// we should have at least a full page and at least 1 more item
		Assert.assertEquals("custom invalid page size 101 items", 100, events
				.getJSONArray("events").length());
		Assert.assertEquals("custom invalid page size 101 has_more", true,
				events.getBoolean("has_more"));

		// test get default page (1), custom invalid size : aaa
		events = wr.path(EVENTS_PATH).queryParam("page_size", "aaa")
				.accept(MediaType.APPLICATION_JSON).get(JSONObject.class);

		// we should have at least a full page and at least 1 more item
		Assert.assertEquals("custom invalid page size aaa items", 100, events
				.getJSONArray("events").length());
		Assert.assertEquals("custom invalid page size aaa paging has_more",
				true, events.getBoolean("has_more"));

		// test get custom page (2), custom size : 2
		events = wr.path(EVENTS_PATH).queryParam("page", "2")
				.queryParam("page_size", "2")
				.accept(MediaType.APPLICATION_JSON).get(JSONObject.class);

		// we should have at least a full page and at least 1 more item
		Assert.assertEquals("custom page 2 custom page size 2 items", 2, events
				.getJSONArray("events").length());
		Assert.assertEquals("custom page 2 custom page size 2 has_more", true,
				events.getBoolean("has_more"));
		String event1ID = events.getJSONArray("events").getJSONObject(1)
				.getString("guid");

		// repeat with next page
		events = wr.path(EVENTS_PATH).queryParam("page", "3")
				.queryParam("page_size", "2")
				.accept(MediaType.APPLICATION_JSON).get(JSONObject.class);

		// we should have at least a full page and at least 1 more item
		Assert.assertEquals("custom page 3 custom page size 2 items", 2, events
				.getJSONArray("events").length());
		Assert.assertEquals("custom page 3 custom page size 2 paging has_more",
				true, events.getBoolean("has_more"));
		String event2Id = events.getJSONArray("events").getJSONObject(1)
				.getString("guid");

		// event ids should differ ( sloppy test to check another page is
		// returned )
		Assert.assertNotEquals("different events on pages", event1ID, event2Id);
	}
}
