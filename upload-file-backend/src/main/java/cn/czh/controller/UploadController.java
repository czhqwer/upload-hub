package cn.czh.controller;

import cn.czh.base.Result;
import cn.czh.dto.req.CreateMultipartUpload;
import cn.czh.entity.StorageConfig;
import cn.czh.entity.UploadFile;
import cn.czh.service.IAuthService;
import cn.czh.service.IStorageService;
import cn.czh.service.StorageServiceFactory;
import cn.czh.utils.FileTypeUtil;
import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import cn.hutool.crypto.SecureUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/upload")
public class UploadController {

    @Resource
    private StorageServiceFactory storageServiceFactory;
    @Resource
    private IAuthService authService;

    /**
     * 从请求中获取存储类型（示例：从请求头获取）
     * @param request HTTP 请求对象
     * @return 存储类型（"MINIO" 或 "LOCAL"）
     */
    private String getStorageTypeFromRequest(HttpServletRequest request) {
        String storageType = request.getHeader("Storage-Type");
        if (storageType == null || storageType.trim().isEmpty()) {
            storageType = StorageConfig.LOCAL; // 默认值
        }
        return storageType;
    }

    @GetMapping("/getUploadProgress")
    public Result<?> getUploadProgress(HttpServletRequest request, String identifier) {
        String storageType = getStorageTypeFromRequest(request);
        IStorageService storageService = storageServiceFactory.getStorageService(storageType);
        return Result.success(storageService.getUploadProgress(identifier));
    }

    @PostMapping("/createMultipartUpload")
    public Result<?> createMultipartUpload(HttpServletRequest request, @Valid @RequestBody CreateMultipartUpload req) {
        String storageType = getStorageTypeFromRequest(request);
        IStorageService storageService = storageServiceFactory.getStorageService(storageType);
        return Result.success(storageService.createMultipartUpload(req));
    }

    @GetMapping("/getPreSignUploadUrl")
    public Result<?> getPreSignUploadUrl(HttpServletRequest request, String identifier, Integer partNumber) {
        String storageType = getStorageTypeFromRequest(request);
        IStorageService storageService = storageServiceFactory.getStorageService(storageType);
        UploadFile uploadFile = storageService.getByIdentifier(identifier);
        if (uploadFile == null) {
            return Result.success("分片任务不存在");
        }
        Map<String, String> params = new HashMap<>();
        params.put("partNumber", partNumber.toString());
        params.put("uploadId", uploadFile.getUploadId());
        return Result.success(storageService.genPreSignUploadUrl(uploadFile.getObjectKey(), params));
    }

    @PostMapping("/part")
    public Result<?> uploadPart(
            HttpServletRequest request,
            @RequestParam("uploadId") String uploadId,
            @RequestParam("partNumber") Integer partNumber,
            @RequestParam("file") MultipartFile partFile) {
        String storageType = getStorageTypeFromRequest(request);
        IStorageService storageService = storageServiceFactory.getStorageService(storageType);
        storageService.uploadPart(uploadId, partNumber, partFile);
        return Result.success("分片上传成功");
    }

    @PostMapping("/merge")
    public Result<?> merge(HttpServletRequest request, String identifier) {
        boolean mainUser = authService.isMainUser(request.getRemoteAddr());
        String storageType = getStorageTypeFromRequest(request);
        IStorageService storageService = storageServiceFactory.getStorageService(storageType);
        return Result.success(storageService.merge(identifier, mainUser));
    }

    @PostMapping
    public Result<?> uploadFile(HttpServletRequest request, MultipartFile file, String folder, Integer fileType) throws IOException {
        if (file == null || file.getBytes().length == 0) {
            return Result.error("上传文件不能为空");
        }
        String filename = file.getOriginalFilename();
        if (fileType != null) {
            String fileTypeExists = FileTypeUtil.isFileTypeExists(fileType, filename);
            if (fileTypeExists != null) {
                return Result.success("数据格式不正确");
            }
        }

        boolean mainUser = authService.isMainUser(request.getRemoteAddr());

        String md5 = SecureUtil.md5(file.getInputStream());
        synchronized (md5.intern()) {
            String dateFormat = DateUtil.format(new Date(), DatePattern.PURE_DATE_PATTERN);
            assert filename != null;
            String objectName = (folder == null ? "default" : folder) + "/" +
                    dateFormat + "/" + md5 + "." + FileTypeUtil.getFileSuffix(filename);
            String storageType = getStorageTypeFromRequest(request);
            IStorageService storageService = storageServiceFactory.getStorageService(storageType);
            return Result.success(storageService.uploadFile(file, md5, objectName, mainUser));
        }
    }
}