package com.bubua12.atlas.monitor.controller;

import com.bubua12.atlas.common.core.result.CommonResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 服务器监控控制器
 */
@RestController
@RequestMapping("/server")
public class ServerController {

    @GetMapping("/info")
    public CommonResult<Map<String, Object>> info() throws UnknownHostException {

        Runtime runtime = Runtime.getRuntime();
        Map<String, Object> serverInfo = new LinkedHashMap<>();

        // =========================
        // 基础信息
        // =========================
        serverInfo.put("主机名称", InetAddress.getLocalHost().getHostName());
        serverInfo.put("操作系统", System.getProperty("os.name"));
        serverInfo.put("系统架构", System.getProperty("os.arch"));
        serverInfo.put("Java版本", System.getProperty("java.version"));
        serverInfo.put("系统核心数", runtime.availableProcessors());

        // =========================
        // JVM 内存
        // =========================
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long maxMemory = runtime.maxMemory();
        long usedMemory = totalMemory - freeMemory;

        serverInfo.put("JVM已申请内存", formatMemory(totalMemory));
        serverInfo.put("JVM已使用内存", formatMemory(usedMemory));
        serverInfo.put("JVM空闲内存", formatMemory(freeMemory));
        serverInfo.put("JVM最大可用内存", formatMemory(maxMemory));

        // =========================
        // 服务器物理内存
        // =========================
        try {
            com.sun.management.OperatingSystemMXBean osBean =
                    (com.sun.management.OperatingSystemMXBean)
                            java.lang.management.ManagementFactory.getOperatingSystemMXBean();

            long totalPhysicalMemory = osBean.getTotalMemorySize();
            long freePhysicalMemory = osBean.getFreeMemorySize();
            long usedPhysicalMemory = totalPhysicalMemory - freePhysicalMemory;

            serverInfo.put("服务器总内存", formatMemory(totalPhysicalMemory));
            serverInfo.put("服务器已用内存", formatMemory(usedPhysicalMemory));
            serverInfo.put("服务器剩余内存", formatMemory(freePhysicalMemory));

        } catch (Exception e) {
            serverInfo.put("服务器内存信息", "获取失败");
        }

        return CommonResult.success(serverInfo);
    }


    /**
     * 字节转 MB
     */
    private String formatMemory(long bytes) {
        return String.format("%.2f MB", bytes / 1024.0 / 1024.0);
    }
}
