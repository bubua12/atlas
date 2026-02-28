package com.bubua12.atlas.infra.controller;

import com.bubua12.atlas.common.core.result.CommonResult;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

/**
 * 代码生成控制器
 */
@RestController
@RequestMapping("/codegen")
public class CodeGenController {

    @GetMapping("/tables")
    public CommonResult<List<String>> tables() {
        // TODO: implement table listing logic
        return CommonResult.success(new ArrayList<>());
    }

    @PostMapping("/generate/{tableName}")
    public CommonResult<Void> generate(@PathVariable String tableName) {
        // TODO: implement code generation logic
        return CommonResult.success();
    }
}
