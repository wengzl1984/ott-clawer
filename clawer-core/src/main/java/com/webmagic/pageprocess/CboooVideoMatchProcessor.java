package com.webmagic.pageprocess;

import com.webmagic.util.PhantomJsDriver;
import org.apache.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.processor.PageProcessor;
import us.codecraft.webmagic.selector.Html;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Auther: ljs
 * @Date: 2019/6/15 12:31
 * @Description:
 */
@Component
public class CboooVideoMatchProcessor implements PageProcessor {
    private static Logger log = Logger.getLogger(CboooVideoMatchProcessor.class);

    @Autowired
    PhantomJsDriver phantomJsDriver;
    private SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private String searchVideoName = null;//要查询的媒资名称
    private Map<String, String> ruleMap = new HashMap<String, String>();//规则map
    private WebDriver driver = null;
    private Site site;
    ;
    @Value("${phantomjs.maxPageNum}")
    private int maxPageNum;
    private String relateType = null;

    public CboooVideoMatchProcessor() {

    }

    public CboooVideoMatchProcessor(Map<String, String> map, Site site) {
        this.ruleMap = map;
        this.site = site;

    }

    @Override
    public Site getSite() {
        return site;
    }

    public void setSite(Site site) {
        this.site = site;
    }

    public Map<String, String> getRuleMap() {
        return ruleMap;
    }

    public void setRuleMap(Map<String, String> ruleMap) {
        this.ruleMap = ruleMap;
    }

    @Override
    public void process(Page page) {
        log.info("start time=" + format.format(new Date()));
        if (page.getRequest().getUrl().indexOf("http://cbooo.cn/search?k=") != -1) {
            searchVideoName = page.getRequest().getUrl().substring(25);
            relateType = "5";
            try {
                searchVideoName = URLDecoder.decode(searchVideoName, "utf-8");
            } catch (UnsupportedEncodingException e) {
                log.error("CboooPageProcessor UnsupportedEncodingException ", e);
            }
            log.info("searchVideoName=" + searchVideoName);
            if (driver == null) {
                driver = phantomJsDriver.getPhantomJSDriver();
                driver.manage().window().setSize(new Dimension(1920, 1080));

            }
            driver.get(page.getRequest().getUrl());
            try {
                WebElement webElement = driver.findElement(By.id("top"));
                String str = webElement.getAttribute("outerHTML");
                str = str.replaceAll("&nbsp;", "");
                // System.out.println("str=" + str);
                Html html = new Html(str);
                List<String> descInfo = html.xpath("//*[@id=\"top\"]/div[3]/div[2]/div[2]/ul[1]/*/span/text()").all();
                List<String> link = html.xpath("//*[@id=\"top\"]/div[3]/div[2]/div[2]/ul[1]/*/a/@href").all();
                log.info("link=" + link);
                List<String> linkName = html.xpath("//*[@id=\"top\"]/div[3]/div[2]/div[2]/ul[1]/*/a/text()").all();

                for (int i = 0; i < linkName.size(); i++) {
                    log.info("linkName=" + linkName.get(i));
                    if ((descInfo.get(i).length()) > 5 && descInfo.get(i).indexOf("万") != -1) {
                        log.info("add page link=" + link.get(i));
                        page.addTargetRequest(link.get(i));
                    }
                }
                //说明未查询到符合的资源
                if (page.getTargetRequests().size() <= 0) {
                    log.info("searchVideoName=" + searchVideoName + ",can not find");
                    cboooPageException(page, null);
                }
                log.info("currentPageNum=" + ruleMap.get("CURRENT_PAGE_NUM") + ",maxPageNum=" + maxPageNum);
                if ((Integer.valueOf(ruleMap.get("CURRENT_PAGE_NUM"))) % maxPageNum == 0) {
                    log.info("cbooo close phantomjs ");
                    driver.close();//关闭
                    driver.quit();//退出
                    driver = null;
                }
            } catch (Exception e) {
                cboooPageException(page, e);

            }

        } else if (page.getRequest().getUrl().indexOf("http://www.cbooo.cn/m/") != -1) {
            relateType = "5";
            getCboooInfo(page);
        }
        log.info("end time=" + format.format(new Date()));
        log.info("page.response=" + page.getStatusCode());
    }

    private void getCboooInfo(Page page) {
        try {
            //导演
            String videoName = page.getHtml().xpath("//*[@id=\"top\"]/div[3]/div[2]/div/div[1]/div[2]/div[1]/h2/text()").get();
            String videoBoxOffice = page.getHtml().xpath("//*[@id=\"top\"]/div[3]/div[2]/div/div[1]/div[2]/div[1]/p[1]/span/text()").get();
            log.info("videoBoxOffice=" + videoBoxOffice);
            log.info("videoName=" + videoName);
            //取出的票房信息为累计票房xxxx万 解析处理
            if (videoBoxOffice != null) {
                if (videoBoxOffice.indexOf("累计票房") != -1) {
                    videoBoxOffice = videoBoxOffice.replace("累计票房", "");
                    videoBoxOffice = videoBoxOffice.replace("万", "");
                }
            }
            StringBuffer sb = new StringBuffer();
            sb.append("{" + "\"matchCboooName\":" + "\"" + videoName + "\"");
            sb.append(",\"videoBoxOffice\":" + "\"" + videoBoxOffice + "\"" +
                    "}");

            log.info("retJson=" + sb.toString());
            HashMap<String, Object> pipelineMap = new HashMap<String, Object>();
            pipelineMap.put("info", sb.toString());
            pipelineMap.put("taskId", ruleMap.get(searchVideoName));
            pipelineMap.put("relateType", relateType);

            page.putField("Info", pipelineMap);
        } catch (Exception e) {
            log.error("getCboooInfo exception:" + e);
        }
    }

    /**
     * 中国票房网异常处理
     *
     * @param page
     */
    public void cboooPageException(Page page, Exception e) {
        log.info("cbooo seach videoName=" + searchVideoName + ",url=" + page.getUrl() + ",exception", e);
        HashMap<String, Object> pipelineMap = new HashMap<String, Object>();
        StringBuffer sb = new StringBuffer();
        sb.append("{" + "\"matchCboooName\":" + "\"" + null + "\"");
        sb.append(",\"videoBoxOffice\":" + "\"" + null + "\"" +
                "}");
        pipelineMap.put("info", sb.toString());
        pipelineMap.put("taskId", ruleMap.get(searchVideoName));
        pipelineMap.put("relateType", relateType);

        page.putField("Info", pipelineMap);
    }

}