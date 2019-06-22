package com.spider.extension.service;

import com.alibaba.fastjson.JSON;
import com.spider.extension.constant.ConstConfig;
import com.spider.extension.dao.ClawerDao;
import com.spider.extension.dao.CommonDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service("clawerService")
public class ClawerService {

    @Autowired
    private ClawerDao clawerDao;
    @Autowired
    private CommonDao commonDao;

    public List<Map<String,Object>> selVcmClawerVideo(Map<String,Object> param){
        return clawerDao.selVcmClawerVideo(param);
    }

    //处理豆瓣日志表信息,返回Map中包含code（处理结果标记），logList（待处理日志表列表），clawer（待更新媒资对象）
    //code节点约定：=0等待，=1成功，=2失败
    public Map<String,Object> checkDouBanLogData(Map<String,Object> dataMap){
        Map<String,Object> resultMap = new HashMap<String,Object>();
        if (dataMap == null || !dataMap.containsKey("VIDEO_NAME") || !dataMap.containsKey("DOUBAN_STATUS_TYPE")){
            resultMap.put("code",ConstConfig.LOG_STATUS_DEAL_FAIL);
            Map<String,Object> clawerMap = new HashMap<String,Object>();
            clawerMap.put("id",dataMap.get("ID").toString());//id必然存在，也是更新媒资表的依据
            clawerMap.put("errorType",ConstConfig.LOG_ERROR_TYPE_INFO_LOST);
            clawerMap.put("errorInfo","缺失videoName或douBanStatusType节点信息");
            resultMap.put("clawer",clawerMap);
            return resultMap;
        }
        String videoName = dataMap.get("VIDEO_NAME").toString();
        Map<String,Object> selMap = new HashMap<String,Object>();
        selMap.put("taskId",dataMap.get("ID").toString());
        selMap.put("relateType",ConstConfig.CLAWER_SEL_DOUBAN_TYPE);
        selMap.put("status",ConstConfig.LOG_STATUS_WAIT_DEAL);
        List<Map<String,Object>> list = commonDao.selRecClawerLog(selMap);
        if (list != null && list.size() > 0){
            int cnt = 0;//计算名称匹配的数量，超过1个认为匹配错误
            List<Map<String,Object>> logList = new ArrayList<Map<String,Object>>();
            String responeContent = "";
            String matchDouBanName = "";
            for (Map<String,Object> logMap : list){
                if (logMap.containsKey("RESPONE_CONTENT") && !logMap.get("RESPONE_CONTENT").toString().equals("")){
                    responeContent = logMap.get("RESPONE_CONTENT").toString();
                    Map<String,Object> conMap = JSON.parseObject(responeContent,Map.class);
                    conMap.put("logId",logMap.get("ID").toString());//存放日志主键id，用于后续的日志表记录更新
                    if (conMap.containsKey("matchDouBanName") && !conMap.get("matchDouBanName").toString().equals("")){
                        matchDouBanName = conMap.get("matchDouBanName").toString();
                        if (matchDouBanName.indexOf(videoName) > -1){
                            cnt ++;
                            conMap.put("id",dataMap.get("ID").toString());//媒资表主键id
                            conMap.put("logStatus",ConstConfig.LOG_STATUS_DEAL_SUCESS);//成功标记
                            //匹配成功的情况下，conMap包含媒资表更新的内容，直接放入clawer节点
                            conMap.put("douBanStatusType",ConstConfig.LOG_STATUS_DEAL_SUCESS);
                            resultMap.put("clawer",conMap);
                        }else{
                            conMap.put("logStatus",ConstConfig.LOG_STATUS_DEAL_FAIL);//失败标记
                            conMap.put("errorType",ConstConfig.LOG_ERROR_TYPE_NAME_NO_MATCH);//失败类型：2-媒资与日志中的名称无法匹配
                            conMap.put("errorInfo","媒资与日志中的名称无法匹配");//失败描述
                        }
                        logList.add(conMap);
                    }else{
                        conMap.put("logStatus",ConstConfig.LOG_STATUS_DEAL_FAIL);//失败标记
                        conMap.put("errorType",ConstConfig.LOG_ERROR_TYPE_INFO_LOST);//失败类型：1-信息缺失
                        conMap.put("errorInfo","缺失matchDouBanName节点信息");//失败描述
                        logList.add(conMap);
                    }
                }else{//RESPONE_CONTENT节点缺失，日志记录直接设置为失败
                    Map<String,Object> conMap = new HashMap<String,Object>();
                    conMap.put("logId",logMap.get("ID").toString());//存放日志主键id，用于后续的日志表记录更新
                    conMap.put("logStatus",ConstConfig.LOG_STATUS_DEAL_FAIL);//失败标记
                    conMap.put("errorType",ConstConfig.LOG_ERROR_TYPE_INFO_LOST);//失败类型：1-信息缺失
                    conMap.put("errorInfo","缺失RESPONE_CONTENT字段信息");//失败描述
                    logList.add(conMap);
                }
            }
            if (cnt == 1){//匹配成功
                resultMap.put("code",ConstConfig.LOG_STATUS_DEAL_SUCESS);
            }else{//其他情况下，认为无法匹配
                resultMap.put("code",ConstConfig.LOG_STATUS_DEAL_FAIL);
                Map<String,Object> clawerMap = new HashMap<String,Object>();
                clawerMap.put("id",dataMap.get("ID").toString());
                clawerMap.put("douBanStatusType",ConstConfig.LOG_STATUS_DEAL_FAIL);
                clawerMap.put("errorType",ConstConfig.LOG_ERROR_TYPE_NAME_NO_MATCH);
                clawerMap.put("errorInfo","媒资与日志中的名称无法匹配");
                resultMap.put("clawer",clawerMap);
                for (Map<String,Object> log : logList){
                    if (log.containsKey("logStatus") && log.get("logStatus").toString().equals(ConstConfig.LOG_STATUS_DEAL_SUCESS)){
                        log.put("logStatus",ConstConfig.LOG_STATUS_DEAL_FAIL);
                        log.put("errorType",ConstConfig.LOG_ERROR_TYPE_NAME_NO_MATCH);
                        log.put("errorInfo","媒资与日志中的名称无法匹配");
                    }
                }
            }
            resultMap.put("logList",logList);
            return resultMap;
        }else{//没有找到对应日志表数据的情况下，认为还未查询，等待状态，不做任何数据处理
            resultMap.put("code",ConstConfig.LOG_STATUS_WAIT_DEAL);//等待状态
            return resultMap;
        }
    }

