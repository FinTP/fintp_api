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

package ro.allevo.fintpws.test;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Random;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import ro.allevo.fintpws.model.QueueEntity;
import ro.allevo.fintpws.model.RoutedMessageEntity;

/**
 */
public class TestUtils {
	/**
	 * Field logger.
	 */
	private static Logger logger = LogManager
			.getLogger(TestQueuesResource.class.getName());

	static final String URL_BASE = "http://localhost:8080/fintpWebServices/";

	/**
	 * Gets deployed application url from system property or the default value defined in URL_BASE.
	 * System property may be provided as maven argLine
	 * @return url of web application that is being tested
	 */
	public static String getUrlBase() {
		String argUrl = System.getProperty("webAppUrl");
		if (argUrl != null)
			return argUrl;
		return URL_BASE;
	}

	/**
	 * Method fillResourceData.
	 * 
	 * @param entityObject
	 *            JSONObject
	 * @param entity
	 *            Object
	 * @return JSONObject
	 */
	public static JSONObject fillResourceData(JSONObject entityObject,
			Object entity) {

		String fieldName = "";
		boolean isTransient = false;
		int columnSize = 0;
		Field field = null;
		boolean haveAnnotation = false;
		final Method[] allMethods = entity.getClass().getDeclaredMethods();

		for (Method classMethod : allMethods) {
			if (classMethod.getName().startsWith("set")) {
				isTransient = false;
				columnSize = 0;
				haveAnnotation = false;
				fieldName = classMethod.getName().substring(3).toLowerCase();

				try {
					field = entity.getClass().getDeclaredField(fieldName);
					Annotation[] annotations = field.getDeclaredAnnotations();
					for (Annotation ant : annotations) {
						isTransient = ant.toString().contains("Transient");
						columnSize = getColumnSize(ant.toString());
						haveAnnotation = true;
					}

					if (!isTransient || !haveAnnotation) {
						if (field.getType().toString().contains("BigDecimal")
								|| field.getType().toString().contains("long")) {
							entityObject.put(fieldName, new Random()
									.nextInt((int) Math.pow(10, columnSize)));

						} else {
							if (field.getType().toString()
									.contains("Timestamp")) {
								entityObject.put(fieldName,
										new SimpleDateFormat(
												"yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
												.format(Calendar.getInstance()
														.getTime()));
							} else {
								final String randomVarchar = RandomStringUtils
										.randomAlphanumeric(columnSize);
								entityObject.put(fieldName, randomVarchar);
							}
						}
					}
				} catch (SecurityException e) {
					logger.error("reflectin security exception");
				} catch (NoSuchFieldException e) {
					logger.error("reflection field exception");
				} catch (JSONException e) {
					logger.error("reflection json exception");
				}
			}
		}
		return entityObject;
	}

	/**
	 * Method compareJSONObjects.
	 * 
	 * @param entity
	 *            Object
	 * @param insertedEntity
	 *            JSONObject
	 * @param readedEntity
	 *            JSONObject
	 * @param String
	 *            fields excluded from comparison
	 * @return boolean
	 */
	public static boolean compareJSONObjects(Object entity,
			JSONObject insertedEntity, JSONObject readedEntity, String fields) {

		String fieldName = "";
		boolean isTransient = false;
		Field field = null;
		boolean haveAnnotation = false;
		final Method[] allMethods = entity.getClass().getDeclaredMethods();
		boolean isEqual = true;

		for (Method classMethod : allMethods) {
			if (classMethod.getName().startsWith("set")) {
				isTransient = false;
				haveAnnotation = false;
				fieldName = classMethod.getName().substring(3).toLowerCase();

				try {
					field = entity.getClass().getDeclaredField(fieldName);
					// Ex. Annotation : @javax.persistence.Column(name=,
					// unique=false, nullable=true, insertable=true,
					// updatable=true, columnDefinition=, table=, length=30,
					// precision=0, scale=0)
					Annotation[] annotations = field.getDeclaredAnnotations();
					for (Annotation ant : annotations) {
						isTransient = ant.toString().contains("Transient");
						haveAnnotation = true;
					}
					if (!fields.contains(fieldName)
							&& (!isTransient || !haveAnnotation)
							&& (!field.getType().toString()
									.contains("ro.allevo.fintpws.model"))
							&& (!field.getType().toString()
									.contains("java.util.List"))) {

						if (field.getType().toString().contains("BigDecimal")) {
							if (insertedEntity.optDouble(fieldName) != readedEntity
									.optDouble(fieldName)) {
								logger.debug(fieldName);

								isEqual = false;
							}
						} else {
							if (field.getType().toString()
									.contains("Timestamp")) {
								Timestamp first = new Timestamp(
										new SimpleDateFormat(
												"yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
												.parse(insertedEntity
														.getString(fieldName))
												.getTime());
								Timestamp second = new Timestamp(
										new SimpleDateFormat(
												"yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
												.parse(readedEntity
														.optString(fieldName))
												.getTime());
								if (first.compareTo(second) != 0) {
									logger.debug(fieldName);
									isEqual = false;
								}
							} else {
								if (!insertedEntity.optString(fieldName)
										.equals(readedEntity
												.optString(fieldName))) {
									logger.debug(fieldName);
									isEqual = false;
								}
							}
						}

					}
				} catch (SecurityException e) {
					logger.error("reflectin security exception");
					isEqual = false;
				} catch (NoSuchFieldException e) {
					logger.error("reflection field exception");
					if(!fields.contains(fieldName))
						isEqual = false;
				} catch (JSONException e) {
					logger.error("json exception");
					isEqual = false;
				} catch (ParseException e) {
					logger.error("parse exception");
					isEqual = false;
				}
			}
		}
		return isEqual;
	}

	/**
	 * Method getColumnSize.
	 * 
	 * @param columnAnnotation
	 *            String
	 * @return int
	 */
	private static int getColumnSize(String columnAnnotation) {
		int length = 0;
		String precisionString = "", lengthString = "";

		if (columnAnnotation.contains("precision")) {
			precisionString = columnAnnotation.substring(columnAnnotation
					.indexOf("precision") + 10);

			precisionString = precisionString.substring(0,
					precisionString.indexOf(","));
		}

		if (columnAnnotation.contains("length")) {

			lengthString = columnAnnotation.substring(columnAnnotation
					.indexOf("length") + 7);
			lengthString = lengthString.substring(0, lengthString.indexOf(","));
		}
		if (precisionString.equals("0") || precisionString.equals("")) {
			columnAnnotation = lengthString;
		} else {
			columnAnnotation = precisionString;
		}

		try {
			length = Integer.valueOf(columnAnnotation);
		} catch (NumberFormatException e) {
			return 0;
		}
		return length;
	}

	/**
	 * Method main.
	 * 
	 * @param args
	 *            String[]
	 */
	public static void main(String[] args) {

		JSONObject entityObject = fillResourceData(new JSONObject(),
				new QueueEntity());
		entityObject = fillResourceData(new JSONObject(),
				new RoutedMessageEntity());
		logger.debug(entityObject);
	}
}
