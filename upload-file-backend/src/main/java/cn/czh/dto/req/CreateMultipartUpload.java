package cn.czh.dto.req;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class CreateMultipartUpload {

    @NotBlank(message = "文件标识不能为空")
    private String identifier;

    @NotBlank(message = "文件名称不能为空")
    private String fileName;

    @NotNull(message = "文件大小不能为空")
    private Long totalSize;

    @NotNull(message = "分片大小不能为空")
    private Long chunkSize;

    @NotBlank(message = "文件类型不能为空")
    private String contentType;

    private String folder;

}