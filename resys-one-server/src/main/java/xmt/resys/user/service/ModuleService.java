package xmt.resys.user.service;

import java.util.concurrent.atomic.AtomicBoolean;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.AntPathMatcher;

import lombok.Getter;
import xmt.resys.common.dao.l2.BaseCRUDDao;
import xmt.resys.common.service.BaseCRUDService;
import xmt.resys.user.bean.http.HBModuleVisitInfo;
import xmt.resys.user.bean.mongo.HBModule;
import xmt.resys.user.dao.ModuleDao;
import xmt.resys.web.bean.auth.HBAuthURL;

@Getter
@Service
public class ModuleService extends BaseCRUDService<HBModule> {
    @Resource
    private ModuleDao moduleDao;

    @Override
    public BaseCRUDDao<HBModule> dao() {
        return moduleDao;
    }

    /**
     * 获取匿名用户在细过滤粒度时的module
     */
    public HBModule getAnonymouslyModule() {
        return moduleDao.findOne(sysConfService.getAnonymouslyModuleName());
    }

    /**
     * 下面的都和url匹配相关
     */
    private AntPathMatcher antMatcher;
    private String urlPrefix;

    @PostConstruct
    public void initPathMatcher() {
        antMatcher = new AntPathMatcher();
        antMatcher.setTrimTokens(false);
        antMatcher.setCaseSensitive(true);
        urlPrefix = "/" + sysConfService.getApiPrefixName();// + "/" +
                                                            // sysConfService.getApiVersion();
        logger.info("完成对路径匹配逻辑的初始化【urlPrefix】=" + urlPrefix);
    }

    public boolean match(HBModuleVisitInfo moduleVisit,
                         String path,
                         String method) {
        if (moduleVisit.getSrcModule() != null
                && matches(moduleVisit.getSrcModule(), path, method)) {
            moduleVisit.setCount(moduleVisit.getCount() + 1);
            return true;
        } else {
            return false;
        }
    }

    /**
     * 直接提供request进行匹配
     */
    public boolean matches(HBModule module,
                           HttpServletRequest request) {
        AtomicBoolean matchResult = new AtomicBoolean(false);
        // 只要匹配住一个就可以
        outerFor: for (HBAuthURL authUrl : module.getUrls()) {
            // 先过method这关，放过OPTIONS这类信号
            if (StringUtils.isNotEmpty(authUrl.getMethod())
                    && ("ALL".equals(authUrl.getMethod()) || "OPTIONS".equals(request.getMethod())
                            || authUrl.getMethod().equals(request.getMethod()))) {
                // 再一个个检查
                String rawUrl = getRequestPath(request);
                if (antMatcher.match(authUrl.getUrl(), rawUrl)) {
                    matchResult.set(true);
                    break outerFor;
                }
            }
        }
        return matchResult.get();
    }

    /**
     * 匹配访问的url是否能够通过，从module中摘出来到这里因为bean内构建service较为不现实
     */
    public boolean matches(HBModule module,
                           String path,
                           String method) {
        AtomicBoolean matchResult = new AtomicBoolean(false);
        // 只要匹配住一个就可以
        outerFor: for (HBAuthURL authUrl : module.getUrls()) {
            // 先过method这关
            if ("ALL".equals(authUrl.getMethod())
                    || (StringUtils.isNotEmpty(method) && authUrl.getMethod().equals(method))) {
                // 去除prefix
                if (path.startsWith(urlPrefix)) {
                    path = path.substring(urlPrefix.length(), path.length());
                }
                // 再一个个检查
                if (antMatcher.match(authUrl.getUrl(), path)) {
                    matchResult.set(true);
                    break outerFor;
                }
            }
        }
        return matchResult.get();
    }

    /**
     * 从request中获取实际访问的路径
     */
    private String getRequestPath(HttpServletRequest request) {
        StringBuilder sb = new StringBuilder();
        // 首先拼接版本号，因为版本号现在已经内置到本地servlet路径下了
        sb.append(request.getServletPath());
        if (request.getPathInfo() != null) {
            sb.append(request.getPathInfo());
        }
        return sb.toString();
    }
}
