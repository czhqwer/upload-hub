package cn.czh.controller;

import cn.czh.base.Result;
import cn.czh.service.IFileSearchService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@Slf4j
@RequestMapping("/fileSearch")
@RestController
public class FileSearchController {

    @Resource
    private IFileSearchService fileSearchService;

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
}
