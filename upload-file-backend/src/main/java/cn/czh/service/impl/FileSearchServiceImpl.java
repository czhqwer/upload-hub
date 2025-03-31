package cn.czh.service.impl;

import cn.czh.dto.IndexResult;
import cn.czh.service.IFileSearchService;
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
public class FileSearchServiceImpl implements IFileSearchService {

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
}
