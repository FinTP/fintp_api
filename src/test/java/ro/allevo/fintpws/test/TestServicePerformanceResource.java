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

import ro.allevo.fintpws.model.ServicePerformanceEntity;
import ro.allevo.fintpws.resources.ServicePerformanceResource;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;

/**
 * 
 * @author Edi
 *
 */

@RunWith(JUnit4.class)
public class TestServicePerformanceResource {

	static final String SERVICE_PERFORMANCES_PATH = "api/serviceperformances";
	static final String SERVICE_PERFORMANCES_PATH_WITH_SLASH = SERVICE_PERFORMANCES_PATH
			+ "/";
	static WebResource wr;
	static final String UNIT_TEST_SERVICE_PERFOEMANCE_SEQUENCENO = "9999999";

	@BeforeClass
	public static void startMethod() {
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
		JSONObject servicePerformances = null;
		do {
			findRecords = false;
			servicePerformances = wr.path(SERVICE_PERFORMANCES_PATH)
					.queryParam("page", String.valueOf(page))
					.accept(MediaType.APPLICATION_JSON).get(JSONObject.class);
			JSONArray servicePerformancesArray = servicePerformances
					.getJSONArray("serviceperformances");
			for (int i = 0; i < servicePerformancesArray.length(); i++) {

				String sequenceNo = servicePerformancesArray.getJSONObject(i)
						.getString("sequenceno");
				String serviceId = servicePerformancesArray.getJSONObject(i)
						.getString("serviceid");
				if (sequenceNo
						.startsWith(UNIT_TEST_SERVICE_PERFOEMANCE_SEQUENCENO)) {
					TestServicePerformanceResource.deleteTestServicePerformance(wr, serviceId);
					findRecords = true;
				}
			}
			if (!findRecords) {
				page++;
			}
		} while (servicePerformances.has("has_more"));

	}

	public static JSONObject createTestServicePerformance(WebResource wr,
			String sequenceNo) throws JSONException {

		JSONObject servicePerformance = new JSONObject()
				.put("insertdate", "2013-01-01T12:00:00.000Z")
				.put("mintransactiontime", "1")
				.put("maxtransactiontime", "1")
				.put("meantransactiontime", "1")
				.put("sequenceno", sequenceNo)
				.put("ioidentifier", "1")
				.put("sessionid", "1");

		return TestHelper.assertResponseCreated(wr, "POST",
				SERVICE_PERFORMANCES_PATH, servicePerformance);
	}

	public static JSONObject findTestServicePerformance(WebResource wr,
			String serviceId) {
		ClientResponse clientResponse = wr.path(SERVICE_PERFORMANCES_PATH)
				.type(MediaType.APPLICATION_JSON).get(ClientResponse.class);
		return (clientResponse.getClientResponseStatus()
				.equals(ClientResponse.Status.OK)) ? clientResponse
				.getEntity(JSONObject.class) : null;
	}

	public static JSONObject findRandomServicePerformance(WebResource wr)
			throws JSONException {

		JSONObject servicePerformances = wr.path(SERVICE_PERFORMANCES_PATH)
				.queryParam("filter", "tb").accept(MediaType.APPLICATION_JSON)
				.get(JSONObject.class);
		JSONArray servicePerformancesArray = servicePerformances
				.getJSONArray("serviceperformances");
		if (servicePerformancesArray.length() == 0) {
			return null;
		}

		JSONObject servicePerformance = null;
		do {
			servicePerformance = servicePerformancesArray
					.getJSONObject(new Random()
							.nextInt(servicePerformancesArray.length()));
		} while (!servicePerformance.getString("sequenceno").startsWith(
				UNIT_TEST_SERVICE_PERFOEMANCE_SEQUENCENO));

		return servicePerformance;
	}

	public static void deleteTestServicePerformance(WebResource wr,
			String serviceId) {
		wr.path(SERVICE_PERFORMANCES_PATH_WITH_SLASH).path(serviceId)
				.delete(ClientResponse.class);
	}

	public static JSONObject readTestServicePerformance(WebResource wr,
			String serviceId) throws JSONException {
		return TestHelper.assertResponseOK(wr, "GET",
				SERVICE_PERFORMANCES_PATH_WITH_SLASH + serviceId, null);
	}

	@Test
	public void createServicePerformance() throws JSONException {
		TestHelper.assertResponseMethodNotAllowed(wr, "POST",
				SERVICE_PERFORMANCES_PATH_WITH_SLASH + "20", null);
	}

	@Test
	public void readServicePerformance() throws JSONException {
		// create a service performance with a sequenceNo

		// String sequenceNo = UNIT_TEST_SERVICE_PERFOEMANCE_SEQUENCENO + new
		// Random().nextInt();
		JSONObject response = createTestServicePerformance(wr,
				UNIT_TEST_SERVICE_PERFOEMANCE_SEQUENCENO);

		// now read it
		JSONObject servicePerformance = readTestServicePerformance(wr,
				response.getString("id"));
		Assert.assertEquals("service performance sequenceno not correct",
				UNIT_TEST_SERVICE_PERFOEMANCE_SEQUENCENO,
				servicePerformance.getString("sequenceno"));

		// make sure the metadata is returned
		Assert.assertEquals(
				"href not correct",
				SERVICE_PERFORMANCES_PATH_WITH_SLASH + response.getString("id"),
				servicePerformance.getString("href"));
		Assert.assertEquals("_type not correct",
				ServicePerformanceResource.class.getName(),
				servicePerformance.getString("_type"));

		// check 404 if the service performance with the stupid sequenceno is
		// requested
		TestHelper.assertResponseNotFound(wr, "GET", SERVICE_PERFORMANCES_PATH_WITH_SLASH
				+ "text", null);
	}

