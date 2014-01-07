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

import java.sql.Time;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;


/**
 * The persistent class for the TIMELIMITS database table.
 * 
 */
@Entity
@Table(schema = "FINCFG", name="TIMELIMITS")
@NamedQueries({
	@NamedQuery(name = "TimeLimitEntity.findByName", query = "SELECT t FROM TimeLimitEntity t "
			+ "WHERE t.limitname = :limitname"),
	@NamedQuery(name = "TimeLimitEntity.findAll", query = "SELECT t FROM TimeLimitEntity t order by t.limitname asc"),
	@NamedQuery(name = "TimeLimitEntity.findTotal", query = "SELECT count(t.guid) FROM TimeLimitEntity t"),
	@NamedQuery(name = "TimeLimitEntity.findAllRoutingSchema", query = "SELECT t FROM TimeLimitEntity t "
			+ "WHERE t.guid=:startlimit or t.guid=:stoplimit order by t.guid asc"),
	@NamedQuery(name = "TimeLimitEntity.findTotalRoutingSchema", query = "SELECT count (t.guid) FROM TimeLimitEntity t "
			+ "WHERE t.guid=:startlimit or t.guid=:stoplimit")
	
})
@Cacheable(false)
public class TimeLimitEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(generator="TlGuidGenerator")
	@TableGenerator(name="TlGuidGenerator", table="FINCFG.IDGENLIST",
	pkColumnName="TABCOLNAME", valueColumnName="IDVALUE",
	pkColumnValue="TIMELIMITS_GUID") 
	@Column(unique=true, nullable=false, precision=10)
	private long guid;

/*	
	@OneToMany(targetEntity = RoutingSchemaEntity.class, cascade = {CascadeType.REFRESH}, fetch = FetchType.LAZY)
*/
	
	@Column(length=100)
	private String limitname;

	@Column
	private Timestamp limittime;
	
	@OneToMany(mappedBy="startLimitEntity")
	private List<RoutingSchemaEntity> routingSchemasStart;

	@OneToMany(mappedBy="stopLimitEntity")
	private List<RoutingSchemaEntity> routingSchemasStop;
	
	public TimeLimitEntity() {
	}

	public long getGuid() {
		return this.guid;
	}

	public void setGuid(long guid) {
		this.guid = guid;
	}

	public String getLimitname() {
		return this.limitname;
	}

	public void setLimitname(String limitname) {
		this.limitname = limitname;
	}

	public Timestamp getLimittime() {
		return this.limittime;
	}

	public void setLimittime(Timestamp limittime) {
		this.limittime = limittime;
	}
	
	@Override
	public String toString(){
		return limitname;
	}
}
