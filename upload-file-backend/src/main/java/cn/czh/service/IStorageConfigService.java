package cn.czh.service;

import cn.czh.entity.StorageConfig;

public interface IStorageConfigService {

    /**
     * 根据type获取存储配置
     */
    StorageConfig getStorageConfigByType(String type);

    /**
     * 更新存储配置
     */
    void updateStorageConfig(StorageConfig config);

    /**
     * 测试存储配置
     */
    void testStorageConfig(StorageConfig config);

}
