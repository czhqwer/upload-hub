package cn.czh.service.impl;

import cn.czh.base.BusinessException;
import cn.czh.context.StorageConfigUpdateEvent;
import cn.czh.dto.FileRecordDTO;
import cn.czh.dto.MyPartSummary;
import cn.czh.dto.TaskInfoDTO;
import cn.czh.dto.TaskRecordDTO;
import cn.czh.dto.aws.LocalPartSummary;
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
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service("localStorageService")
@Slf4j
public class LocalStorageService implements IStorageService {

    @Resource
    private UploadFileMapper uploadFileMapper;
    @Resource
    private IStorageConfigService storageConfigService;
    @Resource
    private IFileService fileService;

    private StorageConfig storageConfig;

    @EventListener(ApplicationReadyEvent.class)
    public void init() {
        this.refreshConfig();
    }

    private void refreshConfig() {
        this.storageConfig = storageConfigService.getStorageConfigByType(StorageConfig.LOCAL);
        File rootDir = new File(storageConfig.getBucket());
        if (!rootDir.exists()) {
            if (!rootDir.mkdirs()) {
                throw new BusinessException("无法创建根目录");
            }
        }
        log.info("LocalStorageService refreshed successfully");
    }

    @EventListener
    public void handleConfigUpdate(StorageConfigUpdateEvent event) {
        if (StorageConfig.LOCAL.equals(event.getNewConfig().getType())) {
            refreshConfig();
        }
    }

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

        String filePath = generateFilePath(objectName);
        File dest = new File(filePath);
        if (!dest.getParentFile().exists()) {
            boolean mkdirs = dest.getParentFile().mkdirs();
            if (!mkdirs) {
                throw new BusinessException("无法创建文件目录");
            }
        }

