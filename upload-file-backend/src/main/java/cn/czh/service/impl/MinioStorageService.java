package cn.czh.service.impl;

import cn.czh.base.BusinessException;
import cn.czh.context.StorageConfigUpdateEvent;
import cn.czh.dto.FileRecordDTO;
import cn.czh.dto.MyPartSummary;
import cn.czh.dto.TaskInfoDTO;
import cn.czh.dto.TaskRecordDTO;
import cn.czh.dto.aws.AwsPartSummaryWrapper;
import cn.czh.dto.req.CreateMultipartUpload;
import cn.czh.entity.StorageConfig;
import cn.czh.entity.UploadFile;
import cn.czh.mapper.UploadFileMapper;
import cn.czh.service.IStorageConfigService;
import cn.czh.service.IStorageService;
import cn.czh.utils.ImgUtils;
import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.amazonaws.ClientConfiguration;
import com.amazonaws.HttpMethod;
import com.amazonaws.Protocol;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.*;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.minio.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.context.event.EventListener;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service("minioStorageService")
@Slf4j
public class MinioStorageService implements IStorageService {

    @Resource
    private UploadFileMapper uploadFileMapper;
    @Resource
    private IStorageConfigService storageConfigService;

    /**
     * 预签名url过期时间: 10分钟
     */
    private static final Integer PRE_SIGN_URL_EXPIRE = 10 * 60 * 1000;
    /**
     * 下载URL拼接前缀 TODO 文件下载功能
     */
    private static final String DOWNLOAD_URL = "/downloadFile?objectName=%s";
    /**
     * 后端分片上传到Minio服务器分片大小
     */
    public static final long PART_MAX_SIZE = 5 * 1024 * 1024;

    private StorageConfig storageConfig;
    private MinioClient minioClient;
    private AmazonS3 amazonS3;

    @PostConstruct
    public void init() {
        this.refreshConfig();
    }

    private void refreshConfig() {
        this.storageConfig = storageConfigService.getStorageConfigByType(StorageConfig.MINIO);
        this.minioClient = MinioClient.builder()
                .endpoint(storageConfig.getEndpoint())
                .credentials(storageConfig.getAccessKey(), storageConfig.getSecretKey())
                .build();
        this.amazonS3 = minioAmazonS3Client(storageConfig);
        log.info("MinioStorageService refreshed successfully");
    }

    @EventListener
    public void handleConfigUpdate(StorageConfigUpdateEvent event) {
        if (StorageConfig.MINIO.equals(event.getNewConfig().getType())) {
            refreshConfig();
        }
    }

    private AmazonS3 minioAmazonS3Client(StorageConfig storageConfig) {
        // 设置连接时的参数
        ClientConfiguration config = new ClientConfiguration();
        // 设置连接方式为HTTP，可选参数为HTTP和HTTPS
        config.setProtocol(Protocol.HTTP);
        // 设置网络访问超时时间
        config.setConnectionTimeout(5000);
        config.setUseExpectContinue(true);
        AWSCredentials credentials = new BasicAWSCredentials(storageConfig.getAccessKey(), storageConfig.getSecretKey());
        // 设置Endpoint
        AwsClientBuilder.EndpointConfiguration endPoint = new AwsClientBuilder.EndpointConfiguration(storageConfig.getEndpoint(), Regions.US_EAST_1.name());
        return AmazonS3ClientBuilder.standard()
                .withClientConfiguration(config)
                .withCredentials(new AWSStaticCredentialsProvider(credentials))
                .withEndpointConfiguration(endPoint)
                .withPathStyleAccessEnabled(true)
                .build();
    }

    @Transactional
    @Override
    public FileRecordDTO uploadFile(MultipartFile file, String md5, String objectName) {

        UploadFile uploadFile = getByIdentifier(md5);
        if (ObjectUtil.isNotNull(uploadFile)) {
            FileRecordDTO fileRecord = new FileRecordDTO();
            BeanUtils.copyProperties(uploadFile, fileRecord);
            fileRecord.setFileId(uploadFile.getId());
            fileRecord.setOriginalName(uploadFile.getFileName());
            return fileRecord;

        }
        try {
            // 检查桶是否存在，如果不存在则创建
            boolean bucketExists = minioClient.bucketExists(BucketExistsArgs.builder().bucket(storageConfig.getBucket()).build());
            if (!bucketExists) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(storageConfig.getBucket()).build());
            }
            String filename = file.getOriginalFilename();

