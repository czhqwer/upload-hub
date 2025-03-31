package cn.czh.utils;

import cn.czh.base.BusinessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * 文件搜索工具类，使用 ForkJoinPool 实现高效并行文件索引和搜索
 */
@Component
public class FileSearchUtil {
    private static final Logger log = LoggerFactory.getLogger(FileSearchUtil.class);
    private final Map<String, ConcurrentLinkedQueue<String>> fileIndex = new ConcurrentHashMap<>();
    private final ForkJoinPool forkJoinPool;
    private volatile boolean isIndexed = false;

    /**
     * 构造函数，初始化线程池
     *
     * @param threadMultiplier 线程数倍乘因子，默认使用 CPU 核心数 * 2
     */
    public FileSearchUtil(int threadMultiplier) {
        this.forkJoinPool = new ForkJoinPool(
                Math.max(4, Runtime.getRuntime().availableProcessors() * threadMultiplier));
    }

    /**
     * 默认构造函数，使用 CPU 核心数 * 2 作为线程数
     */
    public FileSearchUtil() {
        this(2);
    }

    /**
     * 获取系统中所有可用磁盘列表
     *
     * @return 磁盘根目录列表
     */
    public List<File> getAvailableDrives() {
        File[] roots = File.listRoots();
        return roots != null ? Arrays.asList(roots) : Collections.emptyList();
    }

    /**
     * 根据指定的磁盘列表构建文件索引
     *
     * @param selectedDrives 要索引的磁盘列表
     * @return 索引构建所用时间（毫秒）
     * @throws IllegalArgumentException 如果传入的磁盘列表为空或无效
     */
    public long buildIndex(List<File> selectedDrives) {
        if (isIndexed) {
            return 0; // 已构建过索引，直接返回
        }

        if (selectedDrives == null || selectedDrives.isEmpty()) {
            throw new BusinessException("所选驱动器列表不能为空");
        }

        long startTime = System.currentTimeMillis();
        List<Future<?>> tasks = new ArrayList<>();

        for (File drive : selectedDrives) {
            if (drive != null && drive.exists() && drive.isDirectory()) {
                tasks.add(forkJoinPool.submit(new IndexTask(drive)));
            }
        }

        if (tasks.isEmpty()) {
            throw new BusinessException("没有选择索引的有效驱动器");
        }

        // 等待所有任务完成
        for (Future<?> task : tasks) {
            try {
                task.get();
            } catch (InterruptedException | ExecutionException e) {
                Thread.currentThread().interrupt();
                throw new BusinessException("索引构建中断", e);
            }
        }

        isIndexed = true;
        return System.currentTimeMillis() - startTime;
    }

    /**
     * 搜索文件（模糊匹配）
     *
     * @param keyword 搜索关键词
     * @return 匹配的文件路径列表
     */
    public List<String> search(String keyword) {
        if (!isIndexed) {
            throw new BusinessException("请先构建索引");
        }

        if (keyword == null || keyword.trim().isEmpty()) {
            return Collections.emptyList();
        }

        String searchKey = keyword.toLowerCase();
        return fileIndex.entrySet().stream()
                .filter(entry -> entry.getKey().contains(searchKey))
                .flatMap(entry -> entry.getValue().stream())
                .collect(Collectors.toList());
    }

    /**
     * 获取索引中的文件总数
     *
     * @return 文件总数
     */
    public long getIndexedFileCount() {
        return fileIndex.values().stream()
                .mapToLong(ConcurrentLinkedQueue::size)
                .sum();
    }

    /**
     * 获取索引占用内存估计值（MB）
     *
     * @return 内存占用（MB）
     */
    public double getMemoryUsageMB() {
        return fileIndex.size() * 16.0 / 1024 / 1024;
    }

    /**
     * 关闭线程池，释放资源
     */
    public void shutdown() {
        if (!forkJoinPool.isShutdown()) {
            forkJoinPool.shutdown();
            log.info("ForkJoinPool 已关闭");
        }
    }

