package com.spider.extension.constant;

public class ConstConfig {
    //榜单对应类型
    public static final String RANKING_SEL_TYPES = new String("1,2,3");
    //媒资对应类型
    public static final String CLAWER_SEL_TYPES = new String("4,5");
    //媒资对应类型细分,=4豆瓣，=5中国网
    public static final String CLAWER_SEL_DOUBAN_TYPE = new String("4");

    public static final String CLAWER_SEL_CBOOO_TYPE = new String("5");
    //日志表状态(其他表状态字段也通用)，=0等待处理，=1处理完成，=2处理失败
    public static final String LOG_STATUS_WAIT_DEAL = new String("0");

    public static final String LOG_STATUS_DEAL_SUCESS = new String("1");

    public static final String LOG_STATUS_DEAL_FAIL = new String("2");

    //错误类型（通用），=1信息缺失，=2媒资与日志中的名称无法匹配
    public static final String LOG_ERROR_TYPE_INFO_LOST = new String("1");

    public static final String LOG_ERROR_TYPE_NAME_NO_MATCH = new String("2");

}
