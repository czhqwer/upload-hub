<template>
  <div class="file-renamer">
    <SelectDir v-model="filePaths" @change="handlePathChange" :showFiles="true" :allowSelectFolder="false"
      :multiple="true" style="width: 100%;" />
    <el-container>
      <el-main>

        <!-- 文件列表 -->
        <el-card v-show="files.length > 0" class="mt-4">
          <div slot="header" class="flex justify-between items-center">
            <span>已选择的文件</span>
            <el-button type="danger" size="small" @click="clearFiles" style="margin-left: 10px;">
              <i class="el-icon-delete mr-1"></i>全部清除
            </el-button>
          </div>
          <el-table :data="files" max-height="250">
            <el-table-column prop="name" label="文件名" width="400"></el-table-column>
            <el-table-column label="操作" width="100">
              <template slot-scope="scope">
                <el-button type="text" size="small" @click="removeFile(scope.$index)">
                  <i class="el-icon-close text-red-500"></i>
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>

        <!-- 重命名选项 -->
        <el-card class="mt-4">
          <el-tabs v-model="activeTab">
            <el-tab-pane label="插入/替换" name="insert">
              <InsertTab />
            </el-tab-pane>
            <el-tab-pane label="删除" name="delete">
              <DeleteTab />
            </el-tab-pane>
            <el-tab-pane label="编号" name="numbering">
              <NumberingTab />
            </el-tab-pane>
            <el-tab-pane label="清理" name="cleanup">
              <CleanupTab />
            </el-tab-pane>
            <el-tab-pane label="高级" name="advanced">
              <AdvancedTab />
            </el-tab-pane>
          </el-tabs>
        </el-card>

        <!-- 操作按钮组 -->
        <div class="action-buttons mt-4">
          <el-button size="medium" @click="reset">重置</el-button>
          <el-button size="medium" type="info" @click="showPreview">预览</el-button>
          <el-button size="medium" type="primary" @click="rename">重命名</el-button>
        </div>

        <!-- 预览对话框 -->
        <el-dialog
          title="重命名预览"
          :visible.sync="previewDialogVisible"
          width="70%"
          :modal="false"
          :append-to-body="true"
          :before-close="handlePreviewClose">
          <div class="preview-area">
            <div v-if="files.length === 0" class="text-center text-gray-500 py-8">
              <i class="el-icon-document text-3xl mb-2"></i>
              <p>未选择文件或预览不可用</p>
            </div>
            <el-table v-else :data="previewData" max-height="400">
              <el-table-column prop="original" label="原文件名" width="300"></el-table-column>
              <el-table-column label="→" width="50" align="center"></el-table-column>
              <el-table-column prop="new" label="新文件名" width="300"></el-table-column>
            </el-table>
          </div>
          <span slot="footer" class="dialog-footer">
            <el-button @click="previewDialogVisible = false">关闭</el-button>
            <el-button type="primary" @click="rename">确认重命名</el-button>
          </span>
        </el-dialog>
      </el-main>
    </el-container>
  </div>
</template>

<script>
import SelectDir from '@/components/SelectDir/SelectDir.vue'
import InsertTab from './BatchRename/InsertTab.vue'
import DeleteTab from './BatchRename/DeleteTab.vue'
import NumberingTab from './BatchRename/NumberingTab.vue'
import CleanupTab from './BatchRename/CleanupTab.vue'
import AdvancedTab from './BatchRename/AdvancedTab.vue'

export default {
  components: {
    SelectDir,
    InsertTab,
    DeleteTab,
    NumberingTab,
    CleanupTab,
    AdvancedTab
  },
  data() {
    return {
      files: [],
      activeTab: 'insert',
      isDragging: false,
      filePaths: '',
      previewDialogVisible: false
    }
  },
  computed: {
    previewData() {
      return this.files.map(file => ({
        original: file.name,
        new: file.name // 这里应实现实际的重命名逻辑
      }))
    }
  },
  methods: {
    handlePathChange(newPaths) {
      this.filePaths = newPaths
      // Handle both string and array inputs
      const pathArray = Array.isArray(newPaths) ? newPaths : [newPaths]
      const newFiles = pathArray.map(path => ({
        name: path.split('\\').pop(), // Get the last part of the path as filename
        path: path // Store the full path
      }))

      // Filter out duplicates before adding
      const uniqueNewFiles = newFiles.filter(newFile =>
        !this.files.some(existingFile => existingFile.path === newFile.path)
      )

      this.files.push(...uniqueNewFiles)
    },
    handleDrop(e) {
      this.unhighlight()
      const newFiles = [...e.dataTransfer.files]
      this.addFiles(newFiles)
    },
    handleFileInput() {
      const newFiles = [...this.$refs.fileInput.files]
      this.addFiles(newFiles)
    },
    highlight() {
      this.isDragging = true
    },
    unhighlight() {
      this.isDragging = false
    },
    addFiles(newFiles) {
      newFiles = newFiles.filter(newFile =>
        !this.files.some(f =>
          f.name === newFile.name &&
          f.size === newFile.size &&
          f.lastModified === newFile.lastModified
        )
      )
      this.files = [...this.files, ...newFiles]
    },
    removeFile(index) {
      this.files.splice(index, 1)
    },
    clearFiles() {
      this.files = []
    },
    reset() {
      this.$message.success('所有重命名规则已重置')
    },
    rename() {
      if (this.files.length === 0) {
        this.$message.warning('请选择要重命名的文件')
        return
      }
      this.$message.success(`将重命名 ${this.files.length} 个文件（此演示为模拟操作）`)
    },
    showPreview() {
      if (this.files.length === 0) {
        this.$message.warning('请选择要重命名的文件')
        return
      }
      this.previewDialogVisible = true
    },
    handlePreviewClose(done) {
      this.$confirm('确认关闭预览？')
        .then(() => {
          done()
        })
        .catch(() => {})
    }
  }
}
</script>

<style scoped>
.file-renamer {
  padding: 20px;
  max-width: 100%;
  margin: 0 auto;
}

.dropzone {
  border: 2px dashed #dcdcdc;
  padding: 20px;
  transition: all 0.3s ease;
  cursor: pointer;
}

.dropzone:hover {
  border-color: #409eff;
  background-color: #ecf5ff;
}

.preview-area {
  min-height: 200px;
}

.mt-4 {
  margin-top: 10px;
}

.action-buttons {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  padding: 10px 0;
}
</style>