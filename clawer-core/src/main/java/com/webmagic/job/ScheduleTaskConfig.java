package com.webmagic.job;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSON;
import com.webmagic.pageprocess.CboooVideoMatchProcessor;
import com.webmagic.pageprocess.DouBanVideoMatchProcessor;
import com.webmagic.pipeline.VideoMatchPipeline;
import com.webmagic.util.PhantomJsDriver;
import com.webmagic.util.UserAgentUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.core.annotation.Order;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.CronTask;
import org.springframework.scheduling.config.IntervalTask;
import org.springframework.scheduling.config.ScheduledTask;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.scheduling.config.TriggerTask;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Component;

import com.webmagic.dao.RecClawerLogMapper;
import com.webmagic.dao.VcmClawerTaskDao;
import com.webmagic.entity.VcmClawerTaskVo;
import com.webmagic.pageprocess.BoardPageProcessor;
import com.webmagic.pipeline.BoradPipeline;
import com.webmagic.util.DateUtil;
import com.webmagic.util.JSONUtil;

import us.codecraft.webmagic.Request;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.Spider;

@Configurable
@Component
@EnableScheduling
@Order(10000)
public class ScheduleTaskConfig implements SchedulingConfigurer {

    private Logger log = LoggerFactory.getLogger(ScheduleTaskConfig.class);

    // 定时扫描oracle的时间
    private static final String tasksScanCron = "0 0 */1 * * ?"; // 一个小时执行一次
    private static final String firstExecCron = "0/10 * * * * ?"; // 10秒执行一次后调整为一个小时执行

    // 状态
    public static enum STATUS {
        TASK_NOT_EXISTS, TASK_EXISTS, FAILURE, SUCCESS;
    }

    // 榜单任务范围
    private static final int[] TASKID = {1, 2, 3, 4, 5};
//    private static final int[] TASKID = { 4};

    // 爬虫榜单-任务编号
    private final static String TASK_PREFIX = "ClawerTaskScan";
    private boolean firstExec = true;
    private ScheduledTaskRegistrar scheduledTaskRegistrar;

    @Override
    public void configureTasks(ScheduledTaskRegistrar scheduledTaskRegistrar) {
        this.scheduledTaskRegistrar = scheduledTaskRegistrar;
        initTask();
    }

    @Autowired // 注入mapper
    private VcmClawerTaskDao vcmClawerTaskDao;

    @Autowired // 注入mapper
    private RecClawerLogMapper recClawerLogMapper;
    @Autowired
    VideoMatchPipeline videoMatchPipeline;
    @Autowired
    DouBanVideoMatchProcessor douBanVideoMatchProcessor;
    @Autowired
    CboooVideoMatchProcessor cboooVideoMatchProcessor;
    @Autowired
    UserAgentUtil userAgentUtil;

