package cn.czh.service.impl;

import cn.czh.dto.FileNode;
import cn.czh.dto.IndexResult;
import cn.czh.service.ILocalFileService;
import cn.czh.utils.FileSearchUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class LocalFileServiceImpl implements ILocalFileService {

    @Resource
    private FileSearchUtil fileSearchUtil;

    @PreDestroy
    public void destroy() {
        log.info("关闭 FileSearchUtil 资源...");
        fileSearchUtil.shutdown();
    }

    @Override
    public List<String> getDrives() {
        List<File> availableDrives = fileSearchUtil.getAvailableDrives();
        return availableDrives.isEmpty() ? Collections.emptyList() : availableDrives.stream().map(File::getAbsolutePath).collect(Collectors.toList());
    }

    @Override
    public IndexResult buildIndex(List<String> driveNames) {
        List<File> availableDrives = fileSearchUtil.getAvailableDrives();
        if (availableDrives.isEmpty()) {
            throw new RuntimeException("未检测到可用磁盘！");
        }

        List<File> selectedDrives = new ArrayList<>();
        Set<String> selectedNames = new HashSet<>(); // 用于去重
        for (String driveName : driveNames) {
            if (driveName.isEmpty()) continue;
            boolean found = false;
            for (File drive : availableDrives) {
                String normalizedDriveName = driveName.endsWith(File.separator) ? driveName : driveName + File.separator;
                if (drive.getAbsolutePath().equalsIgnoreCase(driveName) ||
                        drive.getAbsolutePath().equalsIgnoreCase(normalizedDriveName)) {
                    if (selectedNames.add(drive.getAbsolutePath().toLowerCase())) {
                        selectedDrives.add(drive);
                    }
                    found = true;
                    break;
                }
            }
            if (!found) {
                log.warn("警告：无效的磁盘名 '{}'，将被忽略", driveName);
            }
        }

        if (selectedDrives.isEmpty()) {
            throw new RuntimeException("未选择任何有效磁盘！");
        }

        long indexTime = fileSearchUtil.buildIndex(selectedDrives);
        return new IndexResult(
                indexTime,
                selectedDrives.stream().map(File::getAbsolutePath).collect(Collectors.toList()),
                String.format("%.2f", fileSearchUtil.getMemoryUsageMB()),
                fileSearchUtil.getIndexedFileCount()
        );
    }

    @Override
    public List<String> searchFiles(String keyword) {
        return fileSearchUtil.search(keyword);
    }

    @Override
    public FileNode getFileTree(String path, boolean showFiles, boolean showFolders, int maxDepth) {
        File root = new File(path);
        if (!root.exists() || !root.isDirectory()) {
            return null;
        }
        return buildFileTree(root, showFiles, showFolders, 0, maxDepth);
    }

    private FileNode buildFileTree(File file, boolean showFiles, boolean showFolders, int currentDepth, int maxDepth) {
        // 判断是否匹配类型
        boolean matchesType = (file.isDirectory() && showFolders) || (!file.isDirectory() && showFiles);

        // 如果不匹配类型，则不包含此节点
        if (!matchesType) {
            return null;
        }

        FileNode node = new FileNode();
        node.setName(file.getName());
        node.setPath(file.getAbsolutePath());
        node.setFolder(file.isDirectory());

        // 如果是目录且未达到最大深度，递归构建子节点
        if (file.isDirectory() && (maxDepth == 0 || currentDepth < maxDepth)) {
            File[] files = file.listFiles();
            if (files != null) {
                List<FileNode> children = new ArrayList<>();
                for (File f : files) {
                    FileNode child = buildFileTree(f, showFiles, showFolders, currentDepth + 1, maxDepth);
                    if (child != null) {
                        children.add(child);
                    }
                }
                node.setChildren(children.isEmpty() ? null : children);
            }
        }
        return node;
    }
}
