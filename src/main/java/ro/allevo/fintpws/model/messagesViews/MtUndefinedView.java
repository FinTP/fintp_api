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

package ro.allevo.fintpws.model.messagesViews;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Timestamp;

import javax.persistence.Entity;
import javax.persistence.Id;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

@Entity
public class MtUndefinedView extends MtView implements Serializable{

	
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Id
	private String guid;
	private String batchid;
	private String requestorservice;
	private String correlationid;
	private String queuename;
	private String payload;
	

	public String getGuid(){
		return guid;
	}
	
	public String getBatchid(){
		return batchid;
	}
	
	public String getReuestorservice(){
		return requestorservice;
	}
	
	public String getCorrelationid(){
		return correlationid;
	}
	
	public String getQueuename(){
		return queuename;
	}
	
	public String getPayload(){
		return payload;
	}
	

	@Override
	public JSONObject toJSON() throws JSONException {
		JSONObject entityAsJson = new JSONObject();
		entityAsJson.put("guid", getGuid())
				.put("batchid", getBatchid())
				.put("requestorservice", getReuestorservice())
				.put("correlationid", getCorrelationid())
				.put("queuename", getQueuename())
				.put("payload", getPayload());
		return entityAsJson;
	}
	
}
