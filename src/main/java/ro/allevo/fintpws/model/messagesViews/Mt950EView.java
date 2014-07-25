package ro.allevo.fintpws.model.messagesViews;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Transient;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

@Entity
public class Mt950EView extends MtView implements Serializable  {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private String guid;
	private String msgtype;
	@Id
	private String stmtuid;
	private String insertdate;
	private String  trn;
	private String  valuedate;
	private String  amount;
	private String currency;
	private String queuename;
	private String payload;
	
	public String getGuid() {
		return guid;
	}

	public void setGuid(String guid) {
		this.guid = guid;
	}
	
	public String getStmtuid() {
		return stmtuid;
	}
	public void setStmtuid(String stmtuid) {
		this.stmtuid = stmtuid;
	}
	public String getMsgtype() {
		return msgtype;
	}
	public void setMsgtype(String msgtype) {
		this.msgtype = msgtype;
	}
	public String getPayload() {
		return payload;
	}
	public void setPayload(String payload) {
		this.payload = payload;
	}
	public String getInsertdate() {
		return insertdate;
	}
	public void setInsertdate(String insertdate) {
		this.insertdate = insertdate;
	}
	public String getTrn() {
		return trn;
	}
	public void setTrn(String trn) {
		this.trn = trn;
	}
	public String getValuedate() {
		return valuedate;
	}
	public void setValuedate(String valuedate) {
		this.valuedate = valuedate;
	}
	public String getAmount() {
		return amount;
	}
	public void setAmount(String amount) {
		this.amount = amount;
	}
	public String getCurrency() {
		return currency;
	}
	public void setCurrency(String currency) {
		this.currency = currency;
	}
	
	@Override
	@Transient
	public JSONObject toJSON() throws JSONException {
		JSONObject entityAsJson = new JSONObject();
		entityAsJson.put("insertdate", getInsertdate()).put("guid", getGuid())
				.put("trn", getTrn()).put("msgtype", getMsgtype())
				.put("valuedate", getValuedate()).put("stmtuid", getStmtuid())
				.put("amount", getAmount()).put("queuename", getQueuename())
				.put("currency", getCurrency()).put("payload", getPayload());
		return entityAsJson;
	}
	public String getQueuename() {
		return queuename;
	}
	public void setQueuename(String queuename) {
		this.queuename = queuename;
	}
}
