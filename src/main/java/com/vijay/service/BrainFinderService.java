package com.vijay.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.*;
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

    // 🧠 Core brains that MUST ALWAYS be included
    private static final List<String> CORE_BRAINS = Arrays.asList(
            "conductorAdvisor",           // Brain 0: Planner
            "toolCallAdvisor",            // Brain 2: Hands
            "selfRefineV3Advisor",        // Brain 13: Judge
            "personalityAdvisor"          // Brain 14: Voice
    );

    private final VectorStore brainVectorStore;

    public BrainFinderService(@Qualifier("brainVectorStore") VectorStore brainVectorStore) {
        this.brainVectorStore = brainVectorStore;
    }

    /**
     * Find the most relevant brains for a given query
     * 
     * IMPORTANT: ALWAYS includes core brains (Planner, Hands, Judge, Voice)
     * Then adds specialist brains found via semantic search
     * 
     * Example:
     * Query: "what is 10 + 20"
     * Returns: [conductorAdvisor, toolCallAdvisor, selfRefineV3Advisor, personalityAdvisor, advancedCapabilities]
     */
    public List<String> findBrainsFor(String query) {
        try {
            // STEP 1: Always include core brains
            List<String> brains = new ArrayList<>(CORE_BRAINS);
            
            // STEP 2: Find specialist brains via semantic search
            SearchRequest request = SearchRequest.builder()
                    .query(query)
                    .topK(4)  // Get top 4 specialist brains
                    .build();

            List<Document> similarDocuments = brainVectorStore.similaritySearch(request);

            List<String> specialistBrains = similarDocuments.stream()
                    .map(doc -> (String) doc.getMetadata().get("brainName"))
                    .collect(Collectors.toList());
            
            // STEP 3: Add specialist brains (avoid duplicates with core brains)
            for (String brain : specialistBrains) {
                if (!brains.contains(brain)) {
                    brains.add(brain);
                }
            }
            
            // STEP 4: Sort by execution order
            brains.sort(this::compareByOrder);

            logger.info("🧠 BrainFinder: Core({}) + Specialist({}) = Total({})", 
                    CORE_BRAINS.size(), 
                    specialistBrains.size(), 
                    brains.size());
            logger.info("   Selected brains: {}", brains);

            return brains;
        } catch (Exception e) {
            logger.error("❌ Error finding brains: {}", e.getMessage());
            // Fallback: return core brains only
            return new ArrayList<>(CORE_BRAINS);
        }
    }
    
    /**
     * Compare brains by their execution order
     */
    private int compareByOrder(String brain1, String brain2) {
        Map<String, Integer> orderMap = new HashMap<>();
        orderMap.put("conductorAdvisor", 0);
        orderMap.put("toolCallAdvisor", 2);
        orderMap.put("personalityAdvisor", 800);
        orderMap.put("selfRefineV3Advisor", 1000);
        
        return Integer.compare(
                orderMap.getOrDefault(brain1, 500),
                orderMap.getOrDefault(brain2, 500)
        );
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
