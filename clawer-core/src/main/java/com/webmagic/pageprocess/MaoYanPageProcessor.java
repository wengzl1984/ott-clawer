package com.webmagic.pageprocess;

import com.webmagic.entity.ExpectBoardInfoList;
import com.webmagic.entity.HotBoardInfoList;
import com.webmagic.util.PhantomJsDriver;
import com.webmagic.util.UserAgentUtil;
import org.apache.log4j.Logger;
import org.openqa.selenium.WebDriver;
import org.springframework.beans.factory.annotation.Autowired;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.processor.PageProcessor;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Random;

public class MaoYanPageProcessor implements PageProcessor {//修改改类，定制自己的抽取逻辑
    private static Logger log = Logger.getLogger(MaoYanPageProcessor.class);
    private SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

    public static final String EXPECT_LIST = "https://maoyan.com/board/6";//最期待榜单
    public static final String EXPECT_LIST_L = "https://maoyan.com/board/6\\?offset=\\d+";//最期待榜单列表详情
    public static final String HOT_LIST = "https://maoyan.com/board/7";//热映口碑榜单
    public static final String DOMESTIC_LIST = "https://maoyan.com/board/1";//国内票房榜单
    public static final String NORTH_AMERICA_LIST = "https://maoyan.com/board/2";//北美票房榜单
    public static final String TOP_100_LIST = "https://maoyan.com/board/4";//top100票房榜单
    public static final String DOUBAN_LIST = "https://movie.douban.com/subject/\\d+";//豆瓣电影列表
    private WebDriver driver = null;

    //抓取网站的相关配置，包括编码、抓取间隔、重试次数等
    private Site site = Site.me()
            .setCharset("utf-8")
            .setTimeOut(30000)
           // .addHeader("Host","movie.douban.com")
            .setRetryTimes(3)
           // .addHeader("Proxy-Authorization", ProxyGeneratedUtil.authHeader(ORDER_NUM, SECRET, (int) (new Date().getTime()/1000)))//设置代理

            .setUserAgent(UserAgentUtil.getRandomUserAgent())
            //.addHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/28.0.1500.71 Safari/537.36")
         //   .addHeader("cookic", "oscid=xxx")

            .setSleepTime(new Random().nextInt(5) * 1000);

    @Override
    public void process(Page page) {

        System.out.println("start time=" + format.format(new Date()));

        System.out.println("page.url===="+page.getUrl());
        if (driver == null) {
            driver = PhantomJsDriver.getPhantomJSDriver();
        }


//        try {
//            driver = phantomJsDriverPool.get();
//
//        } catch (InterruptedException e) {
//            System.out.println("exception:"+e);
//        }

            if (driver != null) {
                   driver.get(page.getRequest().getUrl());

            }

            // System.out.println("===========source="+ driver.getPageSource());
            // driver.close();
            //  driver.quit();
            // driver = null;
            if (page.getUrl().regex("https://maoyan.com/board").match()) {
                log.info("page.getHtml()=" + page.getHtml());
                //加入满足条件的链接
                page.addTargetRequests(
                        page.getHtml().xpath("/html/body/div[3]/ul/*/a/@href").all());
                //page.setSkip(true);
                log.error("获取到的目标地址有：" + page.getTargetRequests().size() + "个");


            } else {
                //  page.addTargetRequest("https://movie.douban.com/subject_search?search_text=何以为家&cat=1002");
                String movieName = page.getHtml().xpath("/html/body/div[3]/div/div[2]/div[1]/h3/text()").get();
                System.out.println("movieName =" + movieName);
                // System.out.println("getHtml ="+page.getHtml());

            }
            if (page.getUrl().regex(HOT_LIST).match()) {
                log.info("热映榜单");
                hotMoviesPost(page);
            } else if (page.getUrl().regex(EXPECT_LIST).match()) {
                log.info("最期待榜单");
                ExpectMoviesPost(page);

            } else if (page.getUrl().regex(DOMESTIC_LIST).match()) {
                log.info("国内票房榜单");
            } else if (page.getUrl().regex(NORTH_AMERICA_LIST).match()) {
                log.info("北美票房榜单");
            } else if (page.getUrl().regex(TOP_100_LIST).match()) {
                log.info("top100票房榜单");
            }
            System.out.println("end time=" + format.format(new Date()));
           //driver.close();

    }

    /**
     * 抓取热门口碑榜单信息
     *
     * @param page 当前页面对象
     */
    private void hotMoviesPost(Page page) {
        //电影名称
        List<String> moveNameList = page.getHtml().xpath("//*[@id=\"app\"]/div/div/div/dl/*/div/div/div[1]/p[1]/a/text()").all();
        //上映时间
        List<String> releaseTimeList = page.getHtml().xpath("//*[@id=\"app\"]/div/div/div/dl/*/div/div/div[1]/p[3]/text()").all();
        //主演名单
        List<String> moveStarsList = page.getHtml().xpath("//*[@id=\"app\"]/div/div/div/dl/*/div/div/div[1]/p[2]/text()").all();
        //评分前半部分
        List<String> moveScoreStartList = page.getHtml().xpath("//*[@id=\"app\"]/div/div/div/dl/*/div/div/div[2]/p/i[1]/text()").all();
        //得分后半部分
        List<String> moveScoreEndList = page.getHtml().xpath("//*[@id=\"app\"]/div/div/div/dl/*/div/div/div[2]/p/i[2]/text()").all();

        log.info("moveNameList=" + moveNameList);
        //测试读取豆瓣的信息

        //设置
        HotBoardInfoList hotBoardInfoList = new HotBoardInfoList();
        hotBoardInfoList.setMoveNameList(moveNameList);
        hotBoardInfoList.setMoveStarList(moveStarsList);
        hotBoardInfoList.setMoveScoreEndList(moveScoreEndList);
        hotBoardInfoList.setMoveScoreStartList(moveScoreStartList);
        hotBoardInfoList.setReleaseTimeList(releaseTimeList);
        page.putField("hotBoardInfoList", hotBoardInfoList);
    }

    private void ExpectMoviesPost(Page page) {
        System.out.println("page.html="+page.getHtml());
        page.addTargetRequests(page.getHtml().links().regex(EXPECT_LIST_L).all());//把所有用户主页的URL加入抓取队列
        //电影名称
        List<String> moveNameList = page.getHtml().xpath("//*[@id=\"app\"]/div/div/div[1]/dl/*/div/div/div[1]/p[1]/a/text()").all();
        //上映时间
        List<String> releaseTimeList = page.getHtml().xpath("//*[@id=\"app\"]/div/div/div[1]/dl/*/div/div/div[1]/p[3]/text()").all();
        //主演名单
        List<String> moveStarsList = page.getHtml().xpath("//*[@id=\"app\"]/div/div/div[1]/dl/*/div/div/div[1]/p[2]/text()").all();
        //本月新增想看人数
        List<String> monthToSeeNumList = page.getHtml().xpath("//*[@id=\"app\"]/div/div/div[1]/dl/*/div/div/div[2]/p[1]/span/span/text()").all();
        //总共想看人数
        List<String> totalToSeeNumList = page.getHtml().xpath("//*[@id=\"app\"]/div/div/div[1]/dl/*/div/div/div[2]/p[2]/span/span/text()").all();
//        for(int y =0;y<totalToSeeNumList.size();y++){
////            try {
////                System.out.println("to see="+totalToSeeNumList.get(y).getBytes("gbk"));
////            } catch (UnsupportedEncodingException e) {
////                e.printStackTrace();
////            }
////        }

        log.info("Expect monthToSeeNumList=" + monthToSeeNumList);
//        try {
//            System.out.println("======"+new String(totalToSeeNumList.get(1).getBytes("UTF-8"),"gbk"));
//        } catch (UnsupportedEncodingException e) {
//            e.printStackTrace();
//        }
        //设置
        ExpectBoardInfoList expectBoardInfoList = new ExpectBoardInfoList();
        expectBoardInfoList.setMoveNameList(moveNameList);
        expectBoardInfoList.setMoveStarsList(moveStarsList);
        expectBoardInfoList.setMonthToSeeNumList(monthToSeeNumList);
        expectBoardInfoList.setTotalToSeeNumList(totalToSeeNumList);
        expectBoardInfoList.setReleaseTimeList(releaseTimeList);
        page.putField("expectBoardInfoList", expectBoardInfoList);

    }


    @Override
    public Site getSite() {
        return site;
    }
}

