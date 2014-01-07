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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.TableGenerator;

@Entity
@Table(schema = "FINCFG", name="USERSROLEMAP")
@NamedQueries({
	@NamedQuery(name = "UserRoleEntity.findUserAuthorities", 
		query = "SELECT ur " +
				"FROM UserRoleEntity ur " +
				"WHERE ur.userid = :userid AND ur.roleid =:roleid")
})

public class UserRoleEntity {
	
	@Id
	@GeneratedValue(generator="UserRoleGenerator")
	@TableGenerator(name="UserRoleGenerator", table="FINCFG.IDGENLIST",
	pkColumnName="TABCOLNAME", valueColumnName="IDVALUE",
	pkColumnValue="USERSROLEMAP_MAPID") 
	@Column(unique=true, nullable=false, precision=38)
	private long mapid;

	
	@Column(precision = 38)
	private long userid;

	@Column(length = 5)
	private long roleid;
	
	public long getMapId(){
		return mapid;
	}
	
	public void setMapId(long mapid){
		this.mapid = mapid;
	}
	
	public long getUserId(){
		return userid;
	}
	
	public void setUserid(long userid){
		this.userid = userid;
	}
	
	public long getRoleid(){
		return roleid;
	}
	
	public void setRoleid(long roleid){
		this.roleid = roleid;
	}
	
}
