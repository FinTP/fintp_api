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
 * The persistent class for the QMOVEPRIVMAPS database table.
 * 
 */
@Entity
@Table(schema = "FINCFG", name="QMOVEPRIVMAPS")
@NamedQueries({
	@NamedQuery(name = "QMovePrivMapEntity.findByMapid", query = "SELECT t FROM QMovePrivMapEntity t "
			+ "WHERE trim(t.mapid) = :mapid"),
	@NamedQuery(name = "QMovePrivMapEntity.findAll", query = "SELECT t FROM QMovePrivMapEntity t order by t.mapid asc"),
	@NamedQuery(name = "QMovePrivMapEntity.findTotal", query = "SELECT count(t.mapid) FROM QMovePrivMapEntity t"),
	@NamedQuery(name = "QMovePrivMapEntity.findAllQueue", query = "SELECT t FROM QMovePrivMapEntity t "
			+ "WHERE trim(t.sourcequeueid)=:queueid or trim(t.destqueueid)=:queueid order by t.mapid asc"),
	@NamedQuery(name = "QMovePrivMapEntity.findTotalQueue", query = "SELECT count (t.mapid) FROM QMovePrivMapEntity t "
			+ "WHERE trim(t.sourcequeueid)=:queueid or trim(t.destqueueid)=:queueid")})
@Cacheable(false)
public class QMovePrivMapEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(generator = "MapidGenerator")
	@TableGenerator(name = "MapidGenerator", table = "FINCFG.IDGENLIST", pkColumnName = "TABCOLNAME", valueColumnName = "IDVALUE", pkColumnValue = "QMOVEPRIVMAPS_MAPID")
	@Column(unique=true, nullable=false, precision=22)
	private long mapid;

	@Column(precision=22)
	private java.math.BigDecimal destqueueid;

	@Column(precision=22)
	private java.math.BigDecimal sourcequeueid;

	public QMovePrivMapEntity() {
	}

	public long getMapid() {
		return this.mapid;
	}

	public void setMapid(long mapid) {
		this.mapid = mapid;
	}

	public java.math.BigDecimal getDestqueueid() {
		return this.destqueueid;
	}

	public void setDestqueueid(java.math.BigDecimal destqueueid) {
		this.destqueueid = destqueueid;
	}

	public java.math.BigDecimal getSourcequeueid() {
		return this.sourcequeueid;
	}

	public void setSourcequeueid(java.math.BigDecimal sourcequeueid) {
		this.sourcequeueid = sourcequeueid;
	}

	@Override
	public String toString() {
		String res = "";
		res += getMapid();
		return res;
	}
	
}
