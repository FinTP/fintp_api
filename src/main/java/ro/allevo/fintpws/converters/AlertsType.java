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

package ro.allevo.fintpws.converters;

import java.util.Locale;
import java.util.ResourceBundle;

public enum AlertsType {
	
	DEFAULT("This value is not allowed"), 
	CHECK(ResourceBundle.getBundle("MessagesBundle", Locale.getDefault()).getString("alert.type.checkQueue")), 
	MESSAGE(ResourceBundle.getBundle("MessagesBundle", Locale.getDefault()).getString("alert.type.newMessage"));
	
	private String name;

	private AlertsType(String name) {
        this.name = name;
	}
	
	public String getName() {
		return name;
	}
	
	public static AlertsType fromName(String name) {
		  for (AlertsType enumVal : AlertsType.values()) {
	        if (String.valueOf(name).equalsIgnoreCase(enumVal.name)) {
	          return enumVal;
	        }
	      }
	      return null;
	}
}
