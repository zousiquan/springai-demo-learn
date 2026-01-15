package org.example.springai.service;

import org.example.springai.advisor.CustomAnswerAdvisor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.List;
import java.util.Map;

@Service
public class RagService {
    private final VectorStore vectorStore;
    private final EmbeddingModel embeddingModel;
    private final OpenAiChatModel chatModel;
    private final ChatClient ragChatClient;
    private final ChatClient mcpChatClient;

    @Autowired
    public RagService(VectorStore vectorStore, EmbeddingModel embeddingModel, OpenAiChatModel chatModel,ChatClient ragChatClient,ChatClient mcpChatClient) {
        this.vectorStore = vectorStore;
        this.embeddingModel = embeddingModel;
        this.chatModel = chatModel;
        this.ragChatClient = ragChatClient;
        this.mcpChatClient = mcpChatClient;
    }

    // 原有方法：加载文本内容入库
    public void loadDocumentToVectorStore(String content, Map<String, Object> metadata) {
        Document document = new Document(content, metadata);
        vectorStore.add(List.of(document));
    }

    // 新增方法：解析本地文件（PDF/Word等）并入库
    public void loadFileToVectorStore(String filePath, Map<String, Object> customMetadata) {
        // 1. 创建Tika文档解析器
        TikaDocumentReader tikaReader = new TikaDocumentReader(new FileSystemResource(new File(filePath)));

        // 2. 解析文件：返回Document列表（大文件会自动分片，小文件返回1个Document）
        List<Document> documents = tikaReader.get();

        // 3. 合并自定义元数据（如文件来源、业务标签等）
        List<Document> updatedDocuments = documents.stream()
                .map(doc -> {
                    // Tika自动提取的元数据：文件名、格式、大小等
                    Map<String, Object> mergedMetadata = doc.getMetadata();
                    // 合并自定义元数据（覆盖重复的key）
                    mergedMetadata.putAll(customMetadata);
                    return doc.mutate().metadata(mergedMetadata).build();
                })
                .toList();
        // 4. 存入向量库（自动向量化）
        vectorStore.add(updatedDocuments);
    }

    // 原有RAG问答方法不变
    public String ragAnswer(String userQuestion) {
        List<Document> relevantDocs = vectorStore.similaritySearch(userQuestion);
        String prompt = """
                请基于以下上下文信息回答用户的问题，只使用上下文里的内容，不要编造信息。
                如果上下文没有相关信息，请回答"无法从知识库中找到相关答案"。
                
                上下文：
                %s
                
                用户问题：%s
                """.formatted(
                relevantDocs.stream().map(Document::getText).reduce("", (a, b) -> a + "\n" + b),
                userQuestion
        );
        return ragChatClient.prompt().user( prompt).call().content();
    }

    public String ragAnswerWithAdvisor(String userQuestion) {
        if(userQuestion.contains("文件")){
           return mcpChatClient.prompt()
                .user(userQuestion)
                .call()
                .content();
        }
        return ChatClient.builder(chatModel)
                .build().prompt()
                .advisors(QuestionAnswerAdvisor.builder(vectorStore).build())
                .advisors(new CustomAnswerAdvisor())
                .user(userQuestion)
                .call()
                .content();
    }


}
