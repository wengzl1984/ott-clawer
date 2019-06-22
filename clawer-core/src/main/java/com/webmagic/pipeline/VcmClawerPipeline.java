package com.webmagic.pipeline;

import com.webmagic.dao.RecClawerLogMapper;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import us.codecraft.webmagic.ResultItems;
import us.codecraft.webmagic.Task;
import us.codecraft.webmagic.pipeline.Pipeline;

import java.util.HashMap;
import java.util.Map;

@Component
public class VcmClawerPipeline implements Pipeline {
    private static Logger log = Logger.getLogger(VcmClawerPipeline.class);

    @Autowired
    RecClawerLogMapper recClawerLogMapper;

    @Override
    @Transactional
    public void process(ResultItems resultItems, Task task) {

        for (Map.Entry<String, Object> entry : resultItems.getAll().entrySet()) {
            log.info("VcmClawerPipeline data insert db,key=" + entry.getKey());
            if (entry.getKey().equals("Info")) {
                Map<String, Object> info = (HashMap<String, Object>) entry.getValue();
                log.info("insert db Info=" + info);
                try {
                    recClawerLogMapper.insert(info);
                } catch (Exception e) {
                    log.error("exception e:", e);
                    // 如果我们需要捕获异常后，同时进行回滚，通过
                    TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();//进行手动回滚操作。

                }

            }
        }


    }
}

