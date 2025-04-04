package cn.czh.service.impl;

import cn.czh.base.BusinessException;
import cn.czh.dto.FileNode;
import cn.czh.dto.IndexResult;
import cn.czh.service.ILocalFileService;
import cn.czh.utils.FileSearchUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.SecureRandom;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class LocalFileServiceImpl implements ILocalFileService {

    @Resource
    private FileSearchUtil fileSearchUtil;

    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/CBC/PKCS5Padding";
    private static final int KEY_LENGTH = 256;
    private static final int ITERATION_COUNT = 65536;
    private static final int SALT_LENGTH = 16;


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

    @Override
    public String encryptFile(String filePath, String password) {
        Path path = Paths.get(filePath);
        if (!Files.exists(path)) {
            throw new BusinessException("文件未找到: " + filePath);
        }

        String encryptedFilePath = filePath + ".encrypted";
        Path encryptedPath = Paths.get(encryptedFilePath);
        if (Files.exists(encryptedPath)) {
            throw new BusinessException("加密文件已存在: " + encryptedFilePath);
        }

        try {
            byte[] salt = new byte[SALT_LENGTH];
            new SecureRandom().nextBytes(salt);

            SecretKey key = generateKey(password, salt);

            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, key);

            try (InputStream in = Files.newInputStream(path);
                 OutputStream out = Files.newOutputStream(encryptedPath)) {

                out.write(salt);

                byte[] iv = cipher.getIV();
                out.write(iv);

                byte[] buffer = new byte[8192];
                int bytesRead;
                while ((bytesRead = in.read(buffer)) != -1) {
                    byte[] output = cipher.update(buffer, 0, bytesRead);
                    if (output != null) {
                        out.write(output);
                    }
                }
                byte[] outputBytes = cipher.doFinal();
                if (outputBytes != null) {
                    out.write(outputBytes);
                }
            }
            return encryptedFilePath;
        } catch (Exception e) {
            try {
                Files.deleteIfExists(encryptedPath);
            } catch (IOException ex) {
                log.error("删除加密失败的文件时出错: {}", ex.getMessage(), ex);
            }
            throw new BusinessException("文件加密失败", e);
        }
    }

    @Override
    public String decryptFile(String filePath, String password) {
        Path path = Paths.get(filePath);
        if (!Files.exists(path)) {
            throw new BusinessException("文件未找到: " + filePath);
        }

        String decryptedFilePath = filePath.replace(".encrypted", "");
        Path decryptedPath = Paths.get(decryptedFilePath);
        if (Files.exists(decryptedPath)) {
            throw new BusinessException("解密文件已存在: " + decryptedFilePath);
        }

        try {
            // Read salt and IV
            byte[] salt = new byte[SALT_LENGTH];
            byte[] iv = new byte[16];
            try (InputStream in = Files.newInputStream(path)) {
                if (in.read(salt) != SALT_LENGTH) {
                    throw new BusinessException("加密文件格式无效");
                }
                if (in.read(iv) != 16) {
                    throw new BusinessException("加密文件格式无效");
                }
            }

            SecretKey key = generateKey(password, salt);

            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, key, new javax.crypto.spec.IvParameterSpec(iv));

            try (InputStream in = Files.newInputStream(path);
                 OutputStream out = Files.newOutputStream(decryptedPath)) {

                in.skip(SALT_LENGTH + 16);

                byte[] buffer = new byte[8192];
                int bytesRead;
                while ((bytesRead = in.read(buffer)) != -1) {
                    byte[] output = cipher.update(buffer, 0, bytesRead);
                    if (output != null) {
                        out.write(output);
                    }
                }
                byte[] outputBytes = cipher.doFinal();
                if (outputBytes != null) {
                    out.write(outputBytes);
                }
            }
            return decryptedFilePath;
        } catch (Exception e) {
            // 删除已生成的解密文件
            try {
                Files.deleteIfExists(decryptedPath);
            } catch (IOException ex) {
                log.error("删除解密失败的文件时出错: {}", ex.getMessage(), ex);
            }
            throw new BusinessException("文件解密失败", e);
        }
    }

    private SecretKey generateKey(String password, byte[] salt) throws Exception {
        PBEKeySpec keySpec = new PBEKeySpec(password.toCharArray(), salt, ITERATION_COUNT, KEY_LENGTH);
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        byte[] keyBytes = factory.generateSecret(keySpec).getEncoded();
        return new SecretKeySpec(keyBytes, ALGORITHM);
    }
}
