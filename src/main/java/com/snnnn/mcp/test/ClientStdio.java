//package com.snnnn.mcp.test;
//
//import io.modelcontextprotocol.client.transport.ServerParameters;
//import io.modelcontextprotocol.client.transport.StdioClientTransport;
//
//import java.io.File;
//
///**
// * 使用stdio传输，MCP服务器由客户端自动启动
// * 但你需要先构建服务器jar:
// *
// * <pre>
// * mvn clean install -DskipTests
// * </pre>
// */
//public class ClientStdio {
//
//    public static void main(String[] args) {
//
//        System.out.println(new File(".").getAbsolutePath());
//
//        var stdioParams = ServerParameters.builder("java")
//                .args("-Dspring.ai.mcp.server.stdio=true",
//                        "-Dspring.main.web-application-type=none",
//                        "-Dlogging.pattern.console=",
//                        "-jar",
//                        "target/mcp-demo-0.0.1-SNAPSHOT.jar")
//                .build();
//
//        var transport = new StdioClientTransport(stdioParams);
//
//        new SampleClient(transport).run();
//    }
//
//}