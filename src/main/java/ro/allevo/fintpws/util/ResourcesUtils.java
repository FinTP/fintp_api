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

import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Time;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.Metamodel;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.PathSegment;
import javax.ws.rs.core.UriInfo;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

/**
 * @author costi
 * @version $Revision: 1.0 $
 */
public final class ResourcesUtils {

	/**
	 * Field logger.
	 */
	private static Logger logger = LogManager.getLogger(ResourcesUtils.class
			.getName());

	/**
	 * Field ENTITYMETHODSET. (value is ""set"")
	 */
	static final String ENTITYMETHODSET = "set";
	/**
	 * Field ENTITYMETHODGET. (value is ""get"")
	 */
	static final String ENTITYMETHODGET = "get";
	/**
	 * Field logger.
	 */
	// private static Logger logger = LogManager.getLogger(ResourcesUtils.class
	// .getName());

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
	/**
	 * Field FILTER_TYPE_EXACT. (value is ""_exact"")
	 */
	static final String FILTER_TYPE_EXACT = "_exact";
	/**
	 * Field FILTER_TYPE_CONTAINS. (value is ""_contains"")
	 */
	static final String FILTER_TYPE_CONTAINS = "_contains";
	/**
	 * Field FILTER_TYPE_END. (value is ""_end"")
	 */
	static final String FILTER_TYPE_END = "_end";

	/**
	 * Field ERROR_REASON_REFLECTION_ERROR. (value is ""Something bad happened
	 * when reflecting"")
	 */
	static final String ERROR_REASON_REFLECTION_ERROR = "Something bad happened when reflecting";

	/**
	 * Constructor for ResourcesUtils.
	 */
	private ResourcesUtils() {
	}

	/**
	 * Method optTimestamp. Converts a string value to a timestamp if the string
	 * is not empty. Returns null otherwise.
	 * 
	 * @param stringTime
	 *            String
	 * @return Timestamp Returns null if the input string is null or empty.
	 * @throws ParseException
	 */
	public static Timestamp optTimestamp(String stringTime)
			throws ParseException {

		if ((null == stringTime) || (stringTime.isEmpty())) {
			return null;
		}

		
		return new Timestamp(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
					.parse(stringTime).getTime());
		
	}

	/**
	 * Method optTime.Converts a string value to a time if the string is not
	 * empty. Returns null otherwise.
	 * 
	 * @param stringTime
	 *            String
	 * @return Time
	 * @throws ParseException
	 */
	public static Timestamp optTime(String stringTime) throws ParseException {

		if ((null == stringTime) || (stringTime.isEmpty())) {
			return null;
		}

		return new Timestamp(new SimpleDateFormat("hh:mm:ss").parse(stringTime)
				.getTime());
	}

	/**
	 * Method asTimestampMandatory. Converts a string value to a timestamp.
	 * 
	 * @param stringTime
	 *            String
	 * @return Timestamp Returns curent time if the input string is null or
	 *         empty.
	 * @throws ParseException
	 */
	public static Timestamp getTimestamp(String stringTime)
			throws ParseException {

		Timestamp finalTimestamp = optTimestamp(stringTime);

		return (null == finalTimestamp) ? new Timestamp(
				System.currentTimeMillis()) : finalTimestamp;
	}
	
	public static String getIsoDateFromTimestamp(Timestamp timestamp){
		if(null == timestamp)
			return null;
		
		return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
				.format(timestamp);
	}
	
	/**
	 * Method getTime.
	 * 
	 * @param stringTime
	 *            String
	 * @return Time
	 * @throws ParseException
	 */
	public static String getISODate(Date date) throws ParseException {
		return new SimpleDateFormat("yyyy-MM-dd").format(date);
	}

	/**
	 * Method getTime.
	 * 
	 * @param stringTime
	 *            String
	 * @return Time
	 * @throws ParseException
	 */
	public static Timestamp getTime(String stringTime) throws ParseException {

		Timestamp finalTimestamp = optTime(stringTime);

		if(null == finalTimestamp){
			System.out.println(new Time(System.currentTimeMillis()));
		}else{
			System.out.println(finalTimestamp);
		}
		return (null == finalTimestamp) ? new Timestamp(System.currentTimeMillis())
				: finalTimestamp;
	}

