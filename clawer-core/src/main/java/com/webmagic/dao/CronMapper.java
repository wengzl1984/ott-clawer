package com.webmagic.dao;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.Map;

@Repository
public interface CronMapper {
    Map<String,Object> selectByPrimaryKey(Integer id);
}
