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
 * The persistent class for the STATUS database table.
 * 
 */
@Entity
@Table(schema = "FINDATA", name = "STATUS")
@NamedQueries({
		@NamedQuery(name = "StatusEntity.findByGuid", query = "SELECT s FROM StatusEntity s "
				+ "WHERE trim(s.guid) = :guid"),
		@NamedQuery(name = "StatusEntity.findByCorrelationId", query = "SELECT s FROM StatusEntity s "
				+ "WHERE trim(s.correlationid) = :correlationid order by s.insertdate asc, s.guid"),
		@NamedQuery(name = "StatusEntity.findAll", query = "SELECT s FROM StatusEntity s order by s.insertdate asc, s.guid"),
		@NamedQuery(name = "StatusEntity.findTotalByCorrelationId", query = "SELECT count(s.guid) FROM StatusEntity s "
				+ "WHERE trim(s.correlationid) = :correlationid"),
		@NamedQuery(name = "StatusEntity.findTotal", query = "SELECT count(s.guid) FROM StatusEntity s") })
@Cacheable(false)
public class StatusEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	@Column(length = 3000)
	private String additionalinfo;

	//@Column(name = "CLASS", length = 20)
	@Transient
	private String class_;

	@Column(nullable = false, length = 30)
	private String correlationid;

	@Column(nullable = false)
	private Timestamp eventdate;

	@Id
	@Column(nullable = false, length = 30)
	private String guid;

	@Column(length = 3500)
	private String innerexception;

	@Column(nullable = false)
	private Timestamp insertdate;

	@Column(nullable = false, length = 30)
	private String machine;

	@Column(nullable = false, length = 256)
	private String message;

	@Column(nullable = false, precision = 10)
	private BigDecimal service;

	//@Column(length = 30)
	@Transient
	private String sessionid;

	@Column(nullable = false, length = 20)
	private String type;

	public StatusEntity() {
	}

	public String getAdditionalinfo() {
		return this.additionalinfo;
	}

	public void setAdditionalinfo(String additionalinfo) {
		this.additionalinfo = additionalinfo;
	}

	public String getClass_() {
		return this.class_;
	}

	public void setClass_(String class_) {
		this.class_ = class_;
	}

	public String getCorrelationid() {
		return this.correlationid;
	}

	public void setCorrelationid(String correlationid) {
		this.correlationid = correlationid;
	}

	public Timestamp getEventdate() {
		return this.eventdate;
	}

	public void setEventdate(Timestamp eventdate) {
		this.eventdate = eventdate;
	}

	public String getGuid() {
		return this.guid;
	}

	public void setGuid(String guid) {
		this.guid = guid;
	}

	public String getInnerexception() {
		return this.innerexception;
	}

	public void setInnerexception(String innerexception) {
		this.innerexception = innerexception;
	}

	public Timestamp getInsertdate() {
		return this.insertdate;
	}

	public void setInsertdate(Timestamp insertdate) {
		this.insertdate = insertdate;
	}

	public String getMachine() {
		return this.machine;
	}

	public void setMachine(String machine) {
		this.machine = machine;
	}

	public String getMessage() {
		return this.message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public BigDecimal getService() {
		return this.service;
	}

	public void setService(BigDecimal service) {
		this.service = service;
	}

	public String getSessionid() {
		return this.sessionid;
	}

	public void setSessionid(String sessionid) {
		this.sessionid = sessionid;
	}

	public String getType() {
		return this.type;
	}

	public void setType(String type) {
		this.type = type;
	}

}
