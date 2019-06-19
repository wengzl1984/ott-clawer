package com.webmagic.entity;

import java.io.Serializable;
import java.util.Date;

public class VcmClawerTaskVo implements Serializable {

	private static final long serialVersionUID = 1L;
	private int id;
	private String taskName;
	private int source;
	private int reptileContent;
	private String reptileUrl;
	private int maxPreNum;
	private int frequencyNum;
	private String reptileDate;
	private Date startDate;
	private int status;
	private int createUser;
	private Date createDate;
	private int updateUser;
	private Date updateDate;
	private int delFlag;
	private String ruleJson;
	private String infoDesc;


	@Override
	public String toString() {
		return "VcmClawerTaskVo [id=" + id + ", taskName=" + taskName + ", source=" + source + ", reptileContent="
				+ reptileContent + ", reptileUrl=" + reptileUrl + ", maxPreNum=" + maxPreNum + ", frequencyNum="
				+ frequencyNum + ", reptileDate=" + reptileDate + ", startDate=" + startDate + ", status=" + status
				+ ", createUser=" + createUser + ", createDate=" + createDate + ", updateUser=" + updateUser
				+ ", updateDate=" + updateDate + ", delFlag=" + delFlag + ", ruleJson=" + ruleJson + ", infoDesc="
				+ infoDesc + "]";
	}

	
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getTaskName() {
		return taskName;
	}

	public void setTaskName(String taskName) {
		this.taskName = taskName;
	}

	public int getSource() {
		return source;
	}

	public void setSource(int source) {
		this.source = source;
	}

	public int getReptileContent() {
		return reptileContent;
	}

	public void setReptileContent(int reptileContent) {
		this.reptileContent = reptileContent;
	}

	public String getReptileUrl() {
		return reptileUrl;
	}

	public void setReptileUrl(String reptileUrl) {
		this.reptileUrl = reptileUrl;
	}

	public int getMaxPreNum() {
		return maxPreNum;
	}

	public void setMaxPreNum(int maxPreNum) {
		this.maxPreNum = maxPreNum;
	}

	public int getFrequencyNum() {
		return frequencyNum;
	}

	public void setFrequencyNum(int frequencyNum) {
		this.frequencyNum = frequencyNum;
	}

	public String getReptileDate() {
		return reptileDate;
	}

	public void setReptileDate(String reptileDate) {
		this.reptileDate = reptileDate;
	}

	public Date getStartDate() {
		return startDate;
	}

	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public int getCreateUser() {
		return createUser;
	}

	public void setCreateUser(int createUser) {
		this.createUser = createUser;
	}

	public Date getCreateDate() {
		return createDate;
	}

	public void setCreateDate(Date createDate) {
		this.createDate = createDate;
	}

	public int getUpdateUser() {
		return updateUser;
	}

	public void setUpdateUser(int updateUser) {
		this.updateUser = updateUser;
	}

	public Date getUpdateDate() {
		return updateDate;
	}

	public void setUpdateDate(Date updateDate) {
		this.updateDate = updateDate;
	}

	public int getDelFlag() {
		return delFlag;
	}

	public void setDelFlag(int delFlag) {
		this.delFlag = delFlag;
	}

	public String getRuleJson() {
		return ruleJson;
	}

	public void setRuleJson(String ruleJson) {
		this.ruleJson = ruleJson;
	}

	public String getInfoDesc() {
		return infoDesc;
	}

	public void setInfoDesc(String infoDesc) {
		this.infoDesc = infoDesc;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

}
