<template>
  <div class="file-encrypt-container">
    <el-form :model="form" label-width="100px">
      <!-- 文件上传 -->
      <el-form-item label="上传文件">
        <el-upload
          ref="upload"
          action=""
          :http-request="handleUpload"
          :before-upload="beforeUpload"
          :multiple="false"
          :show-file-list="false"
        >
          <el-button type="primary">点击上传文件</el-button>
        </el-upload>
      </el-form-item>

      <!-- 加密密钥 -->
      <el-form-item label="密钥">
        <el-input
          v-model="form.key"
          placeholder="请输入加密/解密密钥"
          style="width: 300px;"
          show-password
        ></el-input>
      </el-form-item>

      <!-- 操作按钮 -->
      <el-form-item>
        <el-button type="success" @click="encryptFile" :disabled="!form.file || !form.key">加密文件</el-button>
        <el-button type="warning" @click="decryptFile" :disabled="!form.file || !form.key">解密文件</el-button>
      </el-form-item>
    </el-form>

    <!-- 下载链接 -->
    <div v-if="downloadUrl" class="download-section">
      <el-link :href="downloadUrl" type="primary" :download="downloadFileName">点击下载处理后的文件</el-link>
    </div>
  </div>
</template>

<script>
import { encryptFile, decryptFile } from '@/utils/api';

export default {
  name: 'FileEncrypt',
  data() {
    return {
      form: {
        file: null, // 上传的文件对象
        key: '',
      },
      downloadUrl: '', // 下载链接
      downloadFileName: '', // 下载文件名
    };
  },
  methods: {
    // 文件上传前校验
    beforeUpload(file) {
      this.form.file = file;
      this.downloadUrl = ''; // 清空之前的下载链接
      return false; // 阻止默认上传，使用自定义 http-request
    },
    // 自定义上传处理
    async handleUpload(option) {
      // 文件已通过 beforeUpload 保存到 form.file，无需额外处理
      console.log('handleUpload', option);
    },
    // 加密文件
    async encryptFile() {
      if (!this.validateForm()) return;

      const formData = new FormData();
      formData.append('file', this.form.file);
      formData.append('key', this.form.key);

      try {
        const res = await encryptFile(formData);
        if (res.code === 200) {
          this.handleFileResponse(res.data, this.form.file.name + '.enc');
          this.$message.success('文件加密成功，请下载');
        } else {
          this.$message.error(res.msg || '加密失败');
        }
      } catch (error) {
        this.$message.error('加密请求失败');
        console.error(error);
      }
    },
    // 解密文件
    async decryptFile() {
      if (!this.validateForm()) return;

      const formData = new FormData();
      formData.append('file', this.form.file);
      formData.append('key', this.form.key);

      try {
        const res = await decryptFile(formData);
        if (res.code === 200) {
          const originalName = this.form.file.name.replace('.enc', '');
          this.handleFileResponse(res.data, originalName);
          this.$message.success('文件解密成功，请下载');
        } else {
          this.$message.error(res.msg || '解密失败');
        }
      } catch (error) {
        this.$message.error('解密请求失败');
        console.error(error);
      }
    },
    // 处理后端返回的文件
    handleFileResponse(blob, fileName) {
      const url = window.URL.createObjectURL(blob);
      this.downloadUrl = url;
      this.downloadFileName = fileName;
    },
    // 表单校验
    validateForm() {
      if (!this.form.file) {
        this.$message.warning('请上传文件');
        return false;
      }
      if (!this.form.key) {
        this.$message.warning('请输入密钥');
        return false;
      }
      return true;
    },
  },
};
</script>

<style scoped>
.file-encrypt-container {
  padding: 20px;
}

.download-section {
  margin-top: 20px;
}
</style>