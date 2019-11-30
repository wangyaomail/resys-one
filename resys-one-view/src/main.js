import Vue from "vue";
import App from "./App";
import router from "./router";
import store from "./store";
import iView from "iview";
import config from "@/config";
import importDirective from "@/directive";
import {directive as clickOutside} from "v-click-outside-x";
import installPlugin from "@/plugin";
import "./index.less";
import "@/assets/icons/iconfont.css";
import TreeTable from "tree-table-vue";
import VOrgTree from "v-org-tree";
import "v-org-tree/dist/v-org-tree.css";
import $http from './api/index';

// 实际打包时应该不引入mock
/* eslint-disable */
//if (process.env.NODE_ENV !== "production") require("@/mock");

Vue.use(iView);
Vue.use(TreeTable);
Vue.use(VOrgTree);
/**
 * @description 注册admin内置插件
 */
installPlugin(Vue);
/**
 * @description 生产环境关掉提示
 */
Vue.config.productionTip = false;
/**
 * @description 全局注册应用配置
 */
Vue.prototype.$config = config;
Vue.prototype.$http = $http;
/**
 * 注册指令
 */
importDirective(Vue);
Vue.directive("clickOutside", clickOutside);

/* eslint-disable no-new */
new Vue({
  el: "#app",
  router,
  store,
  render: h => h(App),
  mounted() {
    // 从sessionStorage恢复vuex的基本数据
    if (sessionStorage.user) {
      this.$store.commit("set_user", JSON.parse(sessionStorage.user));
      // 从服务器调用初始化的数据
      this.$store.dispatch("init_module_sys");
    }
  }
});
