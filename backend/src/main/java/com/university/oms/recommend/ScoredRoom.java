package com.university.oms.recommend;

import com.university.oms.model.MeetingRoom;

/**
 * 带评分的会议室推荐结果，包含总分和各维度评分
 */
public class ScoredRoom {
    private MeetingRoom room;
    private double score;
    private double capacityFit;
    private double equipmentMatch;
    private double utilizationBalance;

    public ScoredRoom() {
    }

    public ScoredRoom(MeetingRoom room, double score, double capacityFit,
                      double equipmentMatch, double utilizationBalance) {
        this.room = room;
        this.score = score;
        this.capacityFit = capacityFit;
        this.equipmentMatch = equipmentMatch;
        this.utilizationBalance = utilizationBalance;
    }

    public MeetingRoom getRoom() {
        return room;
    }

    public void setRoom(MeetingRoom room) {
        this.room = room;
    }

    /** 推荐总分（加权） */
    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }

    /** 容量匹配度评分 */
    public double getCapacityFit() {
        return capacityFit;
    }

    public void setCapacityFit(double capacityFit) {
        this.capacityFit = capacityFit;
    }

    /** 设备匹配度评分 */
    public double getEquipmentMatch() {
        return equipmentMatch;
    }

    public void setEquipmentMatch(double equipmentMatch) {
        this.equipmentMatch = equipmentMatch;
    }

    /** 利用率均衡度评分 */
    public double getUtilizationBalance() {
        return utilizationBalance;
    }

    public void setUtilizationBalance(double utilizationBalance) {
        this.utilizationBalance = utilizationBalance;
    }
}
