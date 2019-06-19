package com.webmagic.dao;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.webmagic.entity.VcmClawerTaskVo;


@Repository
public interface VcmClawerTaskDao {
	List<VcmClawerTaskVo> findAll(int[] taskId);
}
