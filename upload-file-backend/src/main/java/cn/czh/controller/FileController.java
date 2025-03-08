package cn.czh.controller;

import cn.czh.base.Result;
import cn.czh.entity.StorageConfig;
import cn.czh.service.IFileService;
import cn.czh.service.IStorageConfigService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

@RestController
@RequestMapping("/file")
public class FileController {

    @Resource
    private IStorageConfigService storageConfigService;
    @Resource
    private IFileService fileService;

    @GetMapping("/preview/{storageType}/**")
    public ResponseEntity<byte[]> previewFile(
            @PathVariable String storageType,
            HttpServletRequest request) throws IOException {
        // 从请求中提取剩余路径作为 objectKey
        String objectKey = request.getRequestURI()
                .substring(request.getContextPath().length())
                .replaceFirst("/file/preview/" + storageType + "/", "");

        if (StorageConfig.LOCAL.equals(storageType)) {
            StorageConfig storageConfig = storageConfigService.getStorageConfigByType(storageType);
            String filePath = storageConfig.getBucket() + "\\" + objectKey;
            File file = new File(filePath);
            if (!file.exists()) {
                return ResponseEntity.notFound().build();
            }
            byte[] fileBytes = Files.readAllBytes(file.toPath());
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            return new ResponseEntity<>(fileBytes, headers, HttpStatus.OK);
        }
        return ResponseEntity.badRequest().build();
    }

    @GetMapping("/page")
    public Result<?> pageFiles(Integer page, Integer pageSize, String storageType, String fileName) {
        if (page == null) {
            page = 1;
        }
        if (pageSize == null) {
            pageSize = 10;
        }
        return Result.success(fileService.pageFiles(page, pageSize, storageType, fileName));
    }
}