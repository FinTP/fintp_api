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
 * The persistent class for the ROUTINGSCHEMAS database table.
 * 
 */
@Entity
@Table(schema = "FINCFG", name="ROUTINGSCHEMAS")
@NamedQueries({
	@NamedQuery(name = "RoutingSchemaEntity.findAll", query = "select rs from RoutingSchemaEntity rs"),
	@NamedQuery(name = "RoutingSchemaEntity.findTotal", query = "select count(rs.guid) from RoutingSchemaEntity rs"),
	@NamedQuery(name = "RoutingSchemaEntity.findByName", query = "select rs from RoutingSchemaEntity rs where trim(rs.name)=:name") })
@Cacheable(false)
public class RoutingSchemaEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(generator = "RsGuidGenerator")
	@TableGenerator(name = "RsGuidGenerator", table = "FINCFG.IDGENLIST",
			pkColumnName="TABCOLNAME", valueColumnName="IDVALUE",
			pkColumnValue="ROUTINGSCHEMAS_GUID")
	@Column(unique=true, nullable=false, precision=10)
	private long guid;

	@Column(nullable=false, precision=5)
	private BigDecimal active;

	@Column(length=250)
	private String description;

	@Column(length=1)
	private String isvisible;

	@Column(nullable=false, length=10)
	private String name;

	@Column(length=10)
	private String sessioncode;

	@Column(nullable=false, precision=10)
	private long startlimit;

	@Column(nullable=false, precision=10)
	private long stoplimit;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@PrimaryKeyJoinColumn(name = "STARTLIMIT")
	private TimeLimitEntity startLimitEntity;

	@ManyToOne(fetch = FetchType.LAZY)
	@PrimaryKeyJoinColumn(name = "STOPLIMIT")
	private TimeLimitEntity stopLimitEntity;
	
	public RoutingSchemaEntity() {
	}

	public long getGuid() {
		return this.guid;
	}

	public void setGuid(long guid) {
		this.guid = guid;
	}

	public BigDecimal getActive() {
		return this.active;
	}

	public void setActive(BigDecimal active) {
		this.active = active;
	}

	public String getDescription() {
		return this.description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getIsvisible() {
		return this.isvisible;
	}

	public void setIsvisible(String isvisible) {
		this.isvisible = isvisible;
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getSessioncode() {
		return this.sessioncode;
	}

	public void setSessioncode(String sessioncode) {
		this.sessioncode = sessioncode;
	}

	public TimeLimitEntity getStartLimitEntity() {
		return startLimitEntity;
	}

	public void setStartLimitEntity(TimeLimitEntity startLimitEntity) {
		this.startLimitEntity = startLimitEntity;
		this.startlimit = startLimitEntity.getGuid();
	}

	public TimeLimitEntity getStopLimitEntity() {
		return stopLimitEntity;
	}

	public void setStopLimitEntity(TimeLimitEntity stopLimitEntity) {
		this.stopLimitEntity = stopLimitEntity;
		this.stoplimit = stopLimitEntity.getGuid();
	}

}
