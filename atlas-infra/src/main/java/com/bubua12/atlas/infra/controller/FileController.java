package com.bubua12.atlas.infra.controller;

import com.bubua12.atlas.common.core.result.CommonResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文件管理控制器（上传、删除）
 */
@RestController
@RequestMapping("/file")
public class FileController {

    @PostMapping("/upload")
    public CommonResult<String> upload(@RequestPart("file") MultipartFile file) {
        // TODO: implement file upload logic
        return CommonResult.success("/upload/" + file.getOriginalFilename());
    }

    @DeleteMapping("/{fileId}")
    public CommonResult<Void> delete(@PathVariable Long fileId) {
        // TODO: implement file deletion logic
        return CommonResult.success();
    }
}
