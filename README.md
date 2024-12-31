# AI Tool Demo - Report Assistance

## Prerequisites
- PostgreSQL
- pgVector
- IntelliJ IDEA (IJ)
- Postman or Browser for testing REST APIs

---

## Requirement
Create an assistance tool for generating a report list based on user interests.

---

## Steps to Implement

### 1. Create Spring Boot Project
Use the [Spring Initializer](https://start.spring.io/) to create a new project with the required dependencies.

### 2. Add Dependencies
Include the necessary dependencies for PostgreSQL, pgVector, and any additional libraries in your project.

![Dependencies](https://github.com/user-attachments/assets/bbfaca8e-8823-48a3-9094-38478077873e)

### 3. Create Project Structure
Organize your project directory and files as shown below:

![Project Structure](https://github.com/user-attachments/assets/e3b65bcb-12bd-4107-94c8-0c802d2786ed)

### 4. Configure pgVector Store and Chat Client
- **pgVector Store**: Uses a locally installed Ollama for the embedding model.
- **Chat Client**: Utilizes Groq as the chat API client.

#### Key Configuration:
- Generate `GROQ_API_KEY` from the GROQ Cloud Console.
- Define the following in `application.properties`:

```properties
spring.application.name=AI-Tool-demo
spring.jpa.hibernate.ddl-auto=update
spring.jpa.hibernate.show-sql=true
spring.datasource.url=jdbc:postgresql://localhost:5432/postgres
spring.datasource.username=tad
spring.datasource.password=tad
spring.ai.openai.base-url=https://api.groq.com/openai
spring.ai.openai.api-key=${GROQ_AI_KEY}
spring.ai.openai.chat.options.model=llama3-70b-8192
spring.ai.ollama.base-url=http://127.0.0.1:11434/
spring.ai.ollama.embedding.options.model=mistral
spring.ai.ollama.embedding.enabled=true
logging.level.org.springframework=debug
```

### 5. Create Configuration Class

```java
@Value("${GROQ_AI_KEY}")
private String GROQ_AI_KEY;

@Bean
PgVectorStore pgVectorStore(JdbcTemplate jdbcTemplate, OllamaEmbeddingModel openAiEmbeddingModel) {
    return new PgVectorStore(jdbcTemplate, openAiEmbeddingModel, 4096, COSINE_DISTANCE, false, NONE, true);
}

@Bean
ChatClient chatClient() {
    var openai = new OpenAiApi("https://api.groq.com/openai", GROQ_AI_KEY);
    var openAiChatOptions = OpenAiChatOptions.builder()
            .withModel("llama3-70b-8192")
            .withTemperature(0.4)
            .withMaxTokens(200)
            .build();
    var openaichatmodel = new OpenAiChatModel(openai, openAiChatOptions);
    return ChatClient.create(openaichatmodel);
}
```

---

### 6. Create REST Controller
Define a controller to initialize `pgVectorStore`, `chatClient`, and a system prompt from the `SystemPrompt.st` file.

```java
@Autowired
ChatClient chatClient;

@Autowired
PgVectorStore pgVectorStore;

@Value("classpath:SystemPrompt.st")
String systempropmt;

private final ReportRepository repository;

public ReportController(ReportRepository repository) {
    this.repository = repository;
}

@GetMapping("/reportlist")
public Map<String, String> reportlist(@RequestParam String message) {
    ChatResponse chatResponse = this.chatClient.prompt()
            .system(systempropmt)
            .user(message)
            .advisors(new QuestionAnswerAdvisor(pgVectorStore, SearchRequest.query(message).withTopK(2)))
            .call()
            .chatResponse();
    return Map.of("Result", chatResponse.getResult().getOutput().getContent());
}
```

---

### 7. Populate PostgreSQL Report Table
Create the report list in the PostgreSQL `Report` table using `JdbcTemplate` and store data in pgVector. This operation should be one-time only.

```java
@Autowired
ReportService reportService;

@Autowired
PgVectorStore pgVectorStore;

@Autowired
ReportRepository repository;

public static void main(String[] args) {
    SpringApplication.run(AiToolDemoApplication.class, args);
}

@Bean
public CommandLineRunner commandLineRunner(JdbcTemplate jdbcTemplate) {
    return args -> {
        if (jdbcTemplate.queryForObject("""
                select count(*)
                from report
                """, Integer.class) == 0) {
            reportService.executeSqlFromFile();
        }

        if (jdbcTemplate.queryForObject("""
                select count(*)
                from vector_store
                """, Integer.class) == 0) {
            List<Report> reportList = repository.findAll();
            List<Document> documentList = new ArrayList<>();
            reportList.forEach(report -> {
                String content = "Name=" + report.getName() + " Description=" + report.getDescription() +
                        " Report Category=" + report.getReportcategory() +
                        " Report Sub category=" + report.getReportsubcategory();
                documentList.add(new Document(content, Map.of(
                        "ID", report.getId(),
                        "Name", report.getName(),
                        "Description", report.getDescription(),
                        "Report Category", report.getReportcategory(),
                        "Report Sub category", report.getReportsubcategory())));
            });
            pgVectorStore.add(documentList);
        }
    };
}
```

---

## Summary
In this project, we:
- Configured GROQ, Vector Store, Embedding Model, and Chat Client.
- Integrated PostgreSQL to manage data.
- Enabled interaction with data using REST APIs and embedding models.

This setup allows you to fetch report lists dynamically based on user input and interest using AI-driven solutions.

