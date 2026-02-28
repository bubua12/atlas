package com.bubua12.atlas.api.infra.dto;

import lombok.Data;

/**
 * 文件数据传输对象
 */
@Data
public class FileDTO {

    /**
     * 文件ID
     */
    private Long fileId;
    /**
     * 文件名称
     */
    private String fileName;
    /**
     * 文件存储路径
     */
    private String filePath;
    /**
     * 文件大小（字节）
     */
    private Long fileSize;
    /**
     * 文件类型（MIME）
     */
    private String fileType;
}
