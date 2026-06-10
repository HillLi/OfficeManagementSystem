package com.university.oms.nlp;

import com.university.oms.model.Document;
import com.university.oms.repository.OmsRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Finds the most similar document using TF-IDF and cosine similarity.
 * Caches TF-IDF vectors to avoid repeated computation.
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
     * Find the most similar document to the given content.
     *
     * @param content   the content to compare
     * @param excludeId document ID to exclude from comparison
     * @return similarity result with score and title, or null if no documents found
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

    /**
     * Invalidate the cache (call when documents change).
     */
    public void invalidateCache() {
        corpusCache = null;
        vectorCache = null;
    }

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
