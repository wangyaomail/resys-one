import $http from "@/api/index";
// import Cookies from 'js-cookie';

const sys = {
  state: {
    sessionId: '',
  },
  getters: {
  },
  mutations: {
    set_session_id(state, payload) {
      state.sessionId = payload;
    },
  },
  actions: {
    get_dictionary({commit}, payload) {
      return new Promise((resolve, reject) => {
        $http.request({
          url:'/dictionary/init',
          method: 'get'
        }).then(res => {
          if (res) {
            if (res.roles) commit('set_all_roles', res.roles);
            if (res.modules) commit('set_all_modules', res.modules);
            if (res.groups) commit('set_all_groups', res.groups);
            if (res.sessionId) commit('set_session_id', res.sessionId);
          }
          resolve(res);
        }).catch(err => [
          reject(err)
        ]);
      });
    },
    init_module_sys({dispatch, commit}) {
      return dispatch('get_dictionary');
    }
  }
};

export default sys;
