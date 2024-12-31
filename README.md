Prerequirement: Postgresql,pgVector,IJ,Postman or Browser for testing Rest API

Requirement:

Create Assistence for getting report list based on user interest.

1. Use Spring Boot Create Project from Spring initilizer and Add Below Dependency
2. ![image](https://github.com/user-attachments/assets/bbfaca8e-8823-48a3-9094-38478077873e)
3. Create project Structure
4. ![image](https://github.com/user-attachments/assets/e3b65bcb-12bd-4107-94c8-0c802d2786ed)
5. Configure PgVectoreStore and ChatClient
   Here PgVector Store use locally installed Ollama for Embedding model and ChatClient as Groq.
   GROQ_API_KEY can be generate from GROQ Cloud Console.
   Define Application.Properties As Below
   ```
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
    logging.level.org.springframework = debug
```
**Configuration Class**
```
    @Value("${GROQ_AI_KEY}")
    private String GROQ_AI_KEY;
    @Bean
    PgVectorStore pgVectorStore(JdbcTemplate jdbcTemplate, OllamaEmbeddingModel openAiEmbeddingModel){
        return new PgVectorStore(jdbcTemplate,openAiEmbeddingModel,4096,COSINE_DISTANCE,false,NONE,true);
    }

    @Bean
    ChatClient chatClient(){
        var openai = new OpenAiApi("https://api.groq.com/openai",GROQ_AI_KEY);
        var openAiChatOptions = OpenAiChatOptions.builder()
                .withModel("llama3-70b-8192")
                .withTemperature(0.4)
                .withMaxTokens(200)
                .build();
        var openaichatmodel = new OpenAiChatModel(openai,openAiChatOptions);
        return ChatClient.create(openaichatmodel);
    }
   ```
7.** Create RestController, Initialize vectorestore,chatclient and systempropmt from SystemPromt.st file**
   ```
     @Autowired
     ChatClient chatClient;
    @Autowired
    PgVectorStore pgVectorStore;
    @Value("classpath:SystemPrompt.st")
    String systempropmt;

    private final ReportRepository repository;

    public ReportController( ReportRepository repository) {
           this.repository = repository;
    }
    @GetMapping("/reportlist")
    public Map<String,String> reportlist(@RequestParam String message){

        ChatResponse chatResponse= this.chatClient.prompt()
                .system(systempropmt)
                .user(message).
                advisors(new QuestionAnswerAdvisor(pgVectorStore,SearchRequest.query(message).withTopK(2)))
                .call()
                .chatResponse();
        return Map.of("Result",chatResponse.getResult().getOutput().getContent());
    }
   ```
8. Now create Report list in postgresql Report table using jdbctemplate and store data in pgvector make sure it should be one timw only.
    ```
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
	public CommandLineRunner commandLineRunner(JdbcTemplate jdbcTemplate){
		return args -> {
			if(jdbcTemplate.queryForObject("""
					select count(*)
					from report
					""",Integer.class)==0){
				reportService.executeSqlFromFile();
			}

			if(jdbcTemplate.queryForObject("""
					select count(*)
					from vector_store
					""",Integer.class)==0){
				List<Report> reportList = repository.findAll();
				List<Document> documentList = new ArrayList<>();
				reportList.forEach(report -> {
					String content = "Name="+report.getName()+" Description="+report.getDescription() + " Report Category="+report.getReportcategory()
							+" Report Sub category="+report.getReportsubcategory();
					documentList.add(new Document(content, Map.of("ID",report.getId(),"Name",report.getName(),"Description",report.getDescription()
							,"Report Category",report.getReportcategory(),"Report Sub category",report.getReportsubcategory())));

				});
				pgVectorStore.add(documentList);
			}

		} ;
	}
    ```

**Summary**
Here we configured GROQ,Vector Store , Embedding Model and Chat Client.
Using this we can interect with Postgresql data.
