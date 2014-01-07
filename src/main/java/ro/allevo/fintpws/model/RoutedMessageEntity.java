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
import java.util.List;
import java.math.BigDecimal;

/**
 * The persistent class for the ROUTEDMESSAGES database table.
 * 
 */
@Entity
@Table(schema = "FINDATA", name = "ROUTEDMESSAGES")
@NamedQueries({
		@NamedQuery(name = "RoutedMessageEntity.findByGuid", query = "SELECT b FROM RoutedMessageEntity b "
				+ "WHERE trim(b.guid) = :guid"),
		@NamedQuery(name = "RoutedMessageEntity.findByCorrelId", query = "SELECT b FROM RoutedMessageEntity b "
				+ "WHERE trim(b.correlationid) = :correlationid"),
		@NamedQuery(name = "RoutedMessageEntity.findTotalQueue", query = "SELECT count(b.guid) FROM RoutedMessageEntity b"
				+ " join b.entryqueue a where b.currentqueue=:queueid "),
		@NamedQuery(name = "RoutedMessageEntity.findTotalFeedbackagg", query = "SELECT count(b.guid) FROM RoutedMessageEntity b"),
		@NamedQuery(name = "RoutedMessageEntity.findAll", query = "SELECT b FROM RoutedMessageEntity b order by b.insertdate, b.guid"),
		@NamedQuery(name = "RoutedMessageEntity.findAllQueue", query = "SELECT b FROM RoutedMessageEntity b "
				+ "where b.currentqueue=:queueid order by b.insertdate, b.guid ") })
//By default EclipseLink enables a shared object cache to cache objects read from the database to avoid repeated 
//database access. If the database is changed directly through JDBC, or by another application or server, 
//the objects in the shared cache will be stale.
@Cacheable(false)
public class RoutedMessageEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@Column(unique = true, nullable = false, length = 30)
	private String guid;

	@Transient
	private BigDecimal ack;

	@Column(unique = true, length = 30)
	private String correlationid;

	private BigDecimal currentqueue;

	private Timestamp insertdate;

	@Column(length = 50)
	private String msgtype;

	@Column(length = 35)
	private String receiver;

	//@Column(length = 35)
	@Transient
	private String receiverapp;

	@Column(length = 35)
	private String sender;

	//@Column(length = 35)
	@Transient
	private String senderapp;

	@Column(length = 35, nullable=false)
	private String trn;

	@Transient
	private BigDecimal userid;

	// bi-directional one-to-one association to EntryQueueEntity
	// EAGER Fetch  get results in one query ( parent and child )
	// LAZY Fetch  get results as sub-query .

	@OneToOne(targetEntity = EntryQueueEntity.class, cascade = { CascadeType.ALL }, fetch = FetchType.LAZY)
	@JoinColumn(name = "CORRELATIONID", referencedColumnName = "CORRELATIONID", insertable = false, updatable = false)
	private EntryQueueEntity entryqueue;

	@OneToMany(targetEntity = StatusEntity.class, fetch = FetchType.LAZY)
	@JoinColumn(name = "CORRELATIONID", referencedColumnName = "CORRELATIONID")
	private List<StatusEntity> status;
/*
	@OneToOne(targetEntity = FeedbackaggEntity.class, cascade = { CascadeType.ALL }, fetch = FetchType.LAZY)
	@JoinColumn(name = "CORRELID", referencedColumnName = "CORRELID", insertable = false, updatable = false)
	private FeedbackaggEntity feedbackagg;
*/
	private BigDecimal amount;
	
	public EntryQueueEntity getEntryQueue() {
		return entryqueue;
	}

	public void setEntryQueue(EntryQueueEntity entryqueue) {
		this.entryqueue = entryqueue;
	}

	public List<StatusEntity> getStatus() {
		return status;
	}

	public void setStatus(List<StatusEntity> status) {
		this.status = status;
	}
	
	
/*
	public FeedbackaggEntity getFeedbackagg() {
		return feedbackagg;
	}

	public void setFeedbackagg(FeedbackaggEntity feedbackagg) {
		this.feedbackagg = feedbackagg;
	}*/

	public RoutedMessageEntity() {
	}

	public String getGuid() {
		return this.guid;
	}

	public void setGuid(String guid) {
		this.guid = guid;
	}

	public BigDecimal getAck() {
		return this.ack;
	}

	public void setAck(BigDecimal ack) {
		this.ack = ack;
	}

	public String getCorrelationid() {
		return this.correlationid;
	}

	public void setCorrelationid(String correlationid) {
		this.correlationid = correlationid;
	}

	public BigDecimal getCurrentqueue() {
		return this.currentqueue;
	}

	public void setCurrentqueue(BigDecimal currentqueue) {
		this.currentqueue = currentqueue;
	}

	public Timestamp getInsertdate() {
		return this.insertdate;
	}

	public void setInsertdate(Timestamp insertdate) {
		this.insertdate = insertdate;
	}

	public String getMsgtype() {
		return this.msgtype;
	}

	public void setMsgtype(String msgtype) {
		this.msgtype = msgtype;
	}

	public String getReceiver() {
		return this.receiver;
	}

	public void setReceiver(String receiver) {
		this.receiver = receiver;
	}

	public String getReceiverapp() {
		return this.receiverapp;
	}

	public void setReceiverapp(String receiverapp) {
		this.receiverapp = receiverapp;
	}

	public String getSender() {
		return this.sender;
	}

	public void setSender(String sender) {
		this.sender = sender;
	}

	public String getSenderapp() {
		return this.senderapp;
	}

	public void setSenderapp(String senderapp) {
		this.senderapp = senderapp;
	}

	public String getTrn() {
		return this.trn;
	}

	public void setTrn(String trn) {
		this.trn = trn;
	}

	public BigDecimal getUserid() {
		return this.userid;
	}

	public void setUserid(BigDecimal userid) {
		this.userid = userid;
	}

	public BigDecimal getAmount() {
		return amount;
	}

	public void setAmount(BigDecimal amount) {
		this.amount = amount;
	}

}
