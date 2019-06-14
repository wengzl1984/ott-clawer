package com.webmagic.dao;



import com.webmagic.entity.HotBoardInfo;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;


/**
 * @author ljs
 * @version V1.0
 * @Title: ${file_name}
 * @Package com.webmagic.dao
 * @Description: TODO
 * @date 2019/5/25 13:48
 */
@Repository
public interface HotBoardDao {

    int insert(HotBoardInfo hotBoardInfo);

}
