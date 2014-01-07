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
 * The persistent class for the ROUTINGRULES database table.
 * 
 */
@Entity
@Table(schema = "FINCFG", name="ROUTINGRULES")
@NamedQueries({
	@NamedQuery(name = "RoutingRuleEntity.findByGuid", query = "SELECT r FROM RoutingRuleEntity r "
			+ "WHERE r.guid=:guid"),
	@NamedQuery(name = "RoutingRuleEntity.findAll", query = "SELECT r FROM RoutingRuleEntity r ORDER BY r.guid asc"),
	@NamedQuery(name = "RoutingRuleEntity.findTotal", query = "SELECT count(r.guid) FROM RoutingRuleEntity r"),
	@NamedQuery(name = "RoutingRuleEntity.findAllRoutingSchema", query = "SELECT r FROM RoutingRuleEntity r " +
			"WHERE r.schemaguid=:schemaguid ORDER BY r.guid asc"),
	@NamedQuery(name = "RoutingRuleEntity.findTotalRoutingSchema", query = "SELECT count(r.guid) FROM RoutingRuleEntity r "+
			"WHERE r.schemaguid=:schemaguid")
	
})
@Cacheable(false)
public class RoutingRuleEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(generator="GuidGenerator")
	@TableGenerator(name="RRGuidGenerator", table="FINCFG.IDGENLIST",
	pkColumnName="TABCOLNAME", valueColumnName="IDVALUE",
	pkColumnValue="ROUTINGRULES_GUID") 
	@Column(unique=true, nullable=false)
	private long guid;

	@Column(nullable=false, length=500)
	private String action;

	@Column(length=70)
	private String description;

	@Column(name="funccond",length=500)
	private String functioncondition; 

	@Column(name="metacond",length=500)
	private String metadatacondition;

	@Column(name="msgcond",length=500)
	private String messagecondition;

	@Column(nullable=false)
	private BigDecimal queueid;

	@Column(nullable=false)
	private BigDecimal ruletype;

	@Column(nullable=false)
	private long schemaguid;

	@Column(nullable=false)
	private BigDecimal sequence;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@PrimaryKeyJoinColumn(name = "QUEUEID")
	private QueueEntity queueEntity;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@PrimaryKeyJoinColumn(name="SCHEMAGUID")
	private RoutingSchemaEntity routingSchemaEntity;

	public RoutingRuleEntity() {
	}

	public long getGuid() {
		return this.guid;
	}

	public void setGuid(long guid) {
		this.guid = guid;
	}

	public String getAction() {
		return this.action;
	}

	public void setAction(String action) {
		this.action = action;
	}

	public String getDescription() {
		return this.description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getFunctioncondition() {
		return functioncondition;
	}

	public void setFunctioncondition(String functioncondition) {
		this.functioncondition = functioncondition;
	}

	public BigDecimal getQueueid() {
		return this.queueid;
	}

	public void setQueueid(BigDecimal queueid) {
		this.queueid = queueid;
	}

	public BigDecimal getRuletype() {
		return this.ruletype;
	}

	public String getMessagecondition() {
		return messagecondition;
	}

	public void setMessagecondition(String messagecondition) {
		this.messagecondition = messagecondition;
	}
	
	public String getMetadatacondition() {
		return metadatacondition;
	}

	public void setMetadatacondition(String metadatacondition) {
		this.metadatacondition = metadatacondition;
	}

	public void setRuletype(BigDecimal ruletype) {
		this.ruletype = ruletype;
	}

	public long getSchemaguid() {
		return this.schemaguid;
	}

	public void setSchemaguid(long schemaguid) {
		this.schemaguid = schemaguid;
	}

	public BigDecimal getSequence() {
		return this.sequence;
	}

	public void setSequence(BigDecimal sequence) {
		this.sequence = sequence;
	}

	public QueueEntity getQueueEntity() {
		return queueEntity;
	}

	public void setQueueEntity(QueueEntity queueEntity) {
		this.queueEntity = queueEntity;
		this.queueid = queueEntity.getGuid();
	}

	public RoutingSchemaEntity getRoutingSchemaEntity() {
		return routingSchemaEntity;
	}

	public void setRoutingSchemaEntity(RoutingSchemaEntity routingSchemaEntity) {
		this.routingSchemaEntity = routingSchemaEntity;
		this.schemaguid = routingSchemaEntity.getGuid();
	}

}
