package cn.czh.service;

import cn.czh.base.BusinessException;
import cn.czh.entity.StorageConfig;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;

@Service
public class StorageServiceFactory {

    @Resource(name = "minioStorageService")
    private IStorageService minioStorageService;

    @Resource(name = "localStorageService")
    private IStorageService localStorageService;

    @Resource(name = "ossStorageService")
    private IStorageService ossStorageService;

    /**
     * 根据存储类型获取对应的存储服务
     * @param storageType 存储类型
     * @return 对应的存储服务实例
     */
    public IStorageService getStorageService(String storageType) {
        switch (storageType) {
            case StorageConfig.MINIO:
                return minioStorageService;
            case StorageConfig.OSS:
                return ossStorageService;
            case StorageConfig.LOCAL:
                return localStorageService;
            default:
                throw new BusinessException("不支持的存储类型");
        }
    }
}