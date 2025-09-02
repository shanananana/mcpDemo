package com.snnnn.mcp.service;

import com.openhtmltopdf.outputdevice.helper.BaseRendererBuilder;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PdfService {

    @Value("${pdf.storageDir:data/pdfs}")
    private String storageDir;

    @Value("${server.port:8081}")
    private int serverPort;

    @Tool(description = "将输入文本生成 PDF 文件并返回下载地址")
    public Map<String, Object> generatePdf(
            @ToolParam(description = "PDF 标题") String title,
            @ToolParam(description = "PDF 正文内容") String content
    ) {
        Map<String, Object> response = new HashMap<>();

        try {
            ensureStorageDir();

            String safeTitle = (title == null || title.isBlank()) ? "document" : slugify(title);
            String timestamp = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss").format(LocalDateTime.now());
            String fileName = safeTitle + "_" + timestamp + ".pdf";
            Path outputPath = Paths.get(storageDir).resolve(fileName);

            writePdf(outputPath, title, content);

            String downloadUrl = "http://localhost:" + serverPort + "/pdf/download?filename=" + URLEncoder.encode(fileName, StandardCharsets.UTF_8);

            response.put("status", "success");
            response.put("message", "PDF 生成成功");
            response.put("fileName", fileName);
            response.put("filePath", outputPath.toAbsolutePath().toString());
            response.put("downloadUrl", downloadUrl);
            response.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            return response;
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", e.getMessage());
            response.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            return response;
        }
    }

    @Tool(description = "根据完整 HTML 生成 PDF 并返回下载地址")
    public Map<String, Object> generatePdfFromHtml(
            @ToolParam(description = "PDF 标题，用于文件名") String title,
            @ToolParam(description = "完整 HTML 内容，包含样式") String html
    ) {
        Map<String, Object> response = new HashMap<>();
        try {
            ensureStorageDir();

            String safeTitle = (title == null || title.isBlank()) ? "document" : slugify(title);
            String timestamp = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss").format(LocalDateTime.now());
            String fileName = safeTitle + "_" + timestamp + ".pdf";
            Path outputPath = Paths.get(storageDir).resolve(fileName);

            // Render HTML to PDF
            try (var out = Files.newOutputStream(outputPath)) {
                PdfRendererBuilder builder = new PdfRendererBuilder();
                builder.useFastMode();
                builder.withHtmlContent(html == null ? "" : html, null);
                // Optional: set a default font for CJK
                builder.useFont(() -> PdfService.class.getResourceAsStream("/fonts/NotoSansCJKsc-Regular.otf"),
                        "Noto Sans CJK SC", 400, BaseRendererBuilder.FontStyle.NORMAL, true);
                builder.toStream(out);
                builder.run();
            }

            String downloadUrl = "http://localhost:" + serverPort + "/pdf/download?filename=" + URLEncoder.encode(fileName, StandardCharsets.UTF_8);

            response.put("status", "success");
            response.put("message", "PDF 生成成功 (HTML)");
            response.put("fileName", fileName);
            response.put("filePath", outputPath.toAbsolutePath().toString());
            response.put("downloadUrl", downloadUrl);
            response.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            return response;
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", e.getMessage());
            response.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            return response;
        }
    }

    public Path resolveFile(String fileName) {
        return Paths.get(storageDir).resolve(fileName).toAbsolutePath();
    }

    private void ensureStorageDir() throws IOException {
        Path dir = Paths.get(storageDir);
        if (!Files.exists(dir)) {
            Files.createDirectories(dir);
        }
    }

    private void writePdf(Path outputPath, String title, String content) throws IOException {
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                float margin = 50;
                float yStart = page.getMediaBox().getHeight() - margin;
                float width = page.getMediaBox().getWidth() - margin * 2;

                contentStream.beginText();
                contentStream.setFont(PDType1Font.HELVETICA_BOLD, 18);
                contentStream.newLineAtOffset(margin, yStart);
                contentStream.showText(title == null || title.isBlank() ? "Document" : title);
                contentStream.endText();

                float leading = 14f;
                float fontSize = 12f;
                float yPosition = yStart - 30;

                List<String> lines = wrapText(content == null ? "" : content, 90);
                contentStream.setFont(PDType1Font.HELVETICA, fontSize);

                contentStream.beginText();
                contentStream.newLineAtOffset(margin, yPosition);
                for (String line : lines) {
                    contentStream.showText(line);
                    contentStream.newLineAtOffset(0, -leading);
                }
                contentStream.endText();
            }

            document.save(outputPath.toFile());
        }
    }

    private static String slugify(String input) {
        String s = input.replaceAll("[^a-zA-Z0-9\u4e00-\u9fa5]+", "_");
        s = s.replaceAll("_+", "_");
        return s.replaceAll("^_+|_+$", "");
    }

    private static List<String> wrapText(String text, int maxCharsPerLine) {
        List<String> result = new ArrayList<>();
        if (text == null || text.isEmpty()) {
            return result;
        }
        String[] paragraphs = text.split("\n");
        for (String p : paragraphs) {
            int idx = 0;
            while (idx < p.length()) {
                int end = Math.min(idx + maxCharsPerLine, p.length());
                result.add(p.substring(idx, end));
                idx = end;
            }
        }
        return result;
    }
}


