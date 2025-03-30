<template>
  <div class="share-file-page">
    <!-- 顶部信息 -->
    <div class="top-info" :class="{ 'sharing-active': enableShare }">
      <div class="info-content">
        <!-- 状态徽章 -->
        <div class="status-badge">
          <i class="el-icon-share"></i>
          <span>{{ enableShare ? '实时分享中' : '未开启分享' }}</span>
          <div class="glow"></div>
          <!-- 信息组 -->
          <div class="info-group">
            <div class="info-item">
              <i class="el-icon-link icon-gradient"></i>
              <div class="info-text">
                <label>分享地址</label>
                <div class="address-box">
                  <span class="address">{{ shareAddress }}</span>
                  <el-button type="text" class="copy-btn" @click="copyLink(shareAddress)">
                    <i class="el-icon-document-copy"></i>
                  </el-button>
                </div>
              </div>
            </div>
          </div>
        </div>

      </div>

      <el-button :disabled="!isAdmin" :type="enableShare ? 'danger' : 'primary'" size="medium" @click="stopSharing" class="stop-btn">
        <i class="el-icon-switch-button"></i>
        {{ enableShare ? '终止分享' : '开始分享' }}
      </el-button>
    </div>

    <!-- 文件表格区域 -->
    <div class="table-container">
      <el-table :data="fileList" style="width: 100%" class="custom-table" max-height="260">
        <el-table-column prop="fileName" label="文件名" min-width="240">
          <template slot-scope="scope">
            <div class="file-name-cell">
              <i class="el-icon-document"></i>
              <span class="file-name">{{ scope.row.fileName }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="totalSize" label="大小" width="120" align="right">
          <template slot-scope="scope">
            <span class="file-size">{{ formatSize(scope.row.totalSize) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" align="center">
          <template slot-scope="scope">
            <el-button type="primary" size="mini" plain @click="downloadFile(scope.row)" class="action-btn">
              <i class="el-icon-download"></i>
            </el-button>
            <el-button type="success" size="mini" plain @click="copyLink(scope.row.accessUrl)" class="action-btn">
              <i class="el-icon-link"></i>
            </el-button>
            <el-button v-if="isAdmin" type="danger" size="mini" plain @click="deleteFile(scope.row.fileIdentifier)" class="action-btn">
              <i class="el-icon-delete"></i>
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>
  </div>
</template>

<script>
import sseManager from '@/utils/sse'
import { getSharedFiles, enableShare, getShareStatus, shareAddress, unShareFile } from '@/utils/api';

export default {
  name: 'ShareFile',
  data() {
    return {
      activeTab: 'share',
      shareAddress: 'http://localhost:10086',
      protocol: 'IPv4',
      fileList: [],
      enableShare: false,
      isAdmin: false,
    };
  },
  mounted() {
    this.updateShareStatusAttribute();
    this.fetchFiles();
    this.getShareStatus();
    this.getShareAddress();
    this.getAuth();

    sseManager.subscribe('sharedFileUpdate', () => {
      this.fetchFiles();
    });

    sseManager.subscribe('enableShare', (event) => {
      this.fileList = [];
      this.enableShare = event.enable;
      this.fetchFiles();
    });
  },

  beforeDestroy() {
    sseManager.unsubscribe('sharedFileUpdate');
    sseManager.unsubscribe('enableShare');
  },
  watch: {
    enableShare() {
      this.updateShareStatusAttribute();
    }
  },
  methods: {
    updateShareStatusAttribute() {
      this.$el.setAttribute('data-enable-share', this.enableShare);
    },
    getAuth() {
      const isAdmin = localStorage.getItem('main')
      this.isAdmin = isAdmin === 'true'
    },
    async getShareAddress() {
      try {
        const res = await shareAddress();
        this.shareAddress = res.data;
      } catch (error) {
        console.error('Error fetching share address:', error);
      }
    },
    async getShareStatus() {
      try {
        const res = await getShareStatus();
        this.enableShare = res.data;
      } catch (error) {
        console.error('获取共享状态失败:', error);
      }
    },
    async fetchFiles() {
      const res = await getSharedFiles();
      if (res.code === 200) {
        this.fileList = res.data;
      }
    },
    async downloadFile(file) {
      try {
        const adjusteUrl = this.replaceLocalhost(file.accessUrl)
        const response = await fetch(adjusteUrl);
        if (!response.ok) {
          throw new Error('网络响应失败');
        }
        const blob = await response.blob();
        // 创建 Blob URL 并触发下载
        const url = window.URL.createObjectURL(blob);
        const link = document.createElement('a');
        link.href = url;
        link.setAttribute('download', file.fileName); // 设置下载文件名
        document.body.appendChild(link);
        link.click();
        document.body.removeChild(link);
        window.URL.revokeObjectURL(url); // 清理内存
        this.$message.success(`文件 ${file.fileName} 开始下载`);
      } catch (error) {
        console.error('下载文件失败:', error);
        this.$message.error(`下载 ${file.fileName} 失败`);
      }
    },
    replaceLocalhost(url) {
      if (!this.shareAddress || !url) return url;
      const shareIp = new URL(this.shareAddress).hostname;
      return url.replace(/localhost/g, shareIp);
    },
    copyLink(accessUrl) {
      const adjusteUrl = this.replaceLocalhost(accessUrl)
      if (navigator.clipboard && navigator.clipboard.writeText) {
        navigator.clipboard.writeText(adjusteUrl)
          .then(() => {
            this.$message.success('复制成功');
          })
          .catch(err => {
            console.error('无法使用 Clipboard API 复制: ', err);
            this.fallbackCopyTextToClipboard(adjusteUrl);
          });
      } else {
        this.fallbackCopyTextToClipboard(adjusteUrl);
      }
    },
    fallbackCopyTextToClipboard(text) {
      const textArea = document.createElement('textarea');
      textArea.value = text;
      textArea.style.position = 'fixed';
      textArea.style.left = '-9999px';
      textArea.style.top = '-9999px';
      document.body.appendChild(textArea);
      textArea.focus();
      textArea.select();

      try {
        const successful = document.execCommand('copy');
        const msg = successful ? '复制成功' : '复制失败';
        this.$message.success(msg);
      } catch (err) {
        console.error('无法使用 execCommand 复制: ', err);
        this.$message.error('复制失败');
      }

      document.body.removeChild(textArea);
    },
    deleteFile(fileIdentifier) {
      this.$confirm('确定要移除此文件吗?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {

        const res = await unShareFile(fileIdentifier);
        if (res.code === 200) {
          this.fetchFiles();
          this.$message.success('移除成功');
        }

      }).catch(() => {
        this.$message.info('已取消删除');
      });
    },

    formatSize(size) {
      if (size < 1024) return size + ' B';
      if (size < 1024 * 1024) return (size / 1024).toFixed(2) + ' KB';
      return (size / (1024 * 1024)).toFixed(2) + ' MB';
    },
    async stopSharing() {
      const res = await enableShare({ enable: !this.enableShare });
      if (res.code === 200) {
        this.getShareStatus();
        if (this.enableShare) {
          this.$message.success('已关闭分享');
        } else {
          this.$message.success('已开启分享');
        }
      }
    },
  },
  computed: {
  },
};
</script>

<style scoped>
.share-file-page {
  padding: 24px;
  background: #f5f7fa;
  height: 400px;
  font-family: 'Segoe UI', system-ui, sans-serif;
}

.top-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: linear-gradient(145deg, #ffffff, #f8faff);
  padding-right: 20px;
  border-radius: 16px;
  margin-bottom: 24px;
  box-shadow: 0 8px 32px rgba(0, 50, 150, 0.08);
  position: relative;
  overflow: hidden;
  border: 1px solid rgba(64, 158, 255, 0.12);
}

/* 状态徽章 */
.status-badge {
  position: relative;
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 8px 16px;
  background: #FFFFFF;
  border-radius: 8px;
  color: #409EFF;
  font-weight: 600;
}

.status-badge i {
  font-size: 20px;
  animation: pulse 1.5s infinite;
}

.glow {
  position: absolute;
  top: 0;
  left: -100%;
  width: 50%;
  height: 100%;
  background: linear-gradient(90deg,
      transparent,
      rgba(64, 158, 255, 0.1),
      transparent);
}

.share-file-page[data-enable-share="true"] :deep(.glow) {
  animation: glow-animation 2s infinite;
}

/* 信息组样式 */
.info-group {
  display: flex;
  gap: 32px;
}

.info-item {
  display: flex;
  align-items: center;
  gap: 12px;
}

.icon-gradient {
  font-size: 24px;
  background: linear-gradient(135deg, #409EFF, #79bbff);
  -webkit-background-clip: text;
  background-clip: text;
  color: transparent;
}

.info-text label {
  display: block;
  color: #7a8599;
  font-size: 12px;
  margin-bottom: 4px;
}

.address-box {
  display: flex;
  align-items: center;
  gap: 8px;
  background: rgba(64, 158, 255, 0.05);
  padding: 6px 12px;
  border-radius: 6px;
  border: 1px solid rgba(64, 158, 255, 0.15);
}

.address {
  font-family: 'JetBrains Mono', monospace;
  color: #2c3e50;
  font-size: 14px;
}

.copy-btn {
  padding: 4px;
  transition: all 0.2s;
}

.copy-btn:hover {
  color: #409EFF;
  transform: scale(1.1);
}

.protocol {
  font-weight: 600;
  color: #409EFF;
  font-size: 15px;
}

/* 按钮样式 */
.stop-btn {
  padding: 10px 24px;
  border-radius: 8px;
  font-weight: 600;
  transition: all 0.2s;
  border: none;
  box-shadow: 0 4px 12px rgba(255, 107, 107, 0.2);
}

.stop-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 6px 16px rgba(255, 107, 107, 0.3);
}

/* 动画 */
@keyframes pulse {
  0% {
    opacity: 0.9;
  }

  50% {
    opacity: 0.6;
  }

  100% {
    opacity: 0.9;
  }
}

@keyframes glow-animation {
  0% {
    left: -100%;
  }

  100% {
    left: 200%;
  }
}

@media (max-width: 768px) {
  .top-info {
    flex-direction: column;
    gap: 16px;
    align-items: stretch;
  }

  .info-group {
    flex-direction: column;
    gap: 16px;
  }

  .stop-btn {
    align-self: flex-end;
  }
}

.table-container {
  background: white;
  border-radius: 16px;
  padding: 10px;
  box-shadow: 0 4px 24px rgba(0, 0, 0, 0.06);
  height: 265px;
  overflow-y: auto;
}

.table-container::-webkit-scrollbar {
  width: 8px;
  height: 8px;
}

.table-container::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 4px;
}

.table-container::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 4px;
}

