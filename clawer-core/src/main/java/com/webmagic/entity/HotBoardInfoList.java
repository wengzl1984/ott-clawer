package com.webmagic.entity;

import java.util.List;

public class HotBoardInfoList {
    private List<String> moveNameList = null;
    private List<String> moveStarList = null;
    private List<String> releaseTimeList = null;
    private List<String> moveScoreStartList = null;
    private List<String> moveScoreEndList = null;

    public List<String> getMoveNameList() {
        return moveNameList;
    }

    public void setMoveNameList(List<String> moveNameList) {
        this.moveNameList = moveNameList;
    }

    public List<String> getMoveStarList() {
        return moveStarList;
    }

    public void setMoveStarList(List<String> moveStarList) {
        this.moveStarList = moveStarList;
    }

    public List<String> getReleaseTimeList() {
        return releaseTimeList;
    }

    public void setReleaseTimeList(List<String> releaseTimeList) {
        this.releaseTimeList = releaseTimeList;
    }

    public List<String> getMoveScoreStartList() {
        return moveScoreStartList;
    }

    public void setMoveScoreStartList(List<String> moveScoreStartList) {
        this.moveScoreStartList = moveScoreStartList;
    }

    public List<String> getMoveScoreEndList() {
        return moveScoreEndList;
    }

    public void setMoveScoreEndList(List<String> moveScoreEndList) {
        this.moveScoreEndList = moveScoreEndList;
    }
}
