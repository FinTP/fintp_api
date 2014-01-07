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

package ro.allevo.fintpws.model;

import java.io.Serializable;
import javax.persistence.*;
import java.sql.Timestamp;
import java.math.BigDecimal;


/**
 * The persistent class for the HISTORY database table.
 * 
 */
@Entity
@Table(schema = "FINDATA", name="HISTORY")
@NamedQueries({
	@NamedQuery(name = "HistoryEntity.findAll", query = "select q from HistoryEntity q order by q.guid asc"),
	@NamedQuery(name = "HistoryEntity.findTotal", query = "select count(q.guid) from HistoryEntity q"),
	@NamedQuery(name = "HistoryEntity.findByGuid", query = "select q from HistoryEntity q where trim(q.guid)=:guid") })
@Cacheable(false)
public class HistoryEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(generator = "HistoryGuidGenerator")
	@TableGenerator(name = "HistoryGuidGenerator", table = "FINCFG.IDGENLIST", pkColumnName = "TABCOLNAME", valueColumnName = "IDVALUE", pkColumnValue = "HISTORY_GUID")
	@Column(unique = true, nullable = false, precision = 22)
	private String guid;

	@Column(length=35)
	private String batchid;

	@Column(nullable=false, length=30)
	private String correlationid;

	@Column(length=40)
	private String feedback;

	@Column(nullable=false, precision=22)
	private BigDecimal holdstatus;

	private Timestamp insertdate;

	@Lob
	@Column(nullable=false)
	private String payload;

	@Column(nullable=false, length=10)
	private long priority;

	@Column(nullable=false, length=30)
	private String requestorservice;

	@Column(nullable=false, length=30)
	private String requesttype;

	@Column(length=30)
	private String responderservice;

	@Column(precision=10)
	private BigDecimal sequence;

	@Column(length=30)
	private String sessionid;

	public HistoryEntity() {
	}

	public String getGuid() {
		return this.guid;
	}

	public void setGuid(String guid) {
		this.guid = guid;
	}

	public String getBatchid() {
		return this.batchid;
	}

	public void setBatchid(String batchid) {
		this.batchid = batchid;
	}

	public String getCorrelationid() {
		return this.correlationid;
	}

	public void setCorrelationid(String correlationid) {
		this.correlationid = correlationid;
	}

	public String getFeedback() {
		return this.feedback;
	}

	public void setFeedback(String feedback) {
		this.feedback = feedback;
	}

	public BigDecimal getHoldstatus() {
		return this.holdstatus;
	}

	public void setHoldstatus(BigDecimal holdstatus) {
		this.holdstatus = holdstatus;
	}

	public Timestamp getInsertdate() {
		return this.insertdate;
	}

	public void setInsertdate(Timestamp insertdate) {
		this.insertdate = insertdate;
	}

	public String getPayload() {
		return this.payload;
	}

	public void setPayload(String payload) {
		this.payload = payload;
	}

	public long getPriority() {
		return this.priority;
	}

	public void setPriority(long priority) {
		this.priority = priority;
	}

	public String getRequestorservice() {
		return this.requestorservice;
	}

	public void setRequestorservice(String requestorservice) {
		this.requestorservice = requestorservice;
	}

	public String getRequesttype() {
		return this.requesttype;
	}

	public void setRequesttype(String requesttype) {
		this.requesttype = requesttype;
	}

	public String getResponderservice() {
		return this.responderservice;
	}

	public void setResponderservice(String responderservice) {
		this.responderservice = responderservice;
	}

	public BigDecimal getSequence() {
		return this.sequence;
	}

	public void setSequence(BigDecimal sequence) {
		this.sequence = sequence;
	}

	public String getSessionid() {
		return this.sessionid;
	}

	public void setSessionid(String sessionid) {
		this.sessionid = sessionid;
	}
	
	@Override
	public String toString(){
		String res = "";
		res+=getGuid();
		return res;
	}

}
