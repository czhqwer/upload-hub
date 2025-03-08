<template>
  <div class="upload-container">
    <div class="upload-window">
      <!-- 标题栏 -->
      <div class="header-wrapper">
        <h1 class="page-title">
          <span class="cloud-icon">☁️</span>
          文件上传中心
        </h1>
        <div class="menu-container" @mouseenter="showMenu = true" @mouseleave="showMenu = false">
          <div class="menu-icon">☰</div>
          <div v-show="showMenu" class="dropdown-menu">
            <div class="menu-item" @click="openSettings">设置</div>
            <div class="menu-item" @click="openAbout">关于</div>
          </div>
        </div>
      </div>

      <!-- 标签导航 -->
      <div class="tab-bar">
        <div v-for="tab in tabs" :key="tab.value" :class="['tab', { 'active': activeTab === tab.value }]"
          @click="switchTab(tab.value)">
          {{ tab.label }}
          <div class="tab-underline"></div>
        </div>
      </div>

      <!-- 上传区域 -->
      <div v-if="activeTab !== 'gallery' && !isWipTab" class="upload-content">
        <upload-file :file-list.sync="fileList" :accept="allowedFormats" :max-size="maxUploadSize" width="100%"
          height="400px" :storage-type="activeTab" />
      </div>

      <!-- 未实现功能提示 -->
      <div v-else-if="isWipTab" class="wip-container">
        <div class="wip-message">
          <span class="wip-icon">🚧</span>
          <h2>功能建设中</h2>
          <p v-if="activeTab === 'obs'">正在加班加点搬砖中，敬请期待！</p>
          <p v-else-if="activeTab === 'qiniu'">七牛云功能正在被疯狂调教，马上就能和大家见面啦！</p>
        </div>
      </div>

      <!-- 图片展示区域 -->
      <div v-else class="gallery-container" @scroll="handleScroll">
        <div class="gallery-header">
          <label for="storage-select">存储类型:</label>
          <select id="storage-select" v-model="selectedStorageType" @change="fetchFiles(true)">
            <option v-for="option in storageOptions" :key="option.value" :value="option.value">
              {{ option.label }}
            </option>
          </select>

          <label for="storage-select">&nbsp;&nbsp;&nbsp;&nbsp;文件名:</label>
          <el-input v-model="selectedFileName" placeholder="请输入文件名" class="file-name-input" style="width: 150px;" @change="fetchFiles(true)"></el-input>
        </div>
        <div v-if="filePage.length === 0 && !loading" class="empty-tips">
          🖼️ 暂无已上传的文件
        </div>
        <div v-else-if="loading && filePage.length === 0" class="loading-tips">
          加载中...
        </div>
        <div v-else class="file-grid">
          <div v-for="file in filePage" :key="file.id" class="file-card">
            <div class="preview-wrapper">
              <img v-if="isImage(file)" :src="file.accessUrl" :alt="file.fileName" class="preview-image" />
              <div v-else class="file-icon">
                📄
              </div>
            </div>
            <div class="file-meta">
              <div class="filename">{{ file.fileName }}</div>
              <div class="file-size-status">
                <div class="file-size">{{ formatSize(file.totalSize) }}</div>
                <div class="status-indicator">{{ formattedDate(file.createTime) }}</div>
              </div>
            </div>
          </div>
        </div>
        <div v-if="loading && filePage.length > 0" class="loading-more">
          加载更多...
        </div>
      </div>

      <!-- 设置表单（弹窗形式） -->
      <div v-if="showSettings" class="settings-modal" @click="closeSettingsOnOutside">
        <div class="settings-content" @click.stop>
          <h2>上传设置</h2>
          <form @submit.prevent="saveSettings">
            <div class="form-group">
              <label>允许上传的文件格式（用英文逗号分隔，例如 .jpg,.png）:</label>
              <input v-model="tempAllowedFormats" type="text" placeholder=".jpg,.png,.mp4" />
            </div>
            <div class="form-group">
              <label>最大上传大小（MB）:</label>
              <input v-model.number="tempMaxSizeMB" type="number" min="1" step="1" />
            </div>
            <div class="form-actions">
              <button type="submit">保存</button>
              <button type="button" @click="closeSettings">取消</button>
            </div>
          </form>
        </div>
      </div>

      <!-- 关于弹窗 -->
      <div v-if="showAbout" class="about-modal" @click="closeAboutOnOutside">
        <div class="about-content" @click.stop>
          <h2>关于</h2>
          <p>这个人很懒，什么都没有留下，只说自己爱吃炸排骨。。。</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import UploadFile from '@/components/UploadFile/UploadFile.vue';
