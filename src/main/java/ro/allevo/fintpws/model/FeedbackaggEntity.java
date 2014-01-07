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

import java.io.Serializable;
import javax.persistence.*;

import java.sql.Timestamp;

/**
 * The persistent class for the FEEDBACKAGG database table.
 * 
 */
@Entity
@Table(schema = "FINDATA", name = "FEEDBACKAGG")
@Cacheable(false)
public class FeedbackaggEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	@Column(length = 10)
	private String appcode;

	@Column(length = 35)
	private String batchid;

	@Column(length = 10)
	private String batchseq;

	@Id
	@Column(length = 30)
	private String correlid;

	@Column(length = 10)
	private String correspcode;

	@Column(nullable = false)
	private Timestamp insertdate;

	@Column(length = 10)
	private String interfacecode;

	@Column(length = 10)
	private String isession;

	@Column(length = 12)
	private String issuer;

	@Column(length = 10)
	private String networkcode;

	@Column(length = 35)
	private String obatchid;

	@Column(length = 10)
	private String osession;

	@Lob()
	private String payload;

	@Column(length = 50)
	private String requestor;

	@Column(length = 30)
	private String swiftmir;

	@Column(length = 35)
	private String trn;

	@Column(length = 32)
	private String wmqid;

	@OneToOne(targetEntity = RoutedMessageEntity.class, cascade = { CascadeType.ALL }, fetch = FetchType.LAZY)
	@PrimaryKeyJoinColumn (name = "CORRELID", referencedColumnName = "CORRELATIONID")
	private RoutedMessageEntity routedMessageEntity;

	
	public FeedbackaggEntity() {
	}

	public String getAppcode() {
		return this.appcode;
	}

	public void setAppcode(String appcode) {
		this.appcode = appcode;
	}

	public String getBatchid() {
		return this.batchid;
	}

	public void setBatchid(String batchid) {
		this.batchid = batchid;
	}

	public String getBatchseq() {
		return this.batchseq;
	}

	public void setBatchseq(String batchseq) {
		this.batchseq = batchseq;
	}

	public String getCorrelid() {
		return this.correlid;
	}

	public void setCorrelid(String correlid) {
		this.correlid = correlid;
	}

	public String getCorrespcode() {
		return this.correspcode;
	}

	public void setCorrespcode(String correspcode) {
		this.correspcode = correspcode;
	}

	public Timestamp getInsertdate() {
		return this.insertdate;
	}

	public void setInsertdate(Timestamp insertdate) {
		this.insertdate = insertdate;
	}

	public String getInterfacecode() {
		return this.interfacecode;
	}

	public void setInterfacecode(String interfacecode) {
		this.interfacecode = interfacecode;
	}

	public String getIsession() {
		return this.isession;
	}

	public void setIsession(String isession) {
		this.isession = isession;
	}

	public String getIssuer() {
		return this.issuer;
	}

	public void setIssuer(String issuer) {
		this.issuer = issuer;
	}

	public String getNetworkcode() {
		return this.networkcode;
	}

	public void setNetworkcode(String networkcode) {
		this.networkcode = networkcode;
	}

	public String getObatchid() {
		return this.obatchid;
	}

	public void setObatchid(String obatchid) {
		this.obatchid = obatchid;
	}

	public String getOsession() {
		return this.osession;
	}

	public void setOsession(String osession) {
		this.osession = osession;
	}

	public String getPayload() {
		return this.payload;
	}

	public void setPayload(String payload) {
		this.payload = payload;
	}

	public String getRequestor() {
		return this.requestor;
	}

	public void setRequestor(String requestor) {
		this.requestor = requestor;
	}

	public String getSwiftmir() {
		return this.swiftmir;
	}

	public void setSwiftmir(String swiftmir) {
		this.swiftmir = swiftmir;
	}

	public String getTrn() {
		return this.trn;
	}

	public void setTrn(String trn) {
		this.trn = trn;
	}

	public String getWmqid() {
		return this.wmqid;
	}

	public void setWmqid(String wmqid) {
		this.wmqid = wmqid;
	}

}
