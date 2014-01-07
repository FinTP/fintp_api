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
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.TableGenerator;

import org.springframework.security.core.GrantedAuthority;

@Entity
@Table(schema = "FINCFG", name="ROLES")
@NamedQueries({
	@NamedQuery(name = "RoleEntity.findAll", query = "SELECT r FROM RoleEntity r order by r.roleid asc"),
	@NamedQuery(name = "RoleEntity.findTotal", query = "SELECT count(r) FROM RoleEntity r"),
	@NamedQuery(name = "RoleEntity.findByName", query = "SELECT r FROM RoleEntity r WHERE trim(r.name)=:name"),
	@NamedQuery(name = "RoleEntity.findUserAuthorities", 
		query = "SELECT r " +
			"FROM RoleEntity r, UserRoleEntity ur " +
			"WHERE ur.userid= :userid AND r.roleid = ur.roleid " +
			"order by r.roleid asc"),
	@NamedQuery(name = "RoleEntity.findTotalUserAuthorities", 
		query = "SELECT count(r.roleid) " +
			"FROM RoleEntity r, UserRoleEntity ur " +
			"WHERE ur.userid = :userid AND r.roleid = ur.roleid "),
	
	@NamedQuery(name = "RoleEntity.findQueuesRoles", 
		query = "SELECT r " +
			"FROM RoleEntity r, QueuesRoleMapEntity qr " +
			"WHERE qr.queueid=:queueid AND r.roleid = qr.roleid " +
			"order by r.roleid asc"),
	@NamedQuery(name = "RoleEntity.findTotalQueuesRoles", 
		query = "SELECT count(r.roleid) " +
			"FROM RoleEntity r, QueuesRoleMapEntity qr " +
			"WHERE qr.queueid = :queueid AND r.roleid = qr.roleid ")
})

public class RoleEntity implements GrantedAuthority, Serializable{

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(generator="RoleGenerator")
	@TableGenerator(name="RoleGenerator", table="FINCFG.IDGENLIST",
	pkColumnName="TABCOLNAME", valueColumnName="IDVALUE",
	pkColumnValue="ROLES_ROLEID") 
	@Column(unique=true, nullable=false)
	private long roleid;
	
	@Column
	private String name;
	
	@Column(length = 300)
	private String description;
	
	@Column
	private long userCreated;
	
	@OneToMany(mappedBy="roleEntity")
	private List<QueuesRoleMapEntity> queueRoles;
	

	public long getRoleid(){
		return roleid;
	}
	
	public void setRoleid(long roleid){
		this.roleid = roleid;
	}
	
	
	@Override
	public String getAuthority() {
		return name;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public long isUserCreated() {
		return userCreated;
	}

	public void setIsUserCreated(long userCreated) {
		this.userCreated = userCreated;
	}

	public List<QueuesRoleMapEntity> getQueueRoles() {
		return queueRoles;
	}

	public void setQueueRoles(List<QueuesRoleMapEntity> queueRoles) {
		this.queueRoles = queueRoles;
	}


	
	
	
}
