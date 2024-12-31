package in.mbs.aitool.AITooldemo.configuration;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.ollama.OllamaEmbeddingModel;

import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.ai.vectorstore.PgVectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.springframework.ai.vectorstore.PgVectorStore.PgDistanceType.COSINE_DISTANCE;
import static org.springframework.ai.vectorstore.PgVectorStore.PgIndexType.NONE;

@Configuration
public class VectorStorePg {
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


}
