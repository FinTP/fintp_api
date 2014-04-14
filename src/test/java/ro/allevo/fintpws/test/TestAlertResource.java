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
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import ro.allevo.fintpws.model.AlertEntity;
import ro.allevo.fintpws.resources.AlertResource;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;

/**
 * Tests for {@link AlertResource}.
 * 
 * @author costi
 */
@RunWith(JUnit4.class)
public class TestAlertResource {
	/**
	 * Field logger.
	 */
	
	static final String UNIT_TEST_ALERT_NAME = "unittest alert name";
	static final String ALERTS_PATH = "api/alerts";
	static final String ALERTS_PATH_WITH_SLASH = ALERTS_PATH + "/";
		
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
		// delete all alerts that start with unit*
		int page = 1;
		boolean findRecords = false;
		JSONObject alerts = null;
		do{
			findRecords = false;
			alerts = wr.path(ALERTS_PATH).queryParam("page", String.valueOf(page))
					.accept(MediaType.APPLICATION_JSON).get(JSONObject.class);
			JSONArray alertsArray = alerts.getJSONArray("alerts");
			for (int i = 0; i < alertsArray.length(); i++) {
				String alertName = alertsArray.getJSONObject(i).getString("alertname");
				if (alertName.startsWith("unit")) {
					TestAlertResource.deleteTestAlert(wr, alertName);
					findRecords = true;
				}
			}
			if (!findRecords){
				page++;
			}
		} while (alerts.has("has_more"));
	}

	public static JSONObject createTestAlert(WebResource wr, String name,
			String desc) throws JSONException {
		JSONObject queue = TestQueueResource.findTestQueue(wr, TestQueueResource.UNIT_TEST_Q_NAME);
		if (null == queue) {
			TestQueueResource.createTestQueue(wr,
					TestQueueResource.UNIT_TEST_Q_NAME, "desc");
			queue = TestQueueResource.readTestQueue(wr,
					TestQueueResource.UNIT_TEST_Q_NAME);
		}
		JSONObject alert = new JSONObject()
				.put("alertname", name)
				.put("type", "new message")
				.put("description", desc)
				.put("queueid", queue.getString("guid"));
		return TestHelper
				.assertResponseCreated(wr, "POST", ALERTS_PATH, alert);
	}

	public static JSONObject findTestAlert(WebResource wr, String name)
			throws JSONException {
		ClientResponse clientResponse = wr.path(ALERTS_PATH).path(name)
				.accept(MediaType.APPLICATION_JSON)
				.type(MediaType.APPLICATION_JSON).get(ClientResponse.class);
		return (clientResponse.getClientResponseStatus()
				.equals(ClientResponse.Status.OK)) ? clientResponse
				.getEntity(JSONObject.class) : null;
	}
	
	public static JSONObject findRandomAlert(WebResource wr)
			throws JSONException {

		JSONObject alerts = wr.path(ALERTS_PATH).queryParam("filter", "tb")
				.accept(MediaType.APPLICATION_JSON).get(JSONObject.class);

		JSONArray alertsArray = alerts.getJSONArray("alerts");
		if(alertsArray.length() == 0){
			return null;
		}
		
		JSONObject alert = null;
		do{
			 alert = alertsArray.getJSONObject(
					new Random().nextInt(alertsArray.length()));
		}while (!alert.getString("alertname").startsWith("unit"));
		
		return alert;

	}

	public static JSONObject readTestAlert(WebResource wr, String name)
			throws JSONException {
		return TestHelper.assertResponseOK(wr, "GET", ALERTS_PATH_WITH_SLASH + name,
				null);
	}

	public static void deleteTestAlert(WebResource wr, String name) {
		wr.path(ALERTS_PATH_WITH_SLASH).path(name).delete(ClientResponse.class);
	}

	@Ignore
	@Test
	public void createAlert() throws JSONException {
		TestHelper.assertResponseMethodNotAllowed(wr, "POST", ALERTS_PATH_WITH_SLASH
				+ UNIT_TEST_ALERT_NAME, null);
	}

	@Ignore
	@Test
	public void readAlert() throws JSONException {
		// create a alert with a random desc
		String alertDesc = "unit test alert desc" + new Random().nextInt();
		createTestAlert(wr, UNIT_TEST_ALERT_NAME, alertDesc);

		// now read it
		JSONObject rj = readTestAlert(wr, UNIT_TEST_ALERT_NAME);
		Assert.assertEquals("alert description not correct", alertDesc,
				rj.getString("description"));

		// make sure the metadata is returned
		Assert.assertEquals("href not correct", ALERTS_PATH_WITH_SLASH
				+ UNIT_TEST_ALERT_NAME, rj.getString("href"));
		Assert.assertEquals("_type not correct", AlertResource.class.getName(),
				rj.getString("_type"));

		// check 404 if the alert with the stupid name is requested
		TestHelper.assertResponseNotFound(wr, "GET", ALERTS_PATH_WITH_SLASH
				+ +new Random().nextInt(), null);
	}
	@Ignore
	@Test
	public void updateAlertOptionalFields() throws JSONException {
		// test update description
		// create a alert with a random desc
		String alertDesc = "unit test alert desc" + new Random().nextInt();
		createTestAlert(wr, UNIT_TEST_ALERT_NAME, alertDesc);

		// change description to a new random one
		String alertDesc2 = "unit test alert desc" + new Random().nextInt();
		JSONObject alert = readTestAlert(wr, UNIT_TEST_ALERT_NAME);
		alert.remove("description");
		alert.put("description", alertDesc2);
		TestHelper.assertResponseOK(wr, "PUT",
				ALERTS_PATH_WITH_SLASH + UNIT_TEST_ALERT_NAME, alert);

		// assert it worked
		JSONObject alert2 = readTestAlert(wr, UNIT_TEST_ALERT_NAME);
		Assert.assertEquals("alert description not correct", alertDesc2,
				alert2.getString("description"));

		// test singular field update
		String alertDesc3 = "unit test alert desc" + new Random().nextInt();
		JSONObject alertnewdesc = new JSONObject().put("description", alertDesc3);
		TestHelper.assertResponseOK(wr, "PUT",
				ALERTS_PATH_WITH_SLASH + UNIT_TEST_ALERT_NAME, alertnewdesc);

		// assert it worked
		JSONObject alert3 = readTestAlert(wr, UNIT_TEST_ALERT_NAME);
		Assert.assertEquals("alert description not correct", alertDesc3,
				alert3.getString("description"));
	}
	@Ignore
	@Test
	public void updateAlertLongName() throws JSONException {
		// create a alert with a random desc
		String alertDesc = "unit test alert desc" + new Random().nextInt();
		createTestAlert(wr, UNIT_TEST_ALERT_NAME, alertDesc);

		// update name
		JSONObject alert = readTestAlert(wr, UNIT_TEST_ALERT_NAME);
		alert.remove("alertname");
		alert.put("alertname", "unittest1234567890123456789012345678901234567890");
		TestHelper.assertResponseBadRequest(wr, "PUT", ALERTS_PATH_WITH_SLASH + UNIT_TEST_ALERT_NAME,
				alert);
	}
	@Ignore
	@Test
	public void updateAlertAllFields() throws JSONException {
		// generate a alert(JSONObject format)
		JSONObject jsonEntity = TestUtils.fillResourceData(new JSONObject(),
				new AlertEntity());
		JSONObject queue = TestQueueResource.findTestQueue(wr, TestQueueResource.UNIT_TEST_Q_NAME);
		
		jsonEntity.remove("alertname");
		jsonEntity.put("queueid", queue.getString("guid"));
		jsonEntity.remove("alertid");
		
		// create a alert
		createTestAlert(wr, UNIT_TEST_ALERT_NAME, "qdesc");

		// update all fields
		TestHelper.assertResponseOK(wr, "PUT", ALERTS_PATH_WITH_SLASH
				+ UNIT_TEST_ALERT_NAME, jsonEntity);

		// now read it
		JSONObject alert = readTestAlert(wr, UNIT_TEST_ALERT_NAME);

		// assert it worked
		Assert.assertEquals("alert fileds not correct", TestUtils
				.compareJSONObjects(new AlertEntity(), jsonEntity, alert,
						"alertname,queueid,alertid"), true);
	}
	@Ignore
	@Test
	public void updateAlertKeyFields() throws JSONException {
		// test update name
		// create a alert with a random name
		String newname = UNIT_TEST_ALERT_NAME + new Random().nextInt();
		createTestAlert(wr, newname, "description");

		// update name
		JSONObject alert = readTestAlert(wr, newname);
		alert.remove("alertname");
		alert.put("alertname", UNIT_TEST_ALERT_NAME);
		TestHelper.assertResponseOK(wr, "PUT", ALERTS_PATH_WITH_SLASH + newname, alert);

		// assert it worked
		JSONObject alert2 = readTestAlert(wr, UNIT_TEST_ALERT_NAME);
		Assert.assertEquals("alert name not correct", UNIT_TEST_ALERT_NAME,
				alert2.getString("alertname"));

		// test name conflict (409)
		// create a second alert
		String alert2name = UNIT_TEST_ALERT_NAME + new Random().nextInt();
		createTestAlert(wr, alert2name, "description");

		// update to name of the 1st alert
		alert2 = readTestAlert(wr, alert2name);
		alert2.remove("alertname");
		alert2.put("alertname", UNIT_TEST_ALERT_NAME);
		TestHelper.assertResponseConflict(wr, "PUT",
				ALERTS_PATH_WITH_SLASH + alert2name, alert2);
	}
	@Ignore
	@Test
	public void deleteAlert() throws JSONException {
		// create a alert with a random desc
		String alertDesc = "unit test alert desc" + new Random().nextInt();
		createTestAlert(wr, UNIT_TEST_ALERT_NAME, alertDesc);

		JSONObject alert = readTestAlert(wr, UNIT_TEST_ALERT_NAME);

		// delete it
		TestHelper.assertResponseOK(wr, "DELETE", ALERTS_PATH_WITH_SLASH
				+ UNIT_TEST_ALERT_NAME, null);

		// read it again to make sure it's gone
		TestHelper.assertResponseNotFound(wr, "GET", ALERTS_PATH_WITH_SLASH
				+ UNIT_TEST_ALERT_NAME, null);

		// check 404 if the alert with the stupid name is requested
		TestHelper.assertResponseNotFound(wr, "DELETE", ALERTS_PATH_WITH_SLASH
				+ new Random().nextInt(), null);
	}
}
