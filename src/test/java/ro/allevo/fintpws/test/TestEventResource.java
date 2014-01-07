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

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.TypedQuery;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import ro.allevo.fintpws.model.StatusEntity;
import ro.allevo.fintpws.model.UserEntity;
import ro.allevo.fintpws.resources.EventResource;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;

/**
 * Tests for {@link EventResource}.
 * 
 * @author horia
 * @version $Revision: 1.0 $
 */
@RunWith(JUnit4.class)
public class TestEventResource {
	/**
	 * Field logger.
	 */
	private static Logger logger = LogManager.getLogger(TestEventResource.class
			.getName());

	/**
	 * Field UNIT_TEST_E_GUID. (value is ""unittest e guid"")
	 */
	static final String UNIT_TEST_E_GUID = "unittest e guid";
	/**
	 * Field EVENTS_PATH. (value is ""api/events"")
	 */
	static final String EVENTS_PATH = "api/events";

	/**
	 * Field UNIT_TEST_M_ID. (value is ""unittest e guid"")
	 */
	static final String UNIT_TEST_M_ID = TestMessageResource.UNIT_TEST_M_ID;
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
		// delete test events from previous runs
		TestEventResource.deleteTestEvent(wr, UNIT_TEST_E_GUID);
		
		// delete test messages from previous runs
		TestMessageResource.deleteTestMessage(wr, UNIT_TEST_M_ID);
		// delete test queue
		TestQueueResource.deleteTestQueue(wr, TestQueueResource.UNIT_TEST_Q_NAME);
	
	}

	/**
	 * Method createTestEvent.
	 * 
	 * @param wr
	 *            WebResource
	 * @param id
	 *            String
	 * @param eventAdditionalInfo
	 *            String
	 * @param correlationid String
	 * @return JSONObject 
	 * @throws JSONException 
	 */
	public static JSONObject createTestEvent(WebResource wr, String id,
			String eventAdditionalInfo, String correlationid) throws JSONException {
		final JSONObject event = new JSONObject()
				.put("guid", id)
				.put("additionalinfo", eventAdditionalInfo)
				.put("correlationid", correlationid)
				.put("eventdate", "2013-01-01T12:00:00.000Z")
				.put("innerexception",
						"ORA-06512: at &quot;QPIDATA.ARCHIVEIDLE_BATCHJOBSINC&quot;, line 32")
				.put("insertdate", "2013-01-01T12:00:00.000Z")
				.put("machine", "bromv3lpar")
				.put("message",
						"An exception has occured during archiving [Database operation error "
								+ "[Execute statement failed [Error - OCI_ERROR. Error code [1]. More info #1"
								+ " [ORA-00001: unique constraint (QPIARCH.PK_SENDERBATCH) violated")
				.put("service", 9).put("type", "Error");
		return TestHelper.assertResponseCreated(wr, "POST", EVENTS_PATH, event);
	}

	/**
	 * Method readTestEvent.
	 * 
	 * @param wr
	 *            WebResource
	 * @param id
	 *            String
	 * @return JSONObject
	 * @throws JSONException
	 */
	public static JSONObject readTestEvent(WebResource wr, String id)
			throws JSONException {
		return TestHelper.assertResponseOK(wr, "GET", EVENTS_PATH + "/" + id,
				null);
	}

	/**
	 * Method deleteTestEvent.
	 * @param wr WebResource
	 * @param id String
	 */
	public static void deleteTestEvent(WebResource wr, String id) {
		// since DELETE is not allowed on events, go to the database to delete
		// it
		EntityManagerFactory dataEntityManagerFactory = Persistence
				.createEntityManagerFactory("fintpDATA");
		EntityManager emd = dataEntityManagerFactory.createEntityManager();
		final TypedQuery<StatusEntity> query = emd.createNamedQuery(
				"StatusEntity.findByGuid", StatusEntity.class);

		final java.util.List<StatusEntity> results = query.setParameter(
				"guid", id).getResultList();
		StatusEntity statusEntity = null;
		if(!results.isEmpty()){
			statusEntity = results.get(0);
		}
		
		try {
			if (null != statusEntity) {
				emd.getTransaction().begin();
				emd.remove(statusEntity);
				emd.getTransaction().commit();
			}
		} finally {
			if (null != emd) {
				emd.close();
			}
		}
	}

	/**
	 * Method createEvent.
	 * 
	 * @throws JSONException
	 */
	@Test
	public void createEvent() throws JSONException {
		TestHelper.assertResponseMethodNotAllowed(wr, "POST", EVENTS_PATH + "/"
				+ UNIT_TEST_E_GUID, null);
	}

	/**
	 * Method readEvent.
	 * 
	 * @throws JSONException
	 */
	@Test
	public void readEvent() throws JSONException {
		
		// create an event with a random additional info( related to previous message)
		final String eventAdditionalInfo = "unit test event additional info"
				+ new Random().nextInt();
		createTestEvent(wr, UNIT_TEST_E_GUID, eventAdditionalInfo, 
				"0000");
		// now read it
		JSONObject eventObject = readTestEvent(wr, UNIT_TEST_E_GUID);
		Assert.assertEquals("additional info not correct", eventAdditionalInfo,
				eventObject.getString("additionalinfo"));

		// make sure the metadata is returned
		Assert.assertEquals("href not correct", EVENTS_PATH + '/'
				+ UNIT_TEST_E_GUID, eventObject.getString("href"));
		Assert.assertEquals("_type not correct", EventResource.class.getName(),
				eventObject.getString("_type"));

		// check 404 if the event with the stupid id is requested
		TestHelper.assertResponseNotFound(wr, "GET", EVENTS_PATH + "/"
				+ +new Random().nextInt(), null);
	}

}
