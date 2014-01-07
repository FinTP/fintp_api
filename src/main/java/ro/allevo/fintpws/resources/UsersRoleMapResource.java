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

//package ro.allevo.fintpws.resources;
//
//import java.math.BigDecimal;
//
//import javax.persistence.EntityManager;
//import javax.ws.rs.core.UriInfo;
//
//import org.apache.logging.log4j.LogManager;
//import org.apache.logging.log4j.Logger;
//
//import ro.allevo.fintpws.model.QueueEntity;
//import ro.allevo.fintpws.model.RoleEntity;
//import ro.allevo.fintpws.model.UserEntity;
//
//public class UsersRoleMapResource extends PagedCollection{
//
//	/**
//	 * Field logger
//	 */
//	private static final Logger logger = LogManager
//			.getLogger(RoleResource.class);
//
//	/**
//	 * Field ERROR_MESSAGE_GET_ROLE. (value is ""Error returning role: "")
//	 */
//	static final String ERROR_MESSAGE_GET_ROLE = "Error returning role : ";
//	/**
//	 * Field ERROR_MESSAGE_ROLE_NOT_FOUND. (value is ""Role with roleid [%s] not
//	 * found"")
//	 */
//	static final String ERROR_MESSAGE_ROLE_NOT_FOUND = "Role with roleid [%s] not found";
//	/**
//	 * Field ERROR_REASON_JSON. (value is ""json"")
//	 */
//	static final String ERROR_REASON_JSON = "json";
//	/**
//	 * Field ERROR_REASON_NUMBER_FORMAT. (value is ""number format"")
//	 */
//	static final String ERROR_REASON_NUMBER_FORMAT = "number format";
//	/**
//	 * Field ERROR_REASON_CONFLICT. (value is ""conflict"")
//	 */
//	static final String ERROR_REASON_CONFLICT = "conflict";
//	/**
//	 * Field ERROR_REASON_ROLLBACK. (value is ""rollback"")
//	 */
//	static final String ERROR_REASON_ROLLBACK = "rollback";
//
//	/**
//	 * Field uriInfo
//	 */
//	private UriInfo uriInfo;
//	/**
//	 * Field entityManagerConfig.
//	 */
//	private EntityManager entityManagerConfig;
//	/**
//	 * roleid
//	 */
//	private String roleid;
//
//	
//	
//	/**
//	 * Constructor for RoleResource
//	 * 
//	 * @param uriInfo
//	 * @param entityManagerConfig
//	 * @param roleid
//	 */
//	public UsersRoleMapResource(UriInfo uriInfo, EntityManager entityManagerConfig,
//			String roleid, UserEntity userEntity) {
//		super(uriInfo,
//				entityManagerConfig.createNativeQuery("", arg1), totalQuery)
//		this.uriInfo = uriInfo;
//		this.entityManagerConfig = entityManagerConfig;
//		this.roleid = roleid;
//		roleEntity = RoleResource.findByRoleid(entityManagerConfig, roleid);
//		
//	}
//}
