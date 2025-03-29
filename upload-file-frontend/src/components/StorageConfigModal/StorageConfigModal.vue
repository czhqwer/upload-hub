<template>
  <div v-if="show" class="storage-config-modal" @click="closeOnOutside">
    <div class="storage-config-content" @click.stop>
      <h2>存储系统配置</h2>
      <div class="form-group">
        <label>存储类型:</label>
        <select v-model="config.type" @change="fetchConfig">
          <option v-for="option in storageOptions" :key="option.value" :value="option.value">
            {{ option.label }}
          </option>
        </select>
      </div>
      <div class="form-group">
        <label>Endpoint:</label>
        <input v-model="config.endpoint" type="text" placeholder="请输入Endpoint" />
      </div>
      <div class="form-group" v-if="config.type !== 'local'">
        <label>Access Key:</label>
        <input v-model="config.accessKey" type="text" placeholder="请输入Access Key" />
      </div>
      <div class="form-group" v-if="config.type !== 'local'">
        <label>Secret Key:</label>
        <input v-model="config.secretKey" type="password" placeholder="请输入Secret Key" />
      </div>
      <div class="form-group">
        <label>Bucket:</label>
        <input v-model="config.bucket" type="text" placeholder="请输入Bucket" />
      </div>
      <div class="form-actions">
        <button type="button" @click="testConfig" :disabled="testing">
          {{ testing ? '测试中...' : '测试连接' }}
        </button>
        <button type="button" @click="saveConfig" :disabled="testing">保存</button>
        <button type="button" @click="close" :disabled="testing">取消</button>
      </div>
    </div>
  </div>
</template>

<script>
import { getStorageConfig, setStorageConfig, testStorageConfig } from '@/utils/api';

export default {
  name: 'StorageConfigModal',
  props: {
    show: Boolean,
    storageOptions: Array
  },
  data() {
    return {
      config: {
        type: 'local',
        endpoint: '',
        accessKey: '',
        secretKey: '',
        bucket: ''
      },
      testing: false
    };
  },
  methods: {
    async fetchConfig() {
      try {
        const response = await getStorageConfig({ type: this.config.type });
        const config = response.data;
        this.config = config ? { ...config } : {
          type: this.config.type,
          endpoint: '',
          accessKey: '',
          secretKey: '',
          bucket: ''
        };
      } catch (error) {
        console.error('获取存储配置失败:', error);
        this.$message.error('获取存储配置失败');
      }
    },
    async saveConfig() {
      try {
        await setStorageConfig(this.config);
        this.$message.success('存储配置保存成功');
        this.close();
      } catch (error) {
        console.error('保存存储配置失败:', error);
        this.$message.error('保存存储配置失败');
      }
    },
    async testConfig() {
      this.testing = true;
      try {
        const response = await testStorageConfig(this.config);
        if (response.code === 200) {
          this.$message.success('存储配置测试成功！');
        }
      } catch (error) {
        console.error('存储配置测试失败:', error);
        this.$message.error(`${error.message || '服务器错误'}`);
      } finally {
        this.testing = false;
      }
    },
    close() {
      this.$emit('close');
    },
    closeOnOutside(event) {
      if (event.target.classList.contains('storage-config-modal')) this.close();
    }
  },
  mounted() {
    this.fetchConfig();
  }
};
</script>

<style scoped>
.storage-config-modal {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(25, 118, 210, 0.2);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}

.storage-config-content {
  background: #ffffff;
  padding: 2rem;
  border-radius: 12px;
  width: 480px;
  box-shadow: 0 8px 32px rgba(25, 118, 210, 0.15);
}

.storage-config-content h2 {
  color: #1976d2;
  font-size: 1.5rem;
  margin: 0 0 2rem;
  text-align: center;
}

.form-group {
  margin-bottom: 1.5rem;
}

.form-group label {
  display: block;
  margin-bottom: 0.5rem;
  font-size: 0.9rem;
  color: #4a5568;
  font-weight: 500;
}

.form-group input,
.form-group select {
  width: 93%;
  padding: 0.75rem 1rem;
  border: 1px solid #cbd5e0;
  border-radius: 8px;
  font-size: 0.95rem;
  background: #f8fafc;
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 24px;
}

.form-actions button {
  padding: 10px 20px;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 500;
}

.form-actions button:first-child {
  background: #ff9800;
  color: white;
}

.form-actions button:nth-child(2) {
  background: #1976d2;
  color: white;
}

.form-actions button:last-child {
  background: #f0f7ff;
  color: #1976d2;
  border: 1px solid #d0e4fc;
}

.form-actions button:disabled {
  background: #cccccc;
  cursor: not-allowed;
}
</style>