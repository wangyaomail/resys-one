package xmt.resys.web.interceptor;

import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import xmt.resys.common.bean.enums.ApiCode;
import xmt.resys.common.bean.http.ResponseBean;

@ControllerAdvice
public class HBResponseBodyAdvice implements ResponseBodyAdvice<Object> {
    @Override
    public Object beforeBodyWrite(Object returnValue,
                                  MethodParameter methodParameter,
                                  MediaType mediaType,
                                  Class<? extends HttpMessageConverter<?>> clazz,
                                  ServerHttpRequest serverHttpRequest,
                                  ServerHttpResponse serverHttpResponse) {
        if (returnValue instanceof ResponseBean) {
            ResponseBean responseBean = (ResponseBean) returnValue;
            if (responseBean.getErrMsg() != null && !responseBean.getErrMsg().equals("")
                    && responseBean.getCode().equals(ApiCode.SUCCESS.getCode())) {
                responseBean.setCode("");
            }
        }
        return returnValue;
    }

    @Override
    public boolean supports(MethodParameter methodParameter,
                            Class<? extends HttpMessageConverter<?>> clazz) {
        return true;
    }
}
