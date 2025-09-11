# PDF 生成工具使用说明

## 功能描述
根据 HTML 内容生成 PDF 文档，支持中文内容渲染。

## 接口信息
- **生成接口**: `POST /pdf/generate-html`
- **下载接口**: `GET /pdf/download?fileId={fileId}`

## 请求参数
```json
{
  "title": "文档标题",
  "html": "<html><body><h1>标题</h1><p>正文内容</p></body></html>"
}
```

### 参数说明
- `title`: 文档标题，用于生成文件名
- `html`: 完整的 HTML 内容，支持内联样式

## 返回格式
```json
{
  "status": "success",
  "message": "PDF 生成成功 (HTML)",
  "data": {
    "fileId": "uuid-string"
  }
}
```

## 使用示例

### PowerShell 调用
```powershell
$body = @{
  title = "示例PDF"
  html  = "<html><body><h1>你好，MCP PDF</h1><p>这是一份由 MCP 接口生成的 PDF。</p></body></html>"
} | ConvertTo-Json -Depth 5

$res = Invoke-RestMethod -Method Post -Uri "http://localhost:8081/pdf/generate-html" -ContentType "application/json" -Body $body
$fileId = $res.data.fileId

# 下载 PDF
Invoke-WebRequest -Uri ("http://localhost:8081/pdf/download?fileId=" + $fileId) -OutFile ".\示例PDF.pdf"
```

### MCP 客户端调用
```json
{
  "tool": "pdf.generateFromHtml",
  "input": {
    "title": "示例PDF",
    "html": "<html><body><h1>你好，MCP PDF</h1><p>这是一份由 MCP 接口生成的 PDF。</p></body></html>"
  }
}
```

## 注意事项
1. 确保服务端有中文字体文件，避免中文显示为问号
2. HTML 内容会被完整渲染到 PDF 中
3. 生成的文件存储在 `data/pdfs` 目录下
4. 使用返回的 `fileId` 进行文件下载
