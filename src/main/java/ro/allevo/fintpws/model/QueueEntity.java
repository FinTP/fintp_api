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
import java.util.List;

/**
 * The persistent class for the QUEUES database table.
 * 
 */
@Entity
@Table(schema = "FINCFG", name = "QUEUES")
@NamedQueries({
		@NamedQuery(name = "QueueEntity.findAll", query = "select q from QueueEntity q order by q.name asc"),
		@NamedQuery(name = "QueueEntity.findTotal", query = "select count(q.name) from QueueEntity q"),
		@NamedQuery(name = "QueueEntity.findByName", query = "select q from QueueEntity q where trim(q.name)=:name")
		 })
public class QueueEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(generator="GuidGenerator")
	@TableGenerator(name="GuidGenerator", table="FINCFG.IDGENLIST",
	pkColumnName="TABCOLNAME", valueColumnName="IDVALUE",
	pkColumnValue="ROUTINGRULES_GUID") 
	
	@Column(unique = true, nullable = false)
	private BigDecimal guid;

	private BigDecimal batchno;

	private BigDecimal connector;

	@Column(length = 100)
	private String description;

	@Column(nullable = false)
	private BigDecimal holdstatus;

	@Column(nullable = false)
	private BigDecimal priority;
	
	@Column(nullable = false, length = 50)
	private String name;

	@Column(name="typeid", nullable = false)
	private long type;

	@OneToMany(targetEntity = QMovePrivMapEntity.class, cascade = { CascadeType.ALL }, fetch = FetchType.LAZY)
	@JoinColumn(name = "SOURCEQUEUEID", referencedColumnName = "GUID", insertable = false, updatable = false)
	private List<QMovePrivMapEntity> qmoveprivmapentity;
	
	@OneToMany(mappedBy="queueEntity")
	private List<QueuesRoleMapEntity> mappings;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@PrimaryKeyJoinColumn(name = "TYPEID")
	private QueueTypeEntity queueTypeEntity;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@PrimaryKeyJoinColumn(name = "CONNECTOR")
	private ServiceMapEntity serviceMapEntity;
	
	@OneToMany(mappedBy = "queueEntity")
	private List<RoutingRuleEntity> routingRules; 
	
	

	public List<QMovePrivMapEntity> getqMovePrivMapEntity() {
		return qmoveprivmapentity;
	}

	public void setqMovePrivMapEntity(List<QMovePrivMapEntity> qmoveprivmapentity) {
		this.qmoveprivmapentity = qmoveprivmapentity;
	}

	public QueueEntity() {
	}

	public BigDecimal getGuid() {
		return this.guid;
	}

	public void setGuid(BigDecimal guid) {
		this.guid = guid;
	}


	public BigDecimal getBatchno() {
		return this.batchno;
	}

	public void setBatchno(BigDecimal batchno) {
		this.batchno = batchno;
	}

	public BigDecimal getConnector() {
		return this.connector;
	}

	public void setConnector(BigDecimal connector) {
		this.connector = connector;
	}

	public String getDescription() {
		return this.description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public BigDecimal getHoldstatus() {
		return this.holdstatus;
	}

	public void setHoldstatus(BigDecimal holdstatus) {
		this.holdstatus = holdstatus;
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public long getType() {
		return type;
	}

	public void setType(long type) {
		this.type = type;
	}
	
	public BigDecimal getPriority() {
		return priority;
	}

	public void setPriority(BigDecimal priority) {
		this.priority = priority;
	}

	public QueueTypeEntity getQueueTypeEntity() {
		return queueTypeEntity;
	}

	public void setQueueTypeEntity(QueueTypeEntity queueTypeEntity) {
		this.queueTypeEntity = queueTypeEntity;
		this.type = queueTypeEntity.getTypeid();
	}

	public ServiceMapEntity getServiceMapEntity() {
		return serviceMapEntity;
	}

	public void setServiceMapEntity(ServiceMapEntity serviceMapEntity) {
		this.serviceMapEntity = serviceMapEntity;
		this.connector = serviceMapEntity.getServiceid();
	}
	



}
