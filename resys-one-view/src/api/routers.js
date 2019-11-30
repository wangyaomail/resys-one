import $http from "@/api/index";

export const getRouterReq = (access) => {
  return $http.request({
    url: "get_router",
    params: {
      access
    },
    method: "get"
  });
};
