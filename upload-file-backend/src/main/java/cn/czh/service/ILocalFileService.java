package cn.czh.service;

import cn.czh.dto.FileNode;
import cn.czh.dto.IndexResult;

import java.io.IOException;
import java.util.List;

public interface ILocalFileService {

    List<String> getDrives();

    IndexResult buildIndex(List<String> drives);

    List<String> searchFiles(String keyword);

    FileNode getFileTree(String path, boolean showFiles, boolean showFolders, int maxDepth);

    String encryptFile(String filePath, String password);

    String decryptFile(String filePath, String password);
}
