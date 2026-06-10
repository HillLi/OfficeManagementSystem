package com.university.oms.nlp;

import com.hankcs.hanlp.HanLP;
import com.hankcs.hanlp.seg.common.Term;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Chinese NLP utilities based on HanLP: tokenization, keyword extraction, TF-IDF, cosine similarity.
 */
public class NlpUtils {

    private static final Set<String> STOP_WORDS = new HashSet<String>(Arrays.asList(
            "的", "了", "在", "是", "我", "有", "和", "就", "不", "人", "都", "一", "一个",
            "上", "也", "很", "到", "说", "要", "去", "你", "会", "着", "没有", "看", "好",
            "自己", "这", "他", "她", "它", "们", "那", "些", "么", "什么", "为", "与", "及",
            "等", "将", "可", "以", "对", "被", "而", "从", "但", "把", "让", "向", "比",
            "所", "其", "如", "此", "之", "后", "前", "则", "中", "能", "该", "还", "已",
            "于", "或", "若", "用", "因", "更", "最", "并", "当", "下", "各", "出", "来"
    ));

    private NlpUtils() {
    }

    /**
     * Chinese word segmentation, filtering stop words and non-noun/verb terms.
     */
    public static List<String> tokenize(String text) {
        if (text == null || text.trim().isEmpty()) {
            return new ArrayList<String>();
        }
        List<Term> terms = HanLP.segment(text);
        List<String> result = new ArrayList<String>();
        for (Term term : terms) {
            String word = term.word.trim();
            String nature = term.nature != null ? term.nature.toString() : "";
            if (word.length() < 2) {
                continue;
            }
            if (STOP_WORDS.contains(word)) {
                continue;
            }
            if (nature.startsWith("n") || nature.startsWith("v") || nature.startsWith("a")) {
                result.add(word);
            }
        }
        return result;
    }

    /**
     * Extract top-N keywords using TextRank algorithm.
     */
    public static List<String> extractKeywords(String text, int topN) {
        if (text == null || text.trim().isEmpty()) {
            return new ArrayList<String>();
        }
        return HanLP.extractKeyword(text, topN);
    }

    /**
     * Compute TF-IDF vector for a given text against a corpus.
     */
    public static Map<String, Double> computeTfIdf(String text, List<String> corpus) {
        List<String> tokens = tokenize(text);
        if (tokens.isEmpty()) {
            return new HashMap<String, Double>();
        }
        Map<String, Integer> tf = new HashMap<String, Integer>();
        for (String token : tokens) {
            tf.put(token, tf.getOrDefault(token, 0) + 1);
        }
        int totalTokens = tokens.size();
        Map<String, Double> tfIdf = new HashMap<String, Double>();
        for (Map.Entry<String, Integer> entry : tf.entrySet()) {
            String word = entry.getKey();
            double tfValue = (double) entry.getValue() / totalTokens;
            int docCount = 0;
            for (String doc : corpus) {
                if (doc.contains(word)) {
                    docCount++;
                }
            }
            double idfValue = Math.log((double) (corpus.size() + 1) / (docCount + 1)) + 1.0;
            tfIdf.put(word, tfValue * idfValue);
        }
        return tfIdf;
    }

    /**
     * Cosine similarity between two TF-IDF vectors.
     */
    public static double cosineSimilarity(Map<String, Double> v1, Map<String, Double> v2) {
        if (v1.isEmpty() || v2.isEmpty()) {
            return 0.0;
        }
        Set<String> allKeys = new HashSet<String>(v1.keySet());
        allKeys.addAll(v2.keySet());
        double dotProduct = 0.0;
        double norm1 = 0.0;
        double norm2 = 0.0;
        for (String key : allKeys) {
            double val1 = v1.getOrDefault(key, 0.0);
            double val2 = v2.getOrDefault(key, 0.0);
            dotProduct += val1 * val2;
            norm1 += val1 * val1;
            norm2 += val2 * val2;
        }
        if (norm1 == 0.0 || norm2 == 0.0) {
            return 0.0;
        }
        return dotProduct / (Math.sqrt(norm1) * Math.sqrt(norm2));
    }
}
