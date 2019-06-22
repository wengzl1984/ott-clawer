package com.spider.extension;

import com.alibaba.fastjson.JSON;
import com.spider.extension.constant.ConstConfig;
import com.spider.extension.service.ClawerService;
import com.spider.extension.service.RankingService;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class JobTask {

    @Autowired
    private RankingService rankingService;

    @Autowired
    private ClawerService clawerService;

    private static Logger log = Logger.getLogger(JobTask.class);

    @Scheduled(cron = "* * 0/2 * * * ")
    public void dealRankDataTask() throws InterruptedException {
        log.info("dealRankDataTask begin:" + new Date());
        //处理榜单信息 begin
        Map<String,Object> param = new HashMap<String,Object>();
        String[] typeArray = ConstConfig.RANKING_SEL_TYPES.split(",");
        param.put("typeArray",typeArray);
        param.put("status",ConstConfig.LOG_STATUS_WAIT_DEAL);
        List<Map<String,Object>> list = rankingService.selRecClawerLog(param);
        if (list != null && list.size() > 0){
            //定义最终要处理的数据列表，需要先对榜单日志信息列表数据进行校验，通过的放入要处理的列表中，再进行处理
            List<Map<String,Object>> dealList = new ArrayList<Map<String,Object>>();
            //定义最终要写错误日志的数据列表，未通过校验的数据放入此列表，再进行处理
            List<Map<String,Object>> errorList = new ArrayList<Map<String,Object>>();
            Map<String,Object> classifyMap = rankingService.classifyLogData(list);
            if (classifyMap != null && classifyMap.containsKey("dealList")){
                List<Map<String,Object>> dealListTmp = (List<Map<String,Object>>)classifyMap.get("dealList");
                if (dealListTmp != null && dealListTmp.size() > 0){
                    dealList.addAll(dealListTmp);
                }
            }
            if (classifyMap != null && classifyMap.containsKey("errorList")){
                List<Map<String,Object>> errorListTmp = (List<Map<String,Object>>)classifyMap.get("errorList");
                if (errorListTmp != null && errorListTmp.size() > 0){
                    errorList.addAll(errorListTmp);
                }
            }
            if (errorList.size() > 0){//存在错误信息，写日志表
                for (Map dataMap : errorList){
                    rankingService.saveRankAndUpdateLog(dataMap,"2");
                }
            }
            if (dealList.size() > 0){//存在需要处理的值
                //先处理原始榜单表信息
                rankingService.transferDataToHis();
                //将本次榜单日志信息插入榜单信息表
                for (Map dataMap : dealList){
                    rankingService.saveRankAndUpdateLog(dataMap,"1");
                }
            }
        }
        //处理榜单信息 end
        log.info("dealRankDataTask end:" + new Date());
    }

    @Scheduled(cron = "* * 1/2 * * * ")
    public void dealClawerDataTask() throws InterruptedException {
        log.info("dealClawerDataTask begin:" + new Date());
        //处理媒资信息 begin
        Map<String,Object> param = new HashMap<String,Object>();
        param.put("status",ConstConfig.LOG_STATUS_WAIT_DEAL);
        List<Map<String,Object>> list = clawerService.selVcmClawerVideo(param);
        if (list != null && list.size() > 0){
            for (Map<String,Object> dataMap:list){
                //处理豆瓣的信息
                Map<String,Object> dbMap = clawerService.checkDouBanLogData(dataMap);
                //处理中国网的信息
                Map<String,Object> cbMap = clawerService.checkCboooLogData(dataMap);
                //媒资表状态更新对象
                Map<String,Object> clawerStatusMap = new HashMap<String,Object>();
                String dbCode = dbMap.get("code").toString();
                String cbCode = cbMap.get("code").toString();
                clawerStatusMap.put("id",dataMap.get("ID").toString());
                //状态为成功或者失败时，才更新数据
                if (dbCode.equals(ConstConfig.LOG_STATUS_DEAL_SUCESS) || dbCode.equals(ConstConfig.LOG_STATUS_DEAL_FAIL)){
                    Map<String,Object> clawerMap = (Map<String,Object>)dbMap.get("clawer");
                    List<Map<String,Object>> logList = (List<Map<String,Object>>)dbMap.get("logList");
                    clawerService.updateClawerInfoAndLog(clawerMap,logList);
                }
                if (cbCode.equals(ConstConfig.LOG_STATUS_DEAL_SUCESS) || cbCode.equals(ConstConfig.LOG_STATUS_DEAL_FAIL)){
                    Map<String,Object> clawerMap = (Map<String,Object>)cbMap.get("clawer");
                    List<Map<String,Object>> logList = (List<Map<String,Object>>)cbMap.get("logList");
                    clawerService.updateClawerInfoAndLog(clawerMap,logList);
                }
                //根据豆瓣和中国网的状态，更新媒资表的状态
                //任意一个失败视为失败
                if (dbCode.equals(ConstConfig.LOG_STATUS_DEAL_FAIL) || cbCode.equals(ConstConfig.LOG_STATUS_DEAL_FAIL)){
                    clawerStatusMap.put("status",ConstConfig.LOG_STATUS_DEAL_FAIL);
                    clawerService.updateClawerInfoAndLog(clawerStatusMap,null);
                }else{
                    //任意一个等待，视为等待
                    if (dbCode.equals(ConstConfig.LOG_STATUS_WAIT_DEAL) || cbCode.equals(ConstConfig.LOG_STATUS_WAIT_DEAL)){
                        clawerStatusMap.put("status",ConstConfig.LOG_STATUS_WAIT_DEAL);
                        clawerService.updateClawerInfoAndLog(clawerStatusMap,null);
                    }else{
                        //两个都成功，视为成功
                        if (dbCode.equals(ConstConfig.LOG_STATUS_DEAL_SUCESS) && cbCode.equals(ConstConfig.LOG_STATUS_DEAL_SUCESS)) {
                            clawerStatusMap.put("status", ConstConfig.LOG_STATUS_DEAL_SUCESS);
                            clawerService.updateClawerInfoAndLog(clawerStatusMap, null);
                        }
                    }
                }
            }
        }
        //处理媒资信息 end
        log.info("dealClawerDataTask end:" + new Date());
    }
}
