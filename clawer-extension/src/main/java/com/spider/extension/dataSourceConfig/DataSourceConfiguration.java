package com.spider.extension.dataSourceConfig;

import com.alibaba.druid.pool.DruidDataSource;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import javax.sql.DataSource;

/**
 * @author ljs
 * @version V1.0
 * @Title: ${file_name}
 * @Package com.lynn.conf
 * @Description: TODO
 * @date 2019/5/27 22:37
 */

@Configuration
@MapperScan(basePackages = "com.spider.extension.dao")
public class DataSourceConfiguration {
    @Autowired
    private Environment env;
    @Bean
    @ConfigurationProperties(prefix = "spring.druid")
    public DataSource readDataSource() {
        return new DruidDataSource();
    }

    /**
     * @Title: sqlSessionFactory
     * @Description:  根据数据源创建SqlSessionFactory
     * @param @param ds
     * @param @return
     * @param @throws Exception
     * @return SqlSessionFactory
     * @throws
     */
//    @Bean
//    public SqlSessionFactory sqlSessionFactory(DataSource ds) throws Exception{
//        SqlSessionFactoryBean sfb = new SqlSessionFactoryBean();
//        sfb.setDataSource(ds);
//        //下边两句仅仅用于*.xml文件，如果整个持久层操作不需要使用到xml文件的话（只用注解就可以搞定），则不加
//        sfb.setTypeAliasesPackage(env.getProperty("mybatis.type-aliases-package"));
//        sfb.setMapperLocations(new PathMatchingResourcePatternResolver().getResources(env.getProperty("mybatis.mapper-locations")));
//        return sfb.getObject();
//    }

}
