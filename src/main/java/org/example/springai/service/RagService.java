package org.example.springai.service;

import org.example.springai.advisor.CustomAnswerAdvisor;
import org.example.springai.config.ChromaVectorStoreFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chroma.vectorstore.ChromaApi;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class RagService {
    private final ChromaApi chromaApi;
    private final EmbeddingModel embeddingModel;
    private final OpenAiChatModel chatModel;
    private final ChatClient ragChatClient;
    private final ChatClient mcpChatClient;
    private final ChromaVectorStoreFactory chromaVectorStoreFactory;


    @Value("${spring.ai.vector-store.chroma.collection-name:coffee_collection}")
    private String defaultCollectionName;
    @Value("${spring.ai.vector-store.chroma.tenant-name:default_tenant}")
    private String defaultTenantName ;
    @Value("${spring.ai.vector-store.chroma.database-name:default_database}")
    private String defaultDatabaseName;



    @Autowired
    public RagService(ChromaVectorStoreFactory chromaVectorStoreFactory, ChromaApi chromaApi, EmbeddingModel embeddingModel, OpenAiChatModel chatModel,ChatClient ragChatClient,ChatClient mcpChatClient) {
        this.chromaVectorStoreFactory = chromaVectorStoreFactory;
        this.chromaApi = chromaApi;
        this.embeddingModel = embeddingModel;
        this.chatModel = chatModel;
        this.ragChatClient = ragChatClient;
        this.mcpChatClient = mcpChatClient;
    }
    
    // 获取知识库列表
    public List<Map<String, String>> getKnowledgeBaseList() {
        List<Map<String, String>> kbList = new ArrayList<>();
        
        try {
            // 调用listCollections方法获取所有collections
            List<ChromaApi.Collection> collections = chromaApi.listCollections(defaultTenantName, defaultDatabaseName);

            // 将collections转换为知识库列表
            assert collections != null;
            collections.forEach(collection -> {
                Map<String, String> kb = new HashMap<>();
                kb.put("id", collection.name());
                kb.put("name", collection.name());
                kb.put("description", "Chroma collection: " + collection.metadata().get("description"));
                kbList.add(kb);
            });
            
            // 如果没有任何知识库，添加默认知识库
            if (kbList.isEmpty()) {
                Map<String, String> defaultKb = new HashMap<>();
                defaultKb.put("id", defaultCollectionName);
                defaultKb.put("name", "默认知识库");
                defaultKb.put("description", "系统默认知识库");
                kbList.add(defaultKb);
            }
        } catch (Exception e) {
            // 如果获取失败，返回默认知识库
            Map<String, String> defaultKb = new HashMap<>();
            defaultKb.put("id", defaultCollectionName);
            defaultKb.put("name", "默认知识库");
            defaultKb.put("description", "系统默认知识库");
            kbList.add(defaultKb);
        }
        
        return kbList;
    }

    // 原有方法：加载文本内容入库，增加collectionName参数
    public void loadDocumentToVectorStore(String content, Map<String, Object> metadata, String collectionName) {
        // 在metadata中添加collectionName，用于区分不同知识库
        Map<String, Object> updatedMetadata = new HashMap<>(metadata);
        updatedMetadata.put("collectionName", collectionName);
        
        Document document = new Document(content, updatedMetadata);
        chromaVectorStoreFactory.getChromaVectorStore(collectionName).add(List.of(document));
    }

    // 新增方法：解析本地文件（PDF/Word等）并入库，增加collectionName参数
    public void loadFileToVectorStore(String filePath, Map<String, Object> customMetadata, String collectionName) {
        // 1. 创建Tika文档解析器
        TikaDocumentReader tikaReader = new TikaDocumentReader(new FileSystemResource(new File(filePath)));

        // 2. 解析文件：返回Document列表（大文件会自动分片，小文件返回1个Document）
        List<Document> documents = tikaReader.get();

        // 3. 合并自定义元数据（如文件来源、业务标签等）
        List<Document> updatedDocuments = documents.stream()
                .map(doc -> {
                    // Tika自动提取的元数据：文件名、格式、大小等
                    Map<String, Object> mergedMetadata = new HashMap<>(doc.getMetadata());
                    // 合并自定义元数据（覆盖重复的key）
                    mergedMetadata.putAll(customMetadata);
                    // 添加collectionName，用于区分不同知识库
                    mergedMetadata.put("collectionName", collectionName);
                    return doc.mutate().metadata(mergedMetadata).build();
                })
                .toList();
        // 4. 存入向量库（自动向量化）
        chromaVectorStoreFactory.getChromaVectorStore(collectionName).add(updatedDocuments);
    }

    // 原有RAG问答方法，增加collectionName参数
    public String ragAnswer(String userQuestion, String collectionName) {
        // 在查询时添加collectionName过滤条件
        List<Document> allDocs = chromaVectorStoreFactory.getChromaVectorStore(collectionName).similaritySearch(userQuestion);
        List<Document> relevantDocs = allDocs.stream()
                .filter(doc -> collectionName.equals(doc.getMetadata().get("collectionName")))
                .toList();
                
        String prompt = """
                请基于以下上下文信息回答用户的问题，只使用上下文里的内容，不要编造信息。
                如果上下文没有相关信息，请回答"无法从知识库中找到相关答案"。
                
                上下文：
                %s
                
                用户问题：%s
                """
                .formatted(
                relevantDocs.stream().map(Document::getText).reduce("", (a, b) -> a + "\n" + b),
                userQuestion
        );
        return ragChatClient.prompt().user( prompt).call().content();
    }

    public String ragAnswerWithAdvisor(String userQuestion, String collectionName, String conversationId) {
        if(userQuestion.contains("文件")){
           return mcpChatClient.prompt()
                .user(userQuestion)
                .system(p -> p.param("chat_memory_conversation_id", conversationId))
                .call()
                .content();
        }

        return ragChatClient
                .prompt()
                .advisors(QuestionAnswerAdvisor.builder(chromaVectorStoreFactory.getChromaVectorStore(collectionName))
                        .build())
                .advisors(new CustomAnswerAdvisor())
                .user( u -> {
                    u.text(userQuestion).metadata("chat_memory_conversation_id", conversationId);
                })
                .call()
                .content();
    }
    
    // 创建知识库
    public void createKnowledgeBase(String collectionName, String description) {
        // 准备metadata
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("description", description);
        // 使用ChromaApi创建新的collection
        chromaApi.createCollection(defaultTenantName, defaultDatabaseName, new ChromaApi.CreateCollectionRequest(collectionName, metadata));
    }
    
    // 删除知识库
    public void deleteKnowledgeBase(String collectionName) {
        // 使用ChromaApi删除指定的collection
        chromaApi.deleteCollection(defaultTenantName, defaultDatabaseName, collectionName);
    }

}
