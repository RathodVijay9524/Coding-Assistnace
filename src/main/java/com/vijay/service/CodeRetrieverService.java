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

@Service
public class CodeRetrieverService {

    private static final Logger logger = LoggerFactory.getLogger(CodeRetrieverService.class);
    
    private final VectorStore summaryStore;
    private final VectorStore chunkStore;
    private final DependencyGraphBuilder dependencyGraph;

    public CodeRetrieverService(@Qualifier("summaryVectorStore") VectorStore summaryStore,
                               @Qualifier("chunkVectorStore") VectorStore chunkStore,
                               DependencyGraphBuilder dependencyGraph) {
        this.summaryStore = summaryStore;
        this.chunkStore = chunkStore;
        this.dependencyGraph = dependencyGraph;
    }

    public CodeContext retrieveCodeContext(String query) {
        logger.info("🔍 Brain 1 (Code Retriever): Searching for code context - '{}'", query);
        
        CodeContext context = new CodeContext();
        
        try {
            // Step 1: Find relevant files using summaries
            List<Document> fileSummaries = summaryStore.similaritySearch(
                SearchRequest.builder()
                    .query(query)
                    .topK(3)
                    .build()
            );
            
            logger.info("📁 Found {} relevant files from summaries", fileSummaries.size());
            
            if (fileSummaries.isEmpty()) {
                logger.info("⚠️ No relevant files found for query: {}", query);
                return context;
            }
            
            // Step 2: Expand using dependency graph
            Set<String> allRelevantFiles = expandWithDependencies(fileSummaries, 2);
            logger.info("🔗 Expanded to {} files using dependency graph", allRelevantFiles.size());
            
            // Step 3: Get detailed code chunks for relevant files
            List<Document> codeChunks = retrieveCodeChunks(query, allRelevantFiles);
            logger.info("🧩 Retrieved {} code chunks", codeChunks.size());
            
            // Step 4: Build context
            context.setFileSummaries(fileSummaries);
            context.setCodeChunks(codeChunks);
            context.setRelevantFiles(allRelevantFiles);
            context.setQuery(query);
            
            logger.info("✅ Brain 1 (Code Retriever): Context built successfully");
            
        } catch (Exception e) {
            logger.error("❌ Brain 1 (Code Retriever): Failed to retrieve context", e);
        }
        
        return context;
    }

    private Set<String> expandWithDependencies(List<Document> fileSummaries, int maxDepth) {
        Set<String> allFiles = new HashSet<>();
        Set<String> visited = new HashSet<>();
        Queue<String> toExplore = new LinkedList<>();
        
        // Start with initially found files
        for (Document doc : fileSummaries) {
            String filename = (String) doc.getMetadata().get("filename");
            if (filename != null) {
                allFiles.add(filename);
                toExplore.add(filename);
            }
        }
        
        // Expand using dependency graph
        int currentDepth = 0;
        while (!toExplore.isEmpty() && currentDepth < maxDepth) {
            int levelSize = toExplore.size();
            
            for (int i = 0; i < levelSize; i++) {
                String currentFile = toExplore.poll();
                if (visited.contains(currentFile)) continue;
                
                visited.add(currentFile);
                
                // Get dependencies (files this file depends on)
                Set<String> dependencies = dependencyGraph.getDependencies(currentFile);
                for (String dep : dependencies) {
                    if (!visited.contains(dep)) {
                        allFiles.add(dep);
                        toExplore.add(dep);
                    }
                }
                
                // Get reverse dependencies (files that depend on this file)
                Set<String> reverseDeps = dependencyGraph.getReverseDependencies(currentFile);
                for (String revDep : reverseDeps) {
                    if (!visited.contains(revDep)) {
                        allFiles.add(revDep);
                        toExplore.add(revDep);
                    }
                }
            }
            
            currentDepth++;
        }
        
        logger.debug("🔗 Dependency expansion: {} → {} files (depth: {})", 
            fileSummaries.size(), allFiles.size(), currentDepth);
        
        return allFiles;
    }

    private List<Document> retrieveCodeChunks(String query, Set<String> relevantFiles) {
        List<Document> allChunks = new ArrayList<>();
        
        // Search for chunks related to the query
        List<Document> queryChunks = chunkStore.similaritySearch(
            SearchRequest.builder()
                .query(query)
                .topK(10)
                .build()
        );
        
        // Filter chunks to only include those from relevant files
        List<Document> filteredChunks = queryChunks.stream()
            .filter(chunk -> {
                String filename = (String) chunk.getMetadata().get("filename");
                return filename != null && relevantFiles.contains(filename);
            })
            .collect(Collectors.toList());
        
        allChunks.addAll(filteredChunks);
        
        // Also get specific chunks for each relevant file
        for (String filename : relevantFiles) {
            List<Document> fileChunks = chunkStore.similaritySearch(
                SearchRequest.builder()
                    .query(filename + " " + query)
                    .topK(3)
                    .build()
            );
            
            // Add chunks that aren't already included
            for (Document chunk : fileChunks) {
                String chunkFilename = (String) chunk.getMetadata().get("filename");
                if (filename.equals(chunkFilename) && !allChunks.contains(chunk)) {
                    allChunks.add(chunk);
                }
            }
        }
        
        // Sort by relevance and limit
        return allChunks.stream()
            .distinct()
            .limit(15)
            .collect(Collectors.toList());
    }

    public CodeContext retrieveSpecificFile(String filename) {
        logger.info("📄 Retrieving specific file: {}", filename);
        
        CodeContext context = new CodeContext();
        
        try {
            // Get file summary
            List<Document> summaries = summaryStore.similaritySearch(
                SearchRequest.builder()
                    .query(filename)
                    .topK(5)
                    .build()
            );
            
            List<Document> fileSummary = summaries.stream()
                .filter(doc -> filename.equals(doc.getMetadata().get("filename")))
                .limit(1)
                .collect(Collectors.toList());
            
            // Get all chunks for this file
            List<Document> chunks = chunkStore.similaritySearch(
                SearchRequest.builder()
                    .query(filename)
                    .topK(20)
                    .build()
            );
            
            List<Document> fileChunks = chunks.stream()
                .filter(doc -> filename.equals(doc.getMetadata().get("filename")))
                .collect(Collectors.toList());
            
            context.setFileSummaries(fileSummary);
            context.setCodeChunks(fileChunks);
            context.setRelevantFiles(Set.of(filename));
            context.setQuery("file: " + filename);
            
            logger.info("✅ Retrieved {} chunks for file: {}", fileChunks.size(), filename);
            
        } catch (Exception e) {
            logger.error("❌ Failed to retrieve file: {}", filename, e);
        }
        
        return context;
    }

    public static class CodeContext {
        private List<Document> fileSummaries = new ArrayList<>();
        private List<Document> codeChunks = new ArrayList<>();
        private Set<String> relevantFiles = new HashSet<>();
        private String query;

        // Getters and setters
        public List<Document> getFileSummaries() { return fileSummaries; }
        public void setFileSummaries(List<Document> fileSummaries) { this.fileSummaries = fileSummaries; }
        
        public List<Document> getCodeChunks() { return codeChunks; }
        public void setCodeChunks(List<Document> codeChunks) { this.codeChunks = codeChunks; }
        
        public Set<String> getRelevantFiles() { return relevantFiles; }
        public void setRelevantFiles(Set<String> relevantFiles) { this.relevantFiles = relevantFiles; }
        
        public String getQuery() { return query; }
        public void setQuery(String query) { this.query = query; }
        
        public boolean isEmpty() {
            return fileSummaries.isEmpty() && codeChunks.isEmpty();
        }
        
        public String getFormattedContext() {
            StringBuilder context = new StringBuilder();
            
            if (!fileSummaries.isEmpty()) {
                context.append("📁 **File Summaries:**\n");
                for (Document summary : fileSummaries) {
                    String filename = (String) summary.getMetadata().get("filename");
                    context.append(String.format("- **%s**: %s\n", filename, summary.getText()));
                }
                context.append("\n");
            }
            
            if (!codeChunks.isEmpty()) {
                context.append("🧩 **Code Chunks:**\n");
                for (Document chunk : codeChunks) {
                    String filename = (String) chunk.getMetadata().get("filename");
                    String type = (String) chunk.getMetadata().get("chunk_type");
                    context.append(String.format("- **%s** (%s):\n```java\n%s\n```\n\n", 
                        filename, type, chunk.getText()));
                }
            }
            
            return context.toString();
        }
    }
}
