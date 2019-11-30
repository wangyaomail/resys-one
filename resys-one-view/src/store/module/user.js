import $http from "@/api/index";

const user = {
  state: {
    user: {},
    allroles: [],
    getRoles: false,
    allrolesArray: {},
    allmodules: [],
    getModules: false,
    allgroups: [],
    getGroup: false,
    allgroupsArray: {}
  },
  getters: {
    user: state => state.user,
    role: state => state.user.roles,
    allroles: state => state.allroles,
    allrolesArray: state => state.allrolesArray,
    allmodules: state => state.allmodules,
    allgroups: state => state.allgroups,
    allgroupsArray: state => state.allgroupsArray,
    groupMap: state => {
      let theMap = {};
      state.allgroups.forEach(group => {
        group.users = group.users ? group.users : [];
        theMap[group.id] = group;
      });
      return theMap;
    },
    groupTree: state => state.allgroups.filter(group => group.roles.includes("role_staff")).map(group => {
      return {
        id: group.id,
        title: group.title,
        leader: group.leader,
        viceLeaders: group.viceLeaders,
        expand: true
      };
    })
  },
  mutations: {
    set_user(state, payload) {
      state.user = payload;
      sessionStorage.userModules = payload.modules;
    },
    set_all_roles(state, payload) {
      state.getRoles = true;
      state.allroles = payload;
      let result = {};
      state.allroles.forEach(element => {
        result[element.id] = element.name;
      });
      state.allrolesArray = result;
    },
    set_all_modules(state, payload) {
      state.getModules = true;
      state.allmodules = payload;
    },
    set_all_groups(state, payload) {
      state.getGroup = true;
      state.allgroups = payload.map(group => {
        group.title = group.name;
        return group;
      });
      let result = {};
      state.allgroups.forEach(element => {
        result[element.id] = element.name;
      });
      state.allgroupsArray = result;
    }
  },
  actions: {
    login({dispatch, commit}, payload) {
      return new Promise((resolve, reject) => {
        $http.request({
          url: "/auth/token/login",
          data: payload,
          method: "post"
        }).then(res => {
          if (res != null) {
            sessionStorage.jwttoken = res.jwtToken;
            sessionStorage.user = JSON.stringify(res);
            commit("set_user", res);
            // 从服务器调用初始化的数据
            dispatch("init_module_sys");
            resolve(res);
          }
        }).catch(err => {
          console.log(err);
          reject(err);
        });
      });
    },
    logout({dispatch, commit}, payload) {
      sessionStorage.removeItem("jwttoken");
      sessionStorage.removeItem("user");
      commit("set_user", {});
    },
    get_basic_user({commit}, payload) {
      return new Promise((resolve, reject) => {
        $http.request({url: "/user/edit/basic/" + payload, method: "get"}).then(res => {
          resolve(res);
        }).catch(err => [
          reject(err)
        ]);
      });
    },
    init_user_roles({dispatch, commit, state}) {
      return new Promise((resolve, reject) => {
        $http.request({url: "/role/init", method: "get"}).then(res => {
          commit("set_all_roles", res);
          resolve(res);
        }).catch(err => [
          reject(err)
        ]);
      });
    },
    init_user_modules({dispatch, commit, state}) {
      return new Promise((resolve, reject) => {
        $http.request({url: "/module/init", method: "get"}).then(res => {
          commit("set_all_modules", res);
          resolve(res);
        }).catch(err => [
          reject(err)
        ]);
      });
    },
    init_user_groups({dispatch, commit, state}) {
      return new Promise((resolve, reject) => {
        $http.request({url: "/usergroup/init", method: "get"}).then(res => {
          commit("set_all_groups", res);
          resolve(res);
        }).catch(err => [
          reject(err)
        ]);
      });
    }
  }
};

export default user;
