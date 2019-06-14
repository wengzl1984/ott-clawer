package com.webmagic.entity;

import java.util.List;

public class ExpectBoardInfoList {
    private List<String> moveNameList = null;//电影名字
    private List<String> moveStarsList = null ;//电影主演名字
    private List<String> releaseTimeList = null;//上映时间
    private List<String> monthToSeeNumList = null;//本月新增想看人数
    private List<String> totalToSeeNumList = null;//总共想看人数

    public List<String> getMoveNameList() {
        return moveNameList;
    }

    public void setMoveNameList(List<String> moveNameList) {
        this.moveNameList = moveNameList;
    }

    public List<String> getMoveStarsList() {
        return moveStarsList;
    }

    public void setMoveStarsList(List<String> moveStarsList) {
        this.moveStarsList = moveStarsList;
    }

    public List<String> getReleaseTimeList() {
        return releaseTimeList;
    }

    public void setReleaseTimeList(List<String> releaseTimeList) {
        this.releaseTimeList = releaseTimeList;
    }

    public List<String> getMonthToSeeNumList() {
        return monthToSeeNumList;
    }

    public void setMonthToSeeNumList(List<String> monthToSeeNumList) {
        this.monthToSeeNumList = monthToSeeNumList;
    }

    public List<String> getTotalToSeeNumList() {
        return totalToSeeNumList;
    }

    public void setTotalToSeeNumList(List<String> totalToSeeNumList) {
        this.totalToSeeNumList = totalToSeeNumList;
    }
}
