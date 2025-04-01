package cn.czh.service;

import cn.czh.dto.FileNode;
import cn.czh.dto.IndexResult;

import java.util.List;

public interface ILocalFileService {

    List<String> getDrives();

    IndexResult buildIndex(List<String> drives);

    List<String> searchFiles(String keyword);

    FileNode getFileTree(String path, boolean showFiles, boolean showFolders, int maxDepth);

}
