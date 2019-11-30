package xmt.resys.web.controller;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.HashSet;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.data.mongodb.core.MongoTemplate;

import xmt.resys.common.bean.anno.RegisterMethod;
import xmt.resys.common.bean.enums.ApiCode;
import xmt.resys.common.bean.enums.ReturnCode;
import xmt.resys.common.bean.http.ResponseBean;
import xmt.resys.common.bean.job.ControllerJob;
import xmt.resys.common.server.MainServer;
import xmt.resys.common.service.TimerService;
import xmt.resys.util.set.HBStringUtil;
import xmt.resys.web.bean.auth.HBUserSession;

/**
 * 最底层的controller
 */
@DependsOn("mainServer")
public abstract class BaseController {
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());
    @Resource
    protected MongoTemplate mongoTemplate;
    @Autowired
    protected MainServer mainServer;
    @Autowired
    protected TimerService timerService;
    @Autowired
    protected HttpServletRequest request;

    @PostConstruct
    private void postInit() {
        // 扫描子类所有有RegisterJob注解的方法
        try {
            // if(this instanceof ArticleBCategoryController) {
            // System.out.println(123);
            // }
            // 首先获取类上的RegisterMethod注解
            RegisterMethod[] couldRegisterList = this.getClass()
                                                     .getAnnotationsByType(RegisterMethod.class);
            HashSet<String> couldRegisterSet = new HashSet<>();
            if (couldRegisterList != null && couldRegisterList.length > 0) {
                for (RegisterMethod couldRegister : couldRegisterList) {
                    String[] methods = couldRegister.methods();
                    if (methods != null && methods.length > 0) {
                        for (String method : methods) {
                            couldRegisterSet.add(method);
                        }
                    }
                }
            }
            Method[] allMethods = this.getClass().getMethods();
            if (allMethods != null && allMethods.length > 0) {
                for (Method method : allMethods) {
                    RegisterMethod anno = method.getAnnotation(RegisterMethod.class);
                    if (anno != null) {
                        if ((!anno.declare()) || couldRegisterSet.contains(method.getName())) {
                            if (HBStringUtil.isNotBlank(anno.name())) {
                                registerJob(new ControllerJob(anno.name(), method, this));
                            } else {
                                registerJob(new ControllerJob(method.getName(), method, this));
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            // 如果方法注册失败就直接退出
            logger.error("注册方法失败", e);
            mainServer.shutdown(ReturnCode.INTERNAL_ERROR.getCode());
        }
        logger.info("初始化controller：" + this.getClass().getSimpleName() + "成功，扫描并注册了方法"
                + jobMap.keySet());
    }

    protected HashMap<String, ControllerJob> jobMap = new HashMap<>();

    /**
     * 注册可以执行的方法，注意这些方法有的是需要权限的，但为了避免重复进行权限校验所以这里不加验证，换句话说权限校验那里必须对不放过的情况做好充足准备
     */
    public void registerJob(ControllerJob job) {
        jobMap.put(job.getKey(), job);
    }

    /**
     * 非映射型controller，对于所有在这个目录下的请求，按照这个进行分发，直接响应某个方法
     * @WARN 注意这样的调用当前不支持带参数，如果带参数和spring的自动注入会打架，因此只支持GET调用
     * @WARN 本方法不验证用户的权限，所有权限校验必须在filter层面完成
     * @WARN 注意通配符的匹配优先级是低于直接配置的配置的
     * @INFO 但是GET进入的方法可以自己从parameter中查找自己需要的参数
     * @INFO 如果需要使用该方法，请在子类重写该方法并注明对父类方法的调用
     * @INFO @RequestMapping(value = "/{func}", method = { RequestMethod.GET })
     * @INFO public ResponseBean man(@PathVariable String func)
     */
    protected ResponseBean man(String func) {
        ResponseBean responseBean = getReturn();
        if (HBStringUtil.isNotBlank(func)) {
            ControllerJob job = jobMap.get(func);
            if (job != null) {
                try {
                    Object result = job.execute();
                    if (result != null) {
                        responseBean.setData(result);
                        // } else {
                        // responseBean.setData("执行成功");
                    }
                } catch (Exception e) {
                    // responseBean.setData("执行报错");
                    responseBean.setCodeEnum(ApiCode.INTERNAL_ERROR);
                    responseBean.setStackMsg(e.getMessage());
                    logger.error("反射执行某个函数出错", e);
                }
            } else {
                responseBean.setCodeEnum(ApiCode.API_NOT_FOUND);
                responseBean.setData("执行的方法不存在，请不要重新尝试调用该方法");
            }
        }
        return returnBean(responseBean);
    }

    public ResponseBean getReturn() {
        return new ResponseBean();
    }

    public ResponseBean returnBean(ResponseBean bean) {
        ResponseBean bean2 = bean.end();
        if (bean2.getData() != null) {
            if (mainServer.conf().getRunOnDev()) {
                logger.debug(bean2.getData().toString());
            }
        }
        return bean2;
    }

    // 从parameter中获取数据，带默认值
    protected String getPara(String name,
                             String defaultVal) {
        String val = request.getParameter(name);
        return HBStringUtil.isNotBlank(val) ? val : defaultVal;
    }

    // 获取用户的id号
    protected String getUseridFromRequest() {
        try {
            Object useridobj = request.getAttribute("userid");
            return useridobj.toString();
        } catch (Exception e) {
            logger.error("没有从request拿到用户id");
            return null;
        }
    }

    // 获取用户名
    protected String getUserNameFromRequest() {
        try {
            Object useridobj = request.getAttribute("username");
            return useridobj.toString();
        } catch (Exception e) {
            logger.error("没有从request拿到用户名");
            return null;
        }
    }

    // 获取手机号
    protected String getUserPhoneNumFromRequest() {
        try {
            Object phoneNumObj = request.getAttribute("phoneNum");
            return phoneNumObj.toString();
        } catch (Exception e) {
            logger.error("没有从request拿到手机号");
            return null;
        }
    }

    // 获取客户端的IP地址
    public String getIpAddr(HttpServletRequest request) {
        String ipAddress = null;
        ipAddress = request.getHeader("x-forwarded-for");
        if (ipAddress == null || ipAddress.length() == 0 || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("Proxy-Client-IP");
        }
        if (ipAddress == null || ipAddress.length() == 0 || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ipAddress == null || ipAddress.length() == 0 || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getRemoteAddr();
            if (ipAddress.equals("127.0.0.1") || ipAddress.equals("0:0:0:0:0:0:0:1")) {
                // 根据网卡取本机配置的IP
                InetAddress inet = null;
                try {
                    inet = InetAddress.getLocalHost();
                } catch (UnknownHostException e) {
                    logger.error("未知主机", e);
                }
                ipAddress = inet.getHostAddress();
            }
        }
        // 对于通过多个代理的情况，第一个IP为客户端真实IP,多个IP按照','分割
        if (ipAddress != null && ipAddress.length() > 15) {
            if (ipAddress.indexOf(",") > 0) {
                ipAddress = ipAddress.substring(0, ipAddress.indexOf(","));
            }
        }
        return ipAddress;
    }

    // 获取refererUrl
    public String getRefererUrl(HttpServletRequest request) {
        return request.getHeader("referer");
    }

    // 将request输出为String
    public String readRequest(HttpServletRequest request) throws IOException {
        StringBuilder sb = new StringBuilder();
        try {
            String line;
            while ((line = request.getReader().readLine()) != null) {
                sb.append(line);
            }
        } finally {
            request.getReader().close();
        }
        return sb.toString();
    }

    /**
     * session在AuthFilter内填充
     */
    public HBUserSession getSession() {
        Object userSession = request.getAttribute("usersession");
        return userSession != null ? (HBUserSession) userSession : null;
    }

    /**
     * token在AuthFilter内填充
     */
    public String getSessionid() {
        Object token = request.getAttribute("usertoken");
        return token != null ? token.toString().split("\\.")[0] : null;
    }
}
