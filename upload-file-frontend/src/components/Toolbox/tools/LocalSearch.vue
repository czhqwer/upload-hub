<template>
  <div class="local-search">
    <el-form :model="searchForm" inline>
      <el-form-item label="选择磁盘：">
        <el-checkbox-group v-model="searchForm.drives">
          <el-checkbox v-for="drive in availableDrives" :key="drive" :label="drive" />
        </el-checkbox-group>
      </el-form-item>
      <el-form-item label="关键字：" style="margin-left: 30px;">
        <el-input v-model="searchForm.keyword" placeholder="输入关键字" clearable />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="searchFiles" :loading="searching">查找</el-button>
      </el-form-item>
    </el-form>

    <!-- 搜索结果区域 -->
    <div class="results-area">
      <!-- 圆形进度条 -->
      <div v-if="searching" class="progress-container">
        <el-progress type="circle" :percentage="progress" :width="120" :stroke-width="8" color="#409EFF" />
      </div>

      <!-- 文件列表 -->
      <el-table v-else-if="searchResults.length > 0" :data="searchResults" style="width: 100%" max-height="400" stripe>
        <el-table-column prop="name" label="文件名" width="200" />
        <el-table-column prop="path" label="路径" />
        <el-table-column label="操作" width="100">
          <template slot-scope="scope">
            <el-button type="text" @click="openLocalDir(scope.row.path)">位置</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 无结果提示 -->
      <div v-else-if="searched" class="no-results">
        未找到匹配的文件
      </div>
    </div>
  </div>
</template>

<script>
import { getLocalDrives, buildIndex, localFileSearch, openDir } from '@/utils/api'
export default {
  name: 'LocalSearch',
  data() {
    return {
      searchForm: {
        drives: [],
        keyword: ''
      },
      availableDrives: ['C:'],
      searchResults: [],
      searching: false,
      searched: false,
      progress: 0,
      progressTimer: null
    }
  },
  mounted() {
    this.fetchAvailableDrives()
  },
  methods: {
    async fetchAvailableDrives() {
      const res = await getLocalDrives()
      if (res.code === 200) {
        this.availableDrives = res.data
      } else {
        this.$message.error('获取磁盘列表失败')
        return
      }
    },
    searchFiles() {
      if (!this.searchForm.drives.length) {
        this.$message.warning('请至少选择一个磁盘')
        return
      }
      if (!this.searchForm.keyword.trim()) {
        this.$message.warning('请输入关键字')
        return
      }

      this.searching = true
      this.searched = false
      this.searchResults = []
      this.progress = 0

      // 开始进度条动画
      this.startProgress()
      buildIndex(this.searchForm.drives).then((res) => {
        if (res.code === 200) {
          localFileSearch(this.searchForm.keyword).then((res) => {
            if (res.code === 200) {
              this.searchResults = res.data
                .filter(path => this.searchForm.drives.some(drive => path.startsWith(drive)))
                .map(path => ({
                  name: path.split('\\').pop(), // 提取文件名
                  path: path
                }))
              this.searching = false
              this.searched = true
            } else {
              this.$message.error('搜索失败')
            }
          })
        }
      }).finally(() => {
        clearInterval(this.progressTimer)
        this.progress = 100
      })
    },
    startProgress() {
      if (this.progressTimer) {
        clearInterval(this.progressTimer)
      }

      let timeElapsed = 0
      const totalTime = 60000 // 60秒

      this.progressTimer = setInterval(() => {
        timeElapsed += 500 // 每0.5秒更新一次

        // 非匀速增长：前快后慢
        const progressRatio = timeElapsed / totalTime
        this.progress = Math.min(99, Math.floor(100 * (1 - Math.pow(1 - progressRatio, 2))))

        if (timeElapsed >= totalTime) {
          clearInterval(this.progressTimer)
        }
      }, 500)
    },
    openLocalDir(path) {
      openDir(path)
    }
  },
  beforeDestroy() {
    // 清理定时器
    if (this.progressTimer) {
      clearInterval(this.progressTimer)
    }
  }
}
</script>

<style scoped>
.local-search {
  padding: 10px;
}

.el-form-item {
  margin-bottom: 15px;
}

.results-area {
  margin-top: 20px;
}

.progress-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 200px;
}

.el-table {
  margin-top: 15px;
  border-radius: 5px;
  overflow: hidden;
}

.no-results {
  text-align: center;
  color: #909399;
  padding: 20px;
  min-height: 200px;
  display: flex;
  align-items: center;
  justify-content: center;
}
</style>
