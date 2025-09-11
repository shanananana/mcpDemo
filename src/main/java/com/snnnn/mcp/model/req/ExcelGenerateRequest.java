package com.snnnn.mcp.model.req;

import java.util.List;
import java.util.Map;

public class ExcelGenerateRequest {

    // 工作簿标题（用于文件名）
    private String title;

    // 工作表名
    private String sheetName;

    // 列头，按顺序渲染
    private List<String> headers;

    // 数据行，每行是列名到值的映射（列名需与 headers 对齐）
    private List<Map<String, Object>> rows;

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getSheetName() { return sheetName; }
    public void setSheetName(String sheetName) { this.sheetName = sheetName; }

    public List<String> getHeaders() { return headers; }
    public void setHeaders(List<String> headers) { this.headers = headers; }

    public List<Map<String, Object>> getRows() { return rows; }
    public void setRows(List<Map<String, Object>> rows) { this.rows = rows; }
}


