package cn.czh.service.impl;

import cn.czh.entity.UploadFile;
import cn.czh.mapper.UploadFileMapper;
import cn.czh.service.IFileService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class FileServiceImpl implements IFileService {

    @Resource
    private UploadFileMapper uploadFileMapper;

    @Override
    public IPage<UploadFile> pageFiles(Integer page, Integer pageSize, String storageType, String fileName) {
        return uploadFileMapper.pageFiles(new Page<>(page, pageSize), storageType, fileName);
    }
}
