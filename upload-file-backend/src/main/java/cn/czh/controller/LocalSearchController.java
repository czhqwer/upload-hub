package cn.czh.controller;

import cn.czh.base.Result;
import cn.czh.service.ILocalFileService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@Slf4j
@RequestMapping("/localFile")
@RestController
public class LocalSearchController {

    @Resource
    private ILocalFileService fileSearchService;

    /**
     * 获取系统磁盘列表
     */
    @GetMapping("/getDrives")
    public Result<?> getDrives() {
        return Result.success(fileSearchService.getDrives());
    }

    /**
     * 根据指定的磁盘列表构建文件索引
     */
    @PostMapping("/buildIndex")
    public Result<?> buildIndex(@RequestBody List<String> drives) {
        return Result.success(fileSearchService.buildIndex(drives));
    }

    /**
     * 根据文件名称模糊查询
     */
    @GetMapping("/search")
    public Result<?> search(@RequestParam String keyword) {
        return Result.success(fileSearchService.searchFiles(keyword));
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
        if (path.length() == 2 && path.endsWith(":")) {
            return Result.error("请选择目录");
        }

        return Result.success(fileSearchService.getFileTree(path, showFiles, showFolders, maxDepth));
    }
}
