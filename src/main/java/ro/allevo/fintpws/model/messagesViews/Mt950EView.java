package ro.allevo.fintpws.model.messagesViews;

import java.io.Serializable;

import javax.persistence.Id;
import javax.persistence.Transient;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

public class Mt950EView extends MtView implements Serializable  {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String insertdate;
	@Id
	private String  trn;
	private String  valuedate;
	private String  amount;
	private String currency;
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
		entityAsJson.put("insertdate", getInsertdate())
				.put("trn", getTrn())
				.put("valuedate", getValuedate())
				.put("amount", getAmount())
				.put("currency", getCurrency());
		return entityAsJson;
	}
}
