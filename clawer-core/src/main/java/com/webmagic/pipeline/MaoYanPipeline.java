package com.webmagic.pipeline;

import com.webmagic.entity.HotBoardInfo;
import com.webmagic.entity.HotBoardInfoList;
import com.webmagic.service.HotHoardService;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import us.codecraft.webmagic.ResultItems;
import us.codecraft.webmagic.Task;
import us.codecraft.webmagic.pipeline.Pipeline;

import java.util.Map;

@Component("PostInfoPipeline")
public class MaoYanPipeline implements Pipeline {
    private static Logger log = Logger.getLogger(MaoYanPipeline.class);

    @Autowired
    private HotHoardService hotHoardService;

    @Override
    @Transactional
    public void process(ResultItems resultItems, Task task) {

        for (Map.Entry<String, Object> entry : resultItems.getAll().entrySet()) {
            System.out.println("data insert db,key="+entry.getKey());
            if (entry.getKey().equals("hotBoardInfoList")) {
                HotBoardInfoList hotBoardInfoList = (HotBoardInfoList) entry.getValue();
                for (int i = 0; i < hotBoardInfoList.getMoveNameList().size(); i++) {
                    System.out.println("hotBoardInfoList insert db");
                    HotBoardInfo hotBoardInfo = new HotBoardInfo();
                 //   hotBoardInfo.setMoveName(hotBoardInfoList.getMoveNameList().get(i));
                  //  hotBoardInfo.setMoveStars(hotBoardInfoList.getMoveStarList().get(i));
                  //  hotBoardInfo.setMoveScore(hotBoardInfoList.getMoveScoreStartList().get(i) + hotBoardInfoList.getMoveScoreEndList().get(i));
                  //  hotBoardInfo.setReleaseTime(hotBoardInfoList.getReleaseTimeList().get(i));
                    try {
                      //  hotHoardService.insertHotBoard(hotBoardInfo);
                        int x = 1/0;//用于测试下面的异常后事务回滚
                    }catch (Exception e){
                        log.error("exception e:",e);
                       // 如果我们需要捕获异常后，同时进行回滚，通过
                    TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();//进行手动回滚操作。

                    }

                }
            }


        }
    }
}