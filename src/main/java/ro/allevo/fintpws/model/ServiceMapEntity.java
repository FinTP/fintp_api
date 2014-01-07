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
 * The persistent class for the SERVICEMAPS database table.
 * 
 */
@Entity
@Table(schema="FINCFG", name="SERVICEMAPS")
@NamedQueries({
	@NamedQuery(name = "ServiceMapEntity.findByFriendlyName", query = "SELECT t FROM ServiceMapEntity t "
			+ "WHERE trim(t.friendlyname) = :friendlyname"),
	@NamedQuery(name = "ServiceMapEntity.findAll", query = "SELECT t FROM ServiceMapEntity t order by t.friendlyname asc"),
	@NamedQuery(name = "ServiceMapEntity.findTotal", query = "SELECT count(t.friendlyname) FROM ServiceMapEntity t"),
	@NamedQuery(name = "ServiceMapEntity.findAllQueue", query = "SELECT t FROM ServiceMapEntity t "
		+ "WHERE t.serviceid=:serviceid ORDER BY t.friendlyname asc"),
	@NamedQuery(name = "ServiceMapEntity.findTotalQueue", query = "SELECT count(r.serviceid) FROM ServiceMapEntity r "
		+ "WHERE r.serviceid=:serviceid")
})
@Cacheable(false)
public class ServiceMapEntity implements Serializable {
	private static final long serialVersionUID = 1L;
	@Id
	@GeneratedValue(generator="ServiceMapServiceidGenerator")
	@TableGenerator(name="ServiceMapServiceidGenerator", table="FINCFG.IDGENLIST",
	pkColumnName="TABCOLNAME", valueColumnName="IDVALUE",
	pkColumnValue="SERVICEMAPS_SERVICEID") 
	@Column(nullable=false, precision=10)
	private BigDecimal serviceid;

	@Column(length=50)
	private String delayednotifq;

	private BigDecimal duplicatecheck;

	@Column(length=50)
	private String duplicatemap;

	@Column(length=50)
	private String duplicatenotifq;

	@Column(length=50)
	private String duplicateq;

	@Column(length=300)
	private String exitpoint;

	@Column(unique=true,nullable=false, length=30)
	private String friendlyname;

	@Column(nullable=false, precision=10)
	private BigDecimal heartbeatinterval;

	@Column(precision=5)
	private BigDecimal ioidentifier;

	private Timestamp lastheartbeat;

	@Column(precision=10)
	private BigDecimal lastsessionid;

	@Column(length=255)
	private String partner;


	@Column(precision=10)
	private BigDecimal servicetype;

	@Column(length=26)
	private String sessionid;

	@Column(nullable=false, precision=10)
	private BigDecimal status;

	@Column(length=255)
	private String version;

	public ServiceMapEntity() {
	}

	public String getDelayednotifq() {
		return this.delayednotifq;
	}

	public void setDelayednotifq(String delayednotifq) {
		this.delayednotifq = delayednotifq;
	}

	public BigDecimal getDuplicatecheck() {
		return this.duplicatecheck;
	}

	public void setDuplicatecheck(BigDecimal duplicatecheck) {
		this.duplicatecheck = duplicatecheck;
	}

	public String getDuplicatemap() {
		return this.duplicatemap;
	}

	public void setDuplicatemap(String duplicatemap) {
		this.duplicatemap = duplicatemap;
	}

	public String getDuplicatenotifq() {
		return this.duplicatenotifq;
	}

	public void setDuplicatenotifq(String duplicatenotifq) {
		this.duplicatenotifq = duplicatenotifq;
	}

	public String getDuplicateq() {
		return this.duplicateq;
	}

	public void setDuplicateq(String duplicateq) {
		this.duplicateq = duplicateq;
	}

	public String getExitpoint() {
		return this.exitpoint;
	}

	public void setExitpoint(String exitpoint) {
		this.exitpoint = exitpoint;
	}

	public String getFriendlyname() {
		return this.friendlyname;
	}

	public void setFriendlyname(String friendlyname) {
		this.friendlyname = friendlyname;
	}

	public BigDecimal getHeartbeatinterval() {
		return this.heartbeatinterval;
	}

	public void setHeartbeatinterval(BigDecimal heartbeatinterval) {
		this.heartbeatinterval = heartbeatinterval;
	}

	public BigDecimal getIoidentifier() {
		return this.ioidentifier;
	}

	public void setIoidentifier(BigDecimal ioidentifier) {
		this.ioidentifier = ioidentifier;
	}

	public Timestamp getLastheartbeat() {
		return this.lastheartbeat;
	}

	public void setLastheartbeat(Timestamp lastheartbeat) {
		this.lastheartbeat = lastheartbeat;
	}

	public BigDecimal getLastsessionid() {
		return this.lastsessionid;
	}

	public void setLastsessionid(BigDecimal lastsessionid) {
		this.lastsessionid = lastsessionid;
	}

	public String getPartner() {
		return this.partner;
	}

	public void setPartner(String partner) {
		this.partner = partner;
	}

	public BigDecimal getServiceid() {
		return this.serviceid;
	}

	public void setServiceid(BigDecimal serviceid) {
		this.serviceid = serviceid;
	}

	public BigDecimal getServicetype() {
		return this.servicetype;
	}

	public void setServicetype(BigDecimal servicetype) {
		this.servicetype = servicetype;
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

	public String getVersion() {
		return this.version;
	}

	public void setVersion(String version) {
		this.version = version;
	}
	
	@Override
	public String toString() {
		return friendlyname;
	}

}
