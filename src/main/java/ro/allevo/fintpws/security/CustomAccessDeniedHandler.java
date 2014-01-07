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

package ro.allevo.fintpws.security;

import java.io.IOException;
import java.io.PrintWriter;

import javax.persistence.EntityNotFoundException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.web.client.ResourceAccessException;

import ro.allevo.fintpws.exceptions.ApplicationJsonException;
import ro.allevo.fintpws.util.JsonResponseWrapper;


public class CustomAccessDeniedHandler implements AccessDeniedHandler{

	private static String ERROR_FORBIDDEN = "resource forbidden";
	public CustomAccessDeniedHandler(){
	}
	
	@Produces(MediaType.APPLICATION_JSON)
	@Override
	public void handle(HttpServletRequest request, HttpServletResponse response,
			AccessDeniedException exception) throws IOException, ServletException {
		
		response.setContentType("application/json");
		response.setStatus(403);
		try {
			JSONObject obj = JsonResponseWrapper.getEntity(
					Status.FORBIDDEN.getStatusCode(), ERROR_FORBIDDEN, null,
					null);
			PrintWriter out = response.getWriter();
			out.print(obj);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

}
