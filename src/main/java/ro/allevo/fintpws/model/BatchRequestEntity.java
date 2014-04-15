package ro.allevo.fintpws.model;

import java.io.Serializable;
import javax.persistence.*;


/**
 * The persistent class for the batchrequests database table.
 * 
 */
@Entity
@Table(schema = "FINDATA", name="BATCHREQUESTS")
public class BatchRequestEntity implements Serializable {
	private static final long serialVersionUID = 1L;
	
	@Id
	private String batchuid;
	private String groupkey;

	public BatchRequestEntity() {
	}


	public String getBatchuid() {
		return this.batchuid;
	}

	public void setBatchuid(String batchuid) {
		this.batchuid = batchuid;
	}


	public String getGroupkey() {
		return this.groupkey;
	}

	public void setGroupkey(String groupkey) {
		this.groupkey = groupkey;
	}

}