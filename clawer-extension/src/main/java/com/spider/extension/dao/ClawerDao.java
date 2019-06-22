package com.spider.extension.dao;

import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository(value = "clawerDao")
public interface ClawerDao {

    public List<Map<String,Object>> selVcmClawerVideo(Map<String, Object> param);

    public void updateClawerInfo(Map<String, Object> param);

}
