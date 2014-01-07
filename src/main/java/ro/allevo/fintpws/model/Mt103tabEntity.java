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


/**
 * The persistent class for the MT103TAB database table.
 * 
 */
@Entity
@Table(schema = "FINDATA", name="MT103TAB")
public class Mt103tabEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@Column(unique=true, nullable=false, length=30)
	private String correlid;

	@Column(length=50)
	private String amount;

	@Column(length=50)
	private String cdaccount;

	@Column(length=3)
	private String currency;

	@Column(length=50)
	private String dbaccount;

	@Column(length=50)
	private String msgtype;

	public Mt103tabEntity() {
	}

	public String getCorrelid() {
		return this.correlid;
	}

	public void setCorrelid(String correlid) {
		this.correlid = correlid;
	}

	public String getAmount() {
		return this.amount;
	}

	public void setAmount(String amount) {
		this.amount = amount;
	}

	public String getCdaccount() {
		return this.cdaccount;
	}

	public void setCdaccount(String cdaccount) {
		this.cdaccount = cdaccount;
	}

	public String getCurrency() {
		return this.currency;
	}

	public void setCurrency(String currency) {
		this.currency = currency;
	}

	public String getDbaccount() {
		return this.dbaccount;
	}

	public void setDbaccount(String dbaccount) {
		this.dbaccount = dbaccount;
	}

	public String getMsgtype() {
		return this.msgtype;
	}

	public void setMsgtype(String msgtype) {
		this.msgtype = msgtype;
	}

}
