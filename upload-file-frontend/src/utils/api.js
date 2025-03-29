import { http, httpExtra } from './http';

/**
 * 获取上传进度
 * @param {string} identifier 文件的 MD5 标识
 * @param {string} storageType 存储类型
 * @returns {Promise<any>} 上传进度信息
 */
const getUploadProgress = (identifier, storageType) => {
    return http.get('/upload/getUploadProgress', {
        params: { identifier },
        headers: { 'Storage-Type': storageType }
    });
};

/**
 * 创建分片上传任务
 * @param {Object} params 参数对象
 * @param {string} params.identifier 文件的 MD5 标识
 * @param {string} params.fileName 文件名称
 * @param {number} params.totalSize 文件总大小 (字节)
 * @param {number} params.chunkSize 分片大小 (字节)
 * @param {string} params.contentType 文件 MIME 类型
 * @param {string} params.folder 文件存储文件夹
 * @param {string} params.storageType 存储类型
 * @returns {Promise<any>} 分片上传任务信息
 */
const createMultipartUpload = ({ identifier, fileName, totalSize, chunkSize, contentType, folder, storageType }) => {
    return http.post('/upload/createMultipartUpload', {
        identifier,
        fileName,
        totalSize,
        chunkSize,
        contentType,
        folder
    }, {
        headers: { 'Storage-Type': storageType }
    });
};

/**
 * 获取预签名分片上传 URL
 * @param {Object} params 参数对象
 * @param {string} params.identifier 文件的 MD5 标识
 * @param {number} params.partNumber 分片编号
 * @param {string} params.storageType 存储类型
 * @returns {Promise<any>} 预签名上传 URL
 */
const getPreSignUploadUrl = ({ identifier, partNumber, storageType }) => {
    return http.get('/upload/getPreSignUploadUrl', {
        params: { identifier, partNumber },
        headers: { 'Storage-Type': storageType }
    });
};

/**
 * 合并分片
 * @param {string} identifier 文件的 MD5 标识
 * @param {string} storageType 存储类型
 * @returns {Promise<any>} 合并结果
 */
const merge = (identifier, storageType) => {
    return http.post('/upload/merge', null, {
        params: { identifier },
        headers: { 'Storage-Type': storageType }
    });
};

/**
 * 上传文件到后端 /upload 接口
 * @param {Object} params 参数对象
 * @param {File} params.file 文件对象
 * @param {string} [params.folder] 文件夹名称 (可选)
 * @param {number} [params.fileType] 文件类型 (可选)
 * @param {string} params.storageType 存储类型
 * @returns {Promise<any>} 上传结果
 */
const uploadFile = ({ file, folder, fileType, storageType }) => {
    const formData = new FormData();
    formData.append('file', file); // 文件字段
    if (folder) formData.append('folder', folder); // 可选的 folder 参数
    if (fileType !== undefined) formData.append('fileType', fileType); // 可选的 fileType 参数

    return http.post('/upload', formData, {
        headers: {
            'Content-Type': 'multipart/form-data',
            'Storage-Type': storageType
        }
    });
};

/**
 * 上传单个分片
 * @param {Object} params 参数对象
 * @param {string} params.uploadId 上传任务 ID
 * @param {number} params.partNumber 分片编号
 * @param {File} params.partFile 分片文件
 * @param {string} params.storageType 存储类型
 * @returns {Promise<any>} 上传结果
 */
const uploadPart = ({ uploadId, partNumber, partFile, storageType }) => {
  const formData = new FormData();
  formData.append('uploadId', uploadId);
  formData.append('partNumber', partNumber);
  formData.append('file', partFile);

  return http.post('/upload/part', formData, {
      headers: {
          'Content-Type': 'multipart/form-data',
          'Storage-Type': storageType
      }
  });
};

/**
 * 分页获取文件列表
 * @param {Object} params 参数对象
 * @param {number} [params.page=1] 页码，默认 1
 * @param {number} [params.pageSize=10] 每页大小，默认 10
 * @param {string} params.storageType 存储类型
 * @returns {Promise<any>} 文件列表
 */
const pageFiles = ({ page = 1, pageSize = 10, storageType, fileName }) => {
  return http.get('/file/page', {
      params: { page, pageSize, storageType, fileName }
  });
};

/**
 * 根据存储类型获取存储配置
 * @param {Object} params 参数对象
 * @param {string} params.type 类型 
 * @returns 
 */
const getStorageConfig = ({ type}) => {
    return http.get('/config', {
        params: { type }
    });
};

/**
 * 修改存储配置
 * @param {Object} params 参数对象
 * @param {number} params.id id
 * @param {string} params.type 类型
 * @param {string} params.endpoint endpoint
 * @param {string} params.accessKey accessKey
 * @param {string} params.secretKey secretKey
 * @param {string} params.bucket bucket
 * @returns 
 */
const setStorageConfig = ({ id, type, endpoint, accessKey, secretKey, bucket }) => {
    return http.patch('/config', {
        id,
        type,
        endpoint,
        accessKey,
        secretKey,
        bucket
    });
}

/**
 * 测试存储配置
 * @param {Object} params 参数对象
 * @param {string} params.type 类型
 * @param {string} params.endpoint endpoint
 * @param {string} params.accessKey accessKey
 * @param {string} params.secretKey secretKey
 * @param {string} params.bucket bucket
 * @returns 
 */
const testStorageConfig = ({ type, endpoint, accessKey, secretKey, bucket }) => {
    return http.post('/config/test', {
        type,
        endpoint,
        accessKey,
        secretKey,
        bucket
    });
}

/**
 * 获取共享文件列表
 * @param {Object} params 参数对象
 * @param {string} params.password 密码
 * @returns 
 */
const getSharedFiles = () => {
    return http.get('/file/sharedFiles');
};

/**
 * 启用或禁用共享
 * @param {Object} params 参数对象
 * @param {boolean} params.enable 是否启用
 * @returns 
 */
const enableShare = ({ enable }) => {
    const formData = new FormData();
    formData.append('enable', enable);
    return http.post("/file/enableShare", formData);
};

/**
 * 获取分享状态
 * @returns
 */
const getShareStatus = () => {
    return http.get("/file/getShareStatus");
}

/**
 * 获取分享地址
 * @returns
 */
const shareAddress = () => {
    return http.get("/file/shareAddress");
}

/**
 * 移除共享文件
 * @param {string} fileIdentifier 
 * @returns 
 */
const unShareFile = (fileIdentifier) => {
    const formData = new FormData();
    formData.append('fileIdentifier', fileIdentifier);
    return http.post("/file/unShareFile", formData);
}

export {
    getUploadProgress,
    createMultipartUpload,
    getPreSignUploadUrl,
    merge,
    uploadFile,
    uploadPart,
    pageFiles,
    getStorageConfig,
    setStorageConfig,
    testStorageConfig,
    getSharedFiles,
    enableShare,
    getShareStatus,
    shareAddress,
    unShareFile,
    httpExtra
};
