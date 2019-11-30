import $http from "@/api/index";

export const login = ({userName, password}) => {
  const data = {
    userName,
    password
  };
  return $http.request({
    url: "auth/token/login",
    data,
    method: "post"
  });
};

export const getUserInfo = (token) => {
  return $http.request({
    url: "get_info",
    params: {
      token
    },
    method: "get"
  });
};

export const logout = (token) => {
  return $http.request({
    url: "logout",
    method: "post"
  });
};