	/**
	 * Method blankIfNull.
	 * 
	 * @param object
	 *            Object
	 * @return Object
	 */
	public static Object blankIfNull(Object object) {
		return (null == object) ? "" : object;
	}

	/**
	 * Method getQueryParamValue. Returns the value of the query parameter named
	 * queryParamName from URI uriInfo.
	 * 
	 * @param uriInfo
	 *            UriInfo
	 * @param queryParamName
	 *            String
	 * 
	 * @return String Null if the parameter with name queryParamName can not be
	 *         found.
	 */
	public static String getQueryParamValue(UriInfo uriInfo,
			String queryParamName) {
		MultivaluedMap<String, String> parameters = uriInfo
				.getQueryParameters();
		if (parameters.containsKey(queryParamName)) {
			return parameters.getFirst(queryParamName);
		}

		return null;
	}

	/**
	 * Method createLink.
	 * 
	 * @param baseObject
	 *            JSONObject
	 * @param href
	 *            String
	 * @param rel
	 *            String
	 * @return JSONObject
	 * @throws JSONException
	 */
	public static JSONObject createLink(JSONObject baseObject, String href,
			String rel) throws JSONException {
		return baseObject.put("link", new JSONObject().put(LINK_HREF, href)
				.put(LINK_REL, rel));
	}

	/**
	 * Method createParentLink. Create link to the parent resource
	 * 
	 * @param baseObject
	 *            JSONObject
	 * @param rel
	 *            String
	 * @param uriInfo
	 *            UriInfo
	 * @return JSONObject
	 * @throws JSONException
	 */
	public static JSONObject createParentLink(JSONObject baseObject,
			String rel, UriInfo uriInfo) throws JSONException {

		// TODO : use URIBuilder for path composition
		// remove last segment and recreate the path
		String href = "";
		List<PathSegment> listSegments = uriInfo.getPathSegments();
		listSegments.remove(listSegments.size() - 1);
		for (PathSegment xp : listSegments) {
			href += xp.getPath() + "/";
		}
		href = href.substring(0, href.length() - 1);

		// add the link
		return createLink(baseObject, href, rel);
	}

	/**
	 * Method getClassDescription.
	 * 
	 * @param c
	 *            Class<?>
	 * @return String
	 */
	public static String getClassDescription(Class<?> c) {
		try {
			if (c.isAnnotationPresent(HtmlDoc.class)) {
				StringBuilder ret = new StringBuilder();
				HtmlDoc doc = c.getAnnotation(HtmlDoc.class);
				ret.append(String.format("<h2>%s</h2>", doc.description()));

				if (!doc.post().isEmpty()) {
					ret.append(String.format("<b>POST</b><br/>%s", doc.post()));
				}
				if (!doc.get().isEmpty()) {
					ret.append(String.format("<b>GET</b><br/>%s", doc.get()));
				}
				if (!doc.put().isEmpty()) {
					ret.append(String.format("<b>PUT</b><br/>%s", doc.put()));
				}
				if (!doc.delete().isEmpty()) {
					ret.append(String.format("<b>DELETE</b><br/>%s",
							doc.delete()));
				}
			}
		} catch (Exception e) {
			logger.error(String.format(
					"Error retrieving description for class [%s]", c), e);
		}
		return String.format("no description for %s", c);
	}

	/**
	 * Method getClassDescription.
	 * 
	 * @param className
	 *            String
	 * @return String
	 */
	public static String getClassDescription(String className) {
		try {
			return getClassDescription(Class.forName(className));
		} catch (Exception e) {
			logger.error(String.format(
					"Error retrieving description for class [%s]", className),
					e);
		}
		return String.format("no description for %s", className);
	}

