package com.webmagic.pageprocess;

import com.webmagic.util.PhantomJsDriver;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.processor.PageProcessor;
import us.codecraft.webmagic.selector.Html;

import java.text.SimpleDateFormat;
import java.util.Date;


public class DouBanPageProcessor implements PageProcessor {

    private SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
    private int countNum = 0;
    private WebDriver driver = null;


    private Site site = Site
            .me()
            .setCharset("UTF-8")
            .setCycleRetryTimes(3)
            .setSleepTime(3 * 1000)
            .addHeader("Connection", "keep-alive")
            .addHeader("Cache-Control", "max-age=0")
            .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:50.0) Gecko/20100101 Firefox/50.0");


    @Override
    public Site getSite() {
        return site;
    }


    @Override
    public void process(Page page) {
        System.out.println("start time=" + format.format(new Date()));
        if (page.getRequest().getUrl().indexOf("https://movie.douban.com/subject_search?") != -1) {
            countNum = +1;
            if (driver == null) {
                driver = PhantomJsDriver.getPhantomJSDriver();
            }
                driver.get(page.getRequest().getUrl());

                //  System.out.println("page.html="+page.getHtml());
                WebElement webElement = driver.findElement(By.id("wrapper"));
                String str = webElement.getAttribute("outerHTML");
                System.out.println("str=" + str);
                Html html = new Html(str);
                String link = html.xpath("//*[@id=\"root\"]/div/div[2]/div[1]/div[1]/div[1]/div[1]/div/div[1]/a/@href").get();
                System.out.println("link=" + link);
                page.addTargetRequest(link);
            driver.get("about:blank");
            //if (countNum > 20) {
                driver.close();//关闭
                driver.quit();//退出
                driver = null;
          //  }

            //  driver.quit();
        } else {
            getDouBanInfo(page);
        }
        System.out.println("end time=" + format.format(new Date()));

    }

    private void getDouBanInfo(Page page) {

        //导演
        String director = page.getHtml().xpath("//*[@id=\"info\"]/span[1]/span[2]/*/text()").all().toString();
        //主演
//        String actor = page.getHtml().xpath("//*[@id=\"info\"]/span[3]/span[2]/span[1]/a/text()").all().toString();
        //xpath写法在浏览器可以出来结果，但是关于主演这个怎么都出不来
        String actor = page.getHtml().xpath("//*[@id=\"info\"]/span[@class=\"actor\"]/span[@class=\"attrs\"]/*/a/text()").all().toString();
        System.out.println("actor1=" + actor);

        actor = page.getHtml().getDocument().select("div[id=info]").select("span[class=actor]").select("span[class=attrs]").text();

        System.out.println("actor2=" + actor);
        //类型
        String type = page.getHtml().xpath("//*[@id=\"info\"]/span[@property=\"v:genre\"]/text()").all().toString();
        //上映日期
        String releaseDate = page.getHtml().xpath("//*[@id=\"info\"]/span[@property=\"v:initialReleaseDate\"]/text()").all().toString();
        //imdb链接
        String imdb = page.getHtml().xpath("//*[@id=\"info\"]/a/@href").all().toString();
        //评分
        String average = page.getHtml().xpath("//*[@id=\"interest_sectl\"]/div[1]/div[2]/strong[@property=\"v:average\"]/text()").all().toString();

        //电影名称
        String movieName = page.getHtml().xpath("//*[@id=\"content\"]/h1/span[@property=\"v:itemreviewed\"]/text()").all().toString();

        //电影链接
        String linkUrl = page.getUrl().get();
        StringBuffer sb = new StringBuffer();

        sb.append("电影名:" + movieName + "\n");
        sb.append("url:" + linkUrl + "\n");
        sb.append("类型:" + type + "\n");
        sb.append("主演:" + actor + "\n");
        sb.append("导演:" + director + "\n");
        sb.append("上映日期:" + releaseDate + "\n");
        sb.append("评分:" + average + "\n");
        sb.append("IMDB:" + imdb + "\n");
        System.out.println("movie info =" + sb.toString());
    }


}