package com.bubua12.atlas.api.infra.dto;

import lombok.Data;

@Data
public class FileDTO {

    private Long fileId;

    private String fileName;

    private String filePath;

    private Long fileSize;

    private String fileType;
}