	/**
	 * Method getTypedQuery.
	 * 
	 * @param uriInfo
	 *            UriInfo
	 * @param em
	 *            EntityManager
	 * @param entity
	 *            Class<?>
	 * @param filterName
	 *            String
	 * @param filterValue
	 *            String
	 * 
	 * @return TypedQuery<?>
	 * @throws SecurityException
	 * @throws NoSuchFieldException
	 * @throws FileNotFoundException
	 * @throws UnsupportedEncodingException
	 */
	
	
	public static TypedQuery<?> getTypedQuery(UriInfo uriInfo,
			EntityManager em, Class<?> entity, String filterName,
			String filterValue) {
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<?> query = cb.createQuery(entity);
		Root<?> queryRoot = query.from(entity);
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
		String filterEntityName = "";
		Predicate predicate = cb.conjunction();
		for (String filterFiled : params.keySet()) {
			if (filterFiled.startsWith(FILTER)) {
				if (filterFiled.endsWith(FILTER_TYPE_EXACT)
						|| filterFiled.endsWith(FILTER_TYPE_CONTAINS)
						|| filterFiled.endsWith(FILTER_TYPE_END)) {
					filterEntityName = filterFiled.substring(
							filterFiled.indexOf("_") + 1,
							filterFiled.lastIndexOf("_"));
				} else {
					filterEntityName = filterFiled.substring(7);
				}
				try {
					// Check if there are field in entity
					queryRoot.get(filterEntityName);
					
					switch(filterFiled.substring(filterFiled.indexOf('_'))){
						case FILTER_TYPE_EXACT: 
							System.out.println("...........--..........");
							predicate = cb.and(predicate, cb.equal(
									queryRoot.get(filterEntityName),
									params.getFirst(filterFiled)));
							break;
						case FILTER_TYPE_CONTAINS:
							predicate = cb.and(predicate, cb.like(
									queryRoot.<String> get(filterEntityName),
									"%" + params.getFirst(filterFiled) + "%"));
							break;
						default:
							if (queryRoot.get(filterEntityName).getJavaType().toString()
									.contains("java.sql.Timestamp")) {

								if (filterFiled.endsWith(FILTER_TYPE_END)) {
									predicate = cb
											.and(predicate,
													cb.lessThanOrEqualTo(
															queryRoot
																	.<Timestamp> get(filterEntityName),
															getTimestamp(params
																	.getFirst(filterFiled))));
								} else {
									predicate = cb
											.and(predicate,
													cb.greaterThanOrEqualTo(
															queryRoot
																	.<Timestamp> get(filterEntityName),
															getTimestamp(params
																	.getFirst(filterFiled))));
								}
							} else {
									predicate = cb
											.and(predicate,
													cb.like(queryRoot
															.<String> get(filterEntityName),
															params.getFirst(filterFiled)
																	+ "%"));
							}
							break;
					}
					
					/*
					if (filterFiled.endsWith(FILTER_TYPE_EXACT)) {
						predicate = cb.and(predicate, cb.equal(
								queryRoot.get(filterEntityName),
								params.getFirst(filterFiled)));
					} else {
						if (filterFiled.endsWith(FILTER_TYPE_CONTAINS)) {
							predicate = cb.and(predicate, cb.like(
									queryRoot.<String> get(filterEntityName),
									"%" + params.getFirst(filterFiled) + "%"));
						} else {
							if (queryRoot.get(filterEntityName).getJavaType().toString()
									.contains("java.sql.Timestamp")) {

								if (filterFiled.endsWith(FILTER_TYPE_END)) {
									predicate = cb
											.and(predicate,
													cb.lessThanOrEqualTo(
															queryRoot
																	.<Timestamp> get(filterEntityName),
															getTimestamp(params
																	.getFirst(filterFiled))));
								} else {
									predicate = cb
											.and(predicate,
													cb.greaterThanOrEqualTo(
															queryRoot
																	.<Timestamp> get(filterEntityName),
															getTimestamp(params
																	.getFirst(filterFiled))));
								}
							} else {
									predicate = cb
											.and(predicate,
													cb.like(queryRoot
															.<String> get(filterEntityName),
															params.getFirst(filterFiled)
																	+ "%"));

							}
						}
					}*/
					}  catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();				}
			}
		}
		if (null != filterName) {
			predicate = cb.and(predicate,
					cb.equal(queryRoot.get(filterName), filterValue));
		}
		query.where(predicate);
		return em.createQuery(query);

	}
	/**
	 * Method getCountTypedQuery.
	 * 
	 * @param uriInfo
	 *            UriInfo
	 * @param em
	 *            EntityManager
	 * @param entity
	 *            Class<?>
	 * @param filterName
	 *            String
	 * @param filterValue
	 *            String
	 * @return TypedQuery<?>
	 * @throws SecurityException
	 * @throws NoSuchFieldException
	 */
	
	
	public static TypedQuery<?> getCountTypedQuery(UriInfo uriInfo,
			EntityManager em, Class<?> entity, String filterName,
			String filterValue) {
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<Long> query = cb.createQuery(Long.class);
		Root<?> queryRoot = query.from(entity);
		query.select(cb.count(queryRoot));

		Predicate predicate = cb.conjunction();
		String filterEntityName = "";
		MultivaluedMap<String, String> params = uriInfo.getQueryParameters();
		for (String filterFiled : params.keySet()) {
			if (filterFiled.startsWith(FILTER)) {
				if (filterFiled.endsWith(FILTER_TYPE_EXACT)
						|| filterFiled.endsWith(FILTER_TYPE_CONTAINS)
						|| filterFiled.endsWith(FILTER_TYPE_END)) {
					filterEntityName = filterFiled.substring(
							filterFiled.indexOf("_") + 1,
							filterFiled.lastIndexOf("_"));
				} else {
					filterEntityName = filterFiled.substring(7);
				}
				try {
					// Check if there are field in entity
					queryRoot.get(filterEntityName);
					if (filterFiled.endsWith(FILTER_TYPE_EXACT)) {
						predicate = cb.and(predicate, cb.equal(
								queryRoot.get(filterEntityName),
								params.getFirst(filterFiled)));
					} else {
						if (filterFiled.endsWith(FILTER_TYPE_CONTAINS)) {
							predicate = cb.and(predicate, cb.like(
									queryRoot.<String> get(filterEntityName),
									"%" + params.getFirst(filterFiled) + "%"));
						} else {
							if (entity.getDeclaredField(filterEntityName)
									.getType().toString()
									
									.contains("java.sql.Timestamp")) {
								if (filterFiled.endsWith(FILTER_TYPE_END)) {
									predicate = cb
											.and(predicate,
													cb.lessThanOrEqualTo(
															queryRoot
																	.<Timestamp> get(filterEntityName),
															getTimestamp(params
																	.getFirst(filterFiled))));
								} else {
									predicate = cb
											.and(predicate,
													cb.greaterThanOrEqualTo(
															queryRoot
																	.<Timestamp> get(filterEntityName),
															getTimestamp(params
																	.getFirst(filterFiled))));
								}
							} else {
								if (entity.getDeclaredField(filterEntityName)
										.getType().isEnum()) {
									Method methodFromName = entity
											.getDeclaredField(filterEntityName)
											.getType()
											.getDeclaredMethod("fromName",
													String.class);
									@SuppressWarnings("unchecked")
									Enum enumVal = Enum
											.valueOf(
													(Class<Enum>) entity
															.getDeclaredField(
																	filterEntityName)
															.getType(),
													methodFromName
															.invoke("fromName",
																	params.getFirst(filterFiled))
															.toString());
									predicate = cb
											.and(predicate,
													cb.like(queryRoot
															.<String> get(filterEntityName),
															enumVal.ordinal()
																	+ "%"));

								} else {
									predicate = cb
											.and(predicate,
													cb.like(queryRoot
															.<String> get(filterEntityName),
															params.getFirst(filterFiled)
																	+ "%"));
								}
							}
						}
					}
				} catch (Exception e) {
					// skip filter field
					logger.error(ERROR_REASON_REFLECTION_ERROR, e);
				}
			}
		}
		if (null != filterName) {
			predicate = cb.and(predicate,
					cb.equal(queryRoot.get(filterName), filterValue));
		}
		query.where(predicate);

		return em.createQuery(query);

	}

	/**
	 * Method hasSortOrFilter.
	 * 
	 * @param uriInfo
	 *            UriInfo
	 * 
	 * @return boolean
	 */
	public static boolean hasSortOrFilter(UriInfo uriInfo) {
		MultivaluedMap<String, String> params = uriInfo.getQueryParameters();
		if (params.containsKey(SORT_FIELD))
			return true;
		for (String filterFiled : params.keySet()) {
			if (filterFiled.startsWith(FILTER))
				return true;
		}
		return false;
	}

}
