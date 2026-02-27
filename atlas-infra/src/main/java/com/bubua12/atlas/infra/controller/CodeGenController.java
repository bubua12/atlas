package com.bubua12.atlas.infra.controller;

import com.bubua12.atlas.common.core.result.CommonResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/codegen")
public class CodeGenController {

    @GetMapping("/tables")
    public CommonResult<List<String>> tables() {
        // TODO: implement table listing logic
        return CommonResult.ok(new ArrayList<>());
    }

    @PostMapping("/generate/{tableName}")
    public CommonResult<Void> generate(@PathVariable String tableName) {
        // TODO: implement code generation logic
        return CommonResult.ok();
    }
}
