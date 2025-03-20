package cn.czh.controller;

import cn.czh.base.Result;
import cn.czh.entity.StorageConfig;
import cn.czh.service.IFileService;
import cn.czh.service.IStorageConfigService;
import cn.czh.utils.FileTypeUtil;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
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
import java.nio.file.Paths;

@RestController
@RequestMapping("/file")
public class FileController {

    @Resource
    private IStorageConfigService storageConfigService;
    @Resource
    private IFileService fileService;

    @GetMapping("/preview/{storageType}/**")
    public ResponseEntity<?> previewFile(
            @PathVariable String storageType,
            HttpServletRequest request) throws IOException {
        // 从请求中提取剩余路径作为 objectKey
        String objectKey = request.getRequestURI()
                .substring(request.getContextPath().length())
                .replaceFirst("/file/preview/" + storageType + "/", "");

        if (StorageConfig.LOCAL.equals(storageType)) {
            StorageConfig storageConfig = storageConfigService.getStorageConfigByType(storageType);
            // 使用 Paths.get 拼接路径，避免反斜杠问题
            String filePath = Paths.get(storageConfig.getBucket(), objectKey).toString();
            File file = new File(filePath);

            // 检查文件是否存在
            if (!file.exists()) {
                return ResponseEntity.notFound().build();
            }

            // 读取文件内容
            byte[] fileBytes = Files.readAllBytes(file.toPath());
            ByteArrayResource resource = new ByteArrayResource(fileBytes);

            // 设置响应头
            HttpHeaders headers = new HttpHeaders();
            String fileName = file.getName().toLowerCase();
            String fileSuffix = FileTypeUtil.getFileSuffix(fileName).toLowerCase();

            // 根据文件类型设置 Content-Type
            MediaType contentType = FileTypeUtil.determineContentType(fileSuffix);
            headers.setContentType(contentType);

            // 设置 Content-Disposition 为 inline，浏览器直接预览
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + file.getName() + "\"");
            headers.setContentLength(fileBytes.length);

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(resource);
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