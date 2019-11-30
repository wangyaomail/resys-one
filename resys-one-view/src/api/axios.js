import axios from "axios";
import store from "@/store";

//import { Spin } from 'iview'

class HttpRequest {
  constructor(baseUrl = baseURL) {
    this.baseUrl = baseUrl;
    this.queue = {};
  }

  getInsideConfig() {
    const config = {
      baseURL: this.baseUrl,
      headers: {}
    };
    return config;
  }

  destroy(url) {
    delete this.queue[url];
    if (!Object.keys(this.queue).length) {
      // Spin.hide()
    }
  }

  interceptors(instance, url) {
    // 请求拦截
    instance.interceptors.request.use(config => {
      // 添加全局的loading...
      if (!Object.keys(this.queue).length) {
        //Spin.show() // 不建议开启，因为界面不友好
      }
      this.queue[url] = true;
      if (sessionStorage.jwttoken) {
        config.headers["jwttoken"] = sessionStorage.jwttoken;
      }
      if (process.env.NODE_ENV !== "production") {
        console.log(config);
      }
      return config;
    }, error => {
      return Promise.reject(error);
    });
    // 响应拦截
    instance.interceptors.response.use(res => {
      this.destroy(url);
      if (res.data.code && res.data.code !== "A001") {
        if (process.env.NODE_ENV === "dev") {
          console.log("%c>>>%c>>>%c>>>%c>>>%c>>>%c>>>%c>>>", "color: #e74c3c", "color: #e67e22", "color: #f1c40f", "color: #2ecc71", "color: #1abc9c", "color: #3498db", "color: #9b59b6");
          console.log("出错了哦，" + res.data.errMsg);
        }
        return Promise.reject(res.data);
      } else {
        const {data, status} = res;
        return Promise.resolve(res.data.data);
      }
    }, error => {
      this.destroy(url);
      if (process.env.NODE_ENV === "dev") {
        let errorInfo = error.response;
        if (!errorInfo) {
          const {request: {statusText, status}, config} = JSON.parse(JSON.stringify(error));
          errorInfo = {
            statusText,
            status,
            request: {responseURL: config.url}
          };
          console.log("%c>>>%c>>>%c>>>%c>>>%c>>>%c>>>%c>>>", "color: #e74c3c", "color: #e67e22", "color: #f1c40f", "color: #2ecc71", "color: #1abc9c", "color: #3498db", "color: #9b59b6");
          console.log(errorInfo);
        } else if (errorInfo.data && errorInfo.data.errMsg) {
          error.msg = errorInfo.data.errMsg;
        }
      }
      return Promise.reject(error);
    });
  }

  request(options) {
    const instance = axios.create();
    options = Object.assign(this.getInsideConfig(), options);
    this.interceptors(instance, options.url);
    return instance(options);
  }
}

export default HttpRequest;
