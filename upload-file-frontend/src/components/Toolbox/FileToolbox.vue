<template>
  <div class="toolbox-container">
    <el-row :gutter="20" class="tools-grid">
      <el-col :span="6" v-for="(tool, index) in tools" :key="index">
        <el-card 
          class="tool-card" 
          @click.native="openTool(tool)"
          shadow="hover"
          :style="{ '--tool-color': tool.color }"
        >
          <div class="tool-item">
            <i :class="tool.icon" class="tool-icon"></i>
            <span class="tool-name">{{ tool.name }}</span>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 动态模态框 -->
    <el-dialog
      :title="currentTool ? currentTool.name : ''"
      :visible.sync="dialogVisible"
      width="60%"
      :before-close="handleClose"
      custom-class="tool-dialog"
    >
      <component v-if="currentTool" :is="currentTool.component" />
    </el-dialog>
  </div>
</template>

<script>
import FileTree from './tools/FileTree.vue'
import FileDiff from './tools/FileDiff.vue'
import FileEncrypt from './tools/FileEncrypt.vue'
import BatchRename from './tools/BatchRename.vue'
import FormatConvert from './tools/FormatConvert.vue'
import LocalSearch from './tools/LocalSearch.vue'

export default {
  name: 'FileToolbox',
  components: {
    FileTree,
    FileDiff,
    FileEncrypt,
    BatchRename,
    FormatConvert,
    LocalSearch,
  },
  data() {
    return {
      tools: [
      { name: '本地查找', icon: 'el-icon-search', component: 'LocalSearch', color: '#7232A8' },
        { name: '文件树状图', icon: 'el-icon-folder-opened', component: 'FileTree', color: '#409EFF' },
        { name: 'Diff比较', icon: 'el-icon-document-copy', component: 'FileDiff', color: '#67C23A' },
        { name: '文件加密', icon: 'el-icon-lock', component: 'FileEncrypt', color: '#E6A23C' },
        { name: '批量修改文件名', icon: 'el-icon-edit', component: 'BatchRename', color: '#F56C6C' },
        { name: '文件格式转换', icon: 'el-icon-refresh', component: 'FormatConvert', color: '#909399' }
      ],
      currentTool: null,
      dialogVisible: false
    }
  },
  methods: {
    openTool(tool) {
      this.currentTool = tool
      this.dialogVisible = true
    },
    handleClose(done) {
      this.$confirm('确定关闭工具吗？')
        .then(() => {
          this.dialogVisible = false
          this.currentTool = null
          done()
        })
        .catch(() => {})
    }
  }
}
</script>

<style scoped>
.toolbox-container {
  padding: 30px;
  height: 400px;
  background: #f5f7fa;
}

.tools-grid {
  max-width: 1200px;
  margin: 0 auto;
}

.tool-card {
  margin-bottom: 20px;
  cursor: pointer;
  border-radius: 10px;
  background: #ffffff;
  transition: all 0.3s ease;
  overflow: hidden;
}

.tool-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 10px 20px rgba(0, 0, 0, 0.1);
}

.tool-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 25px;
  position: relative;
}

.tool-icon {
  font-size: 32px;
  margin-bottom: 15px;
  transition: all 0.3s ease;
}

.tool-card:hover .tool-icon {
  transform: scale(1.1);
}

.tool-name {
  font-size: 16px;
  font-weight: 500;
  color: #303133;
}

.tool-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 4px;
  background: var(--tool-color);
}

/* 自定义模态框样式 */
:deep(.tool-dialog) {
  border-radius: 10px;
  overflow: hidden;
}

:deep(.tool-dialog .el-dialog__header) {
  background: #f5f7fa;
  padding: 15px 20px;
  border-bottom: 1px solid #eee;
}

:deep(.tool-dialog .el-dialog__title) {
  color: #303133;
  font-size: 20px;
  font-weight: 600;
}

:deep(.tool-dialog .el-dialog__body) {
  padding: 20px;
  background: #ffffff;
}

:deep(.el-card.is-hover-shadow:hover)::before {
  display: none;
}

:deep(.el-card.is-hover-shadow)::before {
  display: none;
}
</style>