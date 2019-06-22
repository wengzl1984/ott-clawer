package com.spider.extension.dao;



import com.spider.extension.entity.RankInfoDto;
import org.omg.CORBA.PUBLIC_MEMBER;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;


/**
 * @author ljs
 * @version V1.0
 * @Title: ${file_name}
 * @Package com.webmagic.dao
 * @Description: TODO
 * @date 2019/5/25 13:48
 */
@Repository(value = "rankingDao")
public interface RankingDao {

    public void saveRankInfo(Map<String, Object> param);

    public void delRankInfo();

    public void saveHisRankFromRankingInfo();

    public List<Map<String,Object>> selVcmRankingInfo();

    public Integer selRankingSeq();

    public void saveRankingInfoMatch(Map<String, Object> param);

    public void saveRankingInfoMatchRela(Map<String, Object> param);

    public List<Map<String,Object>> selMediaView(Map<String, Object> param);
}
