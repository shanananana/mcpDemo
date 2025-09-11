# Excel 生成工具使用说明

## 功能描述
根据表头和数据行生成 XLSX 格式的 Excel 文件，支持多种数据类型。

## 接口信息
- **生成接口**: `POST /excel/generate-xlsx`
- **下载接口**: `GET /excel/download?fileId={fileId}`

## 请求参数
```json
{
  "title": "销售报表",
  "sheetName": "Sheet1",
  "headers": ["日期", "地区", "销量", "金额"],
  "rows": [
    {"日期": "2024-01-01", "地区": "华东", "销量": 120, "金额": 35600},
    {"日期": "2024-01-02", "地区": "华北", "销量": 80, "金额": 21000}
  ]
}
```

### 参数说明
- `title`: 工作簿标题，用于生成文件名
- `sheetName`: 工作表名称，默认为 "Sheet1"
- `headers`: 列头数组，定义列的顺序和名称
- `rows`: 数据行数组，每行是列名到值的映射

## 返回格式
```json
{
  "status": "success",
  "message": "XLSX 生成成功",
  "data": {
    "fileId": "uuid-string"
  }
}
```

## 使用示例

### PowerShell 调用
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

$res = Invoke-RestMethod -Method Post -Uri "http://localhost:8081/excel/generate-xlsx" -ContentType "application/json" -Body $body
$fileId = $res.data.fileId

# 下载 Excel
Invoke-WebRequest -Uri ("http://localhost:8081/excel/download?fileId=" + $fileId) -OutFile ".\销售报表.xlsx"
```

### MCP 客户端调用
```json
{
  "tool": "excel.generateXlsx",
  "input": {
    "title": "销售报表",
    "sheetName": "Sheet1",
    "headers": ["日期", "地区", "销量", "金额"],
    "rows": [
      {"日期": "2024-01-01", "地区": "华东", "销量": 120, "金额": 35600},
      {"日期": "2024-01-02", "地区": "华北", "销量": 80, "金额": 21000}
    ]
  }
}
```

## 数据类型支持
- 字符串 (String)
- 数字 (Number)
- 布尔值 (Boolean)
- 日期 (Date)
- 空值 (null)

## 注意事项
1. 列头顺序决定了数据在 Excel 中的列位置
2. 每行数据的键必须与列头完全匹配
3. 列宽会自动调整以适应内容
4. 生成的文件存储在 `data/excels` 目录下
5. 使用返回的 `fileId` 进行文件下载
