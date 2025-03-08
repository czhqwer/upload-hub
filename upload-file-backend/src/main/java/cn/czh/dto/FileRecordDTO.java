package cn.czh.dto;

import lombok.Data;

@Data
public class FileRecordDTO {

    /**
     * 文件ID
     */
    private Long fileId;

    /**
     * 分片上传的uploadId
     */
    private String uploadId;

    /**
     * 文件MD5值
     */
    private String fileIdentifier;

    /**
     * 原始文件名称
     */
    private String originalName;

    /**
     * 访问URL
     */
    private String accessUrl;

    /**
     * 下载URL
     */
    private String downloadUrl;

}
