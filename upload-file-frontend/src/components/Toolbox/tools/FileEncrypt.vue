<template>
  <div class="file-encrypt">
    <el-form :model="form" label-width="100px">
      <el-form-item label="文件内容">
        <el-input v-model="form.content" type="textarea" :rows="5"></el-input>
      </el-form-item>
      <el-form-item label="密码">
        <el-input v-model="form.password" type="password"></el-input>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="encrypt" class="action-btn">加密</el-button>
      </el-form-item>
    </el-form>
  </div>
</template>
<script>
export default {
  name: 'FileEncrypt',
  data() {
    return {
      form: {
        content: '',
        password: ''
      }
    }
  },
  methods: {
    encrypt() {
      this.$message.success('加密功能待实现')
    }
  }
}
</script>

<style scoped>
.file-encrypt {
  padding: 10px;
}
.action-btn {
  margin-left: 100px;
}
</style>