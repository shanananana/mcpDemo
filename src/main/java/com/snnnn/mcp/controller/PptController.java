package com.snnnn.mcp.controller;

import com.snnnn.mcp.model.req.PptGenerateRequest;
import com.snnnn.mcp.model.resp.FileIdResponse;
import com.snnnn.mcp.model.resp.StandardResponse;
import com.snnnn.mcp.service.PptService;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.file.Files;
import java.nio.file.Path;

@RestController
@RequestMapping("/api/ppt")
public class PptController {

    private final PptService pptService;

    public PptController(PptService pptService) {
        this.pptService = pptService;
    }

    @Tool(description = "根据请求生成 PPTX，返回文件ID。请求体包含标题与 slides 列表。")
    @PostMapping("/generate-pptx")
    public StandardResponse<FileIdResponse> generatePptx(@ToolParam(description = "PPT 生成请求体") @RequestBody PptGenerateRequest req) {
        var map = pptService.generatePptx(req);
        String fileId = (String) map.get("fileId");
        return StandardResponse.success(new FileIdResponse(fileId));
    }

    @Tool(description = "根据文件ID下载生成的 PPTX")
    @GetMapping("/download")
    public ResponseEntity<Resource> download(@ToolParam(description = "文件ID(UUID)") @RequestParam("fileId") String fileId) throws Exception {
        Path path = pptService.resolveFileById(fileId);
        if (!Files.exists(path)) {
            return ResponseEntity.notFound().build();
        }
        Resource resource = new FileSystemResource(path);
        String fileName = path.getFileName().toString();
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.presentationml.presentation"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                .body(resource);
    }
}


