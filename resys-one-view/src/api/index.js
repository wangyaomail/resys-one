import HttpRequest from "@/api/axios";
import config from "@/config";

const baseUrl = process.env.NODE_ENV === "development"
  ? config.baseUrl.dev
  : process.env.NODE_ENV === "production"
    ? config.baseUrl.product
    : process.env.NODE_ENV === "test"
      ? config.baseUrl.test
      : config.baseUrl.default;


const http = new HttpRequest(baseUrl);
export default http;
