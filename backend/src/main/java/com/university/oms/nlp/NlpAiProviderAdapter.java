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
 * NLP增强的AI公文起草适配器
 * 使用HanLP TextRank提取核心关键词，自动推断段落结构，生成更丰富的模板内容
 */
@Component
@Primary
public class NlpAiProviderAdapter implements AiProviderAdapter {

    private static final int KEYWORD_TOP_N = 8;

    @Override
    public String draft(String docType, String topic, String keyPoints) {
        String points = keyPoints == null || keyPoints.trim().isEmpty()
                ? "请结合学校实际推进落实。" : keyPoints;

        // 1. 使用TextRank从要点中提取核心关键词
        List<String> keywords = NlpUtils.extractKeywords(topic + " " + points, KEYWORD_TOP_N);
        Set<String> coreKeywords = new LinkedHashSet<String>(keywords);

        // 2. 分词后推断段落结构的关键短语
        List<String> tokens = NlpUtils.tokenize(points);
        Set<String> keyPhrases = new LinkedHashSet<String>();
        for (String token : tokens) {
            if (coreKeywords.contains(token) && keyPhrases.size() < 5) {
                keyPhrases.add(token);
            }
        }

        // 3. 生成增强的模板内容
        StringBuilder sb = new StringBuilder();
        sb.append("关于").append(topic).append("的").append(docType).append("\n\n");
        sb.append("各单位：\n");
        sb.append("为规范推进").append(topic).append("相关工作，根据学校办公管理要求，现将有关事项").append(docType).append("如下：\n");

        // 第一部分：背景与目标
        sb.append("\n一、背景与目标\n");
        sb.append("根据上级精神和学校工作部署，围绕").append(topic).append("，制定本").append(docType).append("。");
        if (!keyPhrases.isEmpty()) {
            sb.append("重点工作包括：").append(String.join("、", keyPhrases)).append("等方面。");
        }
        sb.append("\n");

        // 第二部分：工作内容（使用用户提供的要点）
        sb.append("\n二、工作内容\n");
        sb.append(points).append("\n");

        // 第三部分：根据关键词自动推断工作要求
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

        // 第四部分：时间安排与反馈
        sb.append("\n四、时间安排与反馈\n");
        sb.append("请各单位于本学期末前将落实情况报送至学校办公室，联系人及方式另行通知。\n");

        // 关键词信息脚注
        if (!coreKeywords.isEmpty()) {
            sb.append("\n【智能提取关键词】").append(String.join("、", coreKeywords)).append("\n");
        }

        sb.append("\n北京大学\n").append(LocalDate.now());
        return sb.toString();
    }
}
