package com.webmagic.entity;

import java.util.Date;

/**
 *  @Auther: ljs
 *  @Date: 2019/6/4 20:13
 *  @Description: 猫眼榜单对象实体
 */
public class HotBoardInfo {
    private int videoRanki = -1;//排名
    private String videoName  = null;//媒资名字
    private String videoCast = null ;//主演名字
    private Date videoRelease = null;//上映时间
    private double videoRate = -1;//评分

    public int getVideoRanki() {
        return videoRanki;
    }

    public void setVideoRanki(int videoRanki) {
        this.videoRanki = videoRanki;
    }

    public String getVideoName() {
        return videoName;
    }

    public void setVideoName(String videoName) {
        this.videoName = videoName;
    }

    public String getVideoCast() {
        return videoCast;
    }

    public void setVideoCast(String videoCast) {
        this.videoCast = videoCast;
    }

    public Date getVideoRelease() {
        return videoRelease;
    }

    public void setVideoRelease(Date videoRelease) {
        this.videoRelease = videoRelease;
    }

    public double getVideoRate() {
        return videoRate;
    }

    public void setVideoRate(double videoRate) {
        this.videoRate = videoRate;
    }
}
