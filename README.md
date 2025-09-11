# spring ai 实现 java mcp 示例

## 分层结构
- controller 层：仅处理 HTTP 请求/响应，调用 service
- service 层：承载业务逻辑、MCP 工具（`@Tool`）

## 启动
- 构建：`./mvnw clean package -DskipTests`
- 运行：`java -jar target/mcp-demo-0.0.1-SNAPSHOT.jar`
- 后台：`nohup java -jar target/mcp-demo-0.0.1-SNAPSHOT.jar > app.log 2>&1 &`

## 功能

- MCP Server（WebFlux + SSE + stdio）
- PDF：根据 HTML 生成、下载
- Excel：根据列头 + 行数据生成 XLSX、下载
- PPT：根据 slides 生成 PPTX、下载

## API 示例（通过 MCP 工具）

- GET `/api/mcp/tools` 查看暴露工具与真实路径
- POST `/api/pdf/generate-html` → 返回 `fileId`
- GET  `/api/pdf/download?fileId=...` → 下载 PDF
- POST `/api/excel/generate-xlsx` → 返回 `fileId`
- GET  `/api/excel/download?fileId=...` → 下载 XLSX
- POST `/api/ppt/generate-pptx` → 返回 `fileId`
- GET  `/api/ppt/download?fileId=...` → 下载 PPTX

### 示例：生成 Excel（XLSX）

```powershell
$body = @{
  title = "销售报表"
  sheetName = "Sheet1"
  headers = @("日期","地区","销量","金额")
  rows = @(
    @{ 日期 = "2024-01-01"; 地区 = "华东"; 销量 = 120; 金额 = 35600 },
    @{ 日期 = "2024-01-02"; 地区 = "华北"; 销量 = 80;  金额 = 21000 }
  )
} | ConvertTo-Json -Depth 6

$res = Invoke-RestMethod -Method Post -Uri "http://localhost:8081/api/excel/generate-xlsx" -ContentType "application/json" -Body $body
$fileId = $res.data.fileId
Invoke-WebRequest -Uri ("http://localhost:8081/api/excel/download?fileId=" + $fileId) -OutFile ".\报表.xlsx"
```

### 示例：生成 PPT（PPTX）

```powershell
$body = @{
  title = "季度汇报"
  slides = @(
    @{ heading = "概览"; text = "本季度整体完成率 92%" },
    @{ heading = "重点项目"; text = "A/B/C 项目进展顺利" }
  )
} | ConvertTo-Json -Depth 5

$res = Invoke-RestMethod -Method Post -Uri "http://localhost:8081/api/ppt/generate-pptx" -ContentType "application/json" -Body $body
$fileId = $res.data.fileId
Invoke-WebRequest -Uri ("http://localhost:8081/api/ppt/download?fileId=" + $fileId) -OutFile ".\汇报.pptx"
```

### MCP 客户端 JSON 示例（复杂参数）

```json
{
  "tool": "excel.generateXlsx",
  "input": {
    "title": "销售报表",
    "sheetName": "Sheet1",
    "headers": ["日期","地区","销量","金额"],
    "rows": [
      {"日期": "2024-01-01", "地区": "华东", "销量": 120, "金额": 35600},
      {"日期": "2024-01-02", "地区": "华北", "销量": 80,  "金额": 21000}
    ]
  }
}
```

```json
{
  "tool": "ppt.generatePptx",
  "input": {
    "title": "季度汇报",
    "slides": [
      {"heading": "概览", "text": "本季度整体完成率 92%"},
      {"heading": "重点项目", "text": "A/B/C 项目进展顺利"}
    ]
  }
}
```