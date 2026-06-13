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
 * 会议室智能推荐服务，基于多维加权评分进行推荐
 * 评分维度：容量匹配度（高斯分布）、设备匹配度（分词重叠率）、利用率均衡度
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
     * 基于多维加权评分推荐会议室
     *
     * @param request 推荐请求（含预期人数、设备需求、时间段）
     * @return 按评分降序排列的会议室列表
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
            // 过滤掉有时间段冲突的会议室
            if (request.getStartTime() != null && hasConflict(room.getId(), request.getStartTime(),
                    request.getEndTime(), allMeetings)) {
                continue;
            }

            double capFit = computeCapacityFit(room.getCapacity(), expected);
            double equipMatch = computeEquipmentMatch(room.getEquipment(), request.getEquipment());
            double utilBalance = computeUtilizationBalance(room.getId(), allMeetings);

            // 三维加权总分
            double totalScore = WEIGHT_CAPACITY * capFit
                    + WEIGHT_EQUIPMENT * equipMatch
                    + WEIGHT_UTILIZATION * utilBalance;

            scoredRooms.add(new ScoredRoom(room, totalScore, capFit, equipMatch, utilBalance));
        }

        // 按总分降序排列
        scoredRooms.sort(new Comparator<ScoredRoom>() {
            @Override
            public int compare(ScoredRoom a, ScoredRoom b) {
                return Double.compare(b.getScore(), a.getScore());
            }
        });
        return scoredRooms;
    }

    /**
     * 高斯容量匹配度：exp(-((capacity - expected)^2) / (2 * sigma^2))
     * sigma = expected * 0.3，容量恰好等于预期人数时得满分
     */
    double computeCapacityFit(int capacity, int expected) {
        if (expected <= 0) {
            expected = 1;
        }
        if (capacity < expected) {
            return 0.0; // 容量不足，完全不匹配
        }
        double sigma = expected * 0.3;
        if (sigma < 1.0) {
            sigma = 1.0;
        }
        double diff = capacity - expected;
        return Math.exp(-(diff * diff) / (2.0 * sigma * sigma));
    }

    /**
     * 设备匹配度：基于分词结果的重叠率
     */
    double computeEquipmentMatch(String roomEquipment, String requiredEquipment) {
        if (requiredEquipment == null || requiredEquipment.trim().isEmpty()) {
            return 1.0; // 无特殊设备需求，所有会议室都匹配
        }
        if (roomEquipment == null || roomEquipment.trim().isEmpty()) {
            return 0.0;
        }
        List<String> roomTokens = NlpUtils.tokenize(roomEquipment);
        List<String> requiredTokens = NlpUtils.tokenize(requiredEquipment);
        if (requiredTokens.isEmpty()) {
            // 分词失败时回退到简单包含检查
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
     * 利用率均衡度：1 - (该会议室已占用会议数 / 总活跃会议数)
     * 对过度使用的会议室施加惩罚，促进使用均衡
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

    /** 检查会议室在指定时段是否有时间冲突 */
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
