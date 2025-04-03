<template>
  <div class="select-dir-container">
    <div class="input-wrapper">
      <el-input
        v-model="selectedPath"
        placeholder="请输入或选择目录路径"
        class="dir-input"
        @input="handleInputChange"
      >
        <template slot="append">
          <el-button @click="toggleDropdown">
            <i :class="isDropdownVisible ? 'el-icon-arrow-up' : 'el-icon-arrow-down'"></i>
          </el-button>
        </template>
      </el-input>
    </div>
    
    <div class="dropdown-container" v-show="isDropdownVisible">
      <div class="tree-container">
        <el-tree
          :data="treeData"
          :props="defaultProps"
          node-key="path"
          :load="loadNode"
          lazy
          :expand-on-click-node="false"
          @node-click="handleNodeClick"
          ref="tree"
        >
          <span slot-scope="{ node, data }" class="custom-tree-node">
            <el-radio 
              v-model="selectedPath" 
              @change="handleRadioChange(data)"
            ></el-radio>
            <i :class="data.folder ? 'el-icon-folder' : 'el-icon-document'"></i>
            <span>{{ node.label }}</span>
          </span>
        </el-tree>
      </div>
    </div>
  </div>
</template>

<script>
import { getLocalDrives, getFileTree } from '@/utils/api';

export default {
  name: 'SelectDir',
  props: {
    value: {
      type: String,
      default: ''
    },
    showFiles: {
      type: Boolean,
      default: true
    }
  },
  data() {
    return {
      selectedPath: this.value,
      isDropdownVisible: false,
      treeData: [],
      defaultProps: {
        children: 'children',
        label: 'name',
        isLeaf: (data) => !data.folder
      }
    };
  },
  watch: {
    value(newVal) {
      this.selectedPath = newVal;
    },
    selectedPath(newVal) {
      this.$emit('input', newVal);
    }
  },
  mounted() {
    this.initDrives();
    // 点击外部关闭下拉框
    document.addEventListener('click', this.handleClickOutside);
  },
  beforeDestroy() {
    document.removeEventListener('click', this.handleClickOutside);
  },
  methods: {
    async initDrives() {
      try {
        const res = await getLocalDrives();
        if (res.code === 200) {
          this.treeData = res.data.map(drive => ({
            name: drive,
            path: drive,
            folder: true,
            children: null
          }));
        } else {
          this.$message.error(res.msg || '获取磁盘列表失败');
        }
      } catch (error) {
        console.error('获取磁盘列表失败:', error);
        this.$message.error('获取磁盘列表失败');
      }
    },
    async loadNode(node, resolve) {
      if (node.level === 0) {
        // 根节点，直接返回磁盘列表
        return resolve(this.treeData);
      }
      
      try {
        const res = await getFileTree(node.data.path, this.showFiles, true, 1);
        if (res.code === 200) {
          // 将返回的数据转换为树节点格式
          const children = res.data.children ? res.data.children.map(item => ({
            name: item.name,
            path: item.path,
            folder: item.folder,
            children: item.folder ? [] : null
          })) : [];
          resolve(children);
        } else {
          this.$message.error(res.msg || '获取目录内容失败');
          resolve([]);
        }
      } catch (error) {
        console.error('获取目录内容失败:', error);
        this.$message.error('获取目录内容失败');
        resolve([]);
      }
    },
    handleNodeClick(data) {
      // 点击节点时，如果是文件夹则展开/折叠
      if (data.folder) {
        this.$refs.tree.store.nodesMap[data.path].expanded = 
          !this.$refs.tree.store.nodesMap[data.path].expanded;
      }
    },
    handleRadioChange(data) {
      this.selectedPath = data.path;
      this.isDropdownVisible = false;
      this.$emit('change', data.path);
    },
    handleInputChange(value) {
      // 当用户手动输入时，触发change事件
      this.$emit('change', value);
    },
    toggleDropdown() {
      this.isDropdownVisible = !this.isDropdownVisible;
    },
    handleClickOutside(event) {
      const container = this.$el;
      if (!container.contains(event.target)) {
        this.isDropdownVisible = false;
      }
    }
  }
};
</script>

<style scoped>
.select-dir-container {
  position: relative;
  width: 100%;
}

.input-wrapper {
  width: 100%;
}

.dir-input {
  width: 100%;
}

.dropdown-container {
  position: absolute;
  top: 100%;
  left: 0;
  width: 100%;
  max-height: 300px;
  overflow-y: auto;
  background-color: #fff;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  z-index: 1000;
  margin-top: 5px;
}

.tree-container {
  padding: 10px;
}

.custom-tree-node {
  display: flex;
  align-items: center;
  width: 100%;
}

.custom-tree-node .el-radio {
  margin-right: 8px;
}

.custom-tree-node i {
  margin-right: 8px;
  font-size: 16px;
}

.custom-tree-node i.el-icon-folder {
  color: #f0c78a;
}

.custom-tree-node i.el-icon-document {
  color: #909399;
}
</style> 