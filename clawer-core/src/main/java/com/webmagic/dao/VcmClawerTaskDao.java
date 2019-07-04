package com.webmagic.dao;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Repository;

import com.webmagic.entity.VcmClawerTaskVo;


@Repository
public interface VcmClawerTaskDao {
	List<VcmClawerTaskVo> findAll(int[] taskId);
	public List<Map<String,Object>> selectDouBanToCatchVideo();
	public List<Map<String,Object>> selectCboooToCatchVideo();
	public int updateTaskInfo(Map<String,Object> parameterMap);

}
