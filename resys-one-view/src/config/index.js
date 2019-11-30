export default {
  title: "resys-one-view",
  /**
   * @description token在Cookie中存储的天数，默认1天
   */
  cookieExpires: 1,
  /**
   * @description api请求基础路径
   */
  baseUrl: {
    dev: "http://localhost:9001/resys/v1/b",
    test: "http://192.168.17.132:9001/resys/v1/b",
    product: "http://---",
  },
  /**
   * @description 默认打开的首页的路由name值，默认为home
   */
  homeName: "home",
  /**
   * @description 需要加载的插件
   */
  plugin: {}
};
