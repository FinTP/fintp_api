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

import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;
import javax.persistence.TableGenerator;

@Entity
@Table(schema = "FINCFG", name = "QUEUESROLEMAP")
@NamedQueries({
		@NamedQuery(name = "QueuesRoleMapEntity.findAll", query = "select qr from QueuesRoleMapEntity qr, RoleEntity r" +
				" where qr.roleid=r.roleid and trim(r.name)=:rolename order by qr.mapid asc"),
		@NamedQuery(name = "QueuesRoleMapEntity.findTotal", query = "select count(qr.mapid) from QueuesRoleMapEntity qr, RoleEntity r" +
				" where qr.roleid=r.roleid and trim(r.name)=:rolename"),
		@NamedQuery(name = "QueuesRoleMapEntity.findByQueueName", query = "select qr from QueuesRoleMapEntity qr, RoleEntity r, QueueEntity q" +
				" where qr.roleid=r.roleid and trim(r.name)=:rolename and qr.queueid = q.guid and trim(q.name)=:queuename")
		
		})
public class QueuesRoleMapEntity {

	@Id
	@GeneratedValue(generator = "QueuesRoleMapGenerator")
	@TableGenerator(name = "QueuesRoleMapGenerator", table = "FINCFG.IDGENLIST", pkColumnName = "TABCOLNAME", valueColumnName = "IDVALUE", pkColumnValue = "QUEUESROLEMAP_MAPID")
	@Column(unique = true, precision = 38)
	private long mapid;

	@Column
	private BigDecimal queueid;

	@Column(length = 38)
	private long roleid;

	@Column(length = 2)
	private String actiontype;

	@ManyToOne(fetch = FetchType.LAZY)
	@PrimaryKeyJoinColumn(name = "ROLEID")
	private RoleEntity roleEntity;

	@ManyToOne(fetch = FetchType.LAZY)
	@PrimaryKeyJoinColumn(name = "QUEUEID")
	private QueueEntity queueEntity;

	public long getMapId() {
		return mapid;
	}

	public void setMapId(long mapid) {
		this.mapid = mapid;
	}

	public long getRoleId() {
		return roleid;
	}

	public void setRoleId(long roleid) {
		this.roleid = roleid;
	}

	public BigDecimal getQueueId() {
		return queueid;
	}

	public void setQueueId(BigDecimal queueid) {
		this.queueid = queueid;
	}

	public String getActiontype() {
		return actiontype;
	}

	public void setActiontype(String actiontype) {
		this.actiontype = actiontype;
	}
	
	public String getQueueName(){
		return queueEntity.getName();
	}
	
	public String getRoleName(){
		return roleEntity.getName();
	}
	
	public void setRoleEntity(RoleEntity roleEntity){
		this.roleEntity = roleEntity;
	}
	
	public void setQueueEntity(QueueEntity queueEntity){
		this.queueEntity = queueEntity;
	}
	

}
