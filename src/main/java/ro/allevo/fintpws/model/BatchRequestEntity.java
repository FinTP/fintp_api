package ro.allevo.fintpws.model;

import java.io.Serializable;

import javax.persistence.Cacheable;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.NamedQuery;
import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedStoredProcedureQuery;
import javax.persistence.ParameterMode;
import javax.persistence.StoredProcedureParameter;
import javax.persistence.Table;
import javax.persistence.TableGenerator;



/**
 * The persistent class for the batchrequests database table.
 * 
 */
@Entity
@Table(schema = "FINDATA", name="BATCHREQUESTS")
@NamedQueries({
	@NamedQuery(name = "BatchRequestEntity.findAllByGroupKey", 
			query = "SELECT br FROM BatchRequestEntity br "
			+ "WHERE br.groupkey=:key")
})
@NamedStoredProcedureQuery(
		name="createBatchRequest",
		procedureName="createbatchrequest",
		parameters = {
			@StoredProcedureParameter(type = String.class, name = "inqueuename", mode = ParameterMode.IN),
			@StoredProcedureParameter(type = String.class, name = "inmsgtype", mode = ParameterMode.IN),
			@StoredProcedureParameter(type = String.class, name = "ingroupkey", mode = ParameterMode.IN),
			@StoredProcedureParameter(type = String.class, name = "intimekey", mode = ParameterMode.IN),
			@StoredProcedureParameter(type = String.class, name = "infield1val", mode = ParameterMode.IN),
			@StoredProcedureParameter(type = String.class, name = "infield2val", mode = ParameterMode.IN),
			@StoredProcedureParameter(type = String.class, name = "infield3val", mode = ParameterMode.IN),
			@StoredProcedureParameter(type = String.class, name = "infield4val", mode = ParameterMode.IN),
			@StoredProcedureParameter(type = String.class, name = "infield5val", mode = ParameterMode.IN),
			@StoredProcedureParameter(type = String.class, name = "inusername", mode = ParameterMode.IN)
		})
@Cacheable(false)
public class BatchRequestEntity implements Serializable {
	private static final long serialVersionUID = 1L;
	
	@Id
	@GeneratedValue(generator = "BatchRequestIdGenerator")
	@TableGenerator(name = "BatchRequestIdGenerator", table = "FINCFG.IDGENLIST", pkColumnName = "TABCOLNAME", valueColumnName = "IDVALUE", pkColumnValue = "BATCHREQUESTS_REQUESTID")
	private String requestid;
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