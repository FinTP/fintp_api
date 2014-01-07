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
 * The persistent class for the QUEUEACTIONS database table.
 * 
 */
@Entity
@Table(schema = "FINCFG", name = "QUEUEACTIONS")
@NamedQueries({
		@NamedQuery(name = "QueueActionEntity.findByAction", query = "SELECT t FROM QueueActionEntity t "
				+ "WHERE trim(t.action) = :action"),
		@NamedQuery(name = "QueueActionEntity.findAll", query = "SELECT t FROM QueueActionEntity t order by t.action asc"),
		@NamedQuery(name = "QueueActionEntity.findTotal", query = "SELECT count(t.action) FROM QueueActionEntity t"), })
@Cacheable(false)
public class QueueActionEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(generator = "ActionGenerator")
	@TableGenerator(name = "ActionGenerator", table = "FINCFG.IDGENLIST", pkColumnName = "TABCOLNAME", valueColumnName = "IDVALUE", pkColumnValue = "QUEUEACTIONS_ACTIONID")
	@Column(unique = true, nullable = false, precision = 22)
	private long actionid;

	@Column(nullable = false, length = 100)
	private String action;

	@Column(length = 300)
	private String description;

	@Column(precision = 22)
	private BigDecimal groupmsg;

	@Column(length = 50)
	private String optionvalues;

	@Column(precision = 22)
	private BigDecimal selmsg;
	
	@Column(precision = 22)
	private BigDecimal currmsg;

	public QueueActionEntity() {
	}

	public long getActionid() {
		return this.actionid;
	}

	public void setActionid(long actionid) {
		this.actionid = actionid;
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

	public BigDecimal getGroupmsg() {
		return this.groupmsg;
	}

	public void setGroupmsg(BigDecimal groupmsg) {
		this.groupmsg = groupmsg;
	}

	public String getOptionvalues() {
		return this.optionvalues;
	}

	public void setOptionvalues(String optionvalues) {
		this.optionvalues = optionvalues;
	}

	public BigDecimal getSelmsg() {
		return this.selmsg;
	}

	public void setSelmsg(BigDecimal selmsg) {
		this.selmsg = selmsg;
	}

	@Override
	public String toString() {
		String res = "";
		res += getAction();
		return res;
	}

	public BigDecimal getCurrmsg() {
		return currmsg;
	}

	public void setCurrmsg(BigDecimal currmsg) {
		this.currmsg = currmsg;
	}

}
