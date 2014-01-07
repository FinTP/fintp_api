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
import java.math.BigDecimal;

import javax.persistence.Cacheable;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.TableGenerator;

import ro.allevo.fintpws.converters.AlertsState;
import ro.allevo.fintpws.converters.AlertsType;


/**
 * The persistent class for the ALERTS database table.
 * 
 */
@Entity
@Table(schema = "FINCFG", name="ALERTS")
@NamedQueries({
	@NamedQuery(name = "AlertEntity.findAll", query = "select q from AlertEntity q"),
	@NamedQuery(name = "AlertEntity.findTotal", query = "select count(q.alertname) from AlertEntity q"),
	@NamedQuery(name = "AlertEntity.findAllQueue", query = "select q from AlertEntity q where q.queueid = :queueid"),
	@NamedQuery(name = "AlertEntity.findTotalQueue", query = "select count(q.alertname) from AlertEntity q  where q.queueid = :queueid"),
	@NamedQuery(name = "AlertEntity.findByName", query = "select q from AlertEntity q where trim(q.alertname)=:alertname") })
@Cacheable(false)
public class AlertEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(generator="AlertidGenerator")
    @TableGenerator(name="AlertidGenerator", table="FINCFG.IDGENLIST",
         pkColumnName="TABCOLNAME", valueColumnName="IDVALUE",
         pkColumnValue="ALERTS_ALERTID")
	@Column(unique=true, nullable=false, length=37)
	private String alertid;

	@ManyToOne(targetEntity = QueueEntity.class, cascade = { CascadeType.REFRESH }, fetch = FetchType.LAZY)
	@JoinColumn(name = "QUEUEID", referencedColumnName = "GUID", insertable = false, updatable = false)
	private QueueEntity queueEntity;

	@Column(length=35)
	private String alertname;

	@Column(length=500)
	private String description;

	@Column(length=4000)
	private String emailaddress;

	@Column(length=5)
	private String endtime;

	private BigDecimal frequency;

	@Column(precision=10)
	private BigDecimal queueid;

	@Column(length=5)
	private String starttime;

	@Enumerated(EnumType.ORDINAL)
	private AlertsState state;

	@Enumerated(EnumType.ORDINAL)
	private AlertsType type;

	@Column(length=2)
	private String weekdays;

    public AlertEntity() {
    }

    public QueueEntity getQueueEntity() {
		return queueEntity;
	}

    public AlertsType getType() {
		return type;
	}

	public void setType(AlertsType type) {
		this.type = type;
	}
	
	public String getAlertid() {
		return this.alertid;
	}

	public void setAlertid(String alertid) {
		this.alertid = alertid;
	}

	public String getAlertname() {
		return this.alertname;
	}

	public void setAlertname(String alertname) {
		this.alertname = alertname;
	}

	public String getDescription() {
		return this.description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getEmailaddress() {
		return this.emailaddress;
	}

	public void setEmailaddress(String emailaddress) {
		this.emailaddress = emailaddress;
	}

	public String getEndtime() {
		return this.endtime;
	}

	public void setEndtime(String endtime) {
		this.endtime = endtime;
	}

	public BigDecimal getFrequency() {
		return this.frequency;
	}

	public void setFrequency(BigDecimal frequency) {
		this.frequency = frequency;
	}

	public BigDecimal getQueueid() {
		return this.queueid;
	}

	public void setQueueid(BigDecimal queueid) {
		this.queueid = queueid;
	}

	public String getStarttime() {
		return this.starttime;
	}

	public void setStarttime(String starttime) {
		this.starttime = starttime;
	}

	public AlertsState getState() {
		return this.state;
	}

	public void setState(AlertsState state) {
		this.state = state;
	}

	public String getWeekdays() {
		return this.weekdays;
	}

	public void setWeekdays(String weekdays) {
		this.weekdays = weekdays;
	}
	
}

