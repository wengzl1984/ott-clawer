package com.webmagic.util;

import org.apache.log4j.Logger;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class CronDateUtils {
    private static Logger log = Logger.getLogger(CronDateUtils.class);

    /**
     * Description:
     * @param: [period, catchTime]
     * @return: java.lang.String
     * @auther: ljs
     * @date: 2019/6/1 15:24
     */

    public static String getCron(int period, String catchTime) {

        String hour = null;//小时
        String min = null;//分钟
        String ret = null;
        Date date = new Date();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");


        if (catchTime != null) {
            if (catchTime.indexOf(":") != -1) {
                String catchTimeArr[] = catchTime.split(":");
                if (catchTimeArr.length == 2) {
                    hour = catchTimeArr[0];
                    min = catchTimeArr[1];
                    //0 0 12 1/10 * ? *  从1号开始每隔10天，12点开始执行
                    if ("00".equals(min)) {
                        ret = "0 0";
                    } else {
                        if (min.startsWith("0")) {
                            ret = "0 " + min.substring(1);
                        } else {
                            ret = "0 " + min;
                        }
                    }
                    if ("00".equals(hour)) {
                        ret = ret + " 0";
                    } else {
                        if (hour.startsWith("0")) {
                            ret = ret + " " + hour.substring(1);
                        } else {
                            ret = ret + " " + hour;
                        }
                    }
                    if (period == 0) {
                        ret = ret + " *" + " * ? ";
                    } else {
                        Calendar cal = Calendar.getInstance();
                        cal.add(Calendar.DATE, 30);
                        date = cal.getTime();
                        log.info("任务下次执行时间：" + format.format(date) + " " + catchTime);
                        ret = ret + " " + date.getDate() + " * ?";

                    }

                }
            }
        }
        return ret;
    }

    public static void main(String[] args) {
        String ret = null;
        ret = getCron(29, "21:45");
        System.out.println(ret);
    }
}
