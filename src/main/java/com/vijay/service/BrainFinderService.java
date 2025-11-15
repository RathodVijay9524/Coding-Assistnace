package com.vijay.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 🧠 PHASE 8: Brain RAG - Brain Finder Service
 * 
 * Semantic search for brains, just like ToolFinderService for tools.
 * 
 * Instead of running all 13 brains for every query, we:
 * 1. Embed the user's query
 * 2. Search the brainVectorStore for Top 3-4 similar brains
 * 3. Return only the relevant brains
 * 
 * This saves ~98% of tokens and prevents HTTP 413 errors.
 */
@Service
public class BrainFinderService {

    private static final Logger logger = LoggerFactory.getLogger(BrainFinderService.class);

    private final VectorStore brainVectorStore;

    public BrainFinderService(@Qualifier("brainVectorStore") VectorStore brainVectorStore) {
        this.brainVectorStore = brainVectorStore;
    }

    /**
     * Find the Top 3-4 most relevant brains for a given query
     * 
     * Example:
     * Query: "what is the weather in pune?"
     * Returns: ["QueryPlanner", "ResponseSummarizer", "Judge"]
     * 
     * Example:
     * Query: "explain the add() method in AIAgentToolService"
     * Returns: ["QueryPlanner", "CodeRetriever", "ResponseSummarizer", "Judge"]
     */
    public List<String> findBrainsFor(String query) {
        try {
            SearchRequest request = SearchRequest.builder()
                    .query(query)
                    .topK(4)  // Get top 4 brains
                    .build();

            List<Document> similarDocuments = brainVectorStore.similaritySearch(request);

            List<String> brainNames = similarDocuments.stream()
                    .map(doc -> (String) doc.getMetadata().get("brainName"))
                    .collect(Collectors.toList());

            logger.info("🧠 BrainFinder: Found {} brains for query: '{}'", 
                    brainNames.size(), 
                    query.substring(0, Math.min(50, query.length())));
            logger.info("   Selected brains: {}", brainNames);

            return brainNames;
        } catch (Exception e) {
            logger.error("❌ Error finding brains: {}", e.getMessage());
            // Fallback: return default brains (Planner + Judge)
            return List.of("LocalQueryPlannerAdvisor", "MultiCriteriaJudgeAdvisor");
        }
    }

    /**
     * Get all brains (for debugging/monitoring)
     */
    public List<String> getAllBrains() {
        try {
            SearchRequest request = SearchRequest.builder()
                    .query("*")  // Match all
                    .topK(100)
                    .build();

            List<Document> allDocuments = brainVectorStore.similaritySearch(request);

            return allDocuments.stream()
                    .map(doc -> (String) doc.getMetadata().get("brainName"))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("❌ Error getting all brains: {}", e.getMessage());
            return List.of();
        }
    }
}
