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

import org.apache.commons.lang3.RandomStringUtils;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import ro.allevo.fintpws.model.QueueActionEntity;
import ro.allevo.fintpws.model.QueueTypeEntity;
import ro.allevo.fintpws.resources.QueueActionResource;
import ro.allevo.fintpws.resources.QueueTypeResource;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;

public class TestQueueTypeResource {

	static final String UNIT_TEST_TYPES = "unit HelloWorld";
	static final String QUEUE_TYPES_PATH = "api/queuetypes";
	static final String QUEUE_TYPES_PATH_WITH_SLASH = QUEUE_TYPES_PATH + "/";
	static WebResource wr;
	
	@BeforeClass
	public static void startMethod() throws JSONException {
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
		JSONObject queueTypes = null;
		do {
			findRecords = false;
			queueTypes = wr.path(QUEUE_TYPES_PATH)
					.queryParam("page", String.valueOf(page))
					.accept(MediaType.APPLICATION_JSON).get(JSONObject.class);
			JSONArray queueTypesArray = queueTypes.getJSONArray("queuetypes");
			for (int i = 0; i < queueTypesArray.length(); i++) {

				String typeName = queueTypesArray.getJSONObject(i).optString(
						"typename");
				if (typeName.startsWith("unit")) {
					TestQueueTypeResource.deleteTestQueueType(wr, typeName);
					findRecords = true;
				}
			}
			if (!findRecords) {
				page++;
			}
		} while (queueTypes.has("has_more"));
		TestQueueTypeResource.deleteTestQueueType(wr, "unit_test");
	}
	
	public static JSONObject createTestQueueType(WebResource wr, 
			String typename) throws JSONException {
		JSONObject queueType = new JSONObject().put("typename", typename);
		
		return TestHelper
				.assertResponseCreated(wr, "POST", QUEUE_TYPES_PATH, queueType);
	}
	
	public static JSONObject createTestQueueType(WebResource wr, 
			String typename, String lev1kword1) throws JSONException {
		JSONObject queueType = new JSONObject().put("typename", typename).put("lev1kword1", lev1kword1);
		
		return TestHelper
				.assertResponseCreated(wr, "POST", QUEUE_TYPES_PATH, queueType);
	}
	
	public static JSONObject findTestQueueType(WebResource wr, String typename)
			throws JSONException {
		ClientResponse clientResponse = wr.path(QUEUE_TYPES_PATH).path(typename)
				.accept(MediaType.APPLICATION_JSON)
				.type(MediaType.APPLICATION_JSON).get(ClientResponse.class);
		return (clientResponse.getClientResponseStatus()
				.equals(ClientResponse.Status.OK)) ? clientResponse
				.getEntity(JSONObject.class) : null;
	}

	public static JSONObject findRandomQueueType(WebResource wr)
			throws JSONException {

		JSONObject queueTypes = wr.path(QUEUE_TYPES_PATH).queryParam("filter", "tb")
				.accept(MediaType.APPLICATION_JSON).get(JSONObject.class);

		JSONArray queueTypesArray = queueTypes.getJSONArray("queuetypes");
		if(queueTypesArray.length() == 0){
			return null;
		}
		
		JSONObject queueType = null;
		do{
			 queueType = queueTypesArray.getJSONObject(
					new Random().nextInt(queueTypesArray.length()));
		}while (!queueType.getString("typename").startsWith("unit"));
		
		return queueType;
	}
	
	public static JSONObject readTestQueueType(WebResource wr, String typename)
			throws JSONException {
		return TestHelper.assertResponseOK(wr, "GET", QUEUE_TYPES_PATH_WITH_SLASH + typename,
				null);
	}

	public static void deleteTestQueueType(WebResource wr, String typename) {
		wr.path(QUEUE_TYPES_PATH_WITH_SLASH).path(typename).delete(ClientResponse.class);
	}
	
	@Test
	public void createQueueType() throws JSONException {
		TestHelper.assertResponseMethodNotAllowed(wr, "POST", QUEUE_TYPES_PATH_WITH_SLASH
				+ UNIT_TEST_TYPES, null);
	}
	
	@Test
	public void readQueueType() throws JSONException {
		// create a queue type with a random lev1kword1
		String qlev1kword1 = "unit q type lev" + new Random().nextInt();
		createTestQueueType(wr, UNIT_TEST_TYPES,qlev1kword1);

		// now read it
		JSONObject rj = readTestQueueType(wr, UNIT_TEST_TYPES);
		Assert.assertEquals("queue type lev1kword1 not correct", qlev1kword1,
				rj.getString("lev1kword1"));

		// make sure the metadata is returned
		Assert.assertEquals("href not correct", QUEUE_TYPES_PATH_WITH_SLASH
				+ UNIT_TEST_TYPES, rj.getString("href"));
		Assert.assertEquals("_type not correct", QueueTypeResource.class.getName(),
				rj.getString("_type"));

		// check 404 if the queue action with the stupid name is requested
		TestHelper.assertResponseNotFound(wr, "GET", QUEUE_TYPES_PATH_WITH_SLASH
				+ new Random().nextInt(), null);
	}
	
