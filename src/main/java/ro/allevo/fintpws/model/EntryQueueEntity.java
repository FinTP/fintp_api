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
import java.math.BigDecimal;

/**
 * The persistent class for the ENTRYQUEUE database table.
 * 
 */
@Entity
@Table(schema = "FINDATA", name = "ENTRYQUEUE")
@NamedQueries({
		@NamedQuery(name = "EntryQueueEntity.findAllQueue", query = "SELECT b FROM EntryQueueEntity b "
				+ " left join b.routedmessage r"
				+ " where b.queuename=:queuename order by b.guid "),
		@NamedQuery(name = "EntryQueueEntity.findByGuid", query = "SELECT b FROM EntryQueueEntity b "
				+ "WHERE trim(b.guid) = :guid"),
		@NamedQuery(name = "EntryQueueEntity.findTotalQueue", query = "SELECT count(b) FROM EntryQueueEntity b "
				+ "WHERE b.queuename=:queuename"),
		@NamedQuery(name = "EntryQueueEntity.findDistinctMessagesQueue", query = "SELECT distinct r.msgtype FROM EntryQueueEntity b "
				+ " join b.routedmessage r where b.queuename= :queuename"),
		@NamedQuery(name = "EntryQueueEntity.findTotalDistinctMessagesQueue", query = "SELECT count(distinct r.msgtype)   FROM EntryQueueEntity b "
				+ " join b.routedmessage r where b.queuename= :queuename"),
		@NamedQuery(name = "EntryQueueEntity.findGroupMessagesQueue", query = "SELECT distinct r.msgtype FROM EntryQueueEntity b "
				+ " join b.routedmessage r where b.queuename= :queuename") })

@Cacheable(false)
public class EntryQueueEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@Column(unique = true, nullable = false, length = 30)
	private String guid;

	@Column(length = 35)
	private String batchid;

	@Column(unique = true, nullable = false, length = 30)
	private String correlationid;

	@Column(length = 40)
	private String feedback;

	@Column(nullable = false)
	private BigDecimal holdstatus;

	@Lob()
	@Column(nullable = false)
	private String payload;

	private BigDecimal priority;

	@Column(nullable = false, length = 30)
	private String requestorservice;

	@Column(nullable = false, length = 30)
	private String requesttype;

	@Column(length = 30)
	private String responderservice;

	@Column(nullable = false)
	private BigDecimal sequence;

	@Column(length = 30)
	private String sessionid;

	@Transient
	private BigDecimal status;

	@OneToOne(targetEntity = RoutedMessageEntity.class, cascade = { CascadeType.ALL }, fetch = FetchType.LAZY)
	@JoinColumn(name = "CORRELATIONID", referencedColumnName = "CORRELATIONID", insertable = false, updatable = false)
	private RoutedMessageEntity routedmessage;

	public RoutedMessageEntity getRoutedmessage() {
		return routedmessage;
	}

	public void setRoutedmessage(RoutedMessageEntity routedmessage) {
		this.routedmessage = routedmessage;
	}

	@Column(length = 35)
	private String queuename;

	public EntryQueueEntity() {
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

	public String getPayload() {
		return this.payload;
	}

	public void setPayload(String payload) {
		this.payload = payload;
	}

	public BigDecimal getPriority() {
		return this.priority;
	}

	public void setPriority(BigDecimal priority) {
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

	public BigDecimal getStatus() {
		return this.status;
	}

	public void setStatus(BigDecimal status) {
		this.status = status;
	}

	public String getQueuename() {
		return queuename;
	}

	public void setQueuename(String queuename) {
		this.queuename = queuename;
	}

}