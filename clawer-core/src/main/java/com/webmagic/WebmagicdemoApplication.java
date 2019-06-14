package com.webmagic;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@EnableTransactionManagement(proxyTargetClass = true)
@EnableScheduling   // 2.开启定时任务
@MapperScan("com.webmagic.dao")
@SpringBootApplication
public class WebmagicdemoApplication {

    public static void main(String[] args) {
    	try {
            SpringApplication.run(WebmagicdemoApplication.class, args);

    	} catch (Exception e) {
    		e.printStackTrace();
    	}
    }

}
