package com.spider.extension.dao;

import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository(value = "commonDao")
public interface CommonDao {
    public List<Map<String,Object>> selRecClawerLog(Map<String, Object> param);

    public void updateRecClawerLog(Map<String, Object> param);
}
