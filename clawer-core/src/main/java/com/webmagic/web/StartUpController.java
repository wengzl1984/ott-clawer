package com.webmagic.web;

import com.alibaba.fastjson.JSON;
import com.webmagic.job.BaseTask;
import com.webmagic.job.ScheduleTaskConfig;
import com.webmagic.job.TaskConfig;
import com.webmagic.pageprocess.CboooVideoMatchProcessor;
import com.webmagic.util.PhantomJsDriver;
import com.webmagic.util.UserAgentUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import us.codecraft.webmagic.Request;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.Spider;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


/**
 * 用于测试使用
 */
@RestController
public class StartUpController {
    private Logger log = LoggerFactory.getLogger(StartUpController.class);

    @Autowired
    CboooVideoMatchProcessor cboooVideoMatchProcessor;
    @Autowired
    UserAgentUtil userAgentUtil;

    @GetMapping("/mediaMatch")
    public String matchVideo(@RequestParam("keyWord") String keyWord) {
        String jsonStr = "{" +
                "    \"id\": \"1\",\n" +
                "    \"thread\": \"4\",\n" +
                "    \"retry\": \"2\",\n" +
                "    \"sleep\": \"3\",\n" +
                "    \"timeout\": \"5\",\n" +
                "    \"charset\": \"UTF-8\",\n" +
                "    \"searchUrl\": \"http://cbooo.cn/search?k=\",\n" +
                "    \"linkXpath\": \"//*[@id=\\\"top\\\"]/div[3]/div[2]/div[2]/ul[1]/*/a/@href\",\n" +
                "    \"videoBoxOfficeXpath\": \"//*[@id=\\\"top\\\"]/div[3]/div[2]/div/div[1]/div[2]/div[1]/p[1]/span/text()\",\n" +
                "    \"videoNameXpath\": \"//*[@id=\\\"top\\\"]/div[3]/div[2]/div/div[1]/div[2]/div[1]/h2/text()\"\n" +
                "}";
        System.out.println("json_str=" + jsonStr);
        Map<String, String> ruleMap = (Map<String, String>) JSON.parse(jsonStr);
        System.out.println("这个是用JSON类来解析JSON字符串!!!");
        for (Object map : ruleMap.entrySet()) {
            System.out.println(((Map.Entry) map).getKey() + "=" + ((Map.Entry) map).getValue());
        }

        List<Request> list = new ArrayList<Request>();

        Site site = Site
                .me()
                .setCharset(ruleMap.get("charset"))
                .setTimeOut(Integer.valueOf(ruleMap.get("timeout")) * 1000) //timeOut超时时间 单位毫秒
                .setCycleRetryTimes(Integer.valueOf(ruleMap.get("retry")))
                .setSleepTime(Integer.valueOf(ruleMap.get("sleep")) * 1000)//单位毫秒
                .addHeader("Connection", "keep-alive")
                .addHeader("Cache-Control", "max-age=0")
                .setUserAgent(userAgentUtil.getRandomUserAgent());

        for (int i = 0; i < 1; i++) {
            System.out.println("search url=" + "http://cbooo.cn/search?k=" + PhantomJsDriver.getKeyWord("一出好戏"));
            list.add(new Request("http://cbooo.cn/search?k=" + PhantomJsDriver.getKeyWord("一出好戏")));
            ruleMap.put("CURRENT_PAGE_NUM", i + 1 + "");

        }

        cboooVideoMatchProcessor.setRuleMap(ruleMap);
        cboooVideoMatchProcessor.setSite(site);
        Spider spider = Spider.create(cboooVideoMatchProcessor);

        spider.startRequest(list).thread(1).run();
        return "开始爬虫=" + spider.getPageCount();
    }
    @GetMapping("/getTaskInfo")
    public String getTaskInfo() {
        log.info("当前爬取任务列表如下：");
        StringBuffer sb = new StringBuffer();

        TaskConfig.getTasks().stream().forEach((BaseTask printBask) -> log.info(printBask.toString(),sb.append(printBask.toString()).append("\n")));
        return sb.toString().replaceAll("]","]\r\n");

    }

    @GetMapping("/changeTask")
    public ScheduleTaskConfig.STATUS changeTaskInfo(@RequestParam("taskId") String taskId,@RequestParam("expression") String expression) {
        log.info("当前爬取任务列表如下：");
        StringBuffer sb = new StringBuffer();

        BaseTask baseTask = TaskConfig.getTask(taskId);
        if (baseTask == null || expression == null) {
            return ScheduleTaskConfig.STATUS.TASK_NOT_EXISTS;        }
        log.info("change trigger expression:(id=" + taskId + ",expression=" + expression + ")");
        baseTask.setExpression(expression);
        return ScheduleTaskConfig.STATUS.SUCCESS;
    }
    @GetMapping("/cancelTask")
    public ScheduleTaskConfig.STATUS cancelTask(@RequestParam("taskId") String taskId) {
        if (!TaskConfig.containsTask(taskId)) {
            return ScheduleTaskConfig.STATUS.TASK_NOT_EXISTS;
        }
        try {
            log.info("cancel task:" + taskId);
            TaskConfig.removeTask(taskId).getScheduledTask().cancel();
        } catch (Exception e) {
            log.error("取消任务失败:" + taskId, e);
        }
        return ScheduleTaskConfig.STATUS.SUCCESS;
    }
}
