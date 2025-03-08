package cn.czh.service;

import cn.czh.entity.UploadFile;
import com.baomidou.mybatisplus.core.metadata.IPage;

public interface IFileService {

    IPage<UploadFile> pageFiles(Integer page, Integer pageSize, String storageType, String fileName);

}
