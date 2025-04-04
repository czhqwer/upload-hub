package cn.czh.controller;

import cn.czh.base.Result;
import cn.czh.service.ILocalFileService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.io.File;
import java.util.List;

@Slf4j
@RequestMapping("/localFile")
@RestController
public class LocalFileController {

    @Resource
    private ILocalFileService localFileService;

    /**
     * 获取系统磁盘列表
     */
    @GetMapping("/getDrives")
    public Result<?> getDrives() {
        return Result.success(localFileService.getDrives());
    }

    /**
     * 根据指定的磁盘列表构建文件索引
     */
    @PostMapping("/buildIndex")
    public Result<?> buildIndex(@RequestBody List<String> drives) {
        return Result.success(localFileService.buildIndex(drives));
    }

    /**
     * 根据文件名称模糊查询
     */
    @GetMapping("/search")
    public Result<?> search(@RequestParam String keyword) {
        return Result.success(localFileService.searchFiles(keyword));
    }

    /**
     * 根据文件地址打开目录
     */
    @PostMapping("/openDir")
    public Result<?> openDir(@RequestParam String dir) {
        try {
            Runtime.getRuntime().exec("explorer.exe /select," + dir);
            return Result.success();
        } catch (Exception e) {
            log.error("打开目录失败：{}", e.getMessage(), e);
            return Result.error("打开目录失败");
        }
    }

    /**
     * 根据文件目录展示文件树
     */
    @GetMapping("/getFileTree")
    public Result<?> getFileTree(
            @RequestParam String path,
            @RequestParam(defaultValue = "true") boolean showFiles,
            @RequestParam(defaultValue = "true") boolean showFolders,
            @RequestParam(defaultValue = "3") int maxDepth

    ) {
        // 避免直接输入磁盘进去全盘扫描
        if (path.length() == 3 && path.endsWith(":\\") && maxDepth != 1) {
            return Result.error("请选择目录");
        }

        return Result.success(localFileService.getFileTree(path, showFiles, showFolders, maxDepth));
    }

    @PostMapping("/encrypt")
    public Result<?> encryptFile(@RequestParam String filePath, @RequestParam String password) {
        File file = new File(filePath);
        if (!file.exists()) {
            return Result.error("文件不存在");
        }
        if (file.isDirectory()) {
            return Result.error("不能加密目录，请选择文件");
        }
        return Result.success(localFileService.encryptFile(filePath, password));
    }

    @PostMapping("/decrypt") 
    public Result<?> decryptFile(@RequestParam String filePath, @RequestParam String password) {
        File file = new File(filePath);
        if (!file.exists()) {
            return Result.error("文件不存在");
        }
        if (file.isDirectory()) {
            return Result.error("不能解密目录，请选择文件");
        }
        return Result.success(localFileService.decryptFile(filePath, password));
    }
}
