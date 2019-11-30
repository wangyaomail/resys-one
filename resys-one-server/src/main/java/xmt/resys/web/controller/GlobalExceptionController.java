package xmt.resys.web.controller;

import java.io.PrintWriter;
import java.io.StringWriter;

import javax.servlet.http.HttpServletRequest;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import xmt.resys.common.bean.http.ResponseBean;

@RestControllerAdvice
public class GlobalExceptionController extends BaseController {
    @ExceptionHandler(Exception.class)
    @ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseBean handleException(HttpServletRequest request,
                                        Exception ex) {
        ResponseBean responseBean = getReturn();
        responseBean.setCode("A009");
        responseBean.setErrMsg("出现系统错误");
        try {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            ex.printStackTrace(pw);
            ex.printStackTrace();
            responseBean.setStackMsg(sw.toString());
            sw.close();
            pw.close();
        } catch (Exception e2) {
        }
        return returnBean(responseBean);
    }
}