import { pageFiles } from '@/utils/api';

export default {
  name: 'FileUpload',
  components: { UploadFile },
  data() {
    return {
      fileList: [],
      activeTab: 'local',
      tabs: [
        { label: 'Local', value: 'local' },
        { label: 'MinIO', value: 'minio' },
        { label: 'OSS', value: 'oss' },
        { label: 'OBS', value: 'obs' },
        { label: 'QiNiu', value: 'qiniu' },
        { label: '已上传文件', value: 'gallery' }
      ],
      showMenu: false,
      showSettings: false,
      showAbout: false,
      allowedFormats: '.jpg,.png,.mp4',
      maxUploadSize: 100 * 1024 * 1024,
      tempAllowedFormats: '',
      tempMaxSizeMB: 100,
      // 分页相关
      selectedStorageType: 'local', // 默认存储类型
      selectedFileName: '',
      storageOptions: [
        { label: 'Local', value: 'local' },
        { label: 'MinIO', value: 'minio' },
        { label: 'OSS', value: 'oss' }
      ],
      currentPage: 1,
      pageSize: 10,
      loading: false,
      hasMore: true, // 是否还有更多数据
      filePage: []
    };
  },
  computed: {
    formattedDate() {
      return (date) => {
        const d = new Date(date);
        const year = d.getFullYear();
        const month = String(d.getMonth() + 1).padStart(2, '0');
        const day = String(d.getDate()).padStart(2, '0');
        return `${year}-${month}-${day}`;
      };
    },
    isWipTab() {
      return this.activeTab === 'obs' || this.activeTab === 'qiniu';
    }
  },
  methods: {
    switchTab(tabValue) {
      this.activeTab = tabValue;
      if (tabValue === 'gallery') {
        this.fetchFiles(true);
      } else if (this.isWipTab) {
        this.$message({
          message: tabValue === 'obs' 
            ? 'OBS功能正在施工中，小哥哥们正在挥汗如雨！' 
            : '七牛云功能开发中，程序员正在和咖啡斗智斗勇！',
          type: 'info',
          duration: 2000
        });
      }
    },
    isImage(file) {
      return file.contentType.startsWith('image/');
    },
    formatSize(bytes) {
      if (bytes === 0) return '0 B';
      const k = 1024;
      const sizes = ['B', 'KB', 'MB', 'GB'];
      const i = Math.floor(Math.log(bytes) / Math.log(k));
      return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
    },
    openSettings() {
      this.tempAllowedFormats = this.allowedFormats;
      this.tempMaxSizeMB = this.maxUploadSize / (1024 * 1024);
      this.showSettings = true;
      this.showMenu = false;
    },
    saveSettings() {
      this.allowedFormats = this.tempAllowedFormats;
      this.maxUploadSize = this.tempMaxSizeMB * 1024 * 1024;
      this.showSettings = false;
    },
    closeSettings() {
      this.showSettings = false;
    },
    closeSettingsOnOutside(event) {
      if (event.target.classList.contains('settings-modal')) {
        this.closeSettings();
      }
    },
    openAbout() {
      this.showAbout = true;
      this.showMenu = false;
    },
    closeAboutOnOutside(event) {
      if (event.target.classList.contains('about-modal')) {
        this.showAbout = false;
      }
    },
    // 获取文件列表
    async fetchFiles(reset = false) {
      console.log('fetchFiles called with reset:', reset, this.loading, this.hasMore); // 调试日志

      if (this.loading || (!reset && !this.hasMore)) return;

      this.loading = true;
      if (reset) {
        this.currentPage = 1;
        this.filePage = [];
        this.hasMore = true;
      }

      try {
        const response = await pageFiles({
          page: this.currentPage,
          pageSize: this.pageSize,
          storageType: this.selectedStorageType,
          fileName: this.selectedFileName
        });
        const files = response.data.records || []; 
        this.filePage = reset ? files : this.filePage.concat(files);
        this.hasMore = files.length === this.pageSize;
        if (this.hasMore) this.currentPage++;
      } catch (error) {
        console.error('获取文件列表失败:', error);
        this.$message.error('加载文件列表失败');
      } finally {
        this.loading = false;
      }
    },
    // 滚动触底加载
    handleScroll(event) {
      const container = event.target;
      const isBottom =
        container.scrollTop + container.clientHeight >= container.scrollHeight - 10; // 提前 10px 触发
      if (isBottom && !this.loading && this.hasMore) {
        this.fetchFiles();
      }
    }
  }
};
</script>

