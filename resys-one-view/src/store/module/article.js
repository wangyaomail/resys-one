import $http from "@/api/index";

const state = {
  articleStateList: [{
    name: "草稿箱",
    value: 0,
    type: "primary"
  }, {
    name: "待审核",
    value: 1,
    type: "warning"
  }, {
    name: "已发布",
    value: 2,
    type: "success"
  }, {
    name: "已删除",
    value: 3,
    type: "error"
  }],
  articleCategoryList: [],
  articleCategoryMap: {},
  tagList: [],
  tagMap: {}
};
const getters = {
  articleStateList: state => state.articleStateList,
  articleStateMap: state => {
    let rsMap = {};
    state.articleStateList.forEach(item => {
      rsMap[item.name] = item.value;
    });
    return rsMap;
  },
  articleStateListSelector: state => {
    return state.articleStateList.map(item => {
      return {
        value: item.name,
        label: item.value
      };
    });
  },
  articleStateMapRevert: state => {
    let rsMap = {};
    state.articleStateList.forEach(item => {
      rsMap[item.value] = item;
    });
    return rsMap;
  },
  articleCategoryMap: state => state.articleCategoryMap,
  articleCategoryList: state => state.articleCategoryList,
  articleCategoryRoot: state => state.articleCategoryMap.root,
  articleCategoryLevel: state => level => {
    let lastLevel = [state.articleCategoryMap.root], thisLevel = [];
    if (state.articleCategoryMap.root) {
      for (let j = 0; j < level; j++) {
        for (let item in lastLevel) {
          if (item.children) {
            for (let i in item.children) {
              thisLevel.push(item.children[i]);
            }
          }
        }
        lastLevel = thisLevel;
        thisLevel = [];
      }
    }
  },
  tagRoot: state => state.tagMap.root,
  tagMap: state => state.tagMap
  // tagtree: state => [{
  //   title: "root",
  //   expand: false,
  //   loading: false,
  //   children: state.tagroots.map((node) => {
  //     return {
  //       title: node.id,
  //       loading: false,
  //       parent: node.parent,
  //       expand: false,
  //       children: node.childs ? node.childs : [],
  //       type: node.type
  //     };
  //   })
  // }]
};
const mutations = {
  set_articleCategory(state, payload) {
    state.articleCategoryMap = payload;
    state.articleCategoryList = [];
    for (let i in payload) {
      let item = payload[i];
      item.title = item.id;
      item.expand = true;
      state.articleCategoryList.push(item);
    }
    // 解决后端传来的对象的相对引用的问题
    let diguiReflectList = (theObj) => {
      if (theObj) {
        if (theObj.children) {
          theObj.children = theObj.children.map(item => diguiReflectList(item));
        }
        if (theObj["$ref"]) {
          let str = theObj["$ref"].replace("\$.data", "state.articleCategoryMap");
          if (/[\u4e00-\u9fa5-]/.test(str)) {
            str = str.replace(/[\u4e00-\u9fa5-]+/g, "['$&']").replace(/\.\[/g, "[");
          }
          let rsObj;
          try {
            rsObj = eval(str);
          } catch (err) {
            console.log(str);
            console.log(err);
          }
          return rsObj;
        } else {
          theObj.title = theObj.id;
          theObj.expand = true;
          state.articleCategoryMap[theObj.id] = theObj;
          return theObj;
        }
      } else {
        return theObj;
      }
    };
    state.articleCategoryList = state.articleCategoryList.map(item => diguiReflectList(item));
  },
  set_tags(state, payload) {
    state.tagMap = payload;
    state.tagList = [];
    for (let i in payload) {
      let item = payload[i];
      item.title = item.id;
      item.expand = true;
      state.tagList.push(item);
    }
    // 解决后端传来的对象的相对引用的问题
    let diguiReflectList = (theObj) => {
      if (theObj) {
        if (theObj.children) {
          theObj.children = theObj.children.map(item => diguiReflectList(item));
        }
        if (theObj["$ref"]) {
          let str = theObj["$ref"].replace("\$.data", "state.tagMap");
          if (/[\u4e00-\u9fa5-]/.test(str)) {
            str = str.replace(/[\u4e00-\u9fa5-]+/g, "['$&']").replace(/\.\[/g, "[");
          }
          let rsObj;
          try {
            rsObj = eval(str);
          } catch (err) {
            console.log(str);
            console.log(err);
          }
          return rsObj;
        } else {
          theObj.title = theObj.id;
          theObj.expand = true;
          state.tagMap[theObj.id] = theObj;
          return theObj;
        }
      } else {
        return theObj;
      }
    };
    state.tagList = state.tagList.map(item => diguiReflectList(item));
    // if (payload) {
    //   payload.map((root) => {
    //     state.tagmap[root.id] = root;
    //     if (root.childs) {
    //       root.childs.map((child) => {
    //         child.title = child.id;
    //         child.loading = false;
    //         child.children = [];
    //         child.expand = true;
    //         state.tagmap[child.id] = child;
    //       });
    //     }
    //   });
    //   state.tagroots = payload;
    // }
  }
};
const actions = {
  init_module_article_category({commit}, payload) {
    return new Promise((resolve, reject) => {
      $http.request({url: "/articlecategory/init", method: "get"}).then(res => {
        commit("set_articleCategory", res);
        resolve(res);
      }).catch(err => [
        reject(err)
      ]);
    });
  },
  init_module_article_tag({dispatch, commit}) {
    return new Promise((resolve, reject) => {
      $http.request({url: "/tag/init", method: "get"}).then(res => {
        commit("set_tags", res);
        resolve(res);
      }).catch(err => [
        reject(err)
      ]);
    });
  }
};

export default {
  state,
  getters,
  actions,
  mutations
};
