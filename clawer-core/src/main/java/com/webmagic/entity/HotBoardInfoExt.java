package com.webmagic.entity;

import java.util.Date;
import java.util.List;

/**
 *  @Auther: ljs
 *  @Date: 2019/6/4 20:13
 *  @Description: 猫眼榜单对象实体扩展类
 */
public class HotBoardInfoExt {
    private List<Integer> videoRankiList = null;//排名
    private List<String> videoNameList  = null;//媒资名字
    private List<String> videoCastList = null ;//主演名字
    private List<Date> videoReleaseList = null;//上映时间
    private List<Double> videoRateList = null;//评分

    public List<Integer> getVideoRankiList() {
        return videoRankiList;
    }

    public void setVideoRankiList(List<Integer> videoRankiList) {
        this.videoRankiList = videoRankiList;
    }

    public List<String> getVideoNameList() {
        return videoNameList;
    }

    public void setVideoNameList(List<String> videoNameList) {
        this.videoNameList = videoNameList;
    }

    public List<String> getVideoCastList() {
        return videoCastList;
    }

    public void setVideoCastList(List<String> videoCastList) {
        this.videoCastList = videoCastList;
    }

    public List<Date> getVideoReleaseList() {
        return videoReleaseList;
    }

    public void setVideoReleaseList(List<Date> videoReleaseList) {
        this.videoReleaseList = videoReleaseList;
    }

    public List<Double> getVideoRateList() {
        return videoRateList;
    }

    public void setVideoRateList(List<Double> videoRateList) {
        this.videoRateList = videoRateList;
    }
}
