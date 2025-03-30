package cn.czh.service.impl;

import cn.czh.base.BusinessException;
import cn.czh.context.StorageConfigUpdateEvent;
import cn.czh.dto.FileRecordDTO;
import cn.czh.dto.MyPartSummary;
import cn.czh.dto.TaskInfoDTO;
import cn.czh.dto.TaskRecordDTO;
import cn.czh.dto.aws.OssPartSummaryWrapper;
import cn.czh.dto.req.CreateMultipartUpload;
import cn.czh.entity.StorageConfig;
import cn.czh.entity.UploadFile;
import cn.czh.mapper.UploadFileMapper;
import cn.czh.service.IFileService;
import cn.czh.service.IStorageConfigService;
import cn.czh.service.IStorageService;
import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.*;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 阿里云 OSS 文件上传服务
 */
@Service("ossStorageService")
@Slf4j
public class OssStorageService implements IStorageService {

    @Resource
    private UploadFileMapper uploadFileMapper;
    @Resource
    private IStorageConfigService storageConfigService;
    @Resource
    private IFileService fileService;

    private StorageConfig storageConfig;
    private OSS ossClient;

    /** 预签名 URL 过期时间：10 分钟 */
    private static final long PRE_SIGN_URL_EXPIRE = 10 * 60 * 1000;

    @EventListener(ApplicationReadyEvent.class)
    public void init() {
        this.refreshConfig();
    }

    private void refreshConfig() {
        this.storageConfig = storageConfigService.getStorageConfigByType(StorageConfig.OSS);
        this.ossClient = new OSSClientBuilder().build(
                storageConfig.getEndpoint(),
                storageConfig.getAccessKey(),
                storageConfig.getSecretKey()
        );
        log.info("OssStorageService refreshed successfully");
    }

    @EventListener
    public void handleConfigUpdate(StorageConfigUpdateEvent event) {
        if (StorageConfig.OSS.equals(event.getNewConfig().getType())) {
            refreshConfig();
        }
    }

    /**
     * 上传单个文件
     */
    @Transactional
    @Override
    public FileRecordDTO uploadFile(MultipartFile file, String md5, String objectName, Boolean admin) {
        UploadFile uploadFile = getByIdentifier(md5);
        if (uploadFile != null) {
            FileRecordDTO fileRecord = new FileRecordDTO();
            BeanUtils.copyProperties(uploadFile, fileRecord);
            fileRecord.setFileId(uploadFile.getId());
            fileRecord.setOriginalName(uploadFile.getFileName());
            return fileRecord;
        }

        try {
            InputStream inputStream = file.getInputStream();
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentType(file.getContentType());
            metadata.setContentLength(file.getSize());

            // 上传文件到 OSS
            PutObjectRequest putObjectRequest = new PutObjectRequest(
                    storageConfig.getBucket(),
                    objectName,
                    inputStream,
                    metadata
            );
            ossClient.putObject(putObjectRequest);

            // 记录上传信息到数据库
            uploadFile = new UploadFile();
            uploadFile.setUploadId(md5);
            uploadFile.setFileName(file.getOriginalFilename());
            uploadFile.setObjectKey(objectName);
            uploadFile.setContentType(file.getContentType());
            uploadFile.setAccessUrl(getFileAccessUrl(objectName));
            uploadFile.setDownloadUrl(getFileDownloadUrl(objectName));
            uploadFile.setIsFinish(1);
            uploadFile.setTotalSize(file.getSize());
            uploadFile.setChunkNum(1);
            uploadFile.setChunkSize(file.getSize());
            uploadFile.setFileIdentifier(md5);
            uploadFile.setCreateTime(DateUtil.format(new Date(), "yyyy-MM-dd HH:mm:ss"));
            uploadFile.setStorageType(StorageConfig.OSS);
            uploadFileMapper.insert(uploadFile);

            FileRecordDTO fileRecord = new FileRecordDTO();
            BeanUtils.copyProperties(uploadFile, fileRecord);
            fileRecord.setFileId(uploadFile.getId());
            fileRecord.setOriginalName(file.getOriginalFilename());

            if (!admin) {
                fileService.addSharedFile(md5);
            }
            return fileRecord;
        } catch (IOException e) {
            throw new BusinessException("文件上传失败", e);
        }
    }

    /**
     * 获取上传进度
     */
    @Override
    public TaskInfoDTO getUploadProgress(String identifier) {
        UploadFile task = getByIdentifier(identifier);
        if (task == null) {
            return null;
        }

        TaskInfoDTO result = new TaskInfoDTO()
                .setFinished(task.getIsFinish() == 1)
                .setTaskRecord(TaskRecordDTO.convertFromEntity(task))
                .setPath(getPath(task.getObjectKey()));

        if (task.getIsFinish() == 0) {
            // 获取已上传的分片
            ListPartsRequest listPartsRequest = new ListPartsRequest(
                    storageConfig.getBucket(),
                    task.getObjectKey(),
                    task.getUploadId()
            );
            PartListing partListing = ossClient.listParts(listPartsRequest);
            List<MyPartSummary> partList = partListing.getParts().stream()
                    .map(OssPartSummaryWrapper::new)
                    .collect(Collectors.toList());
            result.setFinished(false).getTaskRecord().setExitPartList(partList);
        }
        return result;
    }

