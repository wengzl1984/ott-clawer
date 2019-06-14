package com.webmagic.dao;

import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;


@Repository
public interface VcmClawerTaskDao {
	String selectByPrimary(int id);
}
