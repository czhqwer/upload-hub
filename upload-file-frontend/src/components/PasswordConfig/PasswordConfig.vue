<template>
  <div>
    <div v-if="show" class="settings-modal" @click="closeOnOutside">
      <div class="settings-content" @click.stop>
        <h2>密码设置</h2>
        <form @submit.prevent="save">
          <div class="form-group">
            <label>是否开启密码校验:</label>
            <div class="radio-group">
              <label class="radio-label">
                <input type="radio" name="enable" :value="true" v-model="tempEnablePassword" />
                是
              </label>
              <label class="radio-label">
                <input type="radio" name="enable" :value="false" v-model="tempEnablePassword" />
                否
              </label>
            </div>
          </div>
          <div class="form-group">
            <label>密码:</label>
            <input v-model="tempPassword" type="text" placeholder="请输入密码" :disabled="!tempEnablePassword" />
          </div>
          <div class="form-actions">
            <button type="submit">保存</button>
            <button type="button" @click="close">取消</button>
          </div>
        </form>
      </div>
    </div>
    <div class="password-config-modal">
      <el-dialog title="请输入密码" :visible.sync="showPasswordDialog" width="30%" :close-on-click-modal="false"
        :show-close="false" custom-class="password-dialog">
        <el-input v-model="inputPassword" type="text" placeholder="请输入密码" />
        <span slot="footer" class="dialog-footer">
          <el-button @click="verifyPassword">确定</el-button>
        </span>
      </el-dialog>
    </div>
  </div>
</template>

<script>
import SparkMD5 from 'spark-md5'
import sseManager from '@/utils/sse';
import { getPassword, setPassword } from '@/utils/api';
export default {
  name: 'PasswordConfig',
  props: {
    show: Boolean,
  },
  data() {
    return {
      tempEnablePassword: false,
      tempPassword: '',
      inputPassword: '',
      showPasswordDialog: false,
      mainUser: false,
    };
  },
  mounted() {
    this.getPassword();
    sseManager.subscribe('setPassword', () => {
      if (!this.mainUser) {
        this.getPassword();
      }
    });
  },
  methods: {
    async getPassword() {
      try {
        const res = await getPassword();
        const dataStr = res.data.toString();
        const password = dataStr.slice(0, -1);
        const mainUser = dataStr.slice(-1) === '1';
        this.mainUser = mainUser;
        this.tempPassword = password;
        if (password) {
          this.tempEnablePassword = true;
          this.showPasswordDialog = true;
        }

        localStorage.setItem('main', mainUser);
        localStorage.setItem('password', password);
        this.$emit('main-user', mainUser);
      } catch (error) {
        console.error('获取密码失败:', error);
      }
    },
    async save() {
      if (this.tempEnablePassword && !this.tempPassword) {
        this.$message.error('请输入密码');
        return;
      }

      let hashedPassword = '';
      if (this.tempEnablePassword) {
        const spark = new SparkMD5();
        spark.append(this.tempPassword);
        hashedPassword = spark.end();
      }

      const res = await setPassword(hashedPassword);
      if (res.code === 200) {
        this.$message.success('保存成功');
        this.close();
      }
    },
    verifyPassword() {
      let hashedPassword = '';
      const spark = new SparkMD5();
      spark.append(this.inputPassword);
      hashedPassword = spark.end();

      if (this.tempPassword === hashedPassword) {
        this.tempPassword = this.inputPassword;
        this.showPasswordDialog = false;
        this.inputPassword = '';
      } else {
        this.$message.error('密码错误');
      }
    },
    close() {
      this.$emit('close');
    },
    closeOnOutside(event) {
      if (event.target.classList.contains('settings-modal')) this.close();
    }
  }
};
</script>

<style scoped>
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

.radio-group {
  display: flex;
  gap: 20px;
}

.radio-group label {
  display: flex;
  align-items: center;
  gap: 5px;
  font-size: 14px;
  color: #333;
}

.form-group input {
  width: 95%;
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

:deep(.password-dialog .el-dialog) {
  border-radius: 12px;
  box-shadow: 0 8px 32px rgba(0, 0, blu0e, 0.2);
  background: linear-gradient(135deg, #ffffff, #f8faff);
}

:deep(.password-dialog .el-dialog__header) {
  padding: 15px 20px;
  border-bottom: 1px solid rgba(64, 158, 255, 0.1);
}

:deep(.password-dialog .el-dialog__title) {
  font-size: 16px;
  font-weight: 600;
  color: #1976d2;
}

:deep(.password-dialog .el-dialog__inner) {
  border-radius: 6px;
  border: 1px solid #e0e0e0;
  padding: 10px;
  font-size: 15px;
}

:deep(.password-dialog .el-button--primary) {
  background: linear-gradient(135deg, #1976d2, #42a5f5);
  border: none;
  padding: 10px 20px;
  border-radius: 6px;
  transition: all 0.3s ease;
}

:deep(.password-dialog .el-button--primary:hover) {
  background: linear-gradient(135deg, #1565c0, #2196f3);
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(25, 118, 210, 0.4);
}
</style>