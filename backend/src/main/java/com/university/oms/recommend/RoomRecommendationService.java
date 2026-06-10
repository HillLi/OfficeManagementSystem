package com.university.oms.recommend;

import com.university.oms.dto.RecommendRoomRequest;
import com.university.oms.model.Meeting;
import com.university.oms.model.MeetingRoom;
import com.university.oms.nlp.NlpUtils;
import com.university.oms.repository.OmsRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Multi-dimensional weighted scoring for smart meeting room recommendation.
 * Dimensions: capacity fit (Gaussian), equipment match (token overlap), utilization balance.
 */
@Service
public class RoomRecommendationService {

    private static final double WEIGHT_CAPACITY = 0.4;
    private static final double WEIGHT_EQUIPMENT = 0.3;
    private static final double WEIGHT_UTILIZATION = 0.3;

    private final OmsRepository repo;

    public RoomRecommendationService(OmsRepository repo) {
        this.repo = repo;
    }

    /**
     * Recommend meeting rooms using multi-dimensional weighted scoring.
     *
     * @param request recommendation request with expected count, equipment, time range
     * @return sorted list of scored rooms (highest score first)
     */
    public List<ScoredRoom> recommendEnhanced(RecommendRoomRequest request) {
        int expected = request.getExpectedCount() != null ? request.getExpectedCount() : 1;
        List<MeetingRoom> allRooms = repo.findAllRooms();
        List<Meeting> allMeetings = repo.findAllMeetings();
        List<ScoredRoom> scoredRooms = new ArrayList<ScoredRoom>();

        for (MeetingRoom room : allRooms) {
            if (!room.isEnabled()) {
                continue;
            }
            // Filter out rooms with time conflicts
            if (request.getStartTime() != null && hasConflict(room.getId(), request.getStartTime(),
                    request.getEndTime(), allMeetings)) {
                continue;
            }

            double capFit = computeCapacityFit(room.getCapacity(), expected);
            double equipMatch = computeEquipmentMatch(room.getEquipment(), request.getEquipment());
            double utilBalance = computeUtilizationBalance(room.getId(), allMeetings);

            double totalScore = WEIGHT_CAPACITY * capFit
                    + WEIGHT_EQUIPMENT * equipMatch
                    + WEIGHT_UTILIZATION * utilBalance;

            scoredRooms.add(new ScoredRoom(room, totalScore, capFit, equipMatch, utilBalance));
        }

        scoredRooms.sort(new Comparator<ScoredRoom>() {
            @Override
            public int compare(ScoredRoom a, ScoredRoom b) {
                return Double.compare(b.getScore(), a.getScore());
            }
        });
        return scoredRooms;
    }

    /**
     * Gaussian capacity fitting: exp(-((capacity - expected)^2) / (2 * sigma^2)).
     * sigma = expected * 0.3. Perfect fit when capacity == expected.
     */
    double computeCapacityFit(int capacity, int expected) {
        if (expected <= 0) {
            expected = 1;
        }
        if (capacity < expected) {
            return 0.0; // room too small, completely unfit
        }
        double sigma = expected * 0.3;
        if (sigma < 1.0) {
            sigma = 1.0;
        }
        double diff = capacity - expected;
        return Math.exp(-(diff * diff) / (2.0 * sigma * sigma));
    }

    /**
     * Equipment match score using tokenized overlap ratio.
     */
    double computeEquipmentMatch(String roomEquipment, String requiredEquipment) {
        if (requiredEquipment == null || requiredEquipment.trim().isEmpty()) {
            return 1.0; // no specific requirement, all rooms match
        }
        if (roomEquipment == null || roomEquipment.trim().isEmpty()) {
            return 0.0;
        }
        List<String> roomTokens = NlpUtils.tokenize(roomEquipment);
        List<String> requiredTokens = NlpUtils.tokenize(requiredEquipment);
        if (requiredTokens.isEmpty()) {
            // Fallback to simple contains check
            return roomEquipment.contains(requiredEquipment) ? 1.0 : 0.0;
        }
        Set<String> roomSet = new HashSet<String>(roomTokens);
        int matched = 0;
        for (String req : requiredTokens) {
            if (roomSet.contains(req)) {
                matched++;
            }
        }
        return (double) matched / requiredTokens.size();
    }

    /**
     * Utilization balance: 1 - (occupied slots / total available slots).
     * Penalizes over-booked rooms to distribute usage evenly.
     */
    double computeUtilizationBalance(Long roomId, List<Meeting> allMeetings) {
        long occupiedCount = 0;
        long totalActive = 0;
        for (Meeting meeting : allMeetings) {
            if ("rejected".equals(meeting.getStatus())) {
                continue;
            }
            totalActive++;
            if (meeting.getRoomId().equals(roomId)) {
                occupiedCount++;
            }
        }
        if (totalActive == 0) {
            return 1.0;
        }
        return 1.0 - ((double) occupiedCount / totalActive);
    }

    private boolean hasConflict(Long roomId, LocalDateTime start, LocalDateTime end,
                                List<Meeting> allMeetings) {
        if (start == null || end == null) {
            return false;
        }
        for (Meeting meeting : allMeetings) {
            if (meeting.getRoomId().equals(roomId)
                    && !"rejected".equals(meeting.getStatus())
                    && start.isBefore(meeting.getEndTime())
                    && end.isAfter(meeting.getStartTime())) {
                return true;
            }
        }
        return false;
    }
}
