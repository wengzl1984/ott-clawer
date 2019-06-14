package com.webmagic.timer;

import com.webmagic.dao.CronMapper;
import com.webmagic.pageprocess.MaoYanPageProcessor;
import com.webmagic.pipeline.MaoYanPipeline;
import com.webmagic.util.CronDateUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.Trigger;
import org.springframework.scheduling.TriggerContext;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Component;
import us.codecraft.webmagic.Spider;

import java.util.Date;
import java.util.Map;

//@Component
//@Configuration      //1.主要用于标记配置类，兼备Component的效果。
//@EnableScheduling   // 2.开启定时任务
public class DynamicBaiDuScheduleTask implements SchedulingConfigurer {

    private static Logger log = Logger.getLogger(DynamicBaiDuScheduleTask.class);

    private int test = 0;//0:开启读取数据库配置，1：写死
    @Autowired      //注入mapper
    @SuppressWarnings("all")
    CronMapper cronMapper;
    @Autowired
    MaoYanPipeline maoYanPipeline;
    //@Value("${spider.thread.num}")
    private int spiderThreadNum = 2;//spider线程数

    /**
     * 执行定时任务.
     */
    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {

        Runnable task = new Runnable() {

            @Override

            public void run() {

                //任务逻辑代码部分.

                System.out.println("TaskCronChange task is running ... " + new Date());
                System.out.println("TaskCronChange task is running ... " + spiderThreadNum);

                Spider.create(new MaoYanPageProcessor())
                        //new PostInfoPageProcessor())
                        //.setDownloader(httpClientDownloader)
                        .addUrl("https://maoyan.com/board")
                        //.addUrl("http://blog.sina.com.cn/s/articlelist_1487828712_0_1.html")
                        .addPipeline(maoYanPipeline)
                        .thread(spiderThreadNum)
                        .run();

            }

        };

        Trigger trigger = new Trigger() {

            @Override
            public Date nextExecutionTime(TriggerContext triggerContext) {
                int period = -1;
                String catchTime = null;

                //任务触发，可修改任务的执行周期.
                //2.1 从数据库获取执行周期
                String cron = null;
                Map<String, Object> cronMap = cronMapper.selectByPrimaryKey(1);
                //2.2 合法性校验.
                catchTime = (String) cronMap.get("fresh");
                try {
                    period = (Integer) cronMap.get("period");
                }catch (Exception e){
                    log.error("period cast int fail,exception=",e);
                }
                if (StringUtils.isEmpty((String) cronMap.get("fresh"))) {
                    // Omitted Code ..
                    log.error("fresh is null please check fresh param" );
                    System.exit(-1);
                }
                if(period <= 0){
                    log.error("period param is invalid,period="+period+",program will exit");
                    System.exit(-1);
                }

                log.info("fresh=" + catchTime);
                log.info("period=" + period);
                cron = CronDateUtils.getCron(period, catchTime);
                log.info("cron=" + cron);
                cron = "0 50 15 28 * ?";//每隔半小时10秒后再执行一次
                //测试使用 0/10 0/30 * * *  ?
                if (test == 1) {
                    System.out.println("开启测试");
                    cron = "10 0/1 * * * ?";//每隔半小时10秒后再执行一次
                }
                CronTrigger trigger = new CronTrigger(cron);
                test = 1;//开启测试
                Date nextExec = trigger.nextExecutionTime(triggerContext);
                //2.3 返回执行周期(Date)
                return nextExec;

            }

        };

        taskRegistrar.addTriggerTask(task, trigger);

    }

}