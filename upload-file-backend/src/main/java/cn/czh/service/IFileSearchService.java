package cn.czh.service;

import cn.czh.dto.IndexResult;

import java.util.List;

public interface IFileSearchService {

    List<String> getDrives();

    IndexResult buildIndex(List<String> drives);

    List<String> searchFiles(String keyword);

}
