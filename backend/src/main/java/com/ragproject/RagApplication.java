package com.ragproject;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentParser;
import dev.langchain4j.data.document.loader.FileSystemDocumentLoader;
import dev.langchain4j.data.document.parser.TextDocumentParser;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.ollama.OllamaEmbeddingModel; 
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import java.io.File;

@SpringBootApplication
public class RagApplication {

    public static void main(String[] args) {
        SpringApplication.run(RagApplication.class, args);
    }

    @Bean
    public EmbeddingModel embeddingModel() {
        return OllamaEmbeddingModel.builder()
                .baseUrl("http://localhost:11434")
                .modelName("phi")
                .build();
    }

    @Bean
    public EmbeddingStore<TextSegment> embeddingStore(MilvusConfig milvusConfig) {
        return milvusConfig.getMilvusStore();
    }

    @Bean
    CommandLineRunner init(EmbeddingModel embeddingModel, 
                          EmbeddingStore<TextSegment> embeddingStore,
                          ResourceLoader resourceLoader) {
        return args -> {
            try {
               
                Resource resource = resourceLoader.getResource("classpath:dataset/ancient_civilizations.txt");
                File file = resource.getFile();
                
               
                DocumentParser parser = new TextDocumentParser();
                Document document = FileSystemDocumentLoader.loadDocument(file.toPath(), parser);

                
                EmbeddingStoreIngestor ingestor = EmbeddingStoreIngestor.builder()
                        .documentSplitter(DocumentSplitters.recursive(300, 30))
                        .embeddingModel(embeddingModel)
                        .embeddingStore(embeddingStore)
                        .build();
                
                
                ingestor.ingest(document);
                System.out.println("Document loaded successfully in Milvus");
            } catch (Exception e) {
                System.err.println("Error loading document: " + e.getMessage());
                e.printStackTrace();
            }
        };
    }
}