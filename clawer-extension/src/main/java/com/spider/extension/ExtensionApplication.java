package com.spider.extension;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableTransactionManagement
@EnableScheduling
public class ExtensionApplication {

    public static void main(String[] args) {
        SpringApplication.run(ExtensionApplication.class, args);
    }
}
