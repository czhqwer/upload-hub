import axios from 'axios';
import axiosExtra from 'axios-extra';

const baseUrl = 'http://localhost:10086';

// 基础 Axios 实例
const http = axios.create({
    baseURL: baseUrl
});

// 配置拦截器，返回 response.data
http.interceptors.response.use(response => {
    return response.data;
});

// 配置 axios-extra 实例，用于控制并发和重试
const httpExtra = axiosExtra.create({
    maxConcurrent: 5, // 并发数为 5
    queueOptions: {
        retry: 3, // 请求失败时最多重试 3 次
        retryIsJump: false // 重试时插入队列尾部而非立即重试
    }
});

export { http, baseUrl, httpExtra };