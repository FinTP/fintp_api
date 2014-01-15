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

import javax.persistence.*;

/**
 * The persistent class for the QUEUETYPES database table.
 * 
 */
@Entity
@Table(schema = "FINCFG", name = "QUEUETYPES")
@NamedQueries({
		@NamedQuery(name = "QueueTypeEntity.findByTypename", query = "SELECT t FROM QueueTypeEntity t "
				+ "WHERE trim(t.typename) = :typename"),
		@NamedQuery(name = "QueueTypeEntity.findAll", query = "SELECT t FROM QueueTypeEntity t order by t.typename asc"),
		@NamedQuery(name = "QueueTypeEntity.findTotal", query = "SELECT count(t.typename) FROM QueueTypeEntity t"), })
@Cacheable(false)
public class QueueTypeEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(generator = "TypeGenerator")
	@TableGenerator(name = "TypeGenerator", table = "FINCFG.IDGENLIST", pkColumnName = "TABCOLNAME", valueColumnName = "IDVALUE", pkColumnValue = "QUEUETYPES_TYPEID")
	@Column(unique = true, nullable = false, precision = 22)
	private long typeid;

/*	
	@OneToMany(targetEntity = RoutingSchemaEntity.class, cascade = {CascadeType.REFRESH}, fetch = FetchType.LAZY)
*/

	@Column(nullable = false, length = 50)
	private String typename;
	
	@OneToMany(mappedBy = "queueTypeEntity")
	private List<QueueEntity> queues;

	public QueueTypeEntity() {
	}

	public long getTypeid() {
		return this.typeid;
	}

	public void setTypeid(long typeid) {
		this.typeid = typeid;
	}

	public String getTypename() {
		return this.typename;
	}

	public void setTypename(String typename) {
		this.typename = typename;
	}

	@Override
	public String toString() {
		String res = "";
		res += getTypename();
		return res;
	}

}
