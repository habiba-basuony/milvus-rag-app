package com.ragproject;

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.output.Response;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.model.ollama.OllamaChatModel;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RagQueryService {

    private final EmbeddingModel embeddingModel;
    private final EmbeddingStore<TextSegment> embeddingStore;
    private final ChatLanguageModel chatModel;

    public RagQueryService(EmbeddingModel embeddingModel, EmbeddingStore<TextSegment> embeddingStore) {
        this.embeddingModel = embeddingModel;
        this.embeddingStore = embeddingStore;

        this.chatModel = OllamaChatModel.builder()
                .baseUrl("http://localhost:11434")
                .modelName("phi")
                .temperature(0.2) 
                .numPredict(128) 
                .build();
    }

    public String query(String userQuestion, int maxResults) {
        // Embed the user question
        Response<dev.langchain4j.data.embedding.Embedding> embeddingResponse = embeddingModel.embed(userQuestion);
        dev.langchain4j.data.embedding.Embedding questionEmbedding = embeddingResponse.content();
        
        // Find relevant segments
        List<EmbeddingMatch<TextSegment>> relevant = embeddingStore.findRelevant(questionEmbedding, maxResults);

        // Build context from relevant segments
        StringBuilder context = new StringBuilder();
        for (EmbeddingMatch<TextSegment> match : relevant) {
            context.append(match.embedded().text()).append("\n");
        }

        // Create prompt and generate response
        String prompt = "Answer the following question based on the context provided:\n\n"
                      + "Context:\n" + context + "\n\nQuestion: " + userQuestion + "\n\nAnswer:";

        return chatModel.generate(prompt);
    }
}