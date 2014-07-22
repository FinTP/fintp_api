package ro.allevo.fintpws.model.messagesViews;

import java.io.Serializable;
import java.math.BigInteger;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Transient;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

@Entity
public class Mt950View extends MtView implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6477110253900706590L;
	private String receiver;
	private String accnumber;
	private String stmtnumber;
	private String valuedate;
	private String obamount;
	private String cbamount;
	private String currency;
	private String txno;
	private BigInteger expno;
	@Id
	private String stmtuid;
	private String queuename;

	public String getReceiver() {
		return receiver;
	}

	public void setReceiver(String receiver) {
		this.receiver = receiver;
	}

	public String getAccnumber() {
		return accnumber;
	}

	public void setAccnumber(String accnumber) {
		this.accnumber = accnumber;
	}

	public String getValuedate() {
		return valuedate;
	}

	public void setValuedate(String valuedate) {
		this.valuedate = valuedate;
	}

	public String getStmtnumber() {
		return stmtnumber;
	}

	public void setStmtnumber(String stmtnumber) {
		this.stmtnumber = stmtnumber;
	}

	public String getObamount() {
		return obamount;
	}

	public void setObamount(String obamount) {
		this.obamount = obamount;
	}

	public String getCbamount() {
		return cbamount;
	}

	public void setCbamount(String cbamount) {
		this.cbamount = cbamount;
	}

	public String getCurrency() {
		return currency;
	}

	public void setCurrency(String currency) {
		this.currency = currency;
	}

	public String getTxno() {
		return txno;
	}

	public void setTxno(String txno) {
		this.txno = txno;
	}

	public BigInteger getExpno() {
		return expno;
	}

	public void setExpno(BigInteger expno) {
		this.expno = expno;
	}

	public String getStmtuid() {
		return stmtuid;
	}

	public void setStmtuid(String stmtuid) {
		this.stmtuid = stmtuid;
	}

	public String getQueuename() {
		return queuename;
	}

	public void setQueuename(String queuename) {
		this.queuename = queuename;
	}

	@Override
	@Transient
	public JSONObject toJSON() throws JSONException {
		JSONObject entityAsJson = new JSONObject();
		entityAsJson.put("receiver", getReceiver())
				.put("accnumber", getAccnumber())
				.put("stmtnumber", getStmtnumber())
				.put("valuedate", getValuedate())
				.put("obamount", getObamount()).put("cbamount", getCbamount())
				.put("currency", getCurrency()).put("txno", getTxno())
				.put("queuename", getQueuename()).put("expno", getExpno())
				.put("stmtuid", getStmtuid());
		return entityAsJson;
	}
}