.table-container::-webkit-scrollbar-thumb:hover {
  background: #a8a8a8;
}

.custom-table {
  --el-table-border-color: transparent;
  --el-table-header-bg-color: #f8f9fc;
  height: calc(100% - 5px);
}

.custom-table::before {
  display: none;
}

.custom-table :deep(.el-table__header th) {
  background: #f8f9fc;
  color: #5e6d82;
  font-weight: 600;
  border-bottom: 2px solid #f0f2f7;
}

.custom-table :deep(.el-table__row) {
  transition: background 0.2s ease;
}

.custom-table :deep(.el-table__row:hover) {
  background: #f8fafd !important;
}

.file-name-cell {
  display: flex;
  align-items: center;
  gap: 12px;
}

.file-name-cell i {
  font-size: 20px;
  color: #909399;
}

.file-name {
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 1;
  -webkit-box-orient: vertical;
}

.file-size {
  font-family: 'JetBrains Mono', monospace;
  color: #7a7d89;
}

.action-btn {
  border-radius: 6px;
  transition: all 0.2s ease !important;
}

.action-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(245, 108, 108, 0.2);
}

/* 调整按钮间距 */
.action-btn {
  background: #fff;
  border: none;
  border-radius: 6px;
  transition: all 0.2s ease !important;
  margin: 0 2px;
  /* 添加按钮间距 */
  padding: 5px;
  /* 调整内边距使按钮更紧凑 */
}

.action-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.2);
}

/* 调整不同类型按钮的悬浮效果 */
.action-btn[type="primary"]:hover {
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.2);
}

.action-btn[type="success"]:hover {
  box-shadow: 0 4px 12px rgba(103, 194, 58, 0.2);
}

.action-btn[type="danger"]:hover {
  box-shadow: 0 4px 12px rgba(245, 108, 108, 0.2);
}

@media (max-width: 768px) {
  .top-info {
    flex-direction: column;
    align-items: stretch;
    gap: 16px;
  }

  .stop-btn {
    align-self: flex-end;
  }

  .table-container {
    border-radius: 12px;
    padding: 8px;
  }
}
</style>