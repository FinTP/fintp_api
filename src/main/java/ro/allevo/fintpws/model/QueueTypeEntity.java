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
	
	@Column(length = 35)
	private String lev1kword1;

	@Column(length = 35)
	private String lev1kword2;

	@Column(length = 35)
	private String lev1kword3;

	@Column(length = 35)
	private String lev1kword4;

	@Column(length = 35)
	private String lev1kword5;

	@Column(length = 35)
	private String lev2kword1;

	@Column(length = 35)
	private String lev2kword2;

	@Column(length = 35)
	private String lev2kword3;

	@Column(length = 35)
	private String lev2kword4;

	@Column(length = 35)
	private String lev2kword5;

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

	public String getLev1kword1() {
		return this.lev1kword1;
	}

	public void setLev1kword1(String lev1kword1) {
		this.lev1kword1 = lev1kword1;
	}

	public String getLev1kword2() {
		return this.lev1kword2;
	}

	public void setLev1kword2(String lev1kword2) {
		this.lev1kword2 = lev1kword2;
	}

	public String getLev1kword3() {
		return this.lev1kword3;
	}

	public void setLev1kword3(String lev1kword3) {
		this.lev1kword3 = lev1kword3;
	}

	public String getLev1kword4() {
		return this.lev1kword4;
	}

	public void setLev1kword4(String lev1kword4) {
		this.lev1kword4 = lev1kword4;
	}

	public String getLev1kword5() {
		return this.lev1kword5;
	}

	public void setLev1kword5(String lev1kword5) {
		this.lev1kword5 = lev1kword5;
	}

	public String getLev2kword1() {
		return this.lev2kword1;
	}

	public void setLev2kword1(String lev2kword1) {
		this.lev2kword1 = lev2kword1;
	}

	public String getLev2kword2() {
		return this.lev2kword2;
	}

	public void setLev2kword2(String lev2kword2) {
		this.lev2kword2 = lev2kword2;
	}

	public String getLev2kword3() {
		return this.lev2kword3;
	}

	public void setLev2kword3(String lev2kword3) {
		this.lev2kword3 = lev2kword3;
	}

	public String getLev2kword4() {
		return this.lev2kword4;
	}

	public void setLev2kword4(String lev2kword4) {
		this.lev2kword4 = lev2kword4;
	}

	public String getLev2kword5() {
		return this.lev2kword5;
	}

	public void setLev2kword5(String lev2kword5) {
		this.lev2kword5 = lev2kword5;
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
