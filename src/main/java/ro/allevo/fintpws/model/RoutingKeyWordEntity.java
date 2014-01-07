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

import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.TableGenerator;


/**
 * The persistent class for the ROUTINGKEYWORDS database table.
 * 
 */
@Entity
@Table(name="ROUTINGKEYWORDS")
@NamedQueries({
	@NamedQuery(name = "RoutingKeyWordEntity.findByKeyword", query = "SELECT s FROM RoutingKeyWordEntity s "
			+ "WHERE s.keyword = :keyword"),
	@NamedQuery(name = "RoutingKeyWordEntity.findAll", query = "SELECT s FROM RoutingKeyWordEntity s order by s.keyword asc"),
	@NamedQuery(name = "RoutingKeyWordEntity.findTotal", query = "SELECT count(s.keyword) FROM RoutingKeyWordEntity s"),
	@NamedQuery(name = "RoutingKeyWordEntity.findAllRoutingKeyWordMaps", query = "SELECT r FROM RoutingKeyWordEntity r " +
			"WHERE r.guid=:keywordid ORDER BY r.guid asc"),
	@NamedQuery(name = "RoutingKeyWordEntity.findTotalRoutingKeyWordMaps", query = "SELECT count(r.mapid) FROM RoutingKeyWordMapEntity r "+
			"WHERE r.keywordid=:keywordid")})
@Cacheable(false)
public class RoutingKeyWordEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(generator = "RoutingKeyWordsGuidGenerator")
	@TableGenerator(name = "RoutingKeyWordsGuidGenerator", table = "FINCFG.IDGENLIST", pkColumnName = "TABCOLNAME",
			valueColumnName = "IDVALUE", pkColumnValue = "ROUTINGKEYWORDS_GUID")
	@Column(unique=true, nullable=false, precision=10)
	private long guid;

	@Column(nullable=false, length=200)
	private String comparer;

	@Column(length=200)
	private String description;

	@Column(nullable=false, length=50)
	private String keyword;

	@Column(length=200)
	private String selector;

	@Column(length=200)
	private String selectoriso;

	@OneToMany(mappedBy="routingKeywordEntity")
	private List<RoutingKeyWordMapEntity> routingKeywordMaps;
	
	public RoutingKeyWordEntity() {
	}

	public long getGuid() {
		return this.guid;
	}

	public void setGuid(long guid) {
		this.guid = guid;
	}

	public String getComparer() {
		return this.comparer;
	}

	public void setComparer(String comparer) {
		this.comparer = comparer;
	}

	public String getKeyword() {
		return this.keyword;
	}

	public void setKeyword(String keyword) {
		this.keyword = keyword;
	}

	public String getSelector() {
		return this.selector;
	}

	public void setSelector(String selector) {
		this.selector = selector;
	}

	public String getSelectoriso() {
		return this.selectoriso;
	}

	public void setSelectoriso(String selectoriso) {
		this.selectoriso = selectoriso;
	}
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	
	@Override
	public String toString() {
		return keyword;
	}

}
