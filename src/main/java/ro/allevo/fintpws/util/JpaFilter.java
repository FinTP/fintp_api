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

package ro.allevo.fintpws.util;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;


public class JpaFilter {
	/*
	 * TODO: ADD JAVADOC
	 */
	
	/**
	 * Field LINK_HREF. (value is ""href"")
	 */
	static final String LINK_HREF = "href";
	/**
	 * Field LINK_REL. (value is ""rel"")
	 */
	static final String LINK_REL = "rel";
	/**
	 * Field SORT_FIELD. (value is ""sort_field"")
	 */
	static final String SORT_FIELD = "sort_field";
	/**
	 * Field SORT_ORDER. (value is ""sort_order"")
	 */
	static final String SORT_ORDER = "sort_order";
	/**
	 * Field SORT_ORDER_TYPE. (value is ""descending"")
	 */
	static final String SORT_ORDER_TYPE = "descending";
	/**
	 * Field FILTER. (value is ""filter_"")
	 */
	static final String FILTER = "filter_";

	
	UriInfo uriInfo;
	EntityManager entityManager;
	Class<?> entity;
	String filterName;
	String filterValue;
	CriteriaBuilder cb;
	CriteriaQuery<?> query;

	public JpaFilter(UriInfo uriInfo, EntityManager entityManager,
			Class<?> entity, String filterName, String filterValue) {
		this(uriInfo, entityManager, entity);
		this.filterName = filterName;
		this.filterValue = filterValue;
	}

	public JpaFilter(UriInfo uriInfo, EntityManager entityManager,
			Class<?> entity) {
		this.entityManager = entityManager;
		this.entity = entity;
		this.uriInfo = uriInfo;
	}

	public TypedQuery<?> createQuery() {
		cb = entityManager.getCriteriaBuilder();
		query = cb.createQuery(entity);
		Root<?> queryRoot = query.from(entity);
		queryRoot = createSortedQuery(queryRoot);
		return createFilterQuery(queryRoot);
	}

	public Root<?> createSortedQuery(Root<?> queryRoot) {

		MultivaluedMap<String, String> params = uriInfo.getQueryParameters();
		if (params.containsKey(SORT_FIELD)) {
			try {
				// Check if there are field in entity
				queryRoot.get(params.getFirst(SORT_FIELD));

				if (params.containsKey(SORT_ORDER)
						&& params.getFirst(SORT_ORDER).equals("descending")) {
					query.orderBy(cb.desc(queryRoot.get(params
							.getFirst(SORT_FIELD))));
				} else {
					query.orderBy(cb.asc(queryRoot.get(params
							.getFirst(SORT_FIELD))));
				}
			} catch (IllegalArgumentException e) {
				// skip sort field
			}
		}
		return queryRoot;
	}

	private List<Filter> getFilterFileds(Root<?> queryRoot) {
		List<Filter> entityFilterFileds = new ArrayList<Filter>();
		

		MultivaluedMap<String, String> queryParameters = uriInfo
				.getQueryParameters();
		String filterName = "";
		String type = "";

		for (String filterFiled : queryParameters.keySet()) {
			if (filterFiled.startsWith(FILTER)) {
				Filter filter = new Filter();
				String[] filedValues = filterFiled.split("_");
				try {
					filterName = filedValues.length > 1 ? filedValues[1] : null;
					type = filedValues.length > 2 ? filedValues[2] : "";

					// Check if there are field in entity
					queryRoot.get(filterName);

					filter.setName(filterName);
					filter.setType(type);
					filter.setValue(queryParameters.getFirst(filterFiled));
					entityFilterFileds.add(filter);
				} catch (IllegalArgumentException e) {
					// skip filter field
				}
			}
		}
		return entityFilterFileds;
	}

	public TypedQuery<?> createFilterQuery(Root<?> queryRoot) {
		String filedName = "";
		String fieldValue = "";
		String filterType = "";
		Predicate predicate = cb.conjunction();
		for (Filter filter : getFilterFileds(queryRoot)) {

			filedName = filter.getName();
			fieldValue = filter.getValue();
			filterType = filter.getType();
			try {
				switch (FilterType.fromName(filterType)) {
				case FILTER_TYPE_EXACT:
					predicate = cb.and(predicate,
							cb.equal(queryRoot.get(filedName), fieldValue));
					break;
				case FILTER_TYPE_CONTAINS:
					predicate = cb.and(predicate, cb.like(
							cb.upper(queryRoot.<String> get(filedName)), "%"
									+ fieldValue.toUpperCase() + "%"));
					break;
				case FILTER_TYPE_END:
					predicate = cb.and(predicate, cb.lessThanOrEqualTo(
							queryRoot.<Timestamp> get(filedName),
							ResourcesUtils.getTimestamp(fieldValue)));
					break;
				default:
					switch (FilterJavaType.fromName(queryRoot.get(filedName)
							.getJavaType().toString())) {
					case FILTER_TYPE_BIGDECIMAL:
						predicate = cb.and(predicate, cb.equal(
								queryRoot.<BigDecimal> get(filedName),
								fieldValue));
						break;
					case FILTER_TYPE_TIMESTAMP:
						predicate = cb.and(predicate, cb.greaterThanOrEqualTo(
								queryRoot.<Timestamp> get(filedName),
								ResourcesUtils.getTimestamp(fieldValue)));
						break;
					default:
						break;
					}
				}

			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
		if (null != filterName) {
			predicate = cb.and(predicate,
					cb.equal(queryRoot.get(filterName), filterValue));
		}
		query.where(predicate);
		return entityManager.createQuery(query);

	}

}

enum FilterType {
	FILTER_TYPE_EXACT("exact"), FILTER_TYPE_CONTAINS(""), FILTER_TYPE_END("end");
	String name;

	private FilterType(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public static FilterType fromName(String name) {
		for (FilterType enumVal : FilterType.values()) {
			if (name.contains(enumVal.name)) {
				return enumVal;
			}
		}
		return null;
	}
}

enum FilterJavaType {
	FILTER_TYPE_TIMESTAMP("java.sql.Timestamp"), FILTER_TYPE_STRING(
			"java.lang.String"), FILTER_TYPE_BIGDECIMAL("java.math.BigDecimal");
	String name;

	private FilterJavaType(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public static FilterJavaType fromName(String name) {
		for (FilterJavaType enumVal : FilterJavaType.values()) {
			if (name.contains(enumVal.name)) {
				return enumVal;
			}
		}
		return null;
	}
}

class Filter {
	String name;
	String type;
	String value;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}
}
