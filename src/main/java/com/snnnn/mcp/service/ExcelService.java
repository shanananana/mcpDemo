package com.snnnn.mcp.service;

import com.snnnn.mcp.model.req.ExcelGenerateRequest;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@Service
public class ExcelService {

    @Value("${excel.storageDir:data/excels}")
    private String storageDir;

    public Map<String, Object> generateXlsx(ExcelGenerateRequest req) {
        Map<String, Object> result = new HashMap<>();
        try {
            ensureStorageDir();

            String fileId = UUID.randomUUID().toString();
            String title = (req.getTitle() == null || req.getTitle().isBlank()) ? "workbook" : slugify(req.getTitle());
            String fileName = title + "_" + fileId + ".xlsx";
            Path out = Paths.get(storageDir).resolve(fileName);

            try (Workbook wb = new XSSFWorkbook()) {
                String sheetName = (req.getSheetName() == null || req.getSheetName().isBlank()) ? "Sheet1" : req.getSheetName();
                Sheet sheet = wb.createSheet(sheetName);

                List<String> headers = Optional.ofNullable(req.getHeaders()).orElse(Collections.emptyList());
                List<Map<String, Object>> rows = Optional.ofNullable(req.getRows()).orElse(Collections.emptyList());

                int rowIdx = 0;

                if (!headers.isEmpty()) {
                    Row headerRow = sheet.createRow(rowIdx++);
                    for (int i = 0; i < headers.size(); i++) {
                        Cell cell = headerRow.createCell(i);
                        cell.setCellValue(headers.get(i));
                    }
                }

                for (Map<String, Object> dataRow : rows) {
                    Row r = sheet.createRow(rowIdx++);
                    for (int i = 0; i < headers.size(); i++) {
                        String key = headers.get(i);
                        Object val = dataRow.get(key);
                        Cell c = r.createCell(i);
                        if (val == null) {
                            c.setBlank();
                        } else if (val instanceof Number num) {
                            c.setCellValue(num.doubleValue());
                        } else if (val instanceof Boolean b) {
                            c.setCellValue(b);
                        } else if (val instanceof Date d) {
                            c.setCellValue(d);
                        } else {
                            c.setCellValue(String.valueOf(val));
                        }
                    }
                }

                for (int i = 0; i < headers.size(); i++) {
                    sheet.autoSizeColumn(i);
                }

                try (var os = Files.newOutputStream(out)) {
                    wb.write(os);
                }
            }

            result.put("status", "success");
            result.put("message", "XLSX 生成成功");
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
        if (!Files.exists(dir)) return dir.resolve(fileId + ".xlsx");
        try (var stream = Files.list(dir)) {
            return stream.filter(p -> p.getFileName().toString().contains(fileId))
                    .findFirst()
                    .orElse(dir.resolve(fileId + ".xlsx"));
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
