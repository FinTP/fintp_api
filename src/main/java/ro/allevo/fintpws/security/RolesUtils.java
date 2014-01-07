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

package ro.allevo.fintpws.security;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.Query;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import ro.allevo.fintpws.model.QueueEntity;
import ro.allevo.fintpws.model.RoleEntity;
import ro.allevo.fintpws.model.UserEntity;

public class RolesUtils {

	public static boolean hasUserOrAdministratorRole(){
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		UserEntity loggedUser = (UserEntity)authentication.getPrincipal();
		List<RoleEntity> roles =(List<RoleEntity>) loggedUser.getAuthorities();
		for(int roleIndex = 0; roleIndex < roles.size(); roleIndex++){
			if(roles.get(roleIndex).getAuthority().equals("Administrator") || 
					roles.get(roleIndex).getAuthority().equals("Basic user")){
				return true;
			}
				
		}
		return false;
	}
	
	public static boolean hasAdministratorRole(){
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		UserEntity loggedUser = (UserEntity)authentication.getPrincipal();
		List<RoleEntity> roles =(List<RoleEntity>) loggedUser.getAuthorities();
		for(int roleIndex = 0; roleIndex < roles.size(); roleIndex++){
			if(roles.get(roleIndex).getAuthority().equals("Administrator")){
				return true;
			}
				
		}
		return false;
	}
	
	public static boolean hasReportsRole(){
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		UserEntity loggedUser = (UserEntity)authentication.getPrincipal();
		List<RoleEntity> roles =(List<RoleEntity>) loggedUser.getAuthorities();
		for(int roleIndex = 0; roleIndex < roles.size(); roleIndex++){
			if(roles.get(roleIndex).getAuthority().equals("Reports")){
				return true;
			}
		}
		return false;
	}
	
	public static boolean hasReadAuthorityOnQueue(QueueEntity queueEntity) {
		EntityManagerFactory configEntityManagerFactory = Persistence
				.createEntityManagerFactory("fintpCFG");
		EntityManager entityManagerConfig = configEntityManagerFactory.createEntityManager();
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		UserEntity loggedUser = (UserEntity)authentication.getPrincipal();
		Query query = entityManagerConfig.createQuery(
				"SELECT ur.roleid FROM UserRoleEntity ur, QueuesRoleMapEntity qr " +
				"WHERE ur.roleid = qr.roleid " +
				"AND ur.userid=:userid " +
				"AND qr.queueid=:queueid");
		query.setParameter("userid", loggedUser.getUserid());
		query.setParameter("queueid", queueEntity.getGuid());
		List roles = query.getResultList();
		if(roles.isEmpty()){
			return false;
		}
		return true;
	}
	
	public static boolean hasWriteAuthorityOnQueue(QueueEntity queueEntity) {
		EntityManagerFactory configEntityManagerFactory = Persistence
				.createEntityManagerFactory("fintpCFG");
		EntityManager entityManagerConfig = configEntityManagerFactory.createEntityManager();
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		UserEntity loggedUser = (UserEntity)authentication.getPrincipal();
		Query query = entityManagerConfig.createQuery(
				"SELECT ur.roleid FROM UserRoleEntity ur, QueuesRoleMapEntity qr " +
				"WHERE ur.roleid = qr.roleid " +
				"AND ur.userid=:userid " +
				"AND qr.queueid=:queueid " +
				"AND qr.actiontype = 'RW'");
		query.setParameter("userid", loggedUser.getUserid());
		query.setParameter("queueid", queueEntity.getGuid());
		List roles = query.getResultList();
		if(roles.isEmpty()){
			return false;
		}
		return true;
	}
}
