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
 * The persistent class for the ROUTINGJOBS database table.
 * 
 */
@Entity
@Table(schema = "FINDATA", name="ROUTINGJOBS")
@NamedQueries({
	@NamedQuery(name = "RoutingJobEntity.findAll", query = "select q from RoutingJobEntity q order by q.guid asc"),
	@NamedQuery(name = "RoutingJobEntity.findTotal", query = "select count(q.guid) from RoutingJobEntity q"),
	@NamedQuery(name = "RoutingJobEntity.findByGuid", query = "select q from RoutingJobEntity q where trim(q.guid)=:guid") })
@Cacheable(false)
public class RoutingJobEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(generator = "RoutingJobsGuidGenerator")
	@TableGenerator(name = "RoutingJobsGuidGenerator", table = "FINCFG.IDGENLIST", pkColumnName = "TABCOLNAME", valueColumnName = "IDVALUE", pkColumnValue = "ROUTINGJOBS_GUID")
	@Column(unique=true, nullable=false, length=30)
	private String guid;

	@Column(nullable=false, precision=22)
	private BigDecimal backout;

	@Column(nullable=false, length=200)
	private String function;

	@Column(nullable=false, precision=22)
	private BigDecimal priority;

	@Column(length=50)
	private String routingpoint;

	@Column(nullable=false, precision=22)
	private BigDecimal status;

	@Column(length=5)
	private long userid;

	public RoutingJobEntity() {
	}

	public String getGuid() {
		return this.guid;
	}

	public void setGuid(String guid) {
		this.guid = guid;
	}

	public BigDecimal getBackout() {
		return this.backout;
	}

	public void setBackout(BigDecimal backout) {
		this.backout = backout;
	}

	public String getFunction() {
		return this.function;
	}

	public void setFunction(String function) {
		this.function = function;
	}

	public BigDecimal getPriority() {
		return this.priority;
	}

	public void setPriority(BigDecimal priority) {
		this.priority = priority;
	}

	public String getRoutingpoint() {
		return this.routingpoint;
	}

	public void setRoutingpoint(String routingpoint) {
		this.routingpoint = routingpoint;
	}

	public BigDecimal getStatus() {
		return this.status;
	}

	public void setStatus(BigDecimal status) {
		this.status = status;
	}

	public long getUserid() {
		return this.userid;
	}

	public void setUserid(long userid) {
		this.userid = userid;
	}

	@Override
	public String toString(){
		String res = "";
		res+=getGuid();
		return res;
	}
}