        try {
            file.transferTo(dest);

            uploadFile = new UploadFile();
            uploadFile.setUploadId(md5);
            uploadFile.setFileName(file.getOriginalFilename());
            uploadFile.setObjectKey(objectName);
            uploadFile.setContentType(file.getContentType());
            uploadFile.setAccessUrl(generateWebUrl(objectName));
            uploadFile.setDownloadUrl(generateWebUrl(objectName));
            uploadFile.setIsFinish(1);
            uploadFile.setTotalSize(file.getSize());
            uploadFile.setChunkNum(1);
            uploadFile.setChunkSize(file.getSize());
            uploadFile.setFileIdentifier(md5);
            uploadFile.setStorageType(StorageConfig.LOCAL);
            uploadFile.setCreateTime(DateUtil.format(new Date(), "yyyy-MM-dd HH:mm:ss"));
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

    @Override
    public TaskInfoDTO getUploadProgress(String identifier) {
        UploadFile task = getByIdentifier(identifier);
        if (task == null) {
            return null;
        }

        TaskInfoDTO result = new TaskInfoDTO()
                .setFinished(task.getIsFinish() == 1)
                .setTaskRecord(TaskRecordDTO.convertFromEntity(task))
                .setPath(generateWebUrl(task.getObjectKey()));

        if (task.getIsFinish() == 0) {
            List<MyPartSummary> uploadedParts = getUploadedParts(task.getObjectKey());
            result.setFinished(false).getTaskRecord().setExitPartList(uploadedParts);
        }
        return result;
    }

    @Transactional
    @Override
    public TaskInfoDTO createMultipartUpload(CreateMultipartUpload param) {
        String identifier = param.getIdentifier();
        String fileName = param.getFileName();
        String objectKey = generateObjectKey(param.getFolder(), fileName);

        UploadFile uploadFile = new UploadFile();
        int chunkNum = (int) Math.ceil(param.getTotalSize() * 1.0 / param.getChunkSize());
        uploadFile.setChunkNum(chunkNum)
                .setUploadId(identifier)
                .setChunkSize(param.getChunkSize())
                .setTotalSize(param.getTotalSize())
                .setFileIdentifier(identifier)
                .setFileName(fileName)
                .setObjectKey(objectKey)
                .setIsFinish(0)
                .setStorageType(StorageConfig.LOCAL)
                .setCreateTime(DateUtil.format(new Date(), "yyyy-MM-dd HH:mm:ss"))
                .setContentType(param.getContentType());
        uploadFileMapper.insert(uploadFile);

        String tempDir = getTempDir(objectKey);
        boolean mkdirs = new File(tempDir).mkdirs();
        if (!mkdirs) {
            throw new BusinessException("无法创建临时目录");
        }

        return new TaskInfoDTO()
                .setFinished(false)
                .setTaskRecord(TaskRecordDTO.convertFromEntity(uploadFile))
                .setPath(generateWebUrl(objectKey));
    }

    @Transactional
    @Override
    public UploadFile merge(String identifier, Boolean admin) {
        UploadFile uploadFile = getByIdentifier(identifier);
        if (uploadFile == null) {
            throw new BusinessException("上传任务不存在");
        }

        String tempDir = getTempDir(uploadFile.getObjectKey());
        File[] parts = new File(tempDir).listFiles();
        if (parts == null || parts.length != uploadFile.getChunkNum()) {
            throw new BusinessException("缺少分块");
        }

        String filePath = generateFilePath(uploadFile.getObjectKey());
        File dest = new File(filePath);
        if (!dest.getParentFile().exists()) {
            boolean mkdirs = dest.getParentFile().mkdirs();
            if (!mkdirs) {
                throw new BusinessException("无法创建根目录");
            }
        }

        try (FileOutputStream fos = new FileOutputStream(dest)) {
            for (int i = 1; i <= uploadFile.getChunkNum(); i++) {
                File partFile = new File(tempDir, String.valueOf(i));
                try (FileInputStream fis = new FileInputStream(partFile)) {
                    IOUtils.copy(fis, fos);
                }
                boolean delete = partFile.delete();
                if (!delete) {
                    log.warn("无法删除块文件: {}", partFile.getAbsolutePath());
                }
            }
        } catch (IOException e) {
            throw new BusinessException("文件合并失败", e);
        }

        boolean delete = new File(tempDir).delete();
        if (!delete) {
            log.warn("无法删除临时目录: {}", tempDir);
        }

        uploadFile.setAccessUrl(generateWebUrl(uploadFile.getObjectKey()));
        uploadFile.setDownloadUrl(generateWebUrl(uploadFile.getObjectKey()));
        uploadFile.setIsFinish(1);
        uploadFileMapper.updateById(uploadFile);

        if (!admin) {
            fileService.addSharedFile(uploadFile.getFileIdentifier());
        }
        return uploadFile;
    }

    @Override
    public UploadFile getByIdentifier(String identifier) {
        return uploadFileMapper.selectOne(
                new LambdaQueryWrapper<UploadFile>()
                        .eq(UploadFile::getFileIdentifier, identifier)
        );
    }

    @Override
    public String genPreSignUploadUrl(String objectKey, Map<String, String> params) {
        throw new UnsupportedOperationException("本地存储不支持预签名的URL");
    }

    public void uploadPart(String uploadId, Integer partNumber, MultipartFile partFile) {
        UploadFile uploadFile = getByIdentifier(uploadId);
        if (uploadFile == null) {
            throw new BusinessException("上传任务不存在");
        }

        String tempDir = getTempDir(uploadFile.getObjectKey());
        File partDest = new File(tempDir, partNumber.toString());
        if (!partDest.getParentFile().exists()) {
            boolean mkdirs = partDest.getParentFile().mkdirs();
            if (!mkdirs) {
                throw new BusinessException("无法创建临时目录");
            }
        }

        try {
            partFile.transferTo(partDest);
        } catch (IOException e) {
            throw new BusinessException("分片上传失败", e);
        }
    }

    private String generateFilePath(String objectKey) {
        return storageConfig.getBucket() + File.separator + objectKey.replace("/", File.separator);
    }

    private String getTempDir(String objectKey) {
        return storageConfig.getBucket() + File.separator + "temp" + File.separator + objectKey.replace("/", File.separator);
    }

    private String generateObjectKey(String folder, String fileName) {
        String suffix = fileName.substring(fileName.lastIndexOf("."));
        return StrUtil.format("{}/{}/{}", folder, DateUtil.format(new Date(), DatePattern.PURE_DATE_PATTERN), IdUtil.randomUUID() + suffix);
    }

    private List<MyPartSummary> getUploadedParts(String objectKey) {
        String tempDir = getTempDir(objectKey);
        File[] files = new File(tempDir).listFiles();
        if (files == null) {
            return Collections.emptyList();
        }
        return Arrays.stream(files)
                .map(file -> (MyPartSummary) new LocalPartSummary(
                        Integer.parseInt(file.getName()), file.getName(), file.length(), new Date()
                ))
                .collect(Collectors.toList());
    }

    private String generateWebUrl(String objectKey) {
        return "http://localhost:10086/file/preview/local/" + objectKey;
    }
}