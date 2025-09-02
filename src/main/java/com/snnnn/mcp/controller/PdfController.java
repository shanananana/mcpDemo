package com.snnnn.mcp.controller;

import com.snnnn.mcp.model.req.PdfGenerateHtmlRequest;
import com.snnnn.mcp.model.resp.FileIdResponse;
import com.snnnn.mcp.model.resp.StandardResponse;
import com.snnnn.mcp.service.PdfService;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.nio.file.Files;
import java.nio.file.Path;

@RestController
@RequestMapping("/pdf")
public class PdfController {

	private final PdfService pdfService;

	public PdfController(PdfService pdfService) {
		this.pdfService = pdfService;
	}

	@Tool(description = "根据完整 HTML 生成 PDF，返回文件ID。请求体包含 title 与 html 字段。")
	@PostMapping("/generate-html")
	public StandardResponse<FileIdResponse> generateFromHtml(@ToolParam(description = "包含title与html的请求体") @RequestBody PdfGenerateHtmlRequest req) {
		var map = pdfService.generatePdfFromHtml(req.getTitle(), req.getHtml());
		String fileId = (String) map.get("fileId");
		return StandardResponse.success(new FileIdResponse(fileId));
	}

	@Tool(description = "根据文件ID下载生成的PDF")
	@GetMapping("/download")
	public ResponseEntity<Resource> download(@ToolParam(description = "文件ID(UUID)") @RequestParam("fileId") String fileId) throws Exception {
		Path path = pdfService.resolveFileById(fileId);
		if (!Files.exists(path)) {
			return ResponseEntity.notFound().build();
		}
		Resource resource = new FileSystemResource(path);
		String fileName = path.getFileName().toString();
		return ResponseEntity.ok()
				.contentType(MediaType.APPLICATION_PDF)
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
				.body(resource);
	}
}


