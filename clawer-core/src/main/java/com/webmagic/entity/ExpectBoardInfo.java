package com.webmagic.entity;

public class ExpectBoardInfo {
    private String moveName = null;//电影名字
    private String moveStars = null ;//电影主演名字
    private String releaseTime = null;//上映时间
    private String monthToSeeNum = null;//本月新增想看人数
    private String totalToSeeNum = null;//总共想看人数

    public String getMoveName() {
        return moveName;
    }

    public void setMoveName(String moveName) {
        this.moveName = moveName;
    }

    public String getMoveStars() {
        return moveStars;
    }


    public String getReleaseTime() {
        return releaseTime;
    }

    public void setReleaseTime(String releaseTime) {
        this.releaseTime = releaseTime;
    }

    public void setMoveStars(String moveStars) {
        this.moveStars = moveStars;
    }

    public String getMonthToSeeNum() {
        return monthToSeeNum;
    }

    public void setMonthToSeeNum(String monthToSeeNum) {
        this.monthToSeeNum = monthToSeeNum;
    }

    public String getTotalToSeeNum() {
        return totalToSeeNum;
    }

    public void setTotalToSeeNum(String totalToSeeNum) {
        this.totalToSeeNum = totalToSeeNum;
    }
}