	@Test
	public void updateQueueTypeOptionalFields() throws JSONException {
		// test update description
		// create a queue type with a random typename
		createTestQueueType(wr, UNIT_TEST_TYPES);

		// change levkword to a new random one
		String lev1kword12 = "unit q type lev" + new Random().nextInt() ;
		JSONObject queueType = readTestQueueType(wr, UNIT_TEST_TYPES);
		queueType.remove("typename");
		queueType.put("lev1kword1", lev1kword12);
		TestHelper.assertResponseOK(wr, "PUT",
				QUEUE_TYPES_PATH_WITH_SLASH + UNIT_TEST_TYPES, queueType);

		// assert it worked
		JSONObject queueType2 = readTestQueueType(wr, UNIT_TEST_TYPES);
		Assert.assertEquals("queue type lev1kword1 not correct", lev1kword12,
				queueType2.getString("lev1kword1"));

		// test singular field update
		String lev1kword13 = "unit q type lev" + new Random().nextInt();
		JSONObject queueTypenewlev1kword1 = new JSONObject().put("lev1kword1", lev1kword13);
		TestHelper.assertResponseOK(wr, "PUT",
				QUEUE_TYPES_PATH_WITH_SLASH + UNIT_TEST_TYPES, queueTypenewlev1kword1);

		// assert it worked
		JSONObject queueType3 = readTestQueueType(wr, UNIT_TEST_TYPES);
		Assert.assertEquals("queue type lev1kword1 not correct", lev1kword13,
				queueType3.getString("lev1kword1"));
	}
	
	@Test
	public void updateQueueTypeLongName() throws JSONException {
		// create a queue type with a random typename
		createTestQueueType(wr, UNIT_TEST_TYPES);

		// update name
		JSONObject queueType = readTestQueueType(wr, UNIT_TEST_TYPES);
		queueType.remove("typename");
		queueType.put("typename", RandomStringUtils
				.randomAlphanumeric(101));
		TestHelper.assertResponseBadRequest(wr, "PUT", QUEUE_TYPES_PATH_WITH_SLASH + UNIT_TEST_TYPES,
				queueType);
	}
	
	@Test
	public void updateQueueTypeAllFields() throws JSONException {
		// generate a queue type (JSONObject format)
		JSONObject jsonEntity = TestUtils.fillResourceData(new JSONObject(),
				new QueueTypeEntity());
		jsonEntity.remove("typename");
		jsonEntity.remove("typeid");

		// create a queue type
		createTestQueueType(wr, UNIT_TEST_TYPES);

		// update all fields
		TestHelper.assertResponseOK(wr, "PUT", QUEUE_TYPES_PATH_WITH_SLASH
				+ UNIT_TEST_TYPES, jsonEntity);

		// now read it
		JSONObject queueType = readTestQueueType(wr, UNIT_TEST_TYPES);

		// assert it worked
		Assert.assertEquals("queue type fields not correct", TestUtils
				.compareJSONObjects(new QueueTypeEntity(), jsonEntity, queueType,
						"typename,typeid"), true);
	}
	

	@Test
	public void updateQueueTypeKeyFields() throws JSONException {
		// test update typename
		// create a queue type with a random name
		String newtypename = UNIT_TEST_TYPES + new Random().nextInt();
		createTestQueueType(wr, newtypename);

		// update typename
		JSONObject queueType = readTestQueueType(wr, newtypename);
		queueType.remove("typename");
		queueType.put("typename", UNIT_TEST_TYPES);
		TestHelper.assertResponseOK(wr, "PUT", QUEUE_TYPES_PATH_WITH_SLASH + newtypename, queueType);

		// assert it worked
		JSONObject queueType2 = readTestQueueType(wr, UNIT_TEST_TYPES);
		Assert.assertEquals("queue type typename not correct", UNIT_TEST_TYPES,
				queueType2.getString("typename"));

		// test typename conflict (409)
		// create a second queue type
		String queueType2typename = UNIT_TEST_TYPES + new Random().nextInt();
		createTestQueueType(wr, queueType2typename);

		
		// update to typename of the 1st queue type
		queueType2 = readTestQueueType(wr, queueType2typename);
		queueType2.remove("typename");
		queueType2.put("typename", UNIT_TEST_TYPES);
		TestHelper.assertResponseConflict(wr, "PUT",
				QUEUE_TYPES_PATH_WITH_SLASH + queueType2typename, queueType2);
	}

	@Test
	public void deleteQueueType() throws JSONException {
		// create a queue type with a random desc
		String typename = "unit test queue type desc" + new Random().nextInt();
		createTestQueueType(wr, UNIT_TEST_TYPES);

		JSONObject queueType = readTestQueueType(wr, UNIT_TEST_TYPES);
		
		// delete it
		TestHelper.assertResponseOK(wr, "DELETE", QUEUE_TYPES_PATH_WITH_SLASH
				+ UNIT_TEST_TYPES, null);

		// read it again to make sure it's gone
		TestHelper.assertResponseNotFound(wr, "GET", QUEUE_TYPES_PATH_WITH_SLASH
				+ UNIT_TEST_TYPES, null);

		// check 404 if the queue type with the stupid typename is requested
		TestHelper.assertResponseNotFound(wr, "DELETE", QUEUE_TYPES_PATH_WITH_SLASH
				+ new Random().nextInt(), null);
	}
	
}
