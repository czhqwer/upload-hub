package cn.czh.service.impl;

import cn.czh.entity.SharedFile;
import cn.czh.entity.UploadFile;
import cn.czh.mapper.ShareFileMapper;
import cn.czh.mapper.UploadFileMapper;
import cn.czh.service.IFileService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class FileServiceImpl implements IFileService {

    @Resource
    private UploadFileMapper uploadFileMapper;
    @Resource
    private ShareFileMapper shareFileMapper;

    @Override
    public IPage<UploadFile> pageFiles(Integer page, Integer pageSize, String storageType, String fileName) {
        return uploadFileMapper.pageFiles(new Page<>(page, pageSize), storageType, fileName);
    }

    @Override
    public List<UploadFile> listSharedFiles() {

        List<SharedFile> sharedFiles = shareFileMapper.selectList(null);
        Set<String> files = sharedFiles.stream()
                .map(SharedFile::getFileIdentifier)
                .collect(Collectors.toSet());

        List<UploadFile> uploadFiles = uploadFileMapper.selectList(null);
        if (uploadFiles != null) {
            return uploadFiles.stream()
                    .filter(uploadFile -> files.contains(uploadFile.getFileIdentifier()))
                    .collect(Collectors.toList());
        }

        return Collections.emptyList();
    }

    @Override
    public void addSharedFile(String identifier) {
        SharedFile sharedFile = shareFileMapper.selectOne(
                Wrappers.lambdaQuery(SharedFile.class).eq(SharedFile::getFileIdentifier, identifier)
        );

        if (sharedFile == null) {
            sharedFile = new SharedFile();
            sharedFile.setFileIdentifier(identifier);
            sharedFile.setCreateTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            shareFileMapper.insert(sharedFile);
        }
    }

    @Override
    public void removeSharedFile(String fileIdentifier) {
        shareFileMapper.delete(Wrappers.lambdaQuery(SharedFile.class).eq(SharedFile::getFileIdentifier, fileIdentifier));
    }
}