	@Test
	public void updateServicePerformanceOptionalFields() throws JSONException {
		// test update COMMITEDTRNS
		// create a serviceperformance with a random COMMITEDTRNS

		JSONObject response1 = createTestServicePerformance(wr,
				UNIT_TEST_SERVICE_PERFOEMANCE_SEQUENCENO);

		// change commitedtrns to a new random one
		String commitedtrns = new BigDecimal(new Random().nextInt()) + "";
		JSONObject servicePerformance = readTestServicePerformance(wr,
				response1.getString("id"));
		servicePerformance.remove("commitedtrns");
		servicePerformance.put("commitedtrns", commitedtrns);
		TestHelper.assertResponseOK(
				wr,
				"PUT",
				SERVICE_PERFORMANCES_PATH_WITH_SLASH
						+ response1.getString("id"), servicePerformance);

		// assert it worked
		JSONObject servicePerformance2 = readTestServicePerformance(wr,
				response1.getString("id"));
		Assert.assertEquals("service performance commitedtrns not correct",
				commitedtrns, servicePerformance2.getString("commitedtrns"));

		// test singular field update
		String commitedtrns2 = new BigDecimal(new Random().nextInt()) + "";
		JSONObject tlNew = new JSONObject().put("commitedtrns", commitedtrns2);
		TestHelper.assertResponseOK(
				wr,
				"PUT",
				SERVICE_PERFORMANCES_PATH_WITH_SLASH
						+ response1.getString("id"), tlNew);

		// assert it worked
		JSONObject servicePerformance3 = readTestServicePerformance(wr,
				response1.getString("id"));
		Assert.assertEquals("service performance commitedtrns not correct",
				commitedtrns2, servicePerformance3.getString("commitedtrns"));

	}

	@Test
	public void updateServicePerformanceAllFields() throws JSONException {
		JSONObject jsonEntity = TestUtils.fillResourceData(new JSONObject(),
				new ServicePerformanceEntity());
		jsonEntity.remove("serviceid");
		// jsonEntity.remove("friendlyname");
		
		jsonEntity.put(
				"sequenceno",
				"9999999"
						+ jsonEntity.getString("sequenceno")
								.substring(
										jsonEntity.getString("sequenceno")
												.length() < 8 ? jsonEntity
												.getString("sequenceno")
												.length() : 8));
	// create a service performance
		JSONObject response = createTestServicePerformance(wr,
				UNIT_TEST_SERVICE_PERFOEMANCE_SEQUENCENO);

		// update all fields
		TestHelper.assertResponseOK(wr, "PUT",
				SERVICE_PERFORMANCES_PATH_WITH_SLASH + response.getString("id"), jsonEntity);

		// now read it
		JSONObject servicePerformance = readTestServicePerformance(wr,
				response.getString("id"));

		// assert it worked
		Assert.assertEquals("service performance fields not correct", TestUtils
				.compareJSONObjects(new ServicePerformanceEntity(), jsonEntity,
						servicePerformance, "serviceid"), true);

	}

	@Test
	public void updateServicePerformanceFriendlyName() throws JSONException {
		// create service performance with random sequenceno

		JSONObject response = createTestServicePerformance(wr,
				UNIT_TEST_SERVICE_PERFOEMANCE_SEQUENCENO);

		// update sequenceno
		JSONObject servicePerformance = readTestServicePerformance(wr,
				response.getString("id"));
		servicePerformance.remove("sequenceno");
		servicePerformance.put("sequenceno", "9999999999999");
		TestHelper
				.assertResponseBadRequest(
						wr,
						"PUT",
						SERVICE_PERFORMANCES_PATH_WITH_SLASH
								+ response.getString("id"), servicePerformance);

	}

	@Test
	public void deleteServicePerformance() throws JSONException {

		// create a service perfromance with a sequenceno 
		
		JSONObject response = createTestServicePerformance(wr,
				UNIT_TEST_SERVICE_PERFOEMANCE_SEQUENCENO);

		// delete it
		TestHelper.assertResponseOK(wr, "DELETE",
				SERVICE_PERFORMANCES_PATH_WITH_SLASH + response.getString("id"), null);

		// read it again to make sure it's gone
		TestHelper.assertResponseNotFound(wr, "GET",
				SERVICE_PERFORMANCES_PATH_WITH_SLASH + response.getString("id"), null);

		// check 404 if the service performance
		TestHelper.assertResponseNotFound(wr, "DELETE",
				SERVICE_PERFORMANCES_PATH_WITH_SLASH + "text", null);

	}
}
