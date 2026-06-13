package com.university.oms.nlp;

import com.university.oms.model.Document;
import com.university.oms.repository.OmsRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 文档相似度检测服务，基于TF-IDF和余弦相似度查找最相似的文档
 */
@Service
public class DocumentSimilarityService {

    private final OmsRepository repo;
    private volatile List<String> corpusCache;
    private volatile Map<Long, Map<String, Double>> vectorCache;

    public DocumentSimilarityService(OmsRepository repo) {
        this.repo = repo;
    }

    /**
     * 查找与指定内容最相似的文档
     * 使用TF-IDF向量化后计算余弦相似度
     *
     * @param content   待比较的内容
     * @param excludeId 需要排除的文档ID
     * @return 相似度结果（包含分数和文档标题），无结果时返回null
     */
    public SimilarityResult findMostSimilar(String content, Long excludeId) {
        ensureCache();
        if (corpusCache == null || corpusCache.isEmpty()) {
            return null;
        }
        Map<String, Double> queryVector = NlpUtils.computeTfIdf(content, corpusCache);
        double maxScore = 0.0;
        Document bestMatch = null;
        List<Document> allDocs = repo.findAllDocuments();
        for (Document doc : allDocs) {
            if (doc.getId().equals(excludeId)) {
                continue;
            }
            Map<String, Double> docVector = vectorCache.get(doc.getId());
            if (docVector == null) {
                docVector = NlpUtils.computeTfIdf(
                        doc.getContent() != null ? doc.getContent() : "", corpusCache);
                vectorCache.put(doc.getId(), docVector);
            }
            double score = NlpUtils.cosineSimilarity(queryVector, docVector);
            if (score > maxScore) {
                maxScore = score;
                bestMatch = doc;
            }
        }
        if (bestMatch == null) {
            return null;
        }
        return new SimilarityResult(maxScore, bestMatch.getTitle(), bestMatch.getId());
    }

    /** 使缓存失效（文档变更时调用） */
    public void invalidateCache() {
        corpusCache = null;
        vectorCache = null;
    }

    /** 双重检查锁保证线程安全地初始化语料库缓存和TF-IDF向量缓存 */
    private void ensureCache() {
        if (corpusCache == null) {
            synchronized (this) {
                if (corpusCache == null) {
                    List<Document> docs = repo.findAllDocuments();
                    List<String> corpus = new ArrayList<String>();
                    Map<Long, Map<String, Double>> vectors = new HashMap<Long, Map<String, Double>>();
                    for (Document doc : docs) {
                        String content = doc.getContent() != null ? doc.getContent() : "";
                        corpus.add(content);
                        vectors.put(doc.getId(), NlpUtils.computeTfIdf(content, corpus));
                    }
                    corpusCache = corpus;
                    vectorCache = vectors;
                }
            }
        }
    }

    /** 相似度检测结果 */
    public static class SimilarityResult {
        private final double score;
        private final String title;
        private final Long documentId;

        public SimilarityResult(double score, String title, Long documentId) {
            this.score = score;
            this.title = title;
            this.documentId = documentId;
        }

        public double getScore() {
            return score;
        }

        public String getTitle() {
            return title;
        }

        public Long getDocumentId() {
            return documentId;
        }
    }
}
