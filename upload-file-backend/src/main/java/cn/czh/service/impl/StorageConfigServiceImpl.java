package cn.czh.service.impl;

import cn.czh.base.BusinessException;
import cn.czh.context.StorageConfigUpdateEvent;
import cn.czh.entity.StorageConfig;
import cn.czh.mapper.StorageConfigMapper;
import cn.czh.service.IStorageConfigService;
import cn.hutool.core.util.StrUtil;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.minio.BucketExistsArgs;
import io.minio.MinioClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Slf4j
@Service
public class StorageConfigServiceImpl extends ServiceImpl<StorageConfigMapper, StorageConfig> implements IStorageConfigService {

    @Resource
    private ApplicationEventPublisher eventPublisher;

    @Override
    public StorageConfig getStorageConfigByType(String type) {
        LambdaQueryWrapper<StorageConfig> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(StorageConfig::getType, type);
        StorageConfig config = getOne(queryWrapper);
        if (config == null) {
            throw new BusinessException("未配置存储配置");
        }
        if (!StorageConfig.LOCAL.equals(config.getType())) {
            if (StrUtil.isBlank(config.getSecretKey()) || StrUtil.isBlank(config.getAccessKey()) || StrUtil.isBlank(config.getEndpoint())) {
                throw new BusinessException("存储配置错误");
            }
        }
        return config;
    }

    @Transactional
    @Override
    public void updateStorageConfig(StorageConfig config) {
        super.updateById(config);
        eventPublisher.publishEvent(new StorageConfigUpdateEvent(this, config)); // 发布事件
    }

    @Override
    public void testStorageConfig(StorageConfig config) {
        switch (config.getType().toLowerCase()) {
            case StorageConfig.LOCAL:
                testLocalStorage(config);
                break;
            case StorageConfig.MINIO:
                testMinioStorage(config);
                break;
            case StorageConfig.OSS:
                testOSSStorage(config);
                break;
            default:
                throw new IllegalArgumentException("不支持的存储类型: " + config.getType());
        }
    }

    private void testLocalStorage(StorageConfig config) {
        Path path = Paths.get(config.getBucket());

        if (!Files.exists(path)) {
            try {
                Files.createDirectories(path);
            } catch (IOException e) {
                throw new BusinessException("无法创建目录: " + e.getMessage());
            }
        }

        if (!Files.isDirectory(path)) {
            throw new BusinessException("指定路径不是目录: " + config.getBucket());
        }

        if (!Files.isWritable(path)) {
            throw new BusinessException("路径不可写: " + config.getBucket());
        }

        Path testFile = path.resolve("test.txt");
        try {
            Files.write(testFile, "test".getBytes());
            Files.delete(testFile);
        } catch (IOException e) {
            throw new BusinessException("读写测试失败: " + e.getMessage());
        }
    }

    private void testMinioStorage(StorageConfig config) {
        if (config.getAccessKey() == null || config.getSecretKey() == null) {
            throw new BusinessException("MinIO 需要提供 AccessKey 和 SecretKey");
        }

        MinioClient minioClient = MinioClient.builder()
                .endpoint(config.getEndpoint())
                .credentials(config.getAccessKey(), config.getSecretKey())
                .build();

        try {
            BucketExistsArgs args = BucketExistsArgs.builder().bucket(config.getBucket()).build();
            boolean exists = minioClient.bucketExists(args);
            if (!exists) {
                throw new BusinessException("Bucket 不存在: " + config.getBucket());
            }
        } catch (Exception e) {
            throw new BusinessException("MinIO 测试失败: " + e.getMessage());
        }
    }

    private void testOSSStorage(StorageConfig config) {
        if (config.getAccessKey() == null || config.getSecretKey() == null) {
            throw new BusinessException("OSS 需要提供 AccessKey 和 SecretKey");
        }

        OSS ossClient = new OSSClientBuilder()
                .build(config.getEndpoint(), config.getAccessKey(), config.getSecretKey());

        try {
            ossClient.listBuckets();
        } catch (Exception e) {
            throw new BusinessException("OSS 测试失败: " + e.getMessage());
        } finally {
            ossClient.shutdown();
        }
    }
    
}
