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

import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;

import javax.persistence.RollbackException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.logging.log4j.Logger;
import org.eclipse.persistence.exceptions.ValidationException;
import org.postgresql.util.PSQLException;

import ro.allevo.fintpws.util.JsonResponseWrapper;

/**
 * Class that holds the required response fields ( message, code ). Used by the
 * custom exception mappers to fill the response body.
 */
@XmlRootElement(name = "error")
public class ApplicationJsonException extends WebApplicationException {
	private static final long serialVersionUID = 1L;

	public ApplicationJsonException() {
		super();
	}

	/**
	 * Constructor for ApplicationJsonException.
	 * 
	 * @param message
	 *            String
	 * @param code
	 *            int
	 */
	public ApplicationJsonException(Throwable cause, String message, int code) {
		super(cause, JsonResponseWrapper.getResponse(
				Response.Status.fromStatusCode(code), message, null, null));
	}

	public static void handleSQLException(RollbackException re,
			String errorContext, Logger logger) {
		// traverse the cause to find a possible constraint violation
		Throwable t = re.getCause();
		
		while (null != t) {
			if (t instanceof PSQLException) {
				final String sqlState = ((SQLException) t).getSQLState();
				logger.error(errorContext + t.getMessage(), t);
				//check error belongs to integrity constraint class
				if(sqlState.startsWith("23")){
					throw new ApplicationJsonException(re, errorContext
							+ t.getMessage(),
							Response.Status.CONFLICT.getStatusCode());
				}else{
					throw new ApplicationJsonException(re, errorContext
							+ t.getMessage(),
							Response.Status.BAD_REQUEST.getStatusCode());
		
				}
			}
			
			if (t instanceof SQLIntegrityConstraintViolationException) {
				logger.error(errorContext + t.getMessage(), t);
				throw new ApplicationJsonException(re, errorContext
						+ t.getMessage(),
						Response.Status.CONFLICT.getStatusCode());
			}
			// TODO: check if necessary to go one level lower (instanceof
			// IntegrityException, or DescriptorExcettion, etc)
			if(t instanceof ValidationException){
				logger.error(errorContext + t.getMessage(), t);
				throw new ApplicationJsonException(re, errorContext
						+ t.getMessage(),
						Response.Status.CONFLICT.getStatusCode());
			}
			
			if (t instanceof SQLException) {
				logger.error(errorContext + t.getMessage(), t);
				throw new ApplicationJsonException(re, errorContext
						+ t.getMessage(),
						Response.Status.BAD_REQUEST.getStatusCode());
			}
			t = t.getCause();
		}
	}
}
