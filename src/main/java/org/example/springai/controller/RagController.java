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

    // 原有接口：添加文本内容
    @PostMapping("/add-doc")
    public String addDocument(@RequestBody Map<String, String> request) {
        String content = request.get("content");
        Map<String, Object> metadata = Map.of("title", request.getOrDefault("title", "默认标题"));
        ragService.loadDocumentToVectorStore(content, metadata);
        return "文档已成功添加到向量库";
    }

    // 新增接口：上传文件（PDF/Word等）并解析入库
    @PostMapping("/upload-file")
    public String uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "tag", defaultValue = "通用文档") String tag) {
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
            ragService.loadFileToVectorStore(tempPath, metadata);

            // 3. 删除临时文件
            tempFile.delete();
            return "文件解析并入库成功：" + file.getOriginalFilename();
        } catch (Exception e) {
            log.error("文件处理失败：", e);
            return "文件处理失败：" + e.getMessage();
        }
    }

    // 原有接口：RAG问答
    @GetMapping("/ask")
    public String askQuestion(@RequestParam String question) {
        return ragService.ragAnswer(question);
    }
}
