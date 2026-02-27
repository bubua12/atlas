package com.bubua12.atlas.infra.controller;

import com.bubua12.atlas.common.core.result.CommonResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/file")
public class FileController {

    @PostMapping("/upload")
    public CommonResult<String> upload(@RequestPart("file") MultipartFile file) {
        // TODO: implement file upload logic
        return CommonResult.ok("/upload/" + file.getOriginalFilename());
    }

    @DeleteMapping("/{fileId}")
    public CommonResult<Void> delete(@PathVariable Long fileId) {
        // TODO: implement file deletion logic
        return CommonResult.ok();
    }
}
