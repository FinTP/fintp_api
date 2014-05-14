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
