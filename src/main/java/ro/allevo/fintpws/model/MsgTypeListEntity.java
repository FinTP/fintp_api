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
 * The persistent class for the MSGTYPELIST database table.
 * 
 */
@Entity
@Table(schema = "FINCFG", name = "MSGTYPES")
@NamedQueries({ @NamedQuery(name = "MsgTypeListEntity.findByMsgType", 
		query = "select m.storage from MsgTypeListEntity m where trim(m.messagetype) = :messagetype") })
public class MsgTypeListEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	@Column(length = 100)
	private String businessarea;

	private BigDecimal displayorder;

	@Column(length = 50)
	private String friendlyname;

	@Column(length = 50)
	@Id
	private String messagetype;

	@Column(length = 35)
	private String storage;

	public MsgTypeListEntity() {
	}

	public String getBusinessarea() {
		return this.businessarea;
	}

	public void setBusinessarea(String businessarea) {
		this.businessarea = businessarea;
	}

	public BigDecimal getDisplayorder() {
		return this.displayorder;
	}

	public void setDisplayorder(BigDecimal displayorder) {
		this.displayorder = displayorder;
	}

	public String getFriendlyname() {
		return this.friendlyname;
	}

	public void setFriendlyname(String friendlyname) {
		this.friendlyname = friendlyname;
	}

	public String getMessagetype() {
		return this.messagetype;
	}

	public void setMessagetype(String messagetype) {
		this.messagetype = messagetype;
	}

	public String getStorage() {
		return this.storage;
	}

	public void setStorage(String storage) {
		this.storage = storage;
	}

}
