package com.snnnn.mcp.controller;

import com.snnnn.mcp.model.req.ExcelGenerateRequest;
import com.snnnn.mcp.model.resp.FileIdResponse;
import com.snnnn.mcp.model.resp.StandardResponse;
import com.snnnn.mcp.service.ExcelService;
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
@RequestMapping("/api/excel")
public class ExcelController {

    private final ExcelService excelService;

    public ExcelController(ExcelService excelService) {
        this.excelService = excelService;
    }

    @Tool(description = "根据请求生成 XLSX，返回文件ID。请求体包含标题、表头与行数据。")
    @PostMapping("/generate-xlsx")
    public StandardResponse<FileIdResponse> generateXlsx(@ToolParam(description = "Excel 生成请求体") @RequestBody ExcelGenerateRequest req) {
        var map = excelService.generateXlsx(req);
        String fileId = (String) map.get("fileId");
        return StandardResponse.success(new FileIdResponse(fileId));
    }

    @Tool(description = "根据文件ID下载生成的 XLSX")
    @GetMapping("/download")
    public ResponseEntity<Resource> download(@ToolParam(description = "文件ID(UUID)") @RequestParam("fileId") String fileId) throws Exception {
        Path path = excelService.resolveFileById(fileId);
        if (!Files.exists(path)) {
            return ResponseEntity.notFound().build();
        }
        Resource resource = new FileSystemResource(path);
        String fileName = path.getFileName().toString();
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                .body(resource);
    }
}


