import axios from 'axios';
import { Message } from 'element-ui';
import axiosExtra from 'axios-extra';

const baseUrl = `http://${window.location.hostname}:10086`;

// 基础 Axios 实例
const http = axios.create({
    baseURL: baseUrl
});

// 请求拦截器 - 自动添加 Authorization
http.interceptors.request.use(config => {
    // 从 localStorage 获取 password
    const password = localStorage.getItem('password');
    config.headers['Authorization'] = password ? password : '';
    return config;
}, error => {
    return Promise.reject(error);
});

// 配置拦截器，返回 response.data
http.interceptors.response.use(
    response => {
        try {
            if (response.data && response.data.code !== 200) {
                Message.error(response.data.msg || '请求失败');
            }
            return response.data;
        } catch (error) {
            console.error('拦截器错误:', error);
            Message.error('服务器响应解析失败');
            return Promise.reject(error);
        }
    },
    error => {
        console.error('请求错误:', error);
        Message.error(error.response?.data?.msg || '网络错误，请稍后重试');
        return Promise.reject(error);
    }
);

// 配置 axios-extra 实例，用于控制并发和重试
const httpExtra = axiosExtra.create({
    maxConcurrent: 5, // 并发数为 5
    queueOptions: {
        retry: 3, // 请求失败时最多重试 3 次
        retryIsJump: false // 重试时插入队列尾部而非立即重试
    }
});

httpExtra.interceptors.request.use(config => {
    const password = localStorage.getItem('password');
    if (password) {
        config.headers['Authorization'] = password;
    }
    return config;
}, error => {
    return Promise.reject(error);
});

export { http, baseUrl, httpExtra };