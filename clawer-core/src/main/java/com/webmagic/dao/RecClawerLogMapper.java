package com.webmagic.dao;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.webmagic.entity.VcmClawerLogVo;

@Repository
public interface RecClawerLogMapper {
	public void insertRecord(VcmClawerLogVo vcmClawerLogVo);
	public void insertRecdBatch(List<VcmClawerLogVo> logList);
}
