package com.bubua12.atlas.monitor.controller;

import com.bubua12.atlas.common.core.result.CommonResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/server")
public class ServerController {

    @GetMapping("/info")
    public CommonResult<Map<String, Object>> info() throws UnknownHostException {
        Runtime runtime = Runtime.getRuntime();
        Map<String, Object> serverInfo = new LinkedHashMap<>();
        serverInfo.put("hostname", InetAddress.getLocalHost().getHostName());
        serverInfo.put("osName", System.getProperty("os.name"));
        serverInfo.put("osArch", System.getProperty("os.arch"));
        serverInfo.put("jvmVersion", System.getProperty("java.version"));
        serverInfo.put("availableProcessors", runtime.availableProcessors());
        serverInfo.put("totalMemory", runtime.totalMemory());
        serverInfo.put("freeMemory", runtime.freeMemory());
        serverInfo.put("maxMemory", runtime.maxMemory());
        return CommonResult.ok(serverInfo);
    }
}
