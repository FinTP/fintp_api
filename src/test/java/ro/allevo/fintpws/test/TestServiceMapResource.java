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

import org.junit.runners.JUnit4;

import java.math.BigDecimal;
import java.util.Random;

import javax.ws.rs.core.MediaType;

import org.apache.commons.lang3.RandomStringUtils;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.eclipse.persistence.platform.database.FirebirdPlatform;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import ro.allevo.fintpws.model.ServiceMapEntity;
import ro.allevo.fintpws.resources.ServiceMapResource;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;

@RunWith(JUnit4.class)
public class TestServiceMapResource {

	static final String SERVICE_MAPS_PATH = "api/servicemaps";
	static final String SERVICE_MAPS_PATH_WITH_SLASH = SERVICE_MAPS_PATH + "/";
	static WebResource wr;
	static final String UNIT_TEST_SERVICE_MAP_NAME = "unittest sm name";

	@BeforeClass
	public static void startMethod() {
		final ClientConfig cc = new DefaultClientConfig();
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
		JSONObject serviceMaps = null;
		do {
			findRecords = false;
			serviceMaps = wr.path(SERVICE_MAPS_PATH)
					.queryParam("page", String.valueOf(page))
					.accept(MediaType.APPLICATION_JSON).get(JSONObject.class);
			JSONArray serviceMapsArray = serviceMaps
					.getJSONArray("servicemaps");
			for (int i = 0; i < serviceMapsArray.length(); i++) {

				String friendlyName = serviceMapsArray.getJSONObject(i)
						.getString("friendlyname");
				if (friendlyName.startsWith("unit")) {
					TestServiceMapResource.deleteTestServiceMap(wr,
							friendlyName);
					findRecords = true;
				}
			}
			if (!findRecords) {
				page++;
			}
		} while (serviceMaps.has("has_more"));

	}

	public static JSONObject createTestServiceMap(WebResource wr,
			String friendlyName) throws JSONException {

		JSONObject serviceMap = new JSONObject()
				.put("friendlyname", friendlyName).put("status", "123456")
				.put("heartbeatinterval", "654321");

		return TestHelper.assertResponseCreated(wr, "POST", SERVICE_MAPS_PATH,
				serviceMap);
	}

	public static JSONObject findTestServiceMap(WebResource wr,
			String friendlyName) {
		ClientResponse clientResponse = wr.path(SERVICE_MAPS_PATH)
				.path(friendlyName).accept(MediaType.APPLICATION_JSON)
				.type(MediaType.APPLICATION_JSON).get(ClientResponse.class);
		return (clientResponse.getClientResponseStatus()
				.equals(ClientResponse.Status.OK)) ? clientResponse
				.getEntity(JSONObject.class) : null;
	}

	public static JSONObject findRandomServiceMap(WebResource wr)
			throws JSONException {

		JSONObject serviceMaps = wr.path(SERVICE_MAPS_PATH)
				.queryParam("filter", "tb").accept(MediaType.APPLICATION_JSON)
				.get(JSONObject.class);
		JSONArray serviceMapsArray = serviceMaps.getJSONArray("servicemaps");
		if (serviceMapsArray.length() == 0) {
			return null;
		}

		JSONObject serviceMap = null;
		do {
			serviceMap = serviceMapsArray.getJSONObject(new Random()
					.nextInt(serviceMapsArray.length()));
		} while (!serviceMap.getString("friendlyname").startsWith("unit"));

		return serviceMap;
	}

	public static void deleteTestServiceMap(WebResource wr, String friendlyName) {
		wr.path(SERVICE_MAPS_PATH_WITH_SLASH).path(friendlyName)
				.delete(ClientResponse.class);
	}

	public static JSONObject readTestServiceMap(WebResource wr,
			String friendlyName) throws JSONException {
		return TestHelper.assertResponseOK(wr, "GET",
				SERVICE_MAPS_PATH_WITH_SLASH + friendlyName, null);
	}

	@Test
	public void createServiceMap() throws JSONException {
		TestHelper.assertResponseMethodNotAllowed(wr, "POST",
				SERVICE_MAPS_PATH_WITH_SLASH + "20", null);
	}

	@Test
	public void readServiceMap() throws JSONException {
		// create a service map with unit + random friendlyName
		String friendlyName = "unit" + new Random().nextInt();
		createTestServiceMap(wr, friendlyName);

		// now read it
		JSONObject serviceMap = readTestServiceMap(wr, friendlyName);
		Assert.assertEquals("service map friendlyname not correct",
				friendlyName, serviceMap.getString("friendlyname"));

		// make sure the metadata is returned
		Assert.assertEquals("href not correct", SERVICE_MAPS_PATH_WITH_SLASH
				+ friendlyName, serviceMap.getString("href"));
		Assert.assertEquals("_type not correct",
				ServiceMapResource.class.getName(),
				serviceMap.getString("_type"));

		// check 404 if the service map with the stupid friendlyName is requested
		TestHelper.assertResponseNotFound(wr, "GET",
				SERVICE_MAPS_PATH_WITH_SLASH + "text", null);
	}

	@Test
	public void updateServiceMapOptionalFields() throws JSONException {
		// test update friendlyName
		// create a servicemap with a random friendlyName

		String friendlyName1 = "unit" + new Random().nextInt();
		createTestServiceMap(wr, friendlyName1);

		// change description to a new random one
		String status = new BigDecimal(new Random().nextInt()) + "";
		JSONObject serviceMap = readTestServiceMap(wr, friendlyName1);
		serviceMap.remove("status");
		serviceMap.put("status", status);
		TestHelper.assertResponseOK(wr, "PUT", SERVICE_MAPS_PATH_WITH_SLASH
				+ friendlyName1, serviceMap);

		// assert it worked
		JSONObject serviceMap2 = readTestServiceMap(wr, friendlyName1);
		Assert.assertEquals("service map status not correct", status,
				serviceMap2.getString("status"));

		// test singular field update
		String status2 = new BigDecimal(new Random().nextInt()) + "";
		JSONObject tlNew = new JSONObject().put("status", status2);
		TestHelper.assertResponseOK(wr, "PUT", SERVICE_MAPS_PATH_WITH_SLASH
				+ friendlyName1, tlNew);

		// assert it worked
		JSONObject serviceMap3 = readTestServiceMap(wr, friendlyName1);
		Assert.assertEquals("service map partner not correct", status2,
				serviceMap3.getString("status"));

	}

	@Test
	public void updateServiceMapAllFields() throws JSONException {
		JSONObject jsonEntity = TestUtils.fillResourceData(new JSONObject(),
				new ServiceMapEntity());
		jsonEntity.remove("friendlyname");

		// create a service map
		createTestServiceMap(wr, "unit3");

		// update all fields
		TestHelper.assertResponseOK(wr, "PUT", SERVICE_MAPS_PATH_WITH_SLASH
				+ "unit3", jsonEntity);

		// now read it
		JSONObject serviceMap = readTestServiceMap(wr, "unit3");

		// assert it worked
		Assert.assertEquals("service map fields not correct", TestUtils
				.compareJSONObjects(new ServiceMapEntity(), jsonEntity,
						serviceMap, "friendlyname, serviceid"), true);

	}

	@Test
	public void updateServiceMapFriendlyName() throws JSONException {
		// create service map with random friendlyName

		createTestServiceMap(wr, UNIT_TEST_SERVICE_MAP_NAME);

		// update friendlyName
		JSONObject serviceMap = readTestServiceMap(wr,
				UNIT_TEST_SERVICE_MAP_NAME);
		serviceMap.remove("friendlyname");
		serviceMap
				.put("friendlyname", RandomStringUtils.randomAlphanumeric(51));
		TestHelper.assertResponseBadRequest(wr, "PUT",
				SERVICE_MAPS_PATH_WITH_SLASH + UNIT_TEST_SERVICE_MAP_NAME,
				serviceMap);

	}

	@Test
	public void deleteServiceMap() throws JSONException {

		// create a service map with a random friendlyName
		String name = "unit test " + new Random().nextInt();
		createTestServiceMap(wr, name);

		// delete it
		TestHelper.assertResponseOK(wr, "DELETE", SERVICE_MAPS_PATH_WITH_SLASH
				+ name, null);

		// read it again to make sure it's gone
		TestHelper.assertResponseNotFound(wr, "GET",
				SERVICE_MAPS_PATH_WITH_SLASH + name, null);

		// check 404 if the service map
		TestHelper.assertResponseNotFound(wr, "DELETE",
				SERVICE_MAPS_PATH_WITH_SLASH + "text", null);

	}
}
