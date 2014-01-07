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
 * The persistent class for the ROUTINGKEYWORDMAPS database table.
 * 
 */
@Entity
@Table(schema = "FINCFG" , name="ROUTINGKEYWORDMAPS")
@NamedQueries({
	@NamedQuery(name = "RoutingKeyWordMapEntity.findByMapId", query = "SELECT s FROM RoutingKeyWordMapEntity s "
			+ "WHERE s.mapid = :mapid"),
	@NamedQuery(name = "RoutingKeyWordMapEntity.findAll", query = "SELECT s FROM RoutingKeyWordMapEntity s order by s.mapid asc"),
	@NamedQuery(name = "RoutingKeyWordMapEntity.findTotal", query = "SELECT count(s.mapid) FROM RoutingKeyWordMapEntity s")})
@Cacheable(false)
public class RoutingKeyWordMapEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(generator = "RoutingKeyWordsMapGuidGenerator")
	@TableGenerator(name = "RoutingKeyWordsMapGuidGenerator", table = "FINCFG.IDGENLIST", pkColumnName = "TABCOLNAME",
			valueColumnName = "IDVALUE", pkColumnValue = "ROUTINGKEYWORDMAPS_MAPID")
	@Column(unique=true, nullable=false)
	private long mapid;

	@Column(nullable=false, precision=10)
	private long keywordid;

	@Column(nullable=false, length=35)
	private String mt;

	@Column(nullable=false, length=11)
	private String selector;

	@Column(nullable=false, length=200)
	private String tag;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@PrimaryKeyJoinColumn(name = "KEYWORDID")
	private RoutingKeyWordEntity routingKeywordEntity;

	public RoutingKeyWordMapEntity() {
	}

	public long getMapid() {
		return this.mapid;
	}

	public void setMapid(long mapid) {
		this.mapid = mapid;
	}

//	public java.math.BigDecimal getKeywordid() {
//		return this.keywordid;
//	}

	private void setKeywordid(long keywordid) {
		this.keywordid = keywordid;
	}

	public String getMt() {
		return this.mt;
	}

	public void setMt(String mt) {
		this.mt = mt;
	}

	public String getSelector() {
		return this.selector;
	}

	public void setSelector(String selector) {
		this.selector = selector;
	}

	public String getTag() {
		return this.tag;
	}

	public void setTag(String tag) {
		this.tag = tag;
	}
	
	@Override
	public String toString() {
		return mapid + "";
	}

	public RoutingKeyWordEntity getRoutingKeywordEntity() {
		return routingKeywordEntity;
	}

	public void setRoutingKeywordEntity(RoutingKeyWordEntity routingKeywordEntity) {
		this.routingKeywordEntity = routingKeywordEntity;
		setKeywordid(routingKeywordEntity.getGuid());
	}
	
	

}
