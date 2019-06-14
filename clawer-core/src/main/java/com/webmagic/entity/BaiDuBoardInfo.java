package com.webmagic.entity;

/**
 * @Auther: ljs
 * @Date: 2019/6/5 20:13
 * @Description: 百度榜单对象实体
 */
public class BaiDuBoardInfo {
    private String videoName = null;//媒资名称
    private int  videoRate = -1;//指数
    private int    videoRanki = -1;//排名

    public String getVideoName() {
        return videoName;
    }

    public void setVideoName(String videoName) {
        this.videoName = videoName;
    }

    public int getVideoRate() {
        return videoRate;
    }

    public void setVideoRate(int videoRate) {
        this.videoRate = videoRate;
    }

    public int getVideoRanki() {
        return videoRanki;
    }

    public void setVideoRanki(int videoRanki) {
        this.videoRanki = videoRanki;
    }
}
