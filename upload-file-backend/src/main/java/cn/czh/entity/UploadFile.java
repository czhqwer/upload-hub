package cn.czh.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;

@NoArgsConstructor
@Data
@Accessors(chain = true)
@TableName("upload_file")
public class UploadFile implements Serializable {

    /**
     * 主键
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    /**
     * 分片上传的uploadId
     */
    private String uploadId;
    /**
     * 文件唯一标识（md5）
     */
    private String fileIdentifier;
    /**
     * 文件名
     */
    private String fileName;
    /**
     * 文件的key
     */
    private String objectKey;
    /**
     * 文件大小（byte）
     */
    private Long totalSize;
    /**
     * 每个分片大小（byte）
     */
    private Long chunkSize;
    /**
     * 分片数量
     */
    private Integer chunkNum;
    /**
     * 是否已完成上传(完成合并),1是0否
     */
    private Integer isFinish;
    /**
     * 文件类型
     */
    private String contentType;
    /**
     * 访问地址
     */
    private String accessUrl;
    /**
     * 下载地址
     */
    private String downloadUrl;
    /**
     * 存储类型
     */
    private String storageType;
    /**
     * 创建时间
     */
    private String createTime;

}