package org.example.springai.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.http.HttpRequest;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Flow;

public class BodyPublisherToJson {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    // 从BodyPublisher提取字节流并解析为JSON字符串
    public static CompletableFuture<String> bodyPublisherToJson(HttpRequest.BodyPublisher publisher) {
        CompletableFuture<String> resultFuture = new CompletableFuture<>();
        List<ByteBuffer> byteBuffers = new ArrayList<>();

        // 订阅BodyPublisher的ByteBuffer流
        publisher.subscribe(new Flow.Subscriber<>() {
            @Override
            public void onSubscribe(Flow.Subscription subscription) {
                // 请求所有数据
                subscription.request(Long.MAX_VALUE);
            }

            @Override
            public void onNext(ByteBuffer item) {
                // 收集所有ByteBuffer
                byteBuffers.add(item);
            }

            @Override
            public void onError(Throwable throwable) {
                resultFuture.completeExceptionally(throwable);
            }

            @Override
            public void onComplete() {
                // 拼接ByteBuffer为字节数组，转换为JSON字符串
                int totalLength = byteBuffers.stream().mapToInt(ByteBuffer::remaining).sum();
                byte[] allBytes = new byte[totalLength];
                int position = 0;
                for (ByteBuffer buffer : byteBuffers) {
                    int remaining = buffer.remaining();
                    buffer.get(allBytes, position, remaining);
                    position += remaining;
                }
                // 转换为JSON字符串（UTF-8编码）
                String jsonStr = new String(allBytes, StandardCharsets.UTF_8);
                resultFuture.complete(jsonStr);
            }
        });

        return resultFuture;
    }

    public static void main(String[] args) throws Exception {
        // 1. 构建JSON格式的BodyPublisher
        String jsonStr = "{\"name\":\"Chroma\",\"type\":\"向量库\"}";
        HttpRequest.BodyPublisher publisher = HttpRequest.BodyPublishers.ofString(jsonStr, StandardCharsets.UTF_8);

        // 2. 解析为JSON字符串
        String jsonResult = bodyPublisherToJson(publisher).get();
        System.out.println("从BodyPublisher还原的JSON：" + jsonResult);

        // 3. 进一步解析为Java对象
        MyObject obj = OBJECT_MAPPER.readValue(jsonResult, MyObject.class);
        System.out.println("解析后的Java对象：" + obj.getName() + " - " + obj.getType());
    }

    // 对应的Java实体类
    static class MyObject {
        private String name;
        private String type;

        // getter/setter
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
    }
}
