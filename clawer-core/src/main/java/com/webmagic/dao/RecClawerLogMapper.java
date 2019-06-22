package com.webmagic.dao;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Repository;

import com.webmagic.entity.VcmClawerLogVo;

@Repository
public interface RecClawerLogMapper {
	public void insertRecord(VcmClawerLogVo vcmClawerLogVo);
	public int insert(Map<String,Object> paramterMap);

}