<style scoped>
.wip-container {
  padding: 2rem;
  height: 400px;
  background: #fcfdff;
  border-top: 1px solid #f0f7ff;
  display: flex;
  justify-content: center;
  align-items: center;
}

.wip-message {
  text-align: center;
  color: #666;
}

.wip-icon {
  font-size: 3rem;
  margin-bottom: 1rem;
  display: block;
}

.wip-message h2 {
  font-size: 1.5rem;
  color: #1976d2;
  margin: 0.5rem 0;
}

.wip-message p {
  font-size: 1rem;
  max-width: 400px;
  margin: 0 auto;
  line-height: 1.5;
}

.upload-container {
  width: 100%;
  background: #ffffff;
  border-radius: 14px;
  box-shadow: 0 12px 24px rgba(25, 118, 210, 0.1);
  overflow: hidden;
  border: 1px solid #e3f2fd;
}

/* 标题栏布局 */
.header-wrapper {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1.5rem 2rem;
  background: #1976d2;
}

.page-title {
  margin: 0;
  color: white;
  font-size: 1.5rem;
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: 12px;
}

.cloud-icon {
  font-size: 1.8rem;
  filter: drop-shadow(0 2px 4px rgba(0, 0, 0, 0.1));
}

/* 菜单图标和下拉样式 */
.menu-container {
  position: relative;
}

.menu-icon {
  color: white;
  font-size: 1.8rem;
  cursor: pointer;
  padding: 0 10px;
}

.dropdown-menu {
  position: absolute;
  top: 100%;
  right: 0;
  background: #fff;
  border-radius: 6px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  min-width: 120px;
  z-index: 10;
}

.menu-item {
  padding: 8px 16px;
  color: #333;
  cursor: pointer;
  font-size: 14px;
  transition: background 0.2s;
}

.menu-item:hover {
  background: #f0f7ff;
}

/* 标签导航样式 */
.tab-bar {
  display: flex;
  padding: 0 2rem;
  background: #f8fafd;
  border-bottom: 1px solid #e0eefc;
  gap: 8px;
  position: relative;
}