    //处理中国网日志表信息,返回Map中包含code（处理结果标记），logList（待处理日志表列表），clawer（待更新媒资对象）
    //code节点约定：=0等待，=1成功，=2失败
    public Map<String,Object> checkCboooLogData(Map<String,Object> dataMap){
        Map<String,Object> resultMap = new HashMap<String,Object>();
        if (dataMap == null || !dataMap.containsKey("VIDEO_NAME") || !dataMap.containsKey("CBOOO_STATUS_TYPE")){
            resultMap.put("code",ConstConfig.LOG_STATUS_DEAL_FAIL);
            Map<String,Object> clawerMap = new HashMap<String,Object>();
            clawerMap.put("id",dataMap.get("ID").toString());//id必然存在，也是更新媒资表的依据
            clawerMap.put("errorType",ConstConfig.LOG_ERROR_TYPE_INFO_LOST);
            clawerMap.put("errorInfo","缺失videoName或cboooStatusType节点信息");
            resultMap.put("clawer",clawerMap);
            return resultMap;
        }
        String videoName = dataMap.get("VIDEO_NAME").toString();
        Map<String,Object> selMap = new HashMap<String,Object>();
        selMap.put("taskId",dataMap.get("ID").toString());
        selMap.put("relateType",ConstConfig.CLAWER_SEL_CBOOO_TYPE);
        selMap.put("status",ConstConfig.LOG_STATUS_WAIT_DEAL);
        List<Map<String,Object>> list = commonDao.selRecClawerLog(selMap);
        if (list != null && list.size() > 0){
            int cnt = 0;//计算名称匹配的数量，超过1个认为匹配错误
            List<Map<String,Object>> logList = new ArrayList<Map<String,Object>>();
            String responeContent = "";
            String matchCboooName = "";
            for (Map<String,Object> logMap : list){
                if (logMap.containsKey("RESPONE_CONTENT") && !logMap.get("RESPONE_CONTENT").toString().equals("")){
                    responeContent = logMap.get("RESPONE_CONTENT").toString();
                    Map<String,Object> conMap = JSON.parseObject(responeContent,Map.class);
                    conMap.put("logId",logMap.get("ID").toString());//存放日志主键id，用于后续的日志表记录更新
                    if (conMap.containsKey("matchCboooName") && !conMap.get("matchCboooName").toString().equals("")){
                        matchCboooName = conMap.get("matchCboooName").toString();
                        if (matchCboooName.indexOf(videoName) > -1){
                            cnt ++;
                            conMap.put("id",dataMap.get("ID").toString());//媒资表主键id
                            conMap.put("logStatus",ConstConfig.LOG_STATUS_DEAL_SUCESS);//成功标记
                            //匹配成功的情况下，conMap包含媒资表更新的内容，直接放入clawer节点
                            conMap.put("cboooStatusType",ConstConfig.LOG_STATUS_DEAL_SUCESS);
                            resultMap.put("clawer",conMap);
                        }else{
                            conMap.put("logStatus",ConstConfig.LOG_STATUS_DEAL_FAIL);//失败标记
                            conMap.put("errorType",ConstConfig.LOG_ERROR_TYPE_NAME_NO_MATCH);//失败类型：2-媒资与日志中的名称无法匹配
                            conMap.put("errorInfo","媒资与日志中的名称无法匹配");//失败描述
                        }
                        logList.add(conMap);
                    }else{
                        conMap.put("logStatus",ConstConfig.LOG_STATUS_DEAL_FAIL);//失败标记
                        conMap.put("errorType",ConstConfig.LOG_ERROR_TYPE_INFO_LOST);//失败类型：1-信息缺失
                        conMap.put("errorInfo","缺失matchCboooName节点信息");//失败描述
                        logList.add(conMap);
                    }
                }else{//RESPONE_CONTENT节点缺失，日志记录直接设置为失败
                    Map<String,Object> conMap = new HashMap<String,Object>();
                    conMap.put("logId",logMap.get("ID").toString());//存放日志主键id，用于后续的日志表记录更新
                    conMap.put("logStatus",ConstConfig.LOG_STATUS_DEAL_FAIL);//失败标记
                    conMap.put("errorType",ConstConfig.LOG_ERROR_TYPE_INFO_LOST);//失败类型：1-信息缺失
                    conMap.put("errorInfo","缺失RESPONE_CONTENT字段信息");//失败描述
                    logList.add(conMap);
                }
            }
            if (cnt == 1){//匹配成功
                resultMap.put("code",ConstConfig.LOG_STATUS_DEAL_SUCESS);
            }else{//其他情况下，认为无法匹配
                resultMap.put("code",ConstConfig.LOG_STATUS_DEAL_FAIL);
                Map<String,Object> clawerMap = new HashMap<String,Object>();
                clawerMap.put("id",dataMap.get("ID").toString());
                clawerMap.put("cboooStatusType",ConstConfig.LOG_STATUS_DEAL_FAIL);
                clawerMap.put("errorType",ConstConfig.LOG_ERROR_TYPE_NAME_NO_MATCH);
                clawerMap.put("errorInfo","媒资与日志中的名称无法匹配");
                resultMap.put("clawer",clawerMap);
                for (Map<String,Object> log : logList){
                    if (log.containsKey("logStatus") && log.get("logStatus").toString().equals(ConstConfig.LOG_STATUS_DEAL_SUCESS)){
                        log.put("logStatus",ConstConfig.LOG_STATUS_DEAL_FAIL);
                        log.put("errorType",ConstConfig.LOG_ERROR_TYPE_NAME_NO_MATCH);
                        log.put("errorInfo","媒资与日志中的名称无法匹配");
                    }
                }
            }
            resultMap.put("logList",logList);
            return resultMap;
        }else{//没有找到对应日志表数据的情况下，认为还未查询，等待状态，不做任何数据处理
            resultMap.put("code",ConstConfig.LOG_STATUS_WAIT_DEAL);//等待状态
            return resultMap;
        }
    }

    //数据处理：更新媒资表，并更新相关的日志表
    @Transactional
    public void updateClawerInfoAndLog(Map<String,Object> clawerMap,List<Map<String,Object>> logList){
        if (clawerMap != null){
            clawerDao.updateClawerInfo(clawerMap);
        }
        if (logList != null && logList.size() > 0){
            for (Map<String,Object> log :logList){
                commonDao.updateRecClawerLog(log);//更新日志表
            }
        }
    }
}
