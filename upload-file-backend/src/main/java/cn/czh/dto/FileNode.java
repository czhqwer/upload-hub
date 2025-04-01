package cn.czh.dto;

import lombok.Data;
import java.util.List;

@Data
public class FileNode {
    private String name;      // 文件/文件夹名称
    private String path;      // 绝对路径
    private boolean isFolder; // 是否是文件夹
    private List<FileNode> children; // 子节点
}
