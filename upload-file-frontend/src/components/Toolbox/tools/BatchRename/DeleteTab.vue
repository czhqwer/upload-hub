<template>
  <div class="grid grid-cols-2 gap-4">
    <div>
      <el-form label-width="80px">
        <el-form-item label="按文本删除">
          <div class="flex gap-2">
            <el-input v-model="deleteText" placeholder="要删除的文本" class="flex-1"></el-input>
            <el-select v-model="deleteOption" class="flex-1">
              <el-option label="全部删除" value="all"></el-option>
              <el-option label="仅首次" value="first"></el-option>
              <el-option label="仅最后" value="last"></el-option>
            </el-select>
          </div>
        </el-form-item>
        <el-form-item label="按位置删除">
          <div class="flex gap-2 items-center">
            <el-input-number v-model="startPos" :min="1" size="small" placeholder="起始"></el-input-number>
            <span>至</span>
            <el-input-number v-model="endPos" :min="1" size="small" placeholder="结束"></el-input-number>
            <span>字符</span>
          </div>
        </el-form-item>
      </el-form>
    </div>
    <div>
      <el-form label-width="80px">
        <el-form-item label="删除文件名">
          <el-checkbox v-model="keepExt">保留扩展名</el-checkbox>
          <el-button type="danger" size="small" class="ml-2" @click="deleteAllNames">删除所有名称</el-button>
        </el-form-item>
        <el-form-item label="删除扩展名">
          <div class="flex gap-2">
            <el-button size="small" @click="removeAllExt">移除所有扩展名</el-button>
            <el-input v-model="specificExt" placeholder=".ext" style="width: 100px;"></el-input>
            <el-button size="small" @click="removeSpecificExt">移除特定扩展名</el-button>
          </div>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script>
export default {
  data() {
    return {
      deleteText: '',
      deleteOption: 'all',
      startPos: 1,
      endPos: 1,
      keepExt: true,
      specificExt: ''
    }
  },
  methods: {
    deleteAllNames() {
      this.$message.info('删除所有名称（模拟操作）')
    },
    removeAllExt() {
      this.$message.info('移除所有扩展名（模拟操作）')
    },
    removeSpecificExt() {
      if (!this.specificExt) {
        this.$message.warning('请输入要移除的扩展名')
        return
      }
      this.$message.info(`移除扩展名 ${this.specificExt}（模拟操作）`)
    }
  }
}
</script>