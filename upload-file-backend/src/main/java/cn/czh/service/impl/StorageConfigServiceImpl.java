package cn.czh.service.impl;

import cn.czh.base.BusinessException;
import cn.czh.entity.StorageConfig;
import cn.czh.mapper.StorageConfigMapper;
import cn.czh.service.IStorageConfigService;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;


@Service
public class StorageConfigServiceImpl extends ServiceImpl<StorageConfigMapper, StorageConfig> implements IStorageConfigService {
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
}