    // 内部索引任务类
    private class IndexTask extends RecursiveAction {
        private final File directory;

        public IndexTask(File directory) {
            this.directory = directory;
        }

        @Override
        protected void compute() {
            try {
                File[] files = directory.listFiles();
                if (files == null) return;

                List<IndexTask> subTasks = new ArrayList<>();

                for (File file : files) {
                    if (file.isDirectory()) {
                        IndexTask subTask = new IndexTask(file);
                        subTasks.add(subTask);
                    } else {
                        String fileName = file.getName().toLowerCase();
                        fileIndex.computeIfAbsent(fileName, k -> new ConcurrentLinkedQueue<>())
                                .add(file.getAbsolutePath());
                    }
                }

                if (subTasks.size() < 4) {
                    for (IndexTask subTask : subTasks) {
                        subTask.compute();
                    }
                } else {
                    invokeAll(subTasks);
                }
            } catch (Exception e) {
                log.error("访问目录失败: {}", directory, e);
            }
        }
    }

    // 交互式多选磁盘和搜索
    /*public static void main(String[] args) {
        FileSearchUtil searchUtil = new FileSearchUtil();
        Scanner scanner = new Scanner(System.in);

        // 1. 获取并显示所有磁盘
        List<File> drives = searchUtil.getAvailableDrives();
        if (drives.isEmpty()) {
            System.out.println("未检测到可用磁盘！");
            searchUtil.shutdown();
            return;
        }

        System.out.println("可用磁盘列表：");
        for (int i = 0; i < drives.size(); i++) {
            System.out.println((i + 1) + ". " + drives.get(i).getAbsolutePath());
        }

        // 2. 提示用户输入磁盘名（支持多选，用逗号分隔）
        System.out.print("请输入要搜索的磁盘名（多个磁盘用逗号分隔，例如 C:,D:）：");
        String input = scanner.nextLine().trim();

        // 3. 解析用户输入并选择磁盘
        String[] driveNames = input.split("\\s*,\\s*"); // 支持带空格的逗号分隔
        List<File> selectedDrives = new ArrayList<>();
        Set<String> selectedNames = new HashSet<>(); // 用于去重

        for (String driveName : driveNames) {
            if (driveName.isEmpty()) continue;
            boolean found = false;
            for (File drive : drives) {
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
                System.out.println("警告：无效的磁盘名 '" + driveName + "'，将被忽略");
            }
        }

        if (selectedDrives.isEmpty()) {
            System.out.println("未选择任何有效磁盘！");
            searchUtil.shutdown();
            scanner.close();
            return;
        }

        // 4. 构建指定磁盘的索引
        long indexTime = searchUtil.buildIndex(selectedDrives);
        System.out.println("索引建立完成，用时：" + indexTime + "ms");
        System.out.println("已索引磁盘：" + selectedDrives.stream()
                .map(File::getAbsolutePath)
                .collect(Collectors.joining(", ")));
        System.out.println("索引内存占用：" + String.format("%.2f", searchUtil.getMemoryUsageMB()) + "MB");
        System.out.println("索引文件总数：" + searchUtil.getIndexedFileCount());

        // 5. 提示用户输入搜索关键词
        System.out.print("请输入搜索关键词：");
        String keyword = scanner.nextLine().trim();

        // 6. 执行搜索
        long startTime = System.currentTimeMillis();
        List<String> results = searchUtil.search(keyword);
        long searchTime = System.currentTimeMillis() - startTime;

        // 7. 输出结果
        System.out.println("搜索耗时：" + searchTime + "ms");
        if (!results.isEmpty()) {
            System.out.println("找到 " + results.size() + " 个匹配文件：");
            results.forEach(System.out::println);
        } else {
            System.out.println("未找到文件：" + keyword);
        }

        // 8. 清理资源
        searchUtil.shutdown();
        scanner.close();
    }*/
}