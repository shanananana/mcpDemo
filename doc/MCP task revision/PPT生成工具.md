# PPT 生成工具使用说明

## 功能描述
根据幻灯片内容生成 PPTX 格式的演示文稿，支持标题和正文内容。

## 接口信息
- **生成接口**: `POST /ppt/generate-pptx`
- **下载接口**: `GET /ppt/download?fileId={fileId}`

## 请求参数
```json
{
  "title": "季度汇报",
  "slides": [
    {"heading": "概览", "text": "本季度整体完成率 92%"},
    {"heading": "重点项目", "text": "A/B/C 项目进展顺利"},
    {"heading": "下季度计划", "text": "继续推进核心功能开发"}
  ]
}
```

### 参数说明
- `title`: 演示文稿标题，用于生成文件名
- `slides`: 幻灯片数组，每个元素包含：
  - `heading`: 幻灯片标题
  - `text`: 幻灯片正文内容

## 返回格式
```json
{
  "status": "success",
  "message": "PPTX 生成成功",
  "data": {
    "fileId": "uuid-string"
  }
}
```

## 使用示例

### PowerShell 调用
```powershell
$body = @{
  title = "季度汇报"
  slides = @(
    @{ heading = "概览"; text = "本季度整体完成率 92%" },
    @{ heading = "重点项目"; text = "A/B/C 项目进展顺利" },
    @{ heading = "下季度计划"; text = "继续推进核心功能开发" }
  )
} | ConvertTo-Json -Depth 5

$res = Invoke-RestMethod -Method Post -Uri "http://localhost:8081/ppt/generate-pptx" -ContentType "application/json" -Body $body
$fileId = $res.data.fileId

# 下载 PPT
Invoke-WebRequest -Uri ("http://localhost:8081/ppt/download?fileId=" + $fileId) -OutFile ".\季度汇报.pptx"
```

### MCP 客户端调用
```json
{
  "tool": "ppt.generatePptx",
  "input": {
    "title": "季度汇报",
    "slides": [
      {"heading": "概览", "text": "本季度整体完成率 92%"},
      {"heading": "重点项目", "text": "A/B/C 项目进展顺利"},
      {"heading": "下季度计划", "text": "继续推进核心功能开发"}
    ]
  }
}
```

## 幻灯片布局
- 标题位置：顶部居中，字体大小 28pt
- 正文位置：标题下方，字体大小 18pt
- 页面尺寸：标准 PPT 尺寸
- 自动布局：根据内容自动调整文本框位置

## 注意事项
1. 每个幻灯片都会创建新的页面
2. 标题和正文都是可选的，可以为空
3. 支持中英文混合内容
4. 生成的文件存储在 `data/ppts` 目录下
5. 使用返回的 `fileId` 进行文件下载
6. 建议每页内容适中，避免文字过多影响可读性
