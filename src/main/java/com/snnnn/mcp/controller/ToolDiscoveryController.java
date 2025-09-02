package com.snnnn.mcp.controller;

import com.snnnn.mcp.service.ToolDiscoveryService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/mcp")
public class ToolDiscoveryController {

    private final ToolDiscoveryService toolDiscoveryService;

    public ToolDiscoveryController(ToolDiscoveryService toolDiscoveryService) {
        this.toolDiscoveryService = toolDiscoveryService;
    }

    @GetMapping("/tools")
    public Map<String, Object> getTools() {
        return toolDiscoveryService.listTools();
    }
}


