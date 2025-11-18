# 🗄️ DATABASE SETUP GUIDE - PHASE 3: DATABASE PERSISTENCE

## ✅ WHAT HAS BEEN CONFIGURED

### 1. **Dependencies Added to pom.xml**
```xml
✅ spring-boot-starter-data-jpa
✅ mysql-connector-java (8.0.33)
✅ flyway-core
✅ flyway-mysql
```

### 2. **Database Properties Added to application.properties**
```properties
✅ MySQL Connection URL: jdbc:mysql://localhost:3306/coding-assistance
✅ JPA/Hibernate Configuration
✅ Flyway Migration Configuration
✅ Connection Pool (HikariCP) Configuration
```

### 3. **Repository Configuration**
```java
✅ RepositoryConfig.java - Enables JPA repositories
✅ @EnableJpaRepositories - Scans com.vijay.repository package
✅ @EnableTransactionManagement - Enables transaction support
```

### 4. **JPA Entities Created**
```
✅ EditHistory.java - Tracks all code edits
✅ UserPattern.java - Tracks user editing patterns
✅ SuggestionFeedback.java - Collects feedback on suggestions
```

### 5. **Spring Data Repositories Created**
```
✅ EditHistoryRepository.java - 15+ custom queries
✅ UserPatternRepository.java - 12+ custom queries
✅ SuggestionFeedbackRepository.java - 15+ custom queries
```

### 6. **Service Layer**
```
✅ EditHistoryService.java - Business logic for edit tracking
```

### 7. **REST Controller**
```
✅ EditHistoryController.java - 10 REST endpoints
```

### 8. **Database Migration**
```
✅ V1__Create_Edit_History_Tables.sql - Flyway migration script
```

---

## 📋 SETUP INSTRUCTIONS

### **Step 1: Create MySQL Database**

```sql
-- Create database
CREATE DATABASE IF NOT EXISTS `coding-assistance` 
CHARACTER SET utf8mb4 
COLLATE utf8mb4_unicode_ci;

-- Verify creation
SHOW DATABASES;
```

**Or using command line:**
```bash
mysql -u root -p -e "CREATE DATABASE IF NOT EXISTS coding-assistance CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
```

### **Step 2: Verify MySQL Connection**

```bash
# Test connection
mysql -u root -p -h localhost -e "SELECT 1;"

# Should output:
# +---+
# | 1 |
# +---+
# | 1 |
# +---+
```

### **Step 3: Update application.properties (if needed)**

If your MySQL setup is different, update these properties:

```properties
# Default (localhost with root user)
spring.datasource.url=jdbc:mysql://localhost:3306/coding-assistance?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
spring.datasource.username=root
spring.datasource.password=root

# For different host/port:
spring.datasource.url=jdbc:mysql://your-host:3306/coding-assistance?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
spring.datasource.username=your-username
spring.datasource.password=your-password
```

### **Step 4: Build the Project**

```bash
cd e:\ai_projects\spring-boot\Coding-Assistance

# Clean and build
mvn clean package

# Or just compile (skip tests)
mvn clean compile
```

### **Step 5: Run the Application**

```bash
# Run with Spring Boot Maven plugin
mvn spring-boot:run

# Or run the JAR file
java -jar target/Coding-Assistance-0.0.1-SNAPSHOT.jar
```

### **Step 6: Verify Database Setup**

Once the application starts, check the logs for:

```
✅ Flyway Migration Success:
   - "Successfully validated 1 migration"
   - "Successfully applied 1 migration"

✅ Hibernate Initialization:
   - "HHH000412: Hibernate ORM core version X.X.X"
   - "Database connection successful"

✅ Repository Initialization:
   - "Bootstrapping Spring Data JPA repositories"
   - "Finished Spring Data repository scanning"
```

### **Step 7: Test the Endpoints**

```bash
# Track an edit
curl -X POST http://localhost:8080/api/edits/track \
  -H "Content-Type: application/json" \
  -d '{
    "userId": "test-user",
    "filePath": "src/main/java/Test.java",
    "originalCode": "public void test() {}",
    "editedCode": "public void testMethod() {}",
    "editType": "rename_method",
    "suggestionSource": "AI",
    "accepted": true,
    "description": "Renamed method"
  }'

# Get user statistics
curl http://localhost:8080/api/edits/stats/test-user

# Get user patterns
curl http://localhost:8080/api/edits/patterns/test-user
```

---

## 🔧 TROUBLESHOOTING

### **Issue: "Connection refused" or "Cannot connect to MySQL"**

**Solution:**
```bash
# Check if MySQL is running
mysql -u root -p -e "SELECT 1;"

# If MySQL is not running, start it:
# Windows:
net start MySQL80

# Mac:
brew services start mysql

# Linux:
sudo systemctl start mysql
```

### **Issue: "Access denied for user 'root'@'localhost'"**

