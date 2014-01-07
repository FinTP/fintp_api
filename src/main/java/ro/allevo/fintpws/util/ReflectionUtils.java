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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import ro.allevo.fintpws.resources.ApiResource;

/**
 */
public class ReflectionUtils {
	/**
	 * Field logger.
	 */
	private static Logger logger = LogManager.getLogger(ReflectionUtils.class
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
	 * Field ERROR_REASON_REFLECTION_ERROR. (value is ""Something bad happened when reflecting"")
	 */
	static final String ERROR_REASON_REFLECTION_ERROR = "Something bad happened when reflecting";

	/**
	 * Returns the resource formatted as json
	 * 
	 * @param object
	 *            Object
	 * @param path
	 *            String
	 * @return JSONObject
	 * @throws JSONException
	 */
	public static JSONObject asJson(Object object, String path)
			throws JSONException {
		JSONObject objectAsJson = ApiResource.getMetaResource(path,
				object.getClass());

		Method[] allMethods = object.getClass().getDeclaredMethods();
		for (Method classMethod : allMethods) {
			String methodPrefix = classMethod.getName().substring(3)
					.toLowerCase();
			if (classMethod.getName().startsWith(ENTITYMETHODGET)) {
				try {
					objectAsJson.put(
							methodPrefix,
							object.getClass()
									.getDeclaredMethod(classMethod.getName())
									.invoke(object));
				} catch (IllegalAccessException e) {
					logger.error(ERROR_REASON_REFLECTION_ERROR, e);
				} catch (InvocationTargetException e) {
					logger.error(ERROR_REASON_REFLECTION_ERROR, e);
				} catch (NoSuchMethodException e) {
					logger.error(ERROR_REASON_REFLECTION_ERROR, e);
				} catch (JSONException e) {
					logger.error(String
							.format("Error access object filed ", methodPrefix,
									" from JSONObject", methodPrefix, " "), e);
				}
			}
		}
		return objectAsJson;
	}

	/**
	 * Method updateEntity.
	 * 
	 * @param entity
	 *            Object
	 * @param jsonEntity
	 *            JSONObject
	 * @throws JSONException
	 */
	public static void updateEntity(Object entity, JSONObject jsonEntity)
			throws JSONException {
		String methodPrefix = "";
		try {
			Method[] allMethods = entity.getClass().getDeclaredMethods();
			for (Method classMethod : allMethods) {
				methodPrefix = classMethod.getName().substring(3).toLowerCase();
				if (!jsonEntity.isNull(methodPrefix)
						&& classMethod.getName().startsWith(ENTITYMETHODSET)) {
					entity.getClass()
							.getDeclaredMethod(
									classMethod.getName(),
									entity.getClass()
											.getDeclaredField(methodPrefix)
											.getType())
							.invoke(entity,
									asEntityFieldType(entity, methodPrefix,
											jsonEntity.get(methodPrefix)
													.toString()));
				}
			}
		} catch (IllegalAccessException e) {
			logger.error(ERROR_REASON_REFLECTION_ERROR, e);
		} catch (InvocationTargetException e) {
			logger.error(ERROR_REASON_REFLECTION_ERROR, e);
		} catch (NoSuchMethodException e) {
			logger.error(ERROR_REASON_REFLECTION_ERROR, e);
		} catch (NoSuchFieldException e) {
			logger.error(ERROR_REASON_REFLECTION_ERROR, e);
		} catch (JSONException e) {
			logger.error(String.format("Error access queue  filed ",
					methodPrefix, " from JSONObject", methodPrefix, " "), e);
		}
	}

	/**
	 * Return the value formatted as field type from QueueEntity
	 * @param entity
	 *            Object
	 * @param name
	 *            String
	 * @param val
	 *            String
	 * @return JSONObject 
	 * @throws JSONException
	 */
	private static Object asEntityFieldType(Object entity, String name,
			String val) {
		// TODO timestamp
		String type;
		Object formatedVal = null;

		try {
			type = entity.getClass().getDeclaredField(name).getType()
					.toString();

			if (type.contains("BigDecimal")) {
				formatedVal = new BigDecimal(val);
			}
			if (type.contains("String")) {
				formatedVal = new String(val);
			}

		} catch (NoSuchFieldException e) {
			logger.error(String.format("Error access queue filed ", name), e);
		}

		return formatedVal;
	}

	/**
	 * Method asJson.Call get methods from entity.
	 * 
	 * @param object
	 *            Object
	 * @param objectAsJson
	 *            JSONObject
	 * @return JSONObject 
	 * @throws JSONException
	 */
	public static JSONObject asReflectedJson(Object object,
			JSONObject objectAsJson) throws JSONException {
		Method[] allMethods = object.getClass().getDeclaredMethods();
		for (Method method : allMethods) {
			if (method.getName().startsWith(ENTITYMETHODGET)) {
				String fieldName = method.getName().substring(3).toLowerCase();

				try {
					objectAsJson.put(fieldName, method.invoke(object));
				} catch (IllegalAccessException e) {
					// i'm just doing nothing on purpose and it's bad
					logger.error(ERROR_REASON_REFLECTION_ERROR, e);
				} catch (IllegalArgumentException e) {
					// i'm just doing nothing on purpose and it's bad
					logger.error(ERROR_REASON_REFLECTION_ERROR, e);
				} catch (InvocationTargetException e) {
					// i'm just doing nothing on purpose and it's bad
					logger.error(ERROR_REASON_REFLECTION_ERROR, e);
				}
			}
		}
		return objectAsJson;
	}
}
