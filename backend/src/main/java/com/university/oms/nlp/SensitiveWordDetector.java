package com.university.oms.nlp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Detects sensitive words in document text using an in-memory dictionary.
 * Categories: secrecy-related, political, financial, personal-privacy.
 */
public class SensitiveWordDetector {

    private static final Map<String, List<String>> SENSITIVE_WORDS = new LinkedHashMap<String, List<String>>();

    static {
        SENSITIVE_WORDS.put("涉密", Arrays.asList(
                "国家秘密", "秘密级", "机密级", "绝密级", "涉密文件", "涉密人员", "涉密项目",
                "保密协议", "泄露秘密", "保密审查", "密级", "内部机密"
        ));
        SENSITIVE_WORDS.put("政治", Arrays.asList(
                "政治敏感", "意识形态", "反动", "颠覆", "分裂", "极端势力"
        ));
        SENSITIVE_WORDS.put("财务", Arrays.asList(
                "财务机密", "商业秘密", "核心技术机密", "招标底价", "内部审计结果",
                "未公开财报", "薪酬明细", "资金流向"
        ));
        SENSITIVE_WORDS.put("隐私", Arrays.asList(
                "身份证号", "银行卡号", "个人隐私", "家庭住址", "电话号码",
                "健康状况", "犯罪记录", "征信信息"
        ));
    }

    /**
     * Detect sensitive words in the given text.
     *
     * @return list of matches, each entry is "word(category)"
     */
    public static List<String> detect(String text) {
        List<String> matches = new ArrayList<String>();
        if (text == null || text.trim().isEmpty()) {
            return matches;
        }
        Set<String> seen = new LinkedHashSet<String>();
        for (Map.Entry<String, List<String>> entry : SENSITIVE_WORDS.entrySet()) {
            String category = entry.getKey();
            for (String word : entry.getValue()) {
                if (text.contains(word) && seen.add(word)) {
                    matches.add(word + "(" + category + ")");
                }
            }
        }
        return matches;
    }

    /**
     * Get all categories of detected sensitive words.
     */
    public static Set<String> detectCategories(String text) {
        Set<String> categories = new LinkedHashSet<String>();
        if (text == null || text.trim().isEmpty()) {
            return categories;
        }
        for (Map.Entry<String, List<String>> entry : SENSITIVE_WORDS.entrySet()) {
            for (String word : entry.getValue()) {
                if (text.contains(word)) {
                    categories.add(entry.getKey());
                    break;
                }
            }
        }
        return categories;
    }
}
