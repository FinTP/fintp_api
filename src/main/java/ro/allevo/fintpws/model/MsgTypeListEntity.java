package ro.allevo.fintpws.model;

import java.io.Serializable;

import javax.persistence.*;


/**
 * The persistent class for the msgtypes database table.
 * 
 */
@Entity
@Table(schema = "FINCFG", name = "MSGTYPES")
@NamedQueries({ 
	@NamedQuery(name = "MsgTypeListEntity.findByMsgType", 
			query = "select m.storage from MsgTypeListEntity m where trim(m.messagetype) = :messagetype")})
public class MsgTypeListEntity implements Serializable {
	private static final long serialVersionUID = 1L;
		
	
	private Integer mtid;
	
	@Column(length = 100)
	private String businessarea;

	private Integer displayorder;
	
	@Column(length = 50)
	private String friendlyname;

	@Column(length = 50)
	@Id
	private String messagetype;
	
	@Column(length = 35)
	private String parentmsgtype;

	private String reportingstorage;
	
	@Column(length = 35)
	private String storage;

	public MsgTypeListEntity() {
	}

	public Integer getMtid() {
		return this.mtid;
	}

	public void setMtid(Integer mtid) {
		this.mtid = mtid;
	}

	public String getBusinessarea() {
		return this.businessarea;
	}

	public void setBusinessarea(String businessarea) {
		this.businessarea = businessarea;
	}

	public Integer getDisplayorder() {
		return this.displayorder;
	}

	public void setDisplayorder(Integer displayorder) {
		this.displayorder = displayorder;
	}

	public String getFriendlyname() {
		return this.friendlyname;
	}

	public void setFriendlyname(String friendlyname) {
		this.friendlyname = friendlyname;
	}

	public String getMessagetype() {
		return this.messagetype;
	}

	public void setMessagetype(String messagetype) {
		this.messagetype = messagetype;
	}

	public String getParentmsgtype() {
		return this.parentmsgtype;
	}

	public void setParentmsgtype(String parentmsgtype) {
		this.parentmsgtype = parentmsgtype;
	}

	public String getReportingstorage() {
		return this.reportingstorage;
	}

	public void setReportingstorage(String reportingstorage) {
		this.reportingstorage = reportingstorage;
	}

	public String getStorage() {
		return this.storage;
	}

	public void setStorage(String storage) {
		this.storage = storage;
	}

}