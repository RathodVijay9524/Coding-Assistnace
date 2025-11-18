# TESTING GUIDE

## 1. Overview

This project has tests for:

- **Core services** (business logic)
- **REST controllers** (HTTP layer)
- **Editing / AI services** (Code analysis, suggestions, transformations, semantic search)

All written with **JUnit 5**, **Mockito**, and **Spring Boot Test**.

---

## 2. Running Tests

### 2.1 Run all tests

From project root:

```bash
mvn test
```

### 2.2 Run only service tests

```bash
mvn -Dtest=*ServiceTest test
```

### 2.3 Run only controller tests

```bash
mvn -Dtest=*ControllerTest test
```

### 2.4 Run only editing/AI tests

```bash
mvn -Dtest=com.vijay.editing.*Test test
```

You can always run a specific test class from your IDE by right-clicking the class and selecting **Run test**.

---

## 3. Test Classes

### 3.1 Core Services (Phase 3)

Package: `com.vijay.service`

- `EditHistoryServiceTest`
  - Tests: `trackEdit`, `getUserEditHistory`, `getRecentEdits`, `getUserStatistics`.
- `SuggestionFeedbackServiceTest`
  - Tests: `recordFeedback`, filters (rating, helpful), `getUserFeedbackStatistics`.
- `InlineSuggestionEngineServiceTest`
  - Tests: `generateInlineSuggestions`, `getContextAwareSuggestions`, `getPersonalizedSuggestions`, `getQuickFixSuggestions`.
- `TestGenerationEngineServiceTest`
  - Tests: unit / integration / edge-case test generation, className override, getter/setter filtering.

### 3.2 Controllers

Package: `com.vijay.controller`

- `EditHistoryControllerTest`
  - `/api/edits/track`, `/history/{userId}`, `/recent/{userId}`, `/stats/{userId}`.
- `SuggestionFeedbackControllerTest`
  - `/api/feedback/record`, `/history/{userId}`, `/stats/{userId}`.
- `InlineSuggestionControllerTest`
  - `/api/suggestions/inline`, `/quick-fix`.
- `TestGenerationControllerTest`
  - `/api/tests/generate-unit`, `/api/tests/frameworks`.

These use `@WebMvcTest` + `MockMvc` with `@MockBean` services.

### 3.3 Editing / AI Services

Package: `com.vijay.editing`

- `CodeSelectionAnalyzerTest`
  - Basic selection analysis and pattern detection.
- `EditSuggestionGeneratorTest`
  - Suggestions based on instructions and analysis (extract + rename).
- `CodeTransformationEngineTest`
  - `transformFile` with a simple rename rule.
- `SemanticCodeSearchTest`
  - `searchByIntent`, `calculateSemanticSimilarity` JSON responses.
- `PatternExtractorTest`
  - `extractDesignPatterns` detecting Singleton.
- `DependencyGraphAnalyzerTest`
  - `buildGraph`, `findDependencies` JSON responses.

---

## 4. Notes & Warnings

- `@MockBean` is **deprecated** in Spring Boot 3.4+, but still works.
  - It’s used in controller tests; safe for now.
- Some tests use simple, placeholder behavior (e.g. semantic search and dependency graph rely on mock data in the implementation).
  - These tests confirm **JSON shape and wiring**, not deep business correctness.

---

## 5. Adding New Tests

When adding new services or controllers:

1. Create a corresponding test class under `src/test/java` in the same package structure.
2. For services:
   - Use pure JUnit 5 + Mockito (`@Mock`, `@InjectMocks`).
3. For controllers:
   - Use `@WebMvcTest(YourController.class)`, `MockMvc`, and `@MockBean` for dependencies.
4. Focus on:
   - Happy paths
   - Important edge cases
   - Error handling (exceptions → HTTP 4xx/5xx or default values)

This keeps the suite consistent and easy to extend as the project grows.
