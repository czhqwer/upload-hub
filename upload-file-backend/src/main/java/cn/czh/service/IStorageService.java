package cn.czh.service;

import cn.czh.dto.FileRecordDTO;
import cn.czh.dto.TaskInfoDTO;
import cn.czh.dto.req.CreateMultipartUpload;
import cn.czh.entity.UploadFile;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.Map;

public interface IStorageService {

    /**
     * 普通文件上传
     */
    FileRecordDTO uploadFile(MultipartFile file, String md5, String objectName, Boolean admin);

    /**
     * 获取文件上传进度
     */
    TaskInfoDTO getUploadProgress(String identifier);

    /**
     * 发起分片上传
     */
    TaskInfoDTO createMultipartUpload(CreateMultipartUpload req);

    /**
     * 合并分片文件
     */
    UploadFile merge(String identifier, Boolean admin);

    /**
     * 根据md5标识获取分片上传任务
     */
    UploadFile getByIdentifier(String identifier);

    /**
     * 生成预签名上传url
     */
    String genPreSignUploadUrl (String objectKey, Map<String, String> params);

    /**
     * 上传分片文件
     */
    void uploadPart(String uploadId, Integer partNumber, MultipartFile partFile);

}