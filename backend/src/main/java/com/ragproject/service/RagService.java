package com.ragproject.service;

import dev.langchain4j.chain.ConversationalRetrievalChain;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.retriever.Retriever;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.retriever.EmbeddingStoreRetriever;
import dev.langchain4j.model.ollama.OllamaChatModel;
import org.springframework.stereotype.Service;

@Service
public class RagService {

    private final ConversationalRetrievalChain chain;

    public RagService(EmbeddingStore<TextSegment> embeddingStore,
                     EmbeddingModel embeddingModel) {

        Retriever<TextSegment> retriever = EmbeddingStoreRetriever.from(embeddingStore, embeddingModel, 3);

        ChatLanguageModel chatModel = OllamaChatModel.builder()
                .baseUrl("http://localhost:11434")
                .modelName("phi")
                .temperature(0.2) 
                .numPredict(128) 
                .build();

        MessageWindowChatMemory chatMemory = MessageWindowChatMemory.builder()
                .maxMessages(5) 
                .build();

        this.chain = ConversationalRetrievalChain.builder()
                .chatLanguageModel(chatModel)
                .chatMemory(chatMemory)
                .retriever(retriever)
                .build();
    }

    public String chat(String userMessage) {
        return chain.execute(userMessage);
    }
}