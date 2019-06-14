package com.webmagic.web;

import com.webmagic.pageprocess.BoardInfoPageProcessor;
import com.webmagic.pageprocess.DouBanPageProcessor;
import com.webmagic.pageprocess.MaoYanPageProcessor;
import com.webmagic.pipeline.MaoYanPipeline;
import com.webmagic.util.PhantomJsDriver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import us.codecraft.webmagic.Request;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.downloader.HttpClientDownloader;
import us.codecraft.webmagic.proxy.Proxy;
import us.codecraft.webmagic.proxy.SimpleProxyProvider;

import java.util.ArrayList;
import java.util.List;


/**
 * 用于测试使用
 */
@RestController
public class StartUpController {

    @Autowired
    MaoYanPipeline maoYanPipeline;
    /*
    @Autowired
    ProxyIpMapper proxyIpMapper;
    */
    @GetMapping("/")
    public String index() {

        /*
        List<ProxyIp> proxyList = proxyIpMapper.findAllProxies();
        proxyList = proxyList.subList(0,10);
        List<Proxy> proxies = new ArrayList<>(proxyList.size());
        for(ProxyIp proxyIp : proxyList) {
            proxies.add(new Proxy(proxyIp.getIp(), proxyIp.getPort()));
        }
        */

        List<Request> list = new ArrayList<Request>();
        for(int i=0;i<25;i++){
        }
        for(int i=0;i<1;i++){
            list.add(new Request("https://movie.douban.com/j/search_subjects?type=movie&tag=%E7%83%AD%E9%97%A8&sort=recommend&page_limit=20&page_start=" + i).setPriority(1));
        }


        Spider.create(new DouBanPageProcessor())
                       .startRequest(list)
                .thread(4)
                .run();
        return "爬虫开启";
    }


    @GetMapping("/maoyan")
    public String indexMaoYan() {
        List<Request> list = new ArrayList<Request>();
        for(int i=0;i<1;i++){
           // list.add(new Request(("https://movie.douban.com")));
             list.add(new Request("https://maoyan.com/films/337625"));
             list.add(new Request("https://maoyan.com/films/246061"));
            list.add(new Request("https://maoyan.com/films/1226516"));
            list.add(new Request("https://maoyan.com/films/344328"));
            list.add(new Request("https://maoyan.com/films/1218215"));
            list.add(new Request("https://maoyan.com/films/1242431"));
            list.add(new Request("https://maoyan.com/films/346629"));
        }
        Spider.create(new MaoYanPageProcessor())
               .startRequest(list)
              //  .addPipeline(maoYanPipeline)
                .thread(1)
                .run();

        return "爬虫开启";
    }

    @GetMapping("/DouBanSearch")
    public String douBanSearch1(@RequestParam("keyWord") String keyWord){
        Spider spider=Spider.create(new DouBanPageProcessor());
        HttpClientDownloader httpClientDownloader = new HttpClientDownloader();
        List<Request> list = new ArrayList<Request>();

        for(int i=0;i<1;i++){
            list.add(new Request("https://movie.douban.com/subject_search?search_text="+ PhantomJsDriver.getKeyWord("何以为家")+"&cat=1002"));
            list.add(new Request("https://movie.douban.com/subject_search?search_text="+ PhantomJsDriver.getKeyWord("调音师")+"&cat=1002"));

        }
        spider.setDownloader(httpClientDownloader);
        spider.startRequest(list)
                .thread(1).run();
        return null;
    }

    @GetMapping("/BoardInfo")
    public String boardInfo(){
        Spider spider=Spider.create(new BoardInfoPageProcessor());
        List<Request> list = new ArrayList<Request>();

        for(int i=0;i<1;i++){
            list.add(new Request("https://maoyan.com/board/7"));
            list.add(new Request("http://top.baidu.com/buzz?b=26&c=1&fr=topcategory_c1"));

        }
        spider.startRequest(list)
                .thread(4).run();
        return "开始爬虫";
    }
}