.tab {
  position: relative;
  padding: 14px 32px;
  font-size: 14px;
  color: #607d9f;
  cursor: pointer;
  background: transparent;
  border: none;
  border-radius: 6px 6px 0 0;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.tab:hover {
  color: #1565c0;
  background: rgba(25, 118, 210, 0.05);
}

.tab.active {
  color: #1976d2;
  font-weight: 500;
}

.tab.active .tab-underline {
  width: 100%;
  background: #1976d2;
}

.tab-underline {
  height: 2px;
  width: 0;
  background: transparent;
  transition: width 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

/* 上传和展示区域样式 */
.upload-content {
  padding: 2rem;
  background: #fcfdff;
  min-height: 400px;
  border-top: 1px solid #f0f7ff;
}

.gallery-container {
  padding: 1rem 2rem 2rem;
  height: 400px;
  background: #fcfdff;
  border-top: 1px solid #f0f7ff;
  overflow-y: auto;
}

.gallery-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 1rem;
}

.gallery-header label {
  font-size: 14px;
  color: #333;
}

.gallery-header select {
  padding: 6px 12px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 14px;
  background: #fff;
  cursor: pointer;
}

.empty-tips,
.loading-tips {
  text-align: center;
  color: #999;
  font-size: 1.2rem;
  padding: 4rem 0;
}

.file-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(180px, 1fr));
  gap: 1rem;
}

.loading-more {
  text-align: center;
  color: #666;
  font-size: 14px;
  padding: 1rem 0;
}

.file-card {
  background: #fff;
  border-radius: 6px;
  box-shadow: 0 1px 6px rgba(0, 0, 0, 0.1);
  overflow: hidden;
  transition: transform 0.2s, box-shadow 0.2s;
}

.file-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 3px 8px rgba(25, 118, 210, 0.2);
}

.preview-wrapper {
  position: relative;
  padding-top: 100%;
  background: #f8fafd;
}

.preview-image {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  object-fit: cover;
  border-bottom: 1px solid #eee;
}

.file-icon {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  font-size: 2rem;
  opacity: 0.6;
}

.file-meta {
  padding: 0.8rem;
  display: flex;
  flex-direction: column;
}

.file-size-status {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.filename {
  font-weight: 500;
  color: #333;
  font-size: 0.9rem;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.file-size {
  font-size: 0.7rem;
  color: #666;
  margin: 0.2rem 0;
}

.status-indicator {
  display: inline-block;
  padding: 0.15rem 0.4rem;
  border-radius: 3px;
  font-size: 0.65rem;
  background: #e8f5e9;
  color: #2e7d32;
}

/* 设置弹窗样式 */
.settings-modal {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 100;
}

.settings-content {
  background: #fff;
  padding: 20px;
  border-radius: 8px;
  width: 400px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.settings-content h2 {
  margin-top: 0;
  font-size: 1.2rem;
  color: #1976d2;
}

.form-group {
  margin-bottom: 15px;
}

.form-group label {
  display: block;
  margin-bottom: 5px;
  font-size: 14px;
  color: #333;
}

.form-group input {
  width: 100%;
  padding: 8px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 14px;
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

.form-actions button {
  padding: 8px 16px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
}

.form-actions button[type="submit"] {
  background: #1976d2;
  color: white;
}

.form-actions button[type="button"] {
  background: #eee;
  color: #333;
}

/* 关于弹窗样式 */
.about-modal {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 100;
}

.about-content {
  background: #fff;
  padding: 20px;
  border-radius: 8px;
  width: 400px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  text-align: center;
}

.about-content h2 {
  margin-top: 0;
  font-size: 1.2rem;
  color: #1976d2;
}

.about-content p {
  margin: 10px 0;
  font-size: 14px;
  color: #666;
}

/* 响应式调整 */
@media (max-width: 768px) {
  .file-grid {
    grid-template-columns: repeat(auto-fill, minmax(140px, 1fr));
  }

  .gallery-container {
    padding: 1rem;
    height: 400px;
  }

  .upload-container {
    width: 95%;
    border-radius: 12px;
  }

  .header-wrapper {
    padding: 1rem;
  }

  .page-title {
    font-size: 1.2rem;
  }

  .tab-bar {
    padding: 0 1rem;
    overflow-x: auto;
  }

  .tab {
    padding: 12px 20px;
    font-size: 13px;
    flex-shrink: 0;
  }

  .upload-content {
    padding: 1.5rem;
  }

  .settings-content,
  .about-content {
    width: 90%;
  }
}

@media (max-width: 480px) {
  .tab {
    padding: 10px 16px;
  }
}
</style>