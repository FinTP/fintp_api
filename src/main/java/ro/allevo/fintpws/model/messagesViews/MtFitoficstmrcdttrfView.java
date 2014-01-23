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
import java.util.Date;

import javax.persistence.*;


/**
 */
@Entity
@NamedQueries({
	@NamedQuery(name = "MtFitoficstmrcdttrfView.findByGuid", query="SELECT m FROM MtFitoficstmrcdttrfView m where m.guid=:guid"),// where m.receiver = :receiver and m.valuedate = :valuedate"),
	@NamedQuery(name = "MtFitoficstmrcdttrfView.findAll", query="SELECT m FROM MtFitoficstmrcdttrfView m"),// where m.receiver = :receiver and m.valuedate = :valuedate"),
	@NamedQuery(name = "MtFitoficstmrcdttrfView.findTotal", query = "SELECT count(m.guid) FROM MtFitoficstmrcdttrfView m"),// where m.receiver = :receiver and m.valuedate = :valuedate"),
	@NamedQuery(name = "MtFitoficstmrcdttrfView.findAllGroups", query="SELECT m.receiver,m.valuedate FROM MtFitoficstmrcdttrfView m GROUP BY m.receiver,m.valuedate"),
	@NamedQuery(name = "MtFitoficstmrcdttrfView.findTotalGroups", query = "SELECT count(m.receiver) FROM MtFitoficstmrcdttrfView m GROUP BY m.receiver,m.valuedate")
})
@Cacheable(false)
public class MtFitoficstmrcdttrfView implements Serializable {
	private static final long serialVersionUID = 1L;

	@Convert(converter = ro.allevo.fintpws.converters.AmountConverter.class)
	private Double amount;

	
	private String currency;

	@Id
	private String guid;

	private String msgtype;

	private String queuename;

	private String receiver;

	private String sender;

	private String trn;
	
	@Lob()
	@Column(nullable = false)
	private String payload;

	@Convert(converter = ro.allevo.fintpws.converters.DateConverter.class)
	private Date valuedate;

	public MtFitoficstmrcdttrfView() {
	}

	public Double getAmount() {
		return this.amount;
	}

	public void setAmount(Double amount) {
		this.amount = amount;
	}


	public String getCurrency() {
		return this.currency;
	}

	public void setCurrency(String currency) {
		this.currency = currency;
	}

	public String getGuid() {
		return this.guid;
	}

	public void setGuid(String guid) {
		this.guid = guid;
	}

	public String getMsgtype() {
		return this.msgtype;
	}

	public void setMsgtype(String msgtype) {
		this.msgtype = msgtype;
	}

	public String getQueuename() {
		return this.queuename;
	}

	public void setQueuename(String queuename) {
		this.queuename = queuename;
	}

	public String getReceiver() {
		return this.receiver;
	}

	public void setReceiver(String receiver) {
		this.receiver = receiver;
	}

	public String getSender() {
		return this.sender;
	}

	public void setSender(String sender) {
		this.sender = sender;
	}

	public String getTrn() {
		return this.trn;
	}

	public void setTrn(String trn) {
		this.trn = trn;
	}

	public Date getValuedate() {
		return this.valuedate;
	}

	public void setValuedate(Date valuedate) {
		this.valuedate = valuedate;
	}
	
	public String getPayload() {
		return payload;
	}

	public void setPayload(String payload) {
		this.payload = payload;
	}


}