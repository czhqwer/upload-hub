package cn.czh.service.impl;

import cn.czh.base.BusinessException;
import cn.czh.dto.NotifyMessage;
import cn.czh.entity.SharedFile;
import cn.czh.entity.UploadFile;
import cn.czh.mapper.ShareFileMapper;
import cn.czh.mapper.UploadFileMapper;
import cn.czh.service.IFileService;
import cn.czh.service.ISseService;
import cn.hutool.json.JSON;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class FileServiceImpl implements IFileService {

    @Resource
    private UploadFileMapper uploadFileMapper;
    @Resource
    private ShareFileMapper shareFileMapper;
    @Resource
    private ISseService sseService;

    @Override
    public IPage<UploadFile> pageFiles(Integer page, Integer pageSize, String storageType, String fileName) {
        return uploadFileMapper.pageFiles(new Page<>(page, pageSize), storageType, fileName);
    }

    @Override
    public List<UploadFile> listSharedFiles() {

        List<SharedFile> sharedFiles = shareFileMapper.selectList(null);
        List<String> files = sharedFiles.stream()
                .map(SharedFile::getFileIdentifier)
                .collect(Collectors.toList());

        List<UploadFile> uploadFiles = uploadFileMapper.selectList(null);
        if (uploadFiles == null) {
            return Collections.emptyList();
        }


        Map<String, UploadFile> uploadFileMap = uploadFiles.stream()
                .collect(Collectors.toMap(
                        UploadFile::getFileIdentifier,
                        uploadFile -> uploadFile,
                        (oldValue, newValue) -> oldValue,
                        LinkedHashMap::new
                ));


        return files.stream()
                .filter(uploadFileMap::containsKey)
                .map(uploadFileMap::get)
                .collect(Collectors.toList());
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
            HashMap<String, Object> map = new HashMap<>();
            map.put("fileIdentifier", identifier);
            map.put("action", "add");
            NotifyMessage message = new NotifyMessage("sharedFileUpdate", map);
            sseService.notification(message);
        }
    }

    @Override
    public void removeSharedFile(String fileIdentifier) {
        shareFileMapper.delete(Wrappers.lambdaQuery(SharedFile.class).eq(SharedFile::getFileIdentifier, fileIdentifier));
        HashMap<String, Object> map = new HashMap<>();
        map.put("fileIdentifier", fileIdentifier);
        map.put("action", "remote");
        NotifyMessage message = new NotifyMessage("sharedFileUpdate", map);
        sseService.notification(message);
    }

    @Override
    public void deleteFile(String fileIdentifier) {
        SharedFile sharedFile = shareFileMapper.selectOne(Wrappers.lambdaQuery(SharedFile.class).eq(SharedFile::getFileIdentifier, fileIdentifier));
        if (sharedFile != null) {
            throw new BusinessException("文件正在分享中，无法删除");
        }
        uploadFileMapper.delete(Wrappers.lambdaQuery(UploadFile.class).eq(UploadFile::getFileIdentifier, fileIdentifier));
    }
}
