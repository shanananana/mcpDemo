package com.snnnn.mcp.service;

import com.snnnn.mcp.model.req.PptGenerateRequest;
import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.apache.poi.xslf.usermodel.XSLFSlide;
import org.apache.poi.xslf.usermodel.XSLFTextBox;
import org.apache.poi.xslf.usermodel.XSLFTextParagraph;
import org.apache.poi.xslf.usermodel.XSLFTextRun;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class PptService {

    @Value("${ppt.storageDir:data/ppts}")
    private String storageDir;

    public Map<String, Object> generatePptx(PptGenerateRequest req) {
        Map<String, Object> result = new HashMap<>();
        try {
            ensureStorageDir();

            String fileId = UUID.randomUUID().toString();
            String title = (req.getTitle() == null || req.getTitle().isBlank()) ? "slides" : slugify(req.getTitle());
            String fileName = title + "_" + fileId + ".pptx";
            Path out = Paths.get(storageDir).resolve(fileName);

            try (XMLSlideShow ppt = new XMLSlideShow()) {
                if (req.getSlides() != null) {
                    for (PptGenerateRequest.SlideItem item : req.getSlides()) {
                        XSLFSlide slide = ppt.createSlide();

                        if (item.getHeading() != null && !item.getHeading().isBlank()) {
                            XSLFTextBox titleBox = slide.createTextBox();
                            XSLFTextParagraph p = titleBox.addNewTextParagraph();
                            XSLFTextRun r = p.addNewTextRun();
                            r.setText(item.getHeading());
                            r.setFontSize(28.0);
                            titleBox.setAnchor(new java.awt.Rectangle(50, 40, 620, 60));
                        }

                        if (item.getText() != null && !item.getText().isBlank()) {
                            XSLFTextBox body = slide.createTextBox();
                            XSLFTextParagraph p2 = body.addNewTextParagraph();
                            XSLFTextRun r2 = p2.addNewTextRun();
                            r2.setText(item.getText());
                            r2.setFontSize(18.0);
                            body.setAnchor(new java.awt.Rectangle(50, 120, 620, 360));
                        }
                    }
                }

                try (var os = Files.newOutputStream(out)) {
                    ppt.write(os);
                }
            }

            result.put("status", "success");
            result.put("message", "PPTX 生成成功");
            result.put("fileId", fileId);
            return result;
        } catch (Exception e) {
            result.put("status", "error");
            result.put("message", e.getMessage());
            return result;
        }
    }

    public Path resolveFileById(String fileId) throws IOException {
        Path dir = Paths.get(storageDir).toAbsolutePath();
        if (!Files.exists(dir)) return dir.resolve(fileId + ".pptx");
        try (var stream = Files.list(dir)) {
            return stream.filter(p -> p.getFileName().toString().contains(fileId))
                    .findFirst()
                    .orElse(dir.resolve(fileId + ".pptx"));
        }
    }

    private void ensureStorageDir() throws IOException {
        Path dir = Paths.get(storageDir);
        if (!Files.exists(dir)) Files.createDirectories(dir);
    }

    private String slugify(String in) {
        return in.replaceAll("[^a-zA-Z0-9\u4e00-\u9fa5-_]+", "_");
    }
}
