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
import ro.allevo.fintpws.resources.AlertsResource;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;

/**
 * Tests for {@link AlertsResource}.
 * 
 * @author costi
 */
@RunWith(JUnit4.class)
public class TestAlertsResource {
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
		JSONObject queue = TestQueueResource.findTestQueue(wr, TestQueueResource.UNIT_TEST_Q_NAME);
		if (null == queue) {
			TestQueueResource.createTestQueue(wr, TestQueueResource.UNIT_TEST_Q_NAME, "desc");
			queue = TestQueueResource.readTestQueue(wr, TestQueueResource.UNIT_TEST_Q_NAME);
		}
	}
	
	@After
	public void cleanupAfter() throws JSONException {
		//cleanup();
	}
	
	public static void cleanup() throws JSONException {
		// delete all alerts that start with unit* 
		JSONObject alerts;
		boolean findRecords = false;
		int page = 1;
		do{ 
			findRecords = false;
			alerts = wr.path(ALERTS_PATH).queryParam("page", String.valueOf(page))
					.accept(MediaType.APPLICATION_JSON)
					.get(JSONObject.class);
			JSONArray alertsArray = alerts.getJSONArray("alerts");
			for (int i = 0; i < alertsArray.length(); i++) {
				String alertName = alertsArray.getJSONObject(i).getString("alertname");
				if (alertName.startsWith("unit")) {
					TestAlertResource.deleteTestAlert(wr, alertName);
					findRecords = true;
				}
			}
			if(!findRecords){
				page++;
			}
		}while(alerts.has("has_more"));
		TestQueueResource.deleteTestQueue(wr, TestQueueResource.UNIT_TEST_Q_NAME);
	}
	@Ignore
	@Test
	public void createAlerts() throws JSONException {
		// delete the alert if it already exists
		TestAlertResource.deleteTestAlert(wr, UNIT_TEST_ALERT_NAME);

		// make sure it is deleted
		TestHelper.assertResponseNotFound(wr, "GET", ALERTS_PATH_WITH_SLASH
				+ UNIT_TEST_ALERT_NAME, null);

		// create it with a random desc
		String alertDesc = "unit test alert desc" + new Random().nextInt();
		JSONObject rj = TestAlertResource.createTestAlert(wr, UNIT_TEST_ALERT_NAME,
				alertDesc);
		Assert.assertTrue("uri missing", rj.getString("uri").length() > 0);

		// make sure it is created
		rj = TestAlertResource.readTestAlert(wr, UNIT_TEST_ALERT_NAME);
		Assert.assertEquals("alert description not correct", alertDesc,
				rj.getString("description"));

		// make sure we get a conflict if we create it again
		TestHelper.assertResponseConflict(wr, "POST", ALERTS_PATH, rj);
	}
	@Ignore	
	@Test
	public void insertAlertAllFields() throws JSONException {
		JSONObject queue = TestQueueResource.findTestQueue(wr, TestQueueResource.UNIT_TEST_Q_NAME);
				
		// generate a alert(JSONObject format)
		JSONObject jsonEntity = TestUtils.fillResourceData(new JSONObject(),
				new AlertEntity());
		jsonEntity.put("alertname", "unit"
				+ jsonEntity.getString("alertname").substring(4))
				.put("queueid", queue.getString("guid"));
		// create a alert
		TestHelper.assertResponseCreated(wr, "POST", ALERTS_PATH, jsonEntity);
		// now read it
		JSONObject alert = TestAlertResource.readTestAlert(wr, jsonEntity.getString("alertname"));
		// assert it worked
		Assert.assertEquals("alert filed not correct", TestUtils
				.compareJSONObjects(new AlertEntity(), jsonEntity, alert,
						"alertid, queueid, alertname"), true);
	}
	@Ignore
	@Test
	public void readAlerts() throws JSONException {
		// make sure it makes sense ( >0 alert is available )
		TestAlertResource.createTestAlert(wr, UNIT_TEST_ALERT_NAME, "description");

		JSONObject alerts = wr.path(ALERTS_PATH)
				.accept(MediaType.APPLICATION_JSON).get(JSONObject.class);

		// make sure the metadata is returned
		Assert.assertEquals("href not correct", ALERTS_PATH,
				alerts.getString("href"));
		Assert.assertEquals("_type not correct",
				AlertsResource.class.getName(), alerts.getString("_type"));

		JSONArray alertsList = alerts.getJSONArray("alerts");
		Assert.assertTrue("alert list empty", alertsList.length() > 0);

		// get first alert, see if has a name
		Assert.assertTrue("alert list empty", alertsList.getJSONObject(0)
				.getString("alertname").length() > 0);
	}
	@Ignore
	@Test
	public void updateAlerts() throws JSONException {
		TestHelper.assertResponseMethodNotAllowed(wr, "PUT", ALERTS_PATH_WITH_SLASH,
				null);
	}
	@Ignore
	@Test
	public void deleteAlerts() throws JSONException {
		TestHelper.assertResponseMethodNotAllowed(wr, "DELETE", ALERTS_PATH_WITH_SLASH,
				null);
	}
	@Ignore
	@Test
	public void totalTests() throws JSONException {
		// request t among some garbage
		JSONObject alerts = wr.path(ALERTS_PATH_WITH_SLASH).queryParam("filter", "stu")
				.accept(MediaType.APPLICATION_JSON).get(JSONObject.class);
		int noAlerts = alerts.getInt("total");

		TestAlertResource.createTestAlert(wr, UNIT_TEST_ALERT_NAME, "desc");
		
		// request t among some garbage
		alerts = wr.path(ALERTS_PATH_WITH_SLASH).queryParam("filter", "stu")
				.accept(MediaType.APPLICATION_JSON).get(JSONObject.class);
		int noAlerts2 = alerts.getInt("total");

		Assert.assertEquals("total not +1", noAlerts + 1, noAlerts2);
		
		// request a small page, look for total to be the same ( unoptimized method )
		alerts = wr.path(ALERTS_PATH_WITH_SLASH).queryParam("filter", "stu").queryParam("page_size", "1")
				.accept(MediaType.APPLICATION_JSON).get(JSONObject.class);
		int noAlerts3 = alerts.getInt("total");
		
		Assert.assertEquals("total wrong for page", noAlerts2, noAlerts3);
	}
	@Ignore
	@Test
	public void pagingTests() throws JSONException {
		// TODO : after total is implemented, test the first, last page
		// create 101 alerts
		for (int i = 0; i < 101; i++){
			TestAlertResource.createTestAlert(wr, UNIT_TEST_ALERT_NAME + i, "desc");
		}

		// test get default page (1), default page_size
		JSONObject alerts = wr.path(ALERTS_PATH)
				.accept(MediaType.APPLICATION_JSON).get(JSONObject.class);

		// we should have at least a full page and at least 1 more item
		Assert.assertEquals("default paging items", 100,
				alerts.getJSONArray("alerts").length());
		Assert.assertEquals("default paging has_more", true,
				alerts.getBoolean("has_more"));

		// test get default page (1), custom size : 42
		alerts = wr.path(ALERTS_PATH).queryParam("page_size", "42")
				.accept(MediaType.APPLICATION_JSON).get(JSONObject.class);

		// we should have at least a full page and at least 1 more item
		Assert.assertEquals("custom page size items", 42,
				alerts.getJSONArray("alerts").length());
		Assert.assertEquals("custom page size has_more", true,
				alerts.getBoolean("has_more"));

		// test get default page (1), custom invalid size : -1
		alerts = wr.path(ALERTS_PATH).queryParam("page_size", "-1")
				.accept(MediaType.APPLICATION_JSON).get(JSONObject.class);

		// we should have at least a full page and at least 1 more item
		Assert.assertEquals("custom invalid page size -1 items", 100, alerts
				.getJSONArray("alerts").length());
		Assert.assertEquals("custom invalid page size -1 has_more", true,
				alerts.getBoolean("has_more"));

		// test get default page (1), custom invalid size : 101
		alerts = wr.path(ALERTS_PATH).queryParam("page_size", "101")
				.accept(MediaType.APPLICATION_JSON).get(JSONObject.class);

		// we should have at least a full page and at least 1 more item
		Assert.assertEquals("custom invalid page size 101 items", 100, alerts
				.getJSONArray("alerts").length());
		Assert.assertEquals("custom invalid page size 101 has_more", true,
				alerts.getBoolean("has_more"));

		// test get default page (1), custom invalid size : aaa
		alerts = wr.path(ALERTS_PATH).queryParam("page_size", "aaa")
				.accept(MediaType.APPLICATION_JSON).get(JSONObject.class);

		// we should have at least a full page and at least 1 more item
		Assert.assertEquals("custom invalid page size aaa items", 100, alerts
				.getJSONArray("alerts").length());
		Assert.assertEquals("custom invalid page size aaa paging has_more",
				true, alerts.getBoolean("has_more"));

		// test get custom page (2), custom size : 2
		alerts = wr.path(ALERTS_PATH).queryParam("page", "2")
				.queryParam("page_size", "2")
				.accept(MediaType.APPLICATION_JSON).get(JSONObject.class);

		// we should have at least a full page and at least 1 more item
		Assert.assertEquals("custom page 2 custom page size 2 items", 2, alerts
				.getJSONArray("alerts").length());
		Assert.assertEquals("custom page 2 custom page size 2 has_more", true,
				alerts.getBoolean("has_more"));
		String alert1name = alerts.getJSONArray("alerts").getJSONObject(1)
				.getString("alertname");

		// repeat with next page
		alerts = wr.path(ALERTS_PATH).queryParam("page", "3")
				.queryParam("page_size", "2")
				.accept(MediaType.APPLICATION_JSON).get(JSONObject.class);

		// we should have at least a full page and at least 1 more item
		Assert.assertEquals("custom page 3 custom page size 2 items", 2, alerts
				.getJSONArray("alerts").length());
		Assert.assertEquals("custom page 3 custom page size 2 paging has_more",
				true, alerts.getBoolean("has_more"));
		String alert2name = alerts.getJSONArray("alerts").getJSONObject(1)
				.getString("alertname");

		// alert names should differ ( sloppy test to check another page is
		// returned )
		Assert.assertNotEquals("different alerts on pages", alert1name, alert2name);
	}
}
