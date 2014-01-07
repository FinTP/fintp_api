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

package ro.allevo.fintpws.resources;

import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.core.Application;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * 
 * @author horia
 * @version $Revision: 1.0 $
 */
public class MyApplication extends Application {
	/**
	 * Field logger.
	 */
	private static Logger logger = LogManager.getLogger(MyApplication.class
			.getName());

	/**
	 * Registers root resource ( api entry point )
	 * @return Set<Class<?>>
	 */
	@Override
	public Set<Class<?>> getClasses() {
		logger.debug("Entering Application.getClasses()");
		final Set<Class<?>> classes = new HashSet<Class<?>>();

		// register root resource
		classes.add(ApiResource.class);

		logger.debug("Exiting Application.getClasses()");
		return classes;
	}
}
