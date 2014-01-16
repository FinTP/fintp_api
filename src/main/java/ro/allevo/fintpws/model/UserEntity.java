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

import java.util.Collection;
import java.util.List;

import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Persistence;
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import javax.persistence.TypedQuery;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;


@Entity
@Table(schema = "FINCFG", name="USERS")
@NamedQueries({
	@NamedQuery(name = "UserEntity.findByUsername", query = "select q from UserEntity q where trim(q.username)=:username"), 
	@NamedQuery(name = "UserEntity.findAll", query = "SELECT t FROM UserEntity t order by t.username asc"),
	@NamedQuery(name = "UserEntity.findTotal", query = "SELECT count(t.username) FROM UserEntity t")
})

@Cacheable(false)
public class UserEntity implements UserDetails  {
	private static final long serialVersionUID = 1L;
	
	public UserEntity(){
	}
	
	@Id
	@GeneratedValue(generator="UserIdGenerator")
	@TableGenerator(name="UserIdGenerator", table="FINCFG.IDGENLIST",
	pkColumnName="TABCOLNAME", valueColumnName="IDVALUE",
	pkColumnValue="USERS_USERID") 
	@Column(unique=true, nullable=false, precision=38)
	private long userid;
	
	@Column(length = 256, unique=true)
	private String username;
	
	@Column(length = 256, nullable = false)
	private String password;
	
	@Column(length = 20)
	private String firstname;
	
	@Column(length = 30)
	private String lastname;
	
	@Column(length = 20)
	private String skincolor;
	
	@Column
	private long islocked;
	
	@Column
	private long noretry;
	
	@Column(length = 150)
	private String email;
	
	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		
		EntityManagerFactory configEntityManagerFactory = Persistence
				.createEntityManagerFactory("fintpCFG");
		EntityManager emc = configEntityManagerFactory.createEntityManager();

		TypedQuery<RoleEntity> query = emc.createNamedQuery(
				"RoleEntity.findUserAuthorities", RoleEntity.class);
		List<RoleEntity> authorities = query.setParameter("userid",
				userid).getResultList();
		return authorities;
	}

	public long getUserid(){
		return userid;
	}
	
	public void setUserid(long userid){
		this.userid = userid;
	}
	
	@Override
	public String getPassword() {
		return password;
	}
	
	public void setPassword(String password){
		this.password = password;
	}

	@Override
	public String getUsername() {
		return username;
	}
	
	public void setUsername(String username){
		this.username = username;
	}
	

	@Override
	public boolean isAccountNonExpired() {
		return true;
	}

	@Override
	public boolean isAccountNonLocked() {
		return true;
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return true;
	}

	@Override
	public boolean isEnabled() {
		return true;
	}
	
	@Override
	public String toString(){
		String res = "";
		res+=getUsername();
		return res;
	}

	public String getFirstname() {
		return firstname;
	}

	public void setFirstname(String firstname) {
		this.firstname = firstname;
	}

	public String getLastname() {
		return lastname;
	}

	public void setLastname(String lastname) {
		this.lastname = lastname;
	}

	public String getSkincolor() {
		return skincolor;
	}

	public void setSkincolor(String skincolor) {
		this.skincolor = skincolor;
	}

	public long getIslocked() {
		return islocked;
	}

	public void setIslocked(long islocked) {
		this.islocked = islocked;
	}

	public long getNoretry() {
		return noretry;
	}

	public void setNoretry(long noretry) {
		this.noretry = noretry;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

}
