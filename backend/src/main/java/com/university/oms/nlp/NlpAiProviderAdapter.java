package com.university.oms.nlp;

import com.university.oms.design.AiProviderAdapter;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

/**
 * NLP-enhanced AI draft provider.
 * Uses HanLP TextRank to extract core keywords from keyPoints,
 * auto-infers paragraph structure, and generates richer template content.
 */
@Component
@Primary
public class NlpAiProviderAdapter implements AiProviderAdapter {

    private static final int KEYWORD_TOP_N = 8;

    @Override
    public String draft(String docType, String topic, String keyPoints) {
        String points = keyPoints == null || keyPoints.trim().isEmpty()
                ? "请结合学校实际推进落实。" : keyPoints;

        // 1. Extract core keywords from keyPoints using TextRank
        List<String> keywords = NlpUtils.extractKeywords(topic + " " + points, KEYWORD_TOP_N);
        Set<String> coreKeywords = new LinkedHashSet<String>(keywords);

        // 2. Tokenize keyPoints for paragraph structure inference
        List<String> tokens = NlpUtils.tokenize(points);
        Set<String> keyPhrases = new LinkedHashSet<String>();
        for (String token : tokens) {
            if (coreKeywords.contains(token) && keyPhrases.size() < 5) {
                keyPhrases.add(token);
            }
        }

        // 3. Generate enriched template content
        StringBuilder sb = new StringBuilder();
        sb.append("关于").append(topic).append("的").append(docType).append("\n\n");
        sb.append("各单位：\n");
        sb.append("为规范推进").append(topic).append("相关工作，根据学校办公管理要求，现将有关事项").append(docType).append("如下：\n");

        // Section 1: Background & objectives
        sb.append("\n一、背景与目标\n");
        sb.append("根据上级精神和学校工作部署，围绕").append(topic).append("，制定本").append(docType).append("。");
        if (!keyPhrases.isEmpty()) {
            sb.append("重点工作包括：").append(String.join("、", keyPhrases)).append("等方面。");
        }
        sb.append("\n");

        // Section 2: Main content using keyPoints
        sb.append("\n二、工作内容\n");
        sb.append(points).append("\n");

        // Section 3: Implementation requirements inferred from keywords
        sb.append("\n三、工作要求\n");
        if (coreKeywords.size() >= 2) {
            String[] kwArray = coreKeywords.toArray(new String[0]);
            for (int i = 0; i < Math.min(3, kwArray.length); i++) {
                sb.append((char) ('１' + i)).append("．关于").append(kwArray[i])
                        .append("：请各单位高度重视，明确责任分工，确保").append(kwArray[i]).append("工作落到实处。\n");
            }
        } else {
            sb.append("请各单位结合实际认真落实，按时反馈办理情况。\n");
        }

        // Section 4: Schedule & feedback
        sb.append("\n四、时间安排与反馈\n");
        sb.append("请各单位于本学期末前将落实情况报送至学校办公室，联系人及方式另行通知。\n");

        // Keyword info footer
        if (!coreKeywords.isEmpty()) {
            sb.append("\n【智能提取关键词】").append(String.join("、", coreKeywords)).append("\n");
        }

        sb.append("\n北京大学\n").append(LocalDate.now());
        return sb.toString();
    }
}
