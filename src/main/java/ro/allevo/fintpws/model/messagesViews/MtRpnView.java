package ro.allevo.fintpws.model.messagesViews;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Timestamp;

import javax.persistence.Entity;
import javax.persistence.Id;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

@Entity
public class MtRpnView extends MtView implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5726304439447521291L;
	
	@Id
	private String guid;
	private String msgtype;
	private String sender;
	private String receiver;
	private String trn;
	private Timestamp insertdate;
	private BigDecimal amount;
	private String currency;
	private String origbatchid;
	private String queuename;
	private String payload;
	


	public String getMsgtype() {
		return msgtype;
	}

	public void setMsgtype(String msgtype) {
		this.msgtype = msgtype;
	}

	public String getSender() {
		return sender;
	}

	public void setSender(String sender) {
		this.sender = sender;
	}

	public String getReceiver() {
		return receiver;
	}

	public void setReceiver(String receiver) {
		this.receiver = receiver;
	}

	public String getTrn() {
		return trn;
	}

	public void setTrn(String trn) {
		this.trn = trn;
	}

	public Timestamp getInsertdate() {
		return insertdate;
	}

	public void setInsertdate(Timestamp insertdate) {
		this.insertdate = insertdate;
	}

	public BigDecimal getAmount() {
		return amount;
	}

	public void setAmount(BigDecimal amount) {
		this.amount = amount;
	}

	public String getCurrency() {
		return currency;
	}

	public void setCurrency(String currency) {
		this.currency = currency;
	}

	public String getOrigbatchid() {
		return origbatchid;
	}

	public void setOrigbatchid(String origbatchid) {
		this.origbatchid = origbatchid;
	}

	public String getQueuename() {
		return queuename;
	}

	public void setQueuename(String queuename) {
		this.queuename = queuename;
	}

	public String getPayload() {
		return payload;
	}

	public void setPayload(String payload) {
		this.payload = payload;
	}
	
	@Override
	public JSONObject toJSON() throws JSONException {
		JSONObject entityAsJson = new JSONObject();
		entityAsJson.put("guid", guid)
			.put("messagetype", getMsgtype())
			.put("sender", getSender())
			.put("receiver", getReceiver())
			.put("trn", getTrn())
			.put("insertdate", getInsertdate())
			.put("origbatchid", getOrigbatchid())
			.put("amount", getAmount())
			.put("currency", getCurrency())
			.put("queuename", getQueuename())
			.put("payload", getPayload());
		return entityAsJson;
	}

	

}