            PutObjectArgs putObjectArgs = getPutObjectArgs(file, objectName);
            minioClient.putObject(putObjectArgs);

            uploadFile = new UploadFile();
            uploadFile.setUploadId(getUploadId(objectName));
            uploadFile.setFileName(filename);
            uploadFile.setObjectKey(objectName);
            uploadFile.setContentType(file.getContentType());
            uploadFile.setAccessUrl(getFileAccessUrl(objectName));
            uploadFile.setDownloadUrl(getFileDownloadUrl(objectName));
            uploadFile.setCreateTime(new Date());
            uploadFile.setIsFinish(1);
            uploadFile.setTotalSize(file.getSize());
            uploadFile.setChunkNum(1);
            uploadFile.setChunkSize(file.getSize());
            uploadFile.setFileIdentifier(md5);
            uploadFile.setStorageType(StorageConfig.MINIO);
            uploadFileMapper.insert(uploadFile);

            FileRecordDTO fileRecord = new FileRecordDTO();
            BeanUtils.copyProperties(uploadFile, fileRecord);
            fileRecord.setFileId(uploadFile.getId());
            fileRecord.setOriginalName(filename);
            return fileRecord;
        } catch (Exception e) {
            throw new BusinessException("文件上上传失败", e);
        }
    }

    @Override
    public TaskInfoDTO getUploadProgress(String identifier) {
        UploadFile task = getByIdentifier(identifier);
        if (task == null) {
            return null;
        }
        TaskInfoDTO result = new TaskInfoDTO()
                .setFinished(true)
                .setTaskRecord(TaskRecordDTO.convertFromEntity(task))
                .setPath(getPath(task.getObjectKey()));

        boolean doesObjectExist = amazonS3.doesObjectExist(storageConfig.getBucket(), task.getObjectKey());
        if (!doesObjectExist) {
            // 未上传完，返回已上传的分片
            ListPartsRequest listPartsRequest = new ListPartsRequest(storageConfig.getBucket(), task.getObjectKey(), task.getUploadId());
            PartListing partListing = amazonS3.listParts(listPartsRequest);
            List<MyPartSummary> awsPartSummary = partListing.getParts().stream().map(AwsPartSummaryWrapper::new).collect(Collectors.toList());
            result.setFinished(false).getTaskRecord().setExitPartList(awsPartSummary);
        }
        return result;
    }

    @Transactional
    @Override
    public TaskInfoDTO createMultipartUpload(CreateMultipartUpload param) {
        Date currentDate = new Date();
        String fileName = param.getFileName();
        String suffix = fileName.substring(fileName.lastIndexOf(".") + 1);
        String key = StrUtil.format("{}/{}/{}.{}", param.getFolder(), DateUtil.format(currentDate, DatePattern.PURE_DATE_PATTERN), IdUtil.randomUUID(), suffix);

        UploadFile task = new UploadFile();
        int chunkNum = (int) Math.ceil(param.getTotalSize() * 1.0 / param.getChunkSize());
        task.setChunkNum(chunkNum)
                .setChunkSize(param.getChunkSize())
                .setTotalSize(param.getTotalSize())
                .setFileIdentifier(param.getIdentifier())
                .setFileName(fileName)
                .setObjectKey(key)
                .setIsFinish(0)
                .setCreateTime(currentDate)
                .setContentType(param.getContentType())
                .setStorageType(StorageConfig.MINIO)
                .setUploadId(getUploadId(key));
        uploadFileMapper.insert(task);
        return new TaskInfoDTO().setFinished(false).setTaskRecord(TaskRecordDTO.convertFromEntity(task)).setPath(getPath(key));
    }

    @Transactional
    @Override
    public UploadFile merge(String identifier) {
        UploadFile uploadFile = getByIdentifier(identifier);
        if (uploadFile == null) {
            throw new BusinessException("上传任务不存在");
        }

        ListPartsRequest listPartsRequest = new ListPartsRequest(storageConfig.getBucket(), uploadFile.getObjectKey(), uploadFile.getUploadId());
        PartListing partListing = amazonS3.listParts(listPartsRequest);
        List<PartSummary> parts = partListing.getParts();
        if (!uploadFile.getChunkNum().equals(parts.size())) {
            // 已上传分块数量与记录中的数量不对应，不能合并分块
            throw new BusinessException("分片缺失");
        }
        CompleteMultipartUploadRequest completeMultipartUploadRequest = new CompleteMultipartUploadRequest()
                .withUploadId(uploadFile.getUploadId())
                .withKey(uploadFile.getObjectKey())
                .withBucketName(storageConfig.getBucket())
                .withPartETags(parts.stream().map(partSummary -> new PartETag(partSummary.getPartNumber(), partSummary.getETag())).collect(Collectors.toList()));
        amazonS3.completeMultipartUpload(completeMultipartUploadRequest);

        // 更新数据状态
        uploadFile.setAccessUrl(getFileAccessUrl(uploadFile.getObjectKey()));
        uploadFile.setDownloadUrl(getFileDownloadUrl(uploadFile.getObjectKey()));
        uploadFile.setIsFinish(1);
        uploadFileMapper.updateById(uploadFile);
        return uploadFile;
    }

    @Override
    public UploadFile getByIdentifier(String identifier) {
        return uploadFileMapper.selectOne(new LambdaQueryWrapper<UploadFile>().eq(UploadFile::getFileIdentifier, identifier));
    }

    @Override
    public String genPreSignUploadUrl(String objectKey, Map<String, String> params) {
        Date currentDate = new Date();
        Date expireDate = DateUtil.offsetMillisecond(currentDate, PRE_SIGN_URL_EXPIRE);
        GeneratePresignedUrlRequest request = new GeneratePresignedUrlRequest(storageConfig.getBucket(), objectKey)
                .withExpiration(expireDate).withMethod(HttpMethod.PUT);
        if (params != null) {
            params.forEach(request::addRequestParameter);
        }
        URL preSignedUrl = amazonS3.generatePresignedUrl(request);
        return preSignedUrl.toString();
    }

    @Override
    public void uploadPart(String uploadId, Integer partNumber, MultipartFile partFile) {
        // MinIO 通过前端使用预签名 URL 直接上传分片到 MinIO，服务器端无需处理
    }

    /**
     * 获取文件路径
     */
    private String getPath(String objectKey) {
        return StrUtil.format("{}/{}/{}", storageConfig.getEndpoint(), storageConfig.getBucket(), objectKey);
    }

    private String getUploadId(String key) {
        String contentType = MediaTypeFactory.getMediaType(key).orElse(MediaType.APPLICATION_OCTET_STREAM).toString();
        ObjectMetadata objectMetadata = new ObjectMetadata();
        objectMetadata.setContentType(contentType);
        InitiateMultipartUploadResult initiateMultipartUploadResult = amazonS3
                .initiateMultipartUpload(new InitiateMultipartUploadRequest(storageConfig.getBucket(), key).withObjectMetadata(objectMetadata));
        return initiateMultipartUploadResult.getUploadId();
    }


    /**
     * 获取文件访问URL
     */
    private String getFileAccessUrl(String objectName) {
        return storageConfig.getEndpoint() + "/" +  storageConfig.getBucket() + "/" + objectName;
    }

    /**
     * 获取文件下载URL
     */
    private String getFileDownloadUrl(String objectName) {
        return String.format(DOWNLOAD_URL, objectName);
    }

    /**
     * 设置文件对象
     */
    private PutObjectArgs getPutObjectArgs(MultipartFile file, String objectName) throws IOException {
        InputStream stream = file.getInputStream();
        String fileContentType = ImgUtils.getFileContentType(objectName);
        return PutObjectArgs.builder()
                .object(objectName)
                .bucket(storageConfig.getBucket())
                .contentType(Objects.isNull(fileContentType) ? file.getContentType() : fileContentType)
                .stream(stream, stream.available(), PART_MAX_SIZE).build();
    }

    private PutObjectArgs getPutObjectArgs(ByteArrayInputStream bais, String objectName) {
        return PutObjectArgs.builder()
                .bucket(storageConfig.getBucket())
                .object(objectName)
                .stream(bais, bais.available(), -1)
                .contentType("image/jpeg")
                .build();
    }
}

