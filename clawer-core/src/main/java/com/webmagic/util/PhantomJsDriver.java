package com.webmagic.util;

import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriverService;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * PhantomJs是一个基于webkit内核的无头浏览器，即没有UI界面，即它就是一个浏览器，只是其内的点击、翻页等人为相关操作需要程序设计实现;
 * 因为爬虫如果每次爬取都调用一次谷歌浏览器来实现操作,在性能上会有一定影响,而且连续开启十几个浏览器简直是内存噩梦,
 * 因此选用phantomJs来替换chromeDriver
 * PhantomJs在本地开发时候还好，如果要部署到服务器，就必须下载linux版本的PhantomJs,相比window操作繁琐
 * @author ljs
 * @date 2019/06/02
 */
@Component
public class PhantomJsDriver {
    @Value("${phantomjs.path}")
    private  String phantomJsPath ;


    public  PhantomJSDriver getPhantomJSDriver(){
        //设置必要参数
        DesiredCapabilities dcaps = new DesiredCapabilities();
        //ssl证书支持
        dcaps.setCapability("acceptSslCerts", false);
        //截屏支持
        dcaps.setCapability("takesScreenshot", false);
        //css搜索支持
        dcaps.setCapability("cssSelectorsEnabled", false);
        //js支持
        dcaps.setJavascriptEnabled(true);
        //驱动支持
        System.out.println("phantomJsPath="+phantomJsPath);
        dcaps.setCapability(PhantomJSDriverService.PHANTOMJS_EXECUTABLE_PATH_PROPERTY,phantomJsPath);

        PhantomJSDriver driver = new PhantomJSDriver(dcaps);
        return  driver;
    }
    /**
     * 使用chromeDriver程序正常运行,转换成PhtanomJs后发现查询到的数据不是想要的数据，复制HTML查看页面后,
     * 发现搜索的数据是错乱的,搜索框上显示着？？？，猜测是转码的问题，经过URLEncode之后，程序正常运行。
     * @return
     */
    public static  String getKeyWord(String keyWord) {
        try {
            return URLEncoder.encode(keyWord,"UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return StringUtils.EMPTY;
    }
    public static void main(String[] args) {
        PhantomJsDriver phantomJsDriver = new PhantomJsDriver() ;
        WebDriver driver=phantomJsDriver.getPhantomJSDriver();
        driver.get("http://www.baidu.com");
        System.out.println(driver.getCurrentUrl());
    }
}
