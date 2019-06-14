package com.webmagic.web;

import com.webmagic.dao.VcmClawerTaskDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
@RequestMapping(value = "/test/")
public class TestController {
    @Autowired
    private VcmClawerTaskDao wcm;

    @RequestMapping(value = "todo", method = RequestMethod.GET)
    public void testWcm() {
        wcm.selectByPrimary(1);
    }
}
