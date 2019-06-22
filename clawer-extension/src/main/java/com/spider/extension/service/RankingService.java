package com.spider.extension.service;

import com.alibaba.fastjson.JSON;
import com.spider.extension.constant.ConstConfig;
import com.spider.extension.dao.CommonDao;
import com.spider.extension.dao.RankingDao;
import com.spider.extension.entity.RankInfoDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service("rankingService")
public class RankingService {

    @Autowired
    private RankingDao rankingDao;

    @Autowired
    private CommonDao commonDao;

    public List<Map<String,Object>> selRecClawerLog(Map<String,Object> param){
        return commonDao.selRecClawerLog(param);
    }

    //将查询到的待处理榜单日志信息进行分类与整理，为后续数据更新做准备
    public Map<String,Object> classifyLogData(List<Map<String,Object>> list){
        String responeContent = "";
        //定义最终要处理的数据列表，需要先对榜单日志信息列表数据进行校验，通过的放入要处理的列表中，再进行处理
        List<Map<String,Object>> dealList = new ArrayList<Map<String,Object>>();
        //定义最终要写错误日志的数据列表，未通过校验的数据放入此列表，再进行处理
        List<Map<String,Object>> errorList = new ArrayList<Map<String,Object>>();
        for (Map<String,Object> rankMap : list){
            if (rankMap.containsKey("RESPONE_CONTENT") && !rankMap.get("RESPONE_CONTENT").toString().equals("")){
                responeContent = rankMap.get("RESPONE_CONTENT").toString();
                Map<String,Object> conMap = JSON.parseObject(responeContent,Map.class);
                //逻辑约定：有RESPONE_CONTENT信息且videoName节点存在的数据才处理
                if (conMap.containsKey("videoName") && !conMap.get("videoName").toString().equals("")){
                    //RELATE_TYPE字段是查询条件之一，必然有值
                    conMap.put("type",rankMap.get("RELATE_TYPE").toString());
                    conMap.put("logId",rankMap.get("ID").toString());//存放日志主键id，用于后续的日志表记录更新
                    conMap.put("logStatus",ConstConfig.LOG_STATUS_DEAL_SUCESS);//成功标记
                    dealList.add(conMap);
                }else{
                    conMap.put("logId",rankMap.get("ID").toString());//存放日志主键id，用于后续的日志表记录更新
                    conMap.put("logStatus",ConstConfig.LOG_STATUS_DEAL_FAIL);//失败标记
                    conMap.put("errorType",ConstConfig.LOG_ERROR_TYPE_INFO_LOST);//失败类型：1-信息缺失
                    conMap.put("errorInfo","缺失videoName节点信息");//失败描述
                    errorList.add(conMap);
                }
            }else{//缺失RESPONE_CONTENT，直接记录日志表
                Map<String,Object> conMap = new HashMap<String,Object>();
                conMap.put("logId",rankMap.get("ID").toString());//存放日志主键id，用于后续的日志表记录更新
                conMap.put("logStatus",ConstConfig.LOG_STATUS_DEAL_FAIL);//失败标记
                conMap.put("errorType",ConstConfig.LOG_ERROR_TYPE_INFO_LOST);//失败类型：1-信息缺失
                conMap.put("errorInfo","缺失RESPONE_CONTENT字段信息");//失败描述
                errorList.add(conMap);
            }
        }
        Map<String,Object> resultMap = new HashMap<String,Object>();
        resultMap.put("dealList",dealList);
        resultMap.put("errorList",errorList);
        return  resultMap;
    }

    //迁移榜单数据，逻辑：
    //1、将现有榜单信息数据写到历史榜单中；2、删除现有榜单中的所有数据
    @Transactional
    public void transferDataToHis(){
        //1、将现有榜单信息数据写到历史榜单中；
        List<Map<String,Object>> vcmList = rankingDao.selVcmRankingInfo();
        if (vcmList != null && vcmList.size() > 0){
            rankingDao.saveHisRankFromRankingInfo();
        }
        //2、删除现有榜单中的所有数据
        rankingDao.delRankInfo();
    }

    //榜单日志信息处理：将日志表信息写入榜单表，并修改日志表状态
    @Transactional
    public void saveRankAndUpdateLog(Map<String,Object> dataMap,String opType){
        //opType，操作类型标记，=1表示正常数据处理操作，=2表示仅更新错误日志表
        if (opType != null && opType.equals("1")){
            Integer rankingId = rankingDao.selRankingSeq();
            dataMap.put("rankingId",rankingId);
            rankingDao.saveRankInfo(dataMap); //将榜单日志信息插入榜单信息表
            //增加逻辑：榜单新增后要增加命中相关表
            //查看媒资视图是否有名称模糊匹配的表，有的话表示命中，并把命中的信息写到相关表
            List<Map<String,Object>> mList = rankingDao.selMediaView(dataMap);
            int ottMatch = 0;
            if (mList != null && mList.size() > 0){
                ottMatch = mList.size();
            }
            dataMap.put("ottMatch",ottMatch);
            rankingDao.saveRankingInfoMatch(dataMap);
            if (mList != null && mList.size() > 0){
                Map<String,Object> relaMap = new HashMap<String,Object>();
                relaMap.put("rankingId",rankingId);
                for (Map<String,Object> m : mList){
                     relaMap.put("mediaId",m.get("MEDIA_ID").toString());
                     rankingDao.saveRankingInfoMatchRela(relaMap);
                }
            }
        }
        commonDao.updateRecClawerLog(dataMap);//更新日志表
    }
}