    /**
     * 创建分片上传任务
     */
    @Transactional
    @Override
    public TaskInfoDTO createMultipartUpload(CreateMultipartUpload param) {
        String identifier = param.getIdentifier();
        String fileName = param.getFileName();
        String objectKey = generateObjectKey(param.getFolder(), fileName);

        // 初始化分片上传
        InitiateMultipartUploadRequest initiateRequest = new InitiateMultipartUploadRequest(
                storageConfig.getBucket(),
                objectKey
        );
        InitiateMultipartUploadResult initiateResult = ossClient.initiateMultipartUpload(initiateRequest);
        String uploadId = initiateResult.getUploadId();

        // 记录任务信息
        UploadFile task = new UploadFile();
        int chunkNum = (int) Math.ceil(param.getTotalSize() * 1.0 / param.getChunkSize());
        task.setChunkNum(chunkNum)
                .setUploadId(uploadId)
                .setChunkSize(param.getChunkSize())
                .setTotalSize(param.getTotalSize())
                .setFileIdentifier(identifier)
                .setFileName(fileName)
                .setObjectKey(objectKey)
                .setIsFinish(0)
                .setStorageType(StorageConfig.OSS)
                .setCreateTime(DateUtil.format(new Date(), "yyyy-MM-dd HH:mm:ss"))
                .setContentType(param.getContentType());
        uploadFileMapper.insert(task);

        return new TaskInfoDTO()
                .setFinished(false)
                .setTaskRecord(TaskRecordDTO.convertFromEntity(task))
                .setPath(getPath(objectKey));
    }

    /**
     * 合并分片
     */
    @Transactional
    @Override
    public UploadFile merge(String identifier, Boolean admin) {
        UploadFile uploadFile = getByIdentifier(identifier);
        if (uploadFile == null) {
            throw new BusinessException("上传任务不存在");
        }

        // 检查分片是否完整
        ListPartsRequest listPartsRequest = new ListPartsRequest(
                storageConfig.getBucket(),
                uploadFile.getObjectKey(),
                uploadFile.getUploadId()
        );
        PartListing partListing = ossClient.listParts(listPartsRequest);
        List<PartSummary> parts = partListing.getParts();
        if (parts.size() != uploadFile.getChunkNum()) {
            throw new BusinessException("分片缺失");
        }

        // 合并分片
        CompleteMultipartUploadRequest completeRequest = new CompleteMultipartUploadRequest(
                storageConfig.getBucket(),
                uploadFile.getObjectKey(),
                uploadFile.getUploadId(),
                parts.stream().map(part -> new PartETag(part.getPartNumber(), part.getETag())).collect(Collectors.toList())
        );
        ossClient.completeMultipartUpload(completeRequest);

        // 更新任务状态
        uploadFile.setAccessUrl(getFileAccessUrl(uploadFile.getObjectKey()));
        uploadFile.setDownloadUrl(getFileDownloadUrl(uploadFile.getObjectKey()));
        uploadFile.setIsFinish(1);
        uploadFileMapper.updateById(uploadFile);

        if (!admin) {
            fileService.addSharedFile(identifier);
        }
        return uploadFile;
    }

    /**
     * 根据标识符查询上传任务
     */
    @Override
    public UploadFile getByIdentifier(String identifier) {
        return uploadFileMapper.selectOne(
                new LambdaQueryWrapper<UploadFile>()
                        .eq(UploadFile::getFileIdentifier, identifier)
        );
    }

    /**
     * 生成预签名上传 URL
     */
    @Override
    public String genPreSignUploadUrl(String objectKey, Map<String, String> params) {
        Date expiration = new Date(new Date().getTime() + PRE_SIGN_URL_EXPIRE);
        GeneratePresignedUrlRequest request = new GeneratePresignedUrlRequest(
                storageConfig.getBucket(),
                objectKey
        );
        // 设置 HTTP 方法为 PUT
        request.setMethod(com.aliyun.oss.HttpMethod.PUT);
        // 设置过期时间
        request.setExpiration(expiration);
        request.setContentType("application/octet-stream");
        if (params != null) {
            params.forEach(request::addQueryParameter);
        }
        // 生成预签名 URL
        URL url = ossClient.generatePresignedUrl(request);
        return url.toString();
    }

    /**
     * 上传分片（OSS 使用预签名 URL 上传，服务器端无需实现）
     */
    @Override
    public void uploadPart(String uploadId, Integer partNumber, MultipartFile partFile) {
        // OSS 通过前端使用预签名 URL 直接上传分片到 OSS，服务器端无需处理
    }

    /**
     * 生成对象键（Object Key）
     */
    private String generateObjectKey(String folder, String fileName) {
        String suffix = fileName.substring(fileName.lastIndexOf("."));
        return StrUtil.format("{}/{}/{}", folder, DateUtil.format(new Date(), DatePattern.PURE_DATE_PATTERN), IdUtil.randomUUID() + suffix);
    }

    /**
     * 获取文件路径
     */
    private String getPath(String objectKey) {
        return StrUtil.format("{}/{}/{}", storageConfig.getEndpoint(), storageConfig.getBucket(), objectKey);
    }

    /**
     * 获取文件访问 URL
     */
    private String getFileAccessUrl(String objectName) {
        String endpoint = storageConfig.getEndpoint().replace("https://", "");
        // 构造永久有效的公开 URL
        return String.format("https://%s.%s/%s",
                storageConfig.getBucket(),
                endpoint,
                objectName);
    }

    /**
     * 获取文件下载 URL
     */
    private String getFileDownloadUrl(String objectName) {
        // 自定义下载路径，可根据实际需求调整
        return "/downloadFile?objectName=" + objectName;
    }
}