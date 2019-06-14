package com.webmagic.service;

import com.webmagic.dao.HotBoardDao;
import com.webmagic.entity.HotBoardInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class HotHoardService {
    @Autowired
    private HotBoardDao hotBoardDao;
    @Transactional
    public int insertHotBoard(HotBoardInfo hotBoardInfo) {
        return hotBoardDao.insert(hotBoardInfo);
    }
}
