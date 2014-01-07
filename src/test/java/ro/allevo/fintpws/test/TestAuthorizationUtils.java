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

import javax.ws.rs.core.MediaType;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import com.sun.jersey.api.client.WebResource;

public class TestAuthorizationUtils {

	/**
	 * Field ADMINISTRATOR_MAPPINGS_PATH (the url to queueu mappings for Admin role
	 * (value is "api/roles/Administrator/mappings/")
	 */
	static final String ADMINISTRATOR_MAPPINGS_PATH = "api/roles/Administrator/mappings/";
	/**
	 * Field ADMIN_ROLES_PATH.
	 * (value is "/api/users/admin/roles")
	 */
	static final String ADMIN_ROLES_PATH= "/api/users/admin/roles/";

	public static void allowAdminWriteAuthorityOnQueue(WebResource wr, String queue)
			throws JSONException {
		//check if the role isn't already mapped
		JSONObject mappings = wr.path(ADMINISTRATOR_MAPPINGS_PATH)
				.accept(MediaType.APPLICATION_JSON).get(JSONObject.class);
		
		
		// Map Administrator role to the test queue with "write" authority
		JSONObject mapping = new JSONObject();
		mapping.put("queuename", queue);
		mapping.put("actiontype", "RW");
		if(!checkIfQueueIsMappedToAdmin(wr, queue)){
			TestHelper.assertResponseCreated(wr, "POST",
					ADMINISTRATOR_MAPPINGS_PATH, mapping);
		}
		
	}
	
	public static void removeAdministratorWriteAuthorityQueue(WebResource wr,
			String queue) throws JSONException {
		TestHelper.assertResponseOK(wr, "DELETE", ADMINISTRATOR_MAPPINGS_PATH
				+ queue, null);
	}

	public static void giveAdministratorReportsRole(WebResource wr) throws JSONException{
		JSONObject reportsRole = new JSONObject();
		reportsRole.put("name", "Reports");
		TestHelper.assertResponseCreated(wr, "POST",
				ADMIN_ROLES_PATH, reportsRole);
	}

	public static void removeAdministratorReportsRole(WebResource wr) throws JSONException{
		TestHelper.assertResponseOK(wr, "DELETE", ADMIN_ROLES_PATH
				+ "Reports", null);
	}
	
	private static boolean checkIfQueueIsMappedToAdmin(WebResource wr, String searchedQueue) throws JSONException{
		int page = 1;
		JSONObject mappings = wr.path(ADMINISTRATOR_MAPPINGS_PATH)
				.accept(MediaType.APPLICATION_JSON).get(JSONObject.class);
		do{
			//TODO: first iteration mappings will be instantiated twice
			mappings = wr.path(ADMINISTRATOR_MAPPINGS_PATH)
					.accept(MediaType.APPLICATION_JSON).get(JSONObject.class);
			JSONArray mappingsArray = mappings.getJSONArray("mappings");
			for (int i = 0; i < mappingsArray.length(); i++) {
				String currentQueue = mappingsArray.getJSONObject(i).getString("queuename");
				if (currentQueue.equals(searchedQueue)) {
					return true;
				}
			}
			page++;
			
		} while (mappings.has("has_more"));
		return false;
	}
	
	
}
