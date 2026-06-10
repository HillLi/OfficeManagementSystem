package com.university.oms.recommend;

import com.university.oms.model.MeetingRoom;

/**
 * A meeting room with multi-dimensional scoring for smart recommendation.
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

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }

    public double getCapacityFit() {
        return capacityFit;
    }

    public void setCapacityFit(double capacityFit) {
        this.capacityFit = capacityFit;
    }

    public double getEquipmentMatch() {
        return equipmentMatch;
    }

    public void setEquipmentMatch(double equipmentMatch) {
        this.equipmentMatch = equipmentMatch;
    }

    public double getUtilizationBalance() {
        return utilizationBalance;
    }

    public void setUtilizationBalance(double utilizationBalance) {
        this.utilizationBalance = utilizationBalance;
    }
}
