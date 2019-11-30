import $http from "@/api/index";

export const getTableData = () => {
  return $http.request({
    url: "get_table_data",
    method: "get"
  });
};

export const getDragList = () => {
  return $http.request({
    url: "get_drag_list",
    method: "get"
  });
};

export const errorReq = () => {
  return $http.request({
    url: "error_url",
    method: "post"
  });
};

export const saveErrorLogger = info => {
  return $http.request({
    url: "save_error_logger",
    data: info,
    method: "post"
  });
};

export const uploadImg = formData => {
  return $http.request({
    url: "image/upload",
    data: formData
  });
};

export const getOrgData = () => {
  return $http.request({
    url: "get_org_data",
    method: "get"
  });
};

export const getTreeSelectData = () => {
  return $http.request({
    url: "get_tree_select_data",
    method: "get"
  });
};
