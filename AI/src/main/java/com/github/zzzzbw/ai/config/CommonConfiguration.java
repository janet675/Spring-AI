package com.github.zzzzbw.ai.config;
import io.milvus.client.MilvusServiceClient;
import io.milvus.param.ConnectParam;
import io.milvus.param.IndexType;
import io.milvus.param.MetricType;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.TokenCountBatchingStrategy;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.milvus.MilvusVectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CommonConfiguration {

    @Value("${milvus.host}")
    private String host;
    @Value("${milvus.port}")
    private Integer port;

    /**
     * 定义一个名为 milvusServiceClient 的Bean，用于创建并返回一个 MilvusServiceClient 实例。
     */
    @Bean
    public MilvusServiceClient milvusServiceClient() {
        return new MilvusServiceClient(
                ConnectParam.newBuilder()
                        .withHost(host)
                        .withPort(port)
                        .build());
    }

    /**
     * 定义一个名为 vectorStore2 的Bean，用于创建并返回一个 VectorStore 实例。
     * 使用 MilvusVectorStore.builder 方法构建向量存储对象，并设置以下参数：
     * collectionName：集合名称为 "vector_store_02"。
     * databaseName：数据库名称为 "default"。
     * embeddingDimension：嵌入维度为 1536。
     * indexType：索引类型为 IVF_FLAT，这是一种常见的近似最近邻搜索索引类型。
     * metricType：度量类型为 COSINE，用于计算向量之间的余弦相似度。
     * batchingStrategy：使用 TokenCountBatchingStrategy 策略进行批量处理。
     * initializeSchema：设置为 true，表示在构建时初始化数据库模式。
     */
    @Bean(name = "vectorStore2")
    public VectorStore vectorStore(MilvusServiceClient milvusClient, EmbeddingModel embeddingModel) {
        return MilvusVectorStore.builder(milvusClient, embeddingModel)
                .collectionName("vector_store_02")
                .databaseName("default")
                .embeddingDimension(1536)
                .indexType(IndexType.IVF_FLAT)
                .metricType(MetricType.COSINE)
                .batchingStrategy(new TokenCountBatchingStrategy())
                .initializeSchema(true)
                .build();
    }
}