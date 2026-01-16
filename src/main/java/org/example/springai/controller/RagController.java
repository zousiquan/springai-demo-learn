package org.example.springai.controller;

import lombok.extern.slf4j.Slf4j;
import org.example.springai.service.RagService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/rag")
public class RagController {

    private final RagService ragService;

    @Autowired
    public RagController(RagService ragService) {
        this.ragService = ragService;
    }
    
    // 新增接口：获取知识库列表
    @GetMapping("/knowledge-bases")
    public Object getKnowledgeBases() {
        return Map.of(
                "success", true,
                "data", ragService.getKnowledgeBaseList(),
                "total", ragService.getKnowledgeBaseList().size()
        );
    }

    // 原有接口：添加文本内容，增加知识库参数
    @PostMapping("/add-doc")
    public String addDocument(@RequestBody Map<String, String> request) {
        String content = request.get("content");
        String collectionName = request.getOrDefault("collectionName", "coffee_collection");
        Map<String, Object> metadata = Map.of(
                "title", request.getOrDefault("title", "默认标题")
        );
        ragService.loadDocumentToVectorStore(content, metadata, collectionName);
        return "文档已成功添加到向量库";
    }

    // 新增接口：上传文件（PDF/Word等）并解析入库，增加知识库参数
    @PostMapping("/upload-file")
    public String uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "tag", defaultValue = "通用文档") String tag,
            @RequestParam(value = "collectionName", defaultValue = "coffee_collection") String collectionName) {
        try {
            // 1. 临时保存上传的文件
            String tempPath = "D:\\AgentFramework\\rag\\tmp\\" + UUID.randomUUID() + "_" + file.getOriginalFilename();
            File tempFile = new File(tempPath);
            tempFile.getParentFile().mkdirs();
            file.transferTo(tempFile);

            // 2. 解析文件并入库（自定义元数据：文件名、标签）
            Map<String, Object> metadata = Map.of(
                    "fileName", Objects.requireNonNull(file.getOriginalFilename()),
                    "fileType", Objects.requireNonNull(file.getContentType()),
                    "tag", tag
            );
            ragService.loadFileToVectorStore(tempPath, metadata, collectionName);

            // 3. 删除临时文件
            tempFile.delete();
            return "文件解析并入库成功：" + file.getOriginalFilename();
        } catch (Exception e) {
            log.error("文件处理失败：", e);
            return "文件处理失败：" + e.getMessage();
        }
    }

    // 原有接口：RAG问答，增加知识库参数
    @GetMapping("/ask")
    public String askQuestion(@RequestParam String question, 
                            @RequestParam(value = "collectionName", defaultValue = "coffee_collection") String collectionName) {
        return ragService.ragAnswerWithAdvisor(question, collectionName);
    }
    
    // 新增接口：删除知识库
    @DeleteMapping("/knowledge-bases/{collectionName}")
    public Object deleteKnowledgeBase(@PathVariable String collectionName) {
        try {
            ragService.deleteKnowledgeBase(collectionName);
            return Map.of(
                    "success", true,
                    "message", "知识库删除成功"
            );
        } catch (Exception e) {
            log.error("删除知识库失败：", e);
            return Map.of(
                    "success", false,
                    "message", "删除知识库失败：" + e.getMessage()
            );
        }
    }
}