    /**
     * 定时扫描oracle任务
     */
    private void initTask() {

        String baseTaskId = TASK_PREFIX;

        BaseTask baseCronTask = new BaseTask(baseTaskId, firstExecCron) {

            @Override
            public void run() {
                try {
                    List<VcmClawerTaskVo> resultMap = vcmClawerTaskDao.findAll(TASKID);
                    if (resultMap == null || resultMap.size() == 0) {
                        log.info("未查询到相关记录..");
                        return;
                    }

                    for (VcmClawerTaskVo vcmClawerTask : resultMap) {
                        String taskId = String.valueOf(vcmClawerTask.getId());
                        BaseTask oldTask = TaskConfig.getTask(taskId);
                        String expression = getTaskCronExpression(vcmClawerTask.getReptileDate(), vcmClawerTask.getFrequencyNum());
                        if (TaskConfig.containsTask(taskId)) {
                            // 任务时间是否有调整，有调整的话获取新的cron表达式
                            if (!expression.equals(oldTask.getExpression())) {
                                log.info("任务旧cron表达式[" + oldTask.getExpression() + "]新表达式[" + expression +
                                        "],更新任务执行频率...");
                                changeTask(taskId, expression);
                            }

                            //取消任务
                            if (vcmClawerTask.getStatus() == 2) {
                                log.info(oldTask.toString() + ",状态为2，取消爬虫任务");
                                cancelTask(taskId);
                            }

                        } else {
                            if (new Date().compareTo(vcmClawerTask.getStartDate()) > 0 && vcmClawerTask.getStatus() != 2) {
                                //任务开始时间小于当前系统时间，任务开始
                                log.info("启动爬取任务，URL[" + vcmClawerTask.getReptileUrl() + "],起始时间[" + DateUtil.formatDate(vcmClawerTask.getStartDate()) +
                                        "],执行频率[" + expression + "]");

                                if (vcmClawerTask.getId() == 4 || vcmClawerTask.getId() == 5) {

                                    BaseTask clawerTask = new BaseTask(String.valueOf(vcmClawerTask.getId()), expression) {
                                        @Override
                                        public void run() {
                                            Map<String, String> ruleJson = (Map<String, String>) JSON.parse(vcmClawerTask.getRuleJson());
                                            List<Request> list = new ArrayList<Request>();
                                            List<Map<String, Object>> listToCatchVideo = null;
                                            if (vcmClawerTask.getId() == 4) {
                                                listToCatchVideo = vcmClawerTaskDao.selectDouBanToCatchVideo();
                                            } else {
                                                listToCatchVideo = vcmClawerTaskDao.selectCboooToCatchVideo();
                                            }
                                            for (int i = 0; i < listToCatchVideo.size() && i < vcmClawerTask.getMaxPerNum(); i++) {
                                                log.info("listToCatchVideo=" + listToCatchVideo);
                                                list.clear();
                                                if (vcmClawerTask.getId() == 4) {
                                                    list.add(new Request(ruleJson.get("searchUrl") + PhantomJsDriver.getKeyWord((String) listToCatchVideo.get(i).get("VIDEO_NAME")) + "&cat=1002"));

                                                } else {
                                                    list.add(new Request(ruleJson.get("searchUrl") + PhantomJsDriver.getKeyWord((String) listToCatchVideo.get(i).get("VIDEO_NAME"))));

                                                }
                                                ruleJson.put(listToCatchVideo.get(i).get("VIDEO_NAME") + "", listToCatchVideo.get(i).get("ID") + "");
                                                ruleJson.put("CURRENT_PAGE_NUM", i + 1 + "");
                                                Site site = Site
                                                        .me()
                                                        .setCharset(ruleJson.get("charset"))
                                                        .setTimeOut(Integer.valueOf(ruleJson.get("timeout")) * 1000) //timeOut超时时间 单位毫秒
                                                        .setCycleRetryTimes(Integer.valueOf(ruleJson.get("retry")))
                                                        .setSleepTime(Integer.valueOf(ruleJson.get("sleep")) * 1000)//单位毫秒
                                                        .addHeader("Connection", "keep-alive")
                                                        .addHeader("Cache-Control", "max-age=0")
                                                        .setUserAgent(userAgentUtil.getRandomUserAgent());
                                                Spider spider = null;
                                                if (vcmClawerTask.getId() == 4) {
                                                    douBanVideoMatchProcessor.setSite(site);
                                                    douBanVideoMatchProcessor.setRuleMap(ruleJson);
                                                    spider = Spider.create(douBanVideoMatchProcessor);
                                                } else {
                                                    cboooVideoMatchProcessor.setSite(site);
                                                    cboooVideoMatchProcessor.setRuleMap(ruleJson);
                                                    spider = Spider.create(cboooVideoMatchProcessor);
                                                }
                                                spider.startRequest(list).addPipeline(videoMatchPipeline)
                                                        .thread(Integer.valueOf(ruleJson.get("thread"))).run();
                                                ruleJson.remove(listToCatchVideo.get(i).get("VIDEO_NAME") + "");

                                            }
                                        }
                                    };
                                    addTask(clawerTask);

                                } else {
                                    BaseTask clawerTask = new BaseTask(String.valueOf(vcmClawerTask.getId()), expression) {
                                        @Override
                                        public void run() {
                                            Map<String, Object> ruleJson = JSONUtil.json2Map(vcmClawerTask.getRuleJson());
                                            BoardPageProcessor processor = new BoardPageProcessor(ruleJson);
                                            Spider.create(processor)
                                                    .addUrl(vcmClawerTask.getReptileUrl())
                                                    .addPipeline(new BoradPipeline(recClawerLogMapper))
                                                    .thread(Integer.parseInt(ruleJson.get("thread").toString()))
                                                    .run();
                                        }
                                    };
                                    addTask(clawerTask);
                                }

                            } else {
                                //任务开始时间，
                                log.info("扫描URL[" + vcmClawerTask.getReptileUrl() + "]开始时间[" + DateUtil.formatDate(vcmClawerTask.getStartDate()) + "],大于当前系统时间,任务暂不开始...");
                                return;
                            }
                        }
                    }

                    //首次获取任务后将执行周期调整为每个小时一次
                    if (firstExec) {
                        changeTask(baseTaskId, tasksScanCron);
                        firstExec = false;
                    }

                    log.info("当前爬取任务列表如下：");
                    TaskConfig.getTasks().stream().forEach((BaseTask printBask) -> log.info(printBask.toString()));
                } catch (Exception e) {
                    log.error("爬取任务失败:", e);
                    throw e;
                }
            }
        };

        addTask(baseCronTask);
    }

    //获取cron表达式
    public String getTaskCronExpression(String reptileDate, int frequencyNum) {
        String[] dateInfo = reptileDate.split(":");
        StringBuffer BufferCron = new StringBuffer();
        BufferCron.append("0")
                .append(" ")
                .append(Integer.parseInt(dateInfo[1]))
                .append(" ")
                .append(Integer.parseInt(dateInfo[0]))
                .append(" ")
                .append("*/" + frequencyNum)
                .append(" * ?");
        return BufferCron.toString();
    }

    /**
     * 添加任务
     *
     * @param task
     * @return
     */
    public STATUS addTask(BaseTask task) {
        if (scheduledTaskRegistrar == null || task == null) {
            return STATUS.FAILURE;
        }
        if (TaskConfig.containsTask(task.getId())) {
            return STATUS.TASK_EXISTS;
        }
        try {
            addTask0(task);
            TaskConfig.addTask(task);
            return STATUS.SUCCESS;
        } catch (Exception e) {
            log.error("新增定时任务失败:" + task, e);
            throw e;
        }
    }

    /**
     * 改变任务执行频率
     *
     * @param taskId
     * @param expression
     * @return
     */
    public STATUS changeTask(String taskId, String expression) {
        BaseTask baseTask = TaskConfig.getTask(taskId);
        if (baseTask == null || expression == null) {
            return STATUS.TASK_NOT_EXISTS;
        }
        log.info("change trigger expression:(id=" + taskId + ",expression=" + expression + ")");
        baseTask.setExpression(expression);
        return STATUS.SUCCESS;
    }

    /**
     * 取消定时任务
     *
     * @param taskId
     * @return
     */
    public STATUS cancelTask(String taskId) {
        if (!TaskConfig.containsTask(taskId)) {
            return STATUS.TASK_NOT_EXISTS;
        }
        try {
            log.info("cancel task:" + taskId);
            TaskConfig.removeTask(taskId).getScheduledTask().cancel();
        } catch (Exception e) {
            log.error("取消任务失败:" + taskId, e);
        }
        return STATUS.SUCCESS;
    }

    private void addTask0(BaseTask task) {
        log.info("add task:" + task);
        task.setScheduledTask(addTriggerTask(task));

    }

    /**
     * 添加可变时间task
     *
     * @param task
     * @return
     */
    private ScheduledTask addTriggerTask(BaseTask task) {
        return scheduledTaskRegistrar.scheduleTriggerTask(new TriggerTask(task, triggerContext -> {
            CronTrigger trigger = new CronTrigger(task.getExpression());
            return trigger.nextExecutionTime(triggerContext);

        }));
    }

    /**
     * 设置固定频率的定时任务
     *
     * @param task
     * @param interval
     */

    @SuppressWarnings({"deprecation", "unused"})
    private ScheduledTask addFixedRateTask(Runnable task, long interval) {
        return scheduledTaskRegistrar.scheduleFixedRateTask(new IntervalTask(task, interval, 0L));
    }

    /**
     * 设置延迟以固定频率执行的定时任务
     *
     * @param task
     * @param interval
     * @param delay
     */
    @SuppressWarnings({"deprecation", "unused"})
    private ScheduledTask addFixedDelayTask(Runnable task, long interval, long delay) {
        return scheduledTaskRegistrar.scheduleFixedDelayTask(new IntervalTask(task, interval, delay));
    }

    /**
     * 添加不可改变时间表的定时任务
     *
     * @param task
     */
    @SuppressWarnings("unused")
    private ScheduledTask addCronTask(Runnable task, String expression) {
        return scheduledTaskRegistrar.scheduleCronTask(new CronTask(task, expression));
    }

}
