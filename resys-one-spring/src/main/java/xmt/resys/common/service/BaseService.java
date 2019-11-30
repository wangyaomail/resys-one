package xmt.resys.common.service;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.DependsOn;

/**
 * 真正的base service，所有服务都必须继承这个对象
 * @INCLUDE 告诉系统现在一共有多少服务
 * @INFO 这个服务比配置项服务高一级，启动这个服务之前务必把配置项服务准备完毕
 * @INFO Service层面不再需要接口，只有dao层才保留接口
 */
@DependsOn("sysConfService")
public abstract class BaseService {
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());
    @Autowired
    protected SysConfService sysConfService;
    @Autowired
    protected ApplicationContext applicationContext;

    @PostConstruct
    private void makeLogs() {
        logger.info("初始化service：" + this.getClass().getName() + "成功.");
    }
}
