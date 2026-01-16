package org.example.springai.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chroma.vectorstore.ChromaApi;
import org.springframework.ai.chroma.vectorstore.ChromaVectorStore;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * ChromaVectorStore工厂类，用于根据collection名称动态获取ChromaVectorStore实例
 * 使用单例模式，每个collection创建一个对应的ChromaVectorStore实例
 */
@Slf4j
@Component
public class ChromaVectorStoreFactory {

    // 注入ChromaApi
    private final ChromaApi chromaApi;
    // 注入EmbeddingModel
    private final EmbeddingModel embeddingModel;
    // 注入默认的collection名称
    @Value("${spring.ai.vector-store.chroma.collection-name:coffee_collection}")
    private String defaultCollectionName;
    // 注入默认的tenant名称
    @Value("${spring.ai.vector-store.chroma.tenant-name:default_tenant}")
    private String defaultTenantName;
    // 注入默认的database名称
    @Value("${spring.ai.vector-store.chroma.database-name:default_database}")
    private String defaultDatabaseName;

    // 用于存储collection名称和对应的ChromaVectorStore实例
    private final Map<String, ChromaVectorStore> vectorStoreMap = new ConcurrentHashMap<>();

    /**
     * 构造函数，注入依赖
     */
    @Autowired
    public ChromaVectorStoreFactory(ChromaApi chromaApi, EmbeddingModel embeddingModel) {
        this.chromaApi = chromaApi;
        this.embeddingModel = embeddingModel;
    }

    /**
     * 根据collection名称获取ChromaVectorStore实例
     * 
     * @param collectionName collection名称，如果为null或空则返回默认的ChromaVectorStore
     * @return ChromaVectorStore实例
     */
    public ChromaVectorStore getChromaVectorStore(String collectionName) {
        // 如果collectionName为null或空，返回默认的ChromaVectorStore
        if (collectionName == null || collectionName.isEmpty()) {
            return getDefaultChromaVectorStore();
        }

        // 如果map中已经存在对应的ChromaVectorStore实例，直接返回
        if (vectorStoreMap.containsKey(collectionName)) {
            return vectorStoreMap.get(collectionName);
        }

        // 如果map中不存在对应的ChromaVectorStore实例，创建一个新的并存储到map中
        ChromaVectorStore vectorStore = createChromaVectorStore(collectionName);
        vectorStoreMap.put(collectionName, vectorStore);
        return vectorStore;
    }

    /**
     * 获取默认的ChromaVectorStore实例
     * 
     * @return 默认的ChromaVectorStore实例
     */
    private ChromaVectorStore getDefaultChromaVectorStore() {
        // 如果map中已经存在默认的ChromaVectorStore实例，直接返回
        if (vectorStoreMap.containsKey(defaultCollectionName)) {
            return vectorStoreMap.get(defaultCollectionName);
        }

        // 如果map中不存在默认的ChromaVectorStore实例，创建一个新的并存储到map中
        ChromaVectorStore vectorStore = createChromaVectorStore(defaultCollectionName);
        vectorStoreMap.put(defaultCollectionName, vectorStore);
        return vectorStore;
    }

    /**
     * 创建ChromaVectorStore实例
     * 
     * @param collectionName collection名称
     * @return ChromaVectorStore实例
     */
    private ChromaVectorStore createChromaVectorStore(String collectionName) {
        // 使用ChromaVectorStore.Builder模式创建实例，这样可以动态设置collection名称
        // 注意：根据Spring AI的设计，ChromaVectorStore的构造函数可能不直接支持collection名称参数
        // 但我们可以通过Builder模式或其他方式来创建实例
        // 这里我们返回一个新的ChromaVectorStore实例，每个collectionName对应一个实例
        // 虽然我们无法直接设置collection名称，但我们可以通过metadata过滤来实现类似的效果
        // 这是因为在loadDocumentToVectorStore和loadFileToVectorStore方法中，我们已经将collectionName添加到了文档的metadata中
        // 所以在查询时，我们可以通过filterExpression来过滤特定collectionName的文档
        ChromaVectorStore chromaVectorStore = ChromaVectorStore.builder(chromaApi, embeddingModel).tenantName(defaultTenantName).databaseName(defaultDatabaseName).collectionName(collectionName).build();
        try {
            chromaVectorStore.afterPropertiesSet();
        }catch (Exception e){
            log.error("创建ChromaVectorStore实例失败",e);
        }
        return chromaVectorStore;
    }
}
