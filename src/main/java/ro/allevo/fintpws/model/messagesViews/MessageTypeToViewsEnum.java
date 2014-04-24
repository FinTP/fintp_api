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

package ro.allevo.fintpws.model.messagesViews;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.Query;

/**
 * @version $Revision: 1.0 $
 * @author remus
 * Enumeration of all VIEWS, mapping message types to Entities 
 *
 */
public enum MessageTypeToViewsEnum {

	/*
	 * TODO: check if it is OK to build queries, instead of hardcoding them
	 * thus, when implementing MessageEntity one should respect naming convensions
	 * (resolve this after the named query task is solved
	 */
	MT_103("103", Mt103View.class), 
	MT_202("202", Mt202View.class),
	MT_FITOFICSTMRCDTTRF("fitoficstmrcdttrf", MtFitoficstmrcdttrfView.class),
	MT_COREBLKLRGRMTCDTTRF("coreblklrgrmtcdttrf", MtCoreblklrgrmtcdttrfView.class);
	
	
	private String type;
	private Class<? extends SpecificViewAbstract> clazz;
	private static Map<String, MessageTypeToViewsEnum> stringToMessageTypeEnum;
	
	
	private MessageTypeToViewsEnum(String type, Class<? extends SpecificViewAbstract> clazz) {
		this.type = type;
		this.clazz = clazz;
	}
	
	private static void initMapping(){
		stringToMessageTypeEnum = new HashMap<String, MessageTypeToViewsEnum>();
		for(MessageTypeToViewsEnum mt : values()){
			stringToMessageTypeEnum.put(mt.type, mt);
		}
	}
	
	public static MessageTypeToViewsEnum getMessageType(String type){
		if(stringToMessageTypeEnum == null){
			initMapping();
		}
		return stringToMessageTypeEnum.get(type.toLowerCase());
	}


	public Query getItemsQuery(
			EntityManager entityManager, Timestamp time) {
		if(time == null){
			return entityManager.createNativeQuery("SELECT v FROM " + clazz.getSimpleName() + " v", clazz);
		}else{
			return entityManager.createNativeQuery("SELECT v FROM " + clazz.getSimpleName() + " v " +
					" WHERE v.insertdate < :at", clazz).setParameter("at", time);
		}
	}

	public Query getTotalQuery(
			EntityManager entityManager, Timestamp time) {
		if(time == null){
			return entityManager.createNativeQuery("SELECT count(v.guid) FROM " + clazz.getSimpleName() + " v", clazz);
		}else{
			return entityManager.createNativeQuery("SELECT count(v.guid) FROM " + clazz.getSimpleName() + " v " +
					" WHERE v.insertdate < :at", clazz).setParameter("at", time);
		}
	}
	
	public Query getFindByGuidQuery(
			EntityManager entityManager, String guid){
		return entityManager.createNativeQuery("SELECT * FROM " + clazz.getSimpleName() +
				" WHERE GUID = ?" , clazz).setParameter(1, guid);
	}
	
	public Class<? extends SpecificViewAbstract> getClazz() {
		return clazz;
	}
}