**Solution:**
```bash
# Check your password in application.properties
# Update if needed:
spring.datasource.password=your-correct-password

# Or reset MySQL root password:
mysql -u root -e "ALTER USER 'root'@'localhost' IDENTIFIED BY 'root';"
```

### **Issue: "Database 'coding-assistance' doesn't exist"**

**Solution:**
```bash
# Create the database manually:
mysql -u root -p -e "CREATE DATABASE coding-assistance CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"

# Verify:
mysql -u root -p -e "SHOW DATABASES;"
```

### **Issue: "Flyway migration failed"**

**Solution:**
```bash
# Check Flyway logs in application output
# Ensure V1__Create_Edit_History_Tables.sql exists in:
# src/main/resources/db/migration/

# If migration is stuck, you can baseline:
# 1. Delete the flyway_schema_history table
# 2. Restart the application

# Or manually:
mysql -u root -p coding-assistance -e "DROP TABLE IF EXISTS flyway_schema_history;"
```

### **Issue: "Table 'coding-assistance.edit_history' doesn't exist"**

**Solution:**
```bash
# Verify tables were created:
mysql -u root -p coding-assistance -e "SHOW TABLES;"

# Should show:
# +---------------------------+
# | Tables_in_coding_assistance |
# +---------------------------+
# | edit_history              |
# | flyway_schema_history     |
# | suggestion_feedback       |
# | user_pattern              |
# +---------------------------+

# If tables don't exist, check Flyway logs and re-run migration
```

### **Issue: "JPA repositories not found"**

**Solution:**
```bash
# Ensure RepositoryConfig.java exists and is in config package
# Verify @EnableJpaRepositories annotation is present
# Check that repositories are in com.vijay.repository package

# Rebuild:
mvn clean compile
```

---

## 📊 DATABASE SCHEMA

### **Tables Created by Flyway**

#### **edit_history**
- Tracks all code edits
- Columns: id, user_id, file_path, original_code, edited_code, edit_type, suggestion_source, accepted, description, context, lines_changed, complexity, created_at
- Indexes: user_id, file_path, created_at, user_created, edit_type, accepted

#### **user_pattern**
- Tracks user editing patterns
- Columns: id, user_id, pattern_type, frequency, acceptance_rate, description, examples, total_suggestions, accepted_suggestions, last_used, created_at, updated_at, active
- Indexes: user_pattern_id, pattern_type, user_pattern_type, acceptance_rate, active

#### **suggestion_feedback**
- Collects feedback on suggestions
- Columns: id, user_id, suggestion_id, suggestion_type, suggestion_content, rating, action, feedback, user_modification, reason, helpful, relevant, accurate, sentiment, created_at
- Indexes: feedback_user_id, feedback_suggestion_id, feedback_rating, feedback_created, action, sentiment, helpful

### **Views Created**
- v_user_edit_stats - User edit statistics
- v_edit_type_stats - Edit type statistics
- v_suggestion_source_stats - Suggestion source statistics
- v_user_pattern_stats - User pattern statistics
- v_feedback_stats - Feedback statistics

---

## 🚀 NEXT STEPS

### **Phase 3.2: User Feedback System**
- Implement feedback collection endpoints
- Add suggestion rating system
- Track user preferences

### **Phase 3.3: InlineSuggestionEngine**
- Real-time suggestions as user types
- Keyboard shortcuts
- One-click apply

### **Phase 3.4: Test Generation**
- Generate unit tests from code
- Generate test cases
- Integrate with existing code

---

## 📝 CONFIGURATION SUMMARY

| Component | Status | Details |
|-----------|--------|---------|
| **Dependencies** | ✅ Added | JPA, MySQL, Flyway |
| **Database** | ⏳ Manual | Create `coding-assistance` database |
| **Properties** | ✅ Configured | MySQL connection, JPA, Flyway, HikariCP |
| **Entities** | ✅ Created | EditHistory, UserPattern, SuggestionFeedback |
| **Repositories** | ✅ Created | 3 repositories with 40+ queries |
| **Service** | ✅ Created | EditHistoryService with business logic |
| **Controller** | ✅ Created | 10 REST endpoints |
| **Migration** | ✅ Created | Flyway SQL migration script |
| **Configuration** | ✅ Created | RepositoryConfig.java |

---

## ✨ READY TO RUN!

Once you complete the setup steps above, your application will:

1. ✅ Connect to MySQL database
2. ✅ Run Flyway migrations automatically
3. ✅ Create all required tables and views
4. ✅ Initialize Spring Data JPA repositories
5. ✅ Enable REST endpoints for edit tracking
6. ✅ Start collecting edit history and patterns

**Total setup time: ~5 minutes**

---

## 📞 SUPPORT

If you encounter any issues:

1. Check the troubleshooting section above
2. Review application logs for error messages
3. Verify MySQL is running and accessible
4. Ensure database `coding-assistance` exists
5. Check that all files are in correct locations

**You're all set!** 🎉
