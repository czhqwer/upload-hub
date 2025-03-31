package cn.czh.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 文件索引构建结果类
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
public class IndexResult {

    private long indexTime;           // 索引构建耗时（毫秒）

    private List<String> indexedDrives; // 已索引的磁盘列表

    private String memoryUsage;       // 内存占用（MB，格式化为字符串）

    private long fileCount;           // 索引文件总数

}