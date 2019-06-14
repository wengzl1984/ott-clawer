package com.webmagic.pageprocess;

import com.webmagic.entity.HotBoardInfoExt;
import com.webmagic.util.UserAgentUtil;
import org.apache.log4j.Logger;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.processor.PageProcessor;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

/**
 * @Auther: ljs
 * @Date: 2019/6/5 20:20
 * @Description:
 */
public class BoardInfoPageProcessor implements PageProcessor {
    private static Logger log = Logger.getLogger(BoardInfoPageProcessor.class);
    private SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
    public static final String MAOYAN_HOT_URL = "https://maoyan.com/board/7";//最热榜单
    public static final String BAIDU_TV_URL = "http://top.baidu.com/buzz?b=4&c=2&fr=topcategory_c2";
    public static final String BAIDU_MOVIE_URL = "http://top.baidu.com/buzz?b=26&c=1&fr=topcategory_c1";//最期待榜单列表详情

    //抓取网站的相关配置，包括编码、抓取间隔、重试次数等
    private Site site = Site.me()
            .setCharset("utf-8")
            .setTimeOut(30000)
            .setRetryTimes(3)
            .setUserAgent(UserAgentUtil.getRandomUserAgent())
            .setSleepTime(new Random().nextInt(3) * 1000);

    @Override
    public void process(Page page) {
        if (page.getUrl().regex(MAOYAN_HOT_URL).match()) {
            log.info("猫眼热映电影榜单，url=" + page.getUrl());
          //  hotMoviesPost(page);
        } else if (page.getUrl().regex(BAIDU_TV_URL).match()) {//百度电视榜单
            log.info("百度电视榜单，url=" + page.getUrl());
            //ExpectMoviesPost(page);

        } else {//百度电影榜单
            log.info("百度电影榜单，url=" + page.getUrl());
            baiDuMoiveDeal(page);
        }


    }

    /**
     * 抓取热门口碑榜单信息
     *
     * @param page 当前页面对象
     */
    private void hotMoviesPost(Page page) {
        //排名
        List<String> videoRankiListTmp = page.getHtml().xpath("//*[@id=\"app\"]/div/div/div/dl/*/i/text()").all();
        //电影名称
        List<String> videoNameList = page.getHtml().xpath("//*[@id=\"app\"]/div/div/div/dl/*/div/div/div[1]/p[1]/a/text()").all();
        log.info("videoNameList=" + videoNameList);

        //上映时间
        List<String> videoReleaseList = page.getHtml().xpath("//*[@id=\"app\"]/div/div/div/dl/*/div/div/div[1]/p[3]/text()").all();
        log.info("videoReleaseList=" + videoReleaseList);

        //主演名单
        List<String> videoCastList = page.getHtml().xpath("//*[@id=\"app\"]/div/div/div/dl/*/div/div/div[1]/p[2]/text()").all();
        log.info("videoCastList=" + videoCastList);

        //评分前半部分
        List<String> videoRateStartList = page.getHtml().xpath("//*[@id=\"app\"]/div/div/div/dl/*/div/div/div[2]/p/i[1]/text()").all();
        log.info("videoRateStartList=" + videoRateStartList);

        //得分后半部分
        List<String> videoRateEndList = page.getHtml().xpath("//*[@id=\"app\"]/div/div/div/dl/*/div/div/div[2]/p/i[2]/text()").all();
        log.info("videoRateEndList=" + videoRateEndList);

        //评分处理
        List<Double> videoRateList = new ArrayList<>();
        if (videoRateStartList != null && videoReleaseList != null) {
            for (int i = 0; i < videoReleaseList.size(); i++) {
                videoRateList.add(Double.parseDouble(videoRateStartList.get(i)) + Double.parseDouble(videoRateEndList.get(i)) / 10);
            }
        }
        //上映时间处理
        List<Date> videoReleaseTimeList = new ArrayList<>();
        if (videoReleaseList != null) {
            for (int i = 0; i < videoRateStartList.size(); i++) {
                try {
                    videoReleaseTimeList.add(format.parse(videoReleaseList.get(i).replace("上映时间：", "")));
                } catch (ParseException e) {
                    log.error("ParseException releaseTime exception:", e);
                }
            }
        }
        //排名处理
        List<Integer> videoRankiList = new ArrayList<>();
        if (videoRankiListTmp != null) {
            for (int i = 0; i < videoRankiListTmp.size(); i++) {
                videoRankiList.add(Integer.parseInt(videoRankiListTmp.get(i)));
            }
        }
        log.info("videoNameList=" + videoNameList);
        log.info("videoReleaseTimeList=" + videoReleaseTimeList);
        log.info("videoRateList=" + videoRateList);
        log.info("videoRankiList=" + videoRankiList);


        //测试读取豆瓣的信息

        //设置
        HotBoardInfoExt hotBoardInfoExt = new HotBoardInfoExt();
        hotBoardInfoExt.setVideoNameList(videoNameList);
        hotBoardInfoExt.setVideoCastList(videoCastList);
        hotBoardInfoExt.setVideoRateList(videoRateList);
        hotBoardInfoExt.setVideoReleaseList(videoReleaseTimeList);
        page.putField("hotBoardInfoExt", hotBoardInfoExt);
    }

    public void baiDuMoiveDeal(Page page){
        //得到排名
        List<String> videoRankiList = page.getHtml().xpath("//*[@id=\"main\"]/div[2]/div/table/tbody/*/td[1]/span/text()").all();

        //得到搜索指
        List<String> videoRateList = page.getHtml().xpath("//*[@id=\"main\"]/div[2]/div/table/tbody/*/td[4]/span/text()").all();
        //得到媒资名称
        List<String> videoNameList = page.getHtml().xpath("//*[@id=\"main\"]/div[2]/div/table/tbody/*/td[2]/a[1]/text()").all();

        log.info("videoRankiList="+videoRankiList);
        log.info("videoRateList="+videoRateList);
        log.info("videoNameList="+videoNameList);
    }

    @Override
    public Site getSite() {
        return site;
    }
}
