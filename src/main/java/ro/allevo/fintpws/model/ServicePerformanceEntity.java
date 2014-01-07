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
 * The persistent class for the SERVICEPERFORMANCE database table.
 * 
 */
@Entity
@Table(schema = "FINDATA", name = "SERVICEPERFORMANCE")
@NamedQueries({
		@NamedQuery(name = "ServicePerformanceEntity.findByServiceId", query = "SELECT t FROM ServicePerformanceEntity t "
				+ "WHERE t.serviceid = :serviceid"),
		@NamedQuery(name = "ServicePerformanceEntity.findAll", query = "SELECT t FROM ServicePerformanceEntity t order by t.serviceid asc"),
		@NamedQuery(name = "ServicePerformanceEntity.findTotal", query = "SELECT count(t.serviceid) FROM ServicePerformanceEntity t") })
@Cacheable(false)
public class ServicePerformanceEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(generator = "ServicePerformanceServiceidGenerator")
	@TableGenerator(name = "ServicePerformanceServiceidGenerator", table = "FINCFG.IDGENLIST", pkColumnName = "TABCOLNAME", valueColumnName = "IDVALUE", pkColumnValue = "SERVICEPERFORMANCE_SERVICEID")
	@Column(unique = true, nullable = false, precision = 10)
	private long serviceid;

	@Column(precision = 10)
	private BigDecimal abortedtrns;

	@Column(precision = 10)
	private BigDecimal commitedtrns;

	@Column(nullable = false)
	private Timestamp insertdate;

	@Column(nullable = false, precision = 5)
	private BigDecimal ioidentifier;

	@Column(nullable = false)
	private BigDecimal maxtransactiontime;

	@Column(nullable = false)
	private BigDecimal meantransactiontime;

	@Column(nullable = false)
	private BigDecimal mintransactiontime;

	@Column(nullable = false, precision = 10)
	private BigDecimal sequenceno;

	@Column(nullable = false, precision = 10)
	private BigDecimal sessionid;

	public ServicePerformanceEntity() {
	}

	public long getServiceid() {
		return this.serviceid;
	}

	public void setServiceid(long serviceid) {
		this.serviceid = serviceid;
	}

	public BigDecimal getAbortedtrns() {
		return this.abortedtrns;
	}

	public void setAbortedtrns(BigDecimal abortedtrns) {
		this.abortedtrns = abortedtrns;
	}

	public BigDecimal getCommitedtrns() {
		return this.commitedtrns;
	}

	public void setCommitedtrns(BigDecimal commitedtrns) {
		this.commitedtrns = commitedtrns;
	}

	public Timestamp getInsertdate() {
		return this.insertdate;
	}

	public void setInsertdate(Timestamp insertdate) {
		this.insertdate = insertdate;
	}

	public BigDecimal getIoidentifier() {
		return this.ioidentifier;
	}

	public void setIoidentifier(BigDecimal ioidentifier) {
		this.ioidentifier = ioidentifier;
	}

	public BigDecimal getMaxtransactiontime() {
		return this.maxtransactiontime;
	}

	public void setMaxtransactiontime(BigDecimal maxtransactiontime) {
		this.maxtransactiontime = maxtransactiontime;
	}

	public BigDecimal getMeantransactiontime() {
		return this.meantransactiontime;
	}

	public void setMeantransactiontime(BigDecimal meantransactiontime) {
		this.meantransactiontime = meantransactiontime;
	}

	public BigDecimal getMintransactiontime() {
		return this.mintransactiontime;
	}

	public void setMintransactiontime(BigDecimal mintransactiontime) {
		this.mintransactiontime = mintransactiontime;
	}

	public BigDecimal getSequenceno() {
		return this.sequenceno;
	}

	public void setSequenceno(BigDecimal sequenceno) {
		this.sequenceno = sequenceno;
	}

	public BigDecimal getSessionid() {
		return this.sessionid;
	}

	public void setSessionid(BigDecimal sessionid) {
		this.sessionid = sessionid;
	}
	@Override
	public String toString() {
		return serviceid + " ";
	}

}
