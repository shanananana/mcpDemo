# spring ai 实现 java mcp 示例

## 分层结构
- controller 层：仅处理 HTTP 请求/响应，调用 service
- service 层：承载业务逻辑、MCP 工具（`@Tool`）

## 启动
- 构建：`./mvnw clean package -DskipTests`
- 运行：`java -jar target/mcp-demo-0.0.1-SNAPSHOT.jar`
- 后台：`nohup java -jar target/mcp-demo-0.0.1-SNAPSHOT.jar > app.log 2>&1 &`

## API 示例
 
- GET `/api/mcp/tools`