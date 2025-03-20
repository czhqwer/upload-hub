package cn.czh.controller;

import cn.czh.base.Result;
import cn.czh.entity.StorageConfig;
import cn.czh.service.IStorageConfigService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;

@RestController
@RequestMapping("/config")
public class ConfigController {

    @Resource
    private IStorageConfigService storageConfigService;

    @GetMapping
    public Result<?> getConfig(String type) {
        return Result.success(storageConfigService.getStorageConfigByType(type));
    }

    @PatchMapping
    public Result<?> updateConfig(@Valid @RequestBody StorageConfig config) {
        storageConfigService.updateStorageConfig(config);
        return Result.success();
    }

}
