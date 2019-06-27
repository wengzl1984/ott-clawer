package com.webmagic.entity;

import java.io.Serializable;
import java.sql.Date;

public class VcmClawerLogVo implements Serializable {
	private static final long serialVersionUID = 2L;
	private String responeContext;
	private Date execDate;
	private long id;
	private int status;
	private String infoDesc;
	private int errorType;
	private String errorInfo;
	private int taskIf;
	private int relateId;
	private int relateType;
	private String rankingId;
	public String getResponeContext() {
		return responeContext;
	}
	public void setResponeContext(String responeContext) {
		this.responeContext = responeContext;
	}
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public int getStatus() {
		return status;
	}
	public void setStatus(int status) {
		this.status = status;
	}
	public String getInfoDesc() {
		return infoDesc;
	}
	public void setInfoDesc(String infoDesc) {
		this.infoDesc = infoDesc;
	}
	public int getErrorType() {
		return errorType;
	}
	public void setErrorType(int errorType) {
		this.errorType = errorType;
	}
	public String getErrorInfo() {
		return errorInfo;
	}
	public void setErrorInfo(String errorInfo) {
		this.errorInfo = errorInfo;
	}
	public int getTaskIf() {
		return taskIf;
	}
	public void setTaskIf(int taskIf) {
		this.taskIf = taskIf;
	}
	public int getRelateId() {
		return relateId;
	}
	public void setRelateId(int relateId) {
		this.relateId = relateId;
	}
	public int getRelateType() {
		return relateType;
	}
	public void setRelateType(int relateType) {
		this.relateType = relateType;
	}
	public Date getExecDate() {
		return execDate;
	}
	public void setExecDate(Date execDate) {
		this.execDate = execDate;
	}
	public String getRankingId() {
		return rankingId;
	}
	public void setRankingId(String rankingId) {
		this.rankingId = rankingId;
	}
	
}
