package in.mbs.aitool.AITooldemo.controller;

import in.mbs.aitool.AITooldemo.Repository.ReportRepository;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.QuestionAnswerAdvisor;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.vectorstore.PgVectorStore;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class ReportController {

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

}
