import { baseUrl } from './http';

class SSEManager {
  constructor(baseUrl) {
    this.baseUrl = baseUrl;
    this.eventSource = null;
    this.callbacks = {}; // 存储不同类型的回调函数
  }

  subscribe(type, callback) {
    this.callbacks[type] = callback;

    // 如果没有连接，则创建一个新的连接并绑定事件监听器
    if (!this.eventSource) {
      this.eventSource = new EventSource(`${this.baseUrl}/sse/subscribe`);

      this.eventSource.addEventListener('enableShare', (event) => {
        const data = JSON.parse(event.data);
        if (this.callbacks['enableShare']) {
          this.callbacks['enableShare'](data.data);
        }
      });

      this.eventSource.addEventListener('sharedFileUpdate', () => {
        if (this.callbacks['sharedFileUpdate']) {
          this.callbacks['sharedFileUpdate']();
        }
      });

      this.eventSource.addEventListener('setPassword', () => {
        if (this.callbacks['setPassword']) {
          this.callbacks['setPassword']();
        }
      });



      // 错误处理
      this.eventSource.onerror = (error) => {
        console.error('[SSE] Connection error:', error);
        if (this.eventSource.readyState === EventSource.CLOSED) {
          console.log('[SSE] Connection closed');
        }
      };
    }
  }

  unsubscribe(type) {
    if (this.callbacks[type]) {
      delete this.callbacks[type];
    }

    // 如果没有回调函数，关闭连接
    if (Object.keys(this.callbacks).length === 0) {
      this.eventSource.close();
      this.eventSource = null;
    }
  }
}

const sseManager = new SSEManager(baseUrl);
export default sseManager;