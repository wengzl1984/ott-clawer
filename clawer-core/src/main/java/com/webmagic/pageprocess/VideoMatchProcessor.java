package com.webmagic.pageprocess;

import com.webmagic.util.PhantomJsDriver;
import org.apache.log4j.Logger;
import org.openqa.selenium.By;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @Auther: ljs
 * @Date: 2019/6/15 12:31
 * @Description:
 */
@Component
public class VideoMatchProcessor implements PageProcessor {
    private static Logger log = Logger.getLogger(VideoMatchProcessor.class);

    @Autowired
    PhantomJsDriver phantomJsDriver;

    private SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
    private String searchVideoName = null;//要查询的媒资名称
    private Map<String, String> ruleMap = new HashMap<String, String>();//规则map
    private WebDriver driver = null;
    private Site site;
    ;
    @Value("${phantomjs.maxPageNum}")
    private int maxPageNum;
    private String relateType = null;

    public VideoMatchProcessor() {

    }

    public VideoMatchProcessor(Map<String, String> map, Site site) {
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
            }
            driver.get(page.getRequest().getUrl());
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
            log.info("currentPageNum=" + ruleMap.get("CURRENT_PAGE_NUM") + ",maxPageNum=" + maxPageNum);
            if ((Integer.valueOf(ruleMap.get("CURRENT_PAGE_NUM"))) % maxPageNum == 0) {
                log.info("cbooo close phantomjs ");
                driver.close();//关闭
                driver.quit();//退出
                driver = null;
            }

        } else if (page.getRequest().getUrl().indexOf("https://movie.douban.com/subject_search?") != -1) {
            searchVideoName = page.getRequest().getUrl().substring(52, page.getRequest().getUrl().indexOf("&cat=1002"));
            relateType = "4";
            try {
                searchVideoName = URLDecoder.decode(searchVideoName, "utf-8");
            } catch (UnsupportedEncodingException e) {
                log.error("DouBanPageProcessor UnsupportedEncodingException ", e);
            }
            log.info("searchVideoName=" + searchVideoName);
            if (driver == null) {
                driver = phantomJsDriver.getPhantomJSDriver();
            }
            driver.get(page.getRequest().getUrl());
            WebElement webElement = driver.findElement(By.id("wrapper"));
            String str = webElement.getAttribute("outerHTML");
            Html html = new Html(str);
            List<String> link = html.xpath("//*[@id=\"root\"]/div/div[2]/div[1]/div[1]/*/*/div/div[1]/a/@href").all();
            List<String> movieNameList = html.xpath("//*[@id=\"root\"]/div/div[2]/div[1]/div[1]/*/*/div/div[1]/a/text()").all();
            log.info("link=" + link);
            log.info("movieNameList=" + movieNameList);
            for (int i = 0; i < movieNameList.size(); i++) {
                if (movieNameList.get(i) != null && movieNameList.get(i).indexOf(searchVideoName) != -1) {
                    log.info("add page link=" + link.get(i));
                    page.addTargetRequest(link.get(i));
                }
            }
            //说明未查询到符合的资源
            if (page.getTargetRequests().size() <= 0) {
                log.info("searchVideoName=" + searchVideoName + ",can not find");
                HashMap<String, Object> pipelineMap = new HashMap<String, Object>();
                StringBuffer sb = new StringBuffer();
                sb.append("{ " + "\"matchDouBanName\":" + "\"" + null + "\"" + ",\"matchSeriesCountry\":" + "\"" + null + "\",")
                        .append("\"matchSeriesDirector\":" + "\"" + null + "\",")
                        .append("\"matchReleaseDates\":" + "\"" + null + "\",")
                        .append("\"videoCast\":" + "\"" + null + "\",")
                        .append("\"videoTags\":" + "\"" + null + "\",")
                        .append("\"videoRate\":" + "\"" + null + "\",")
                        .append("\"imdb\":" + "\"" + null + "\"}");
                pipelineMap.put("info", sb.toString());
                pipelineMap.put("taskId", ruleMap.get(searchVideoName));
                pipelineMap.put("relateType", relateType);

                page.putField("Info", pipelineMap);
            }
            log.info("currentPageNum=" + ruleMap.get("CURRENT_PAGE_NUM") + ",maxPageNum=" + maxPageNum);
            if ((Integer.valueOf(ruleMap.get("CURRENT_PAGE_NUM"))) % maxPageNum == 0) {
                log.info("douban close phantomjs ");
                driver.close();//关闭
                driver.quit();//退出
                driver = null;
            }

        } else if ("4".equals(relateType)) {
            getDouBanInfo(page);
        } else if ("5".equals(relateType)) {
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

    private void getDouBanInfo(Page page) {

        int id1 = -1;
        int id2 = -1;
        //导演
        String director = page.getHtml().xpath(ruleMap.get("directorXpath")).get();
        if (director != null) {
            director = director.trim();
        }
        log.info("director=" + director);
        //主演
//        String actor = page.getHtml().xpath("//*[@id=\"info\"]/span[3]/span[2]/span[1]/a/text()").all().toString();
        //xpath写法在浏览器可以出来结果，但是关于主演这个怎么都出不来
        //  String actor = page.getHtml().xpath("//*[@id=\"info\"]/span[@class=\"actor\"]/span[@class=\"attrs\"]/*/a/text()").all().toString();

        String actor = page.getHtml().getDocument().select("div[id=info]").select("span[class=actor]").select("span[class=attrs]").text();

        log.info("actor=" + actor);
        //类型
        String type = page.getHtml().xpath(ruleMap.get("typeXpath")).get();
        //上映日期
        String releaseDate = page.getHtml().xpath(ruleMap.get("releaseDateXpath")).get();
        //取出来的值有些2014-08-16(中国大陆)，解析出具体的时间
        if (releaseDate != null) {
            id1 = releaseDate.indexOf("(");
            if (id1 != -1) {
                id2 = releaseDate.indexOf(")", id1);
                if (id2 != -1) {
                    releaseDate = releaseDate.substring(0, id1);
                }
            }
        }
        //imdb链接
        String imdb = page.getHtml().xpath(ruleMap.get("imdbXpath")).get();
        //评分
        String average = page.getHtml().xpath(ruleMap.get("averageXpath")).get();
        //电影名称
        String movieName = page.getHtml().xpath(ruleMap.get("videoNameXpath")).get();
        //国家 地区
        String seriesCountry = page.getHtml().toString();
        id1 = seriesCountry.indexOf("制片国家/地区:");
        if (id1 != -1) {
            id2 = seriesCountry.indexOf("<br>", id1 + 15);
            if (id2 != -1) {
                seriesCountry = seriesCountry.substring(id1 + 15, id2);
                if (seriesCountry != null) {
                    seriesCountry = seriesCountry.replaceAll(" ", "");
                    Pattern p = Pattern.compile("\\s*|\t|\r|\n");
                    Matcher m = p.matcher(seriesCountry);
                    seriesCountry = m.replaceAll("");
                    log.info("seriesCountry=" + seriesCountry);
                }
            }
        }
        StringBuffer sb = new StringBuffer();
        sb.append("{ " + "\"matchDouBanName\":" + "\"" + movieName + "\"" + ",\"matchSeriesCountry\":" + "\"" + seriesCountry + "\",")
                .append("\"matchSeriesDirector\":" + "\"" + director + "\",")
                .append("\"matchReleaseDates\":" + "\"" + releaseDate + "\",")
                .append("\"videoCast\":" + "\"" + actor + "\",")
                .append("\"videoTags\":" + "\"" + type + "\",")
                .append("\"videoRate\":" + "\"" + average + "\",")
                .append("\"imdb\":" + "\"" + imdb + "\"}");
//
        log.info("retJson=" + sb.toString());
        HashMap<String, Object> pipelineMap = new HashMap<String, Object>();
        pipelineMap.put("info", sb.toString());
        pipelineMap.put("taskId", ruleMap.get(searchVideoName));
        pipelineMap.put("relateType", relateType);
        page.putField("Info", pipelineMap);

    }
}