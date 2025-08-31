//package com.snnnn.mcp.test;
//
//import io.modelcontextprotocol.client.McpClient;
//import io.modelcontextprotocol.spec.McpClientTransport;
//import io.modelcontextprotocol.spec.McpSchema.CallToolRequest;
//import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
//import io.modelcontextprotocol.spec.McpSchema.ListToolsResult;
//
//import java.util.Map;
//
///**
// * @author zj
// */
//public class SampleClient {
//
//    private final McpClientTransport transport;
//
//    public SampleClient(McpClientTransport transport) {
//        this.transport = transport;
//    }
//
//    public void run() {
//        McpClientTransport client = McpClient.sync(this.transport).build();
//        client.initialize();
//        client.ping();
//        // 列出并展示可用的工具
//        ListToolsResult toolsList = client.listTools();
//        System.out.println("可用工具 = " + toolsList);
//
//        // 获取成都的天气
//        CallToolResult weatherForecastResult = client.callTool(new CallToolRequest("getWeather",
//                Map.of("cityName", "成都")));
//        System.out.println("返回结果: " + weatherForecastResult.content());
//
//        client.closeGracefully();
//    }
//}