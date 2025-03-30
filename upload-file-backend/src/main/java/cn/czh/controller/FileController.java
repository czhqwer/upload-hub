package cn.czh.controller;

import cn.czh.base.Result;
import cn.czh.dto.NotifyMessage;
import cn.czh.entity.StorageConfig;
import cn.czh.service.IAuthService;
import cn.czh.service.IFileService;
import cn.czh.service.ISseService;
import cn.czh.service.IStorageConfigService;
import cn.czh.utils.FileTypeUtil;
import cn.hutool.core.map.MapUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Enumeration;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/file")
public class FileController {

    @Resource
    private IStorageConfigService storageConfigService;
    @Resource
    private IFileService fileService;
    @Resource
    private IAuthService authService;
    @Resource
    private ISseService sseService;

    @Value("${server.port}")
    private String serverPort;

    private static boolean enableShare = false;

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
    public Result<?> pageFiles(HttpServletRequest request, Integer page, Integer pageSize, String storageType, String fileName) {
        if (!authService.isMainUser(request.getRemoteAddr())) {
            return Result.error("权限不足");
        }

        if (page == null) {
            page = 1;
        }
        if (pageSize == null) {
            pageSize = 10;
        }
        return Result.success(fileService.pageFiles(page, pageSize, storageType, fileName));
    }

    @PostMapping("/deleteFile")
    public Result<?> deleteFile(HttpServletRequest request, @RequestParam String fileIdentifier) {
        if (!authService.isMainUser(request.getRemoteAddr())) {
            return Result.error("权限不足");
        }
        fileService.deleteFile(fileIdentifier);
        return Result.success();
    }

    @PostMapping("/addSharedFile")
    public Result<?> addSharedFile(@RequestParam String fileIdentifier) {
        fileService.addSharedFile(fileIdentifier);
        return Result.success();
    }

    @PostMapping("/unShareFile")
    public Result<?> unShareFile(HttpServletRequest request, @RequestParam String fileIdentifier) {
        if (!authService.isMainUser(request.getRemoteAddr())) {
            return Result.error("非主用户不能取消分享");
        }
        fileService.removeSharedFile(fileIdentifier);
        return Result.success();
    }

    @GetMapping("/sharedFiles")
    public Result<?> getSharedFiles(HttpServletRequest request, @RequestHeader("Authorization") String password) {
        boolean isMainUser = authService.isMainUser(request.getRemoteAddr());
        String mainUserPassword = authService.getMainUserPassword();

        if (!isMainUser && !mainUserPassword.equals(password)) {
            return Result.error("密码错误");
        }
        if (!isMainUser && !enableShare) {
            return Result.error("分享功能未开启");
        }

        return Result.success(fileService.listSharedFiles());
    }

    @PostMapping("/enableShare")
    public Result<?> enableShare(HttpServletRequest request, @RequestParam Boolean enable) {
        if (!authService.isMainUser(request.getRemoteAddr())) {
            return Result.error("非主用户不能修改分享状态");
        }
        enableShare = enable;
        Map<String, Object> data = MapUtil.of("enable", enable);
        sseService.notifySystemEvent(new NotifyMessage("enableShare", data));
        return Result.success();
    }

    @GetMapping("/getShareStatus")
    public Result<?> getShareStatus() {
        return Result.success(enableShare);
    }

    @GetMapping("/shareAddress")
    public Result<?> getShareAddress(Integer clientPort) {
        String ip = getLocalIpAddress();
        String port = clientPort != null ? clientPort.toString() : serverPort;
        return Result.success("http://" + ip + ":" + port);
    }

    private String getLocalIpAddress() {
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface networkInterface = interfaces.nextElement();
                // 跳过回环接口和未启用的接口
                if (networkInterface.isLoopback() || !networkInterface.isUp()) {
                    continue;
                }
                Enumeration<InetAddress> addresses = networkInterface.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress addr = addresses.nextElement();
                    if (addr instanceof Inet4Address && !addr.isLoopbackAddress()) {
                        String ip = addr.getHostAddress();
                        if (ip.startsWith("192.168.") || ip.startsWith("10.") ||
                                (ip.startsWith("172.") && Integer.parseInt(ip.split("\\.")[1]) >= 16 && Integer.parseInt(ip.split("\\.")[1]) <= 31)) {
                            return ip;
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("获取本地IP地址失败：{}", e.getMessage(), e);
        }
        return "127.0.0.1";
    }
}