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

package ro.allevo.fintpws.exceptions;

import javax.persistence.RollbackException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import ro.allevo.fintpws.util.JsonResponseWrapper;

/**
 * Intercepts the javax.persistence.RollbackException and returns the standard JSON response body.
 */
@Provider
public class RollbackExceptionMapper implements ExceptionMapper<RollbackException> {
	
    /**
     * Method toResponse.
     * @param ex RollbackException
     * @return Response
     */
    public Response toResponse(RollbackException ex) {
	 	return JsonResponseWrapper.getResponse(Response.Status.INTERNAL_SERVER_ERROR, ex.getMessage());
	}
}
