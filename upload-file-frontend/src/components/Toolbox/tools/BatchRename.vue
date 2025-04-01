<template>
  <div class="batch-rename">
    <el-upload
      drag
      multiple
      :auto-upload="false"
      :on-change="handleFileChange"
    >
      <i class="el-icon-upload"></i>
      <div class="el-upload__text">将文件拖到此处，或<em>点击上传</em></div>
    </el-upload>
    <el-input v-model="renameRule" placeholder="输入重命名规则" class="rename-input"></el-input>
    <el-button type="primary" @click="rename" class="action-btn">执行重命名</el-button>
  </div>
</template>

<script>
export default {
  name: 'BatchRename',
  data() {
    return {
      renameRule: ''
    }
  },
  methods: {
    handleFileChange(file, fileList) {
      console.log('文件列表:', fileList)
    },
    rename() {
      this.$message.success('批量重命名功能待实现')
    }
  }
}
</script>

<style scoped>
.batch-rename {
  padding: 10px;
}
.rename-input {
  margin: 15px 0;
}
.action-btn {
  margin-top: 10px;
}
</style>