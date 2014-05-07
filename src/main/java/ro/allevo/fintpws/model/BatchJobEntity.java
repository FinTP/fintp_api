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
 * The persistent class for the batchjobs database table.
 * 
 */
@Entity
@Table(schema = "FINDATA", name = "BATCHJOBS")
@NamedQueries({
		@NamedQuery(name = "BatchJobEntity.findByGuid", query = "SELECT r FROM BatchJobEntity r "
				+ "WHERE r.combatchid=:id"),
		@NamedQuery(name = "BatchJobEntity.findByBatchID", query = "SELECT r FROM BatchJobEntity r "
				+ "WHERE r.batchuid=:id"),
		@NamedQuery(name = "BatchJobEntity.findAll", query = "SELECT r FROM BatchJobEntity r ORDER BY r.combatchid asc"),
		@NamedQuery(name = "BatchJobEntity.findTotal", query = "SELECT count(r.combatchid) FROM BatchJobEntity r") })
@Cacheable(false)
public class BatchJobEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@Column(unique = true, nullable = false, length = 35)
	private String combatchid;

	@Column(nullable = false, length = 20)
	private String batchamount;

	@Column(nullable = false)
	private Integer batchcount;

	@Column(nullable = false, length = 16)
	private String batchid;

	@Column(nullable = false)
	private Integer batchstatus;

	@Column(length = 50)
	private String batchtype;

	@Column(length = 32)
	private String batchuid;

	@Column(precision = 20, scale = 2)
	private BigDecimal combatchamt;

	@Column(nullable = false)
	private Integer defjobcount;

	@Column(nullable = false)
	private Timestamp insertdate;

	@Column(length = 500)
	private String reason;

	@Column(nullable = false, length = 50)
	private String routingpoint;

	@Column(nullable = false)
	private Integer userid;

	public BatchJobEntity() {
	}

	public String getCombatchid() {
		return this.combatchid;
	}

	public void setCombatchid(String combatchid) {
		this.combatchid = combatchid;
	}

	public String getBatchamount() {
		return this.batchamount;
	}

	public void setBatchamount(String batchamount) {
		this.batchamount = batchamount;
	}

	public Integer getBatchcount() {
		return this.batchcount;
	}

	public void setBatchcount(Integer batchcount) {
		this.batchcount = batchcount;
	}

	public String getBatchid() {
		return this.batchid;
	}

	public void setBatchid(String batchid) {
		this.batchid = batchid;
	}

	public Integer getBatchstatus() {
		return this.batchstatus;
	}

	public void setBatchstatus(Integer batchstatus) {
		this.batchstatus = batchstatus;
	}

	public String getBatchtype() {
		return this.batchtype;
	}

	public void setBatchtype(String batchtype) {
		this.batchtype = batchtype;
	}

	public String getBatchuid() {
		return this.batchuid;
	}

	public void setBatchuid(String batchuid) {
		this.batchuid = batchuid;
	}

	public BigDecimal getCombatchamt() {
		return this.combatchamt;
	}

	public void setCombatchamt(BigDecimal combatchamt) {
		this.combatchamt = combatchamt;
	}

	public Integer getDefjobcount() {
		return this.defjobcount;
	}

	public void setDefjobcount(Integer defjobcount) {
		this.defjobcount = defjobcount;
	}

	public Timestamp getInsertdate() {
		return this.insertdate;
	}

	public void setInsertdate(Timestamp insertdate) {
		this.insertdate = insertdate;
	}

	public String getReason() {
		return this.reason;
	}

	public void setReason(String reason) {
		this.reason = reason;
	}

	public String getRoutingpoint() {
		return this.routingpoint;
	}

	public void setRoutingpoint(String routingpoint) {
		this.routingpoint = routingpoint;
	}

	public Integer getUserid() {
		return this.userid;
	}

	public void setUserid(Integer userid) {
		this.userid = userid;
	}

}