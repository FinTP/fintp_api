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
 * The persistent class for the MT202TAB database table.
 * 
 */
@Entity
@Table(schema = "FINDATA", name="MT202TAB")
public class Mt202tabEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@Column(unique=true, nullable=false, length=30)
	private String correlid;

	@Column(length=50)
	private String amount;

	@Column(length=3)
	private String currency;

	@Column(length=50)
	private String msgtype;

	@Column(length=6)
	private String stlmdate;

	@Column(precision=3)
	private BigDecimal ttc;

	public Mt202tabEntity() {
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

	public String getCurrency() {
		return this.currency;
	}

	public void setCurrency(String currency) {
		this.currency = currency;
	}

	public String getMsgtype() {
		return this.msgtype;
	}

	public void setMsgtype(String msgtype) {
		this.msgtype = msgtype;
	}

	public String getStlmdate() {
		return this.stlmdate;
	}

	public void setStlmdate(String stlmdate) {
		this.stlmdate = stlmdate;
	}

	public BigDecimal getTtc() {
		return this.ttc;
	}

	public void setTtc(BigDecimal ttc) {
		this.ttc = ttc;
	}

}
