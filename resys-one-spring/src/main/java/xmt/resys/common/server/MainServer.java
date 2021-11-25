package xmt.resys.common.server;

import java.util.concurrent.atomic.AtomicBoolean;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Service;

import sun.misc.Signal;
import sun.misc.SignalHandler;
import xmt.resys.common.service.BaseService;
import xmt.resys.common.service.SysConfService;

/**
 * 完成一些系统初始化的工作
 * @INCLUDE 定线程数量式线程池
 * @INCLUDE 需要异步启动不能阻塞开放web请求的资源的启动
 * @INCLUDE 监听LINUX KILL信号，注意这种方法会导致启动的时候报出警告信息说有些方法不再使用了，暂时没有更好的解决策略
 * @INCLUDE 管理一些资源的柔性关闭
 */
@Service
@DependsOn("sysConfService")
@SuppressWarnings("restriction")
public class MainServer extends BaseService {
    @Autowired
    private ApplicationContext applicationContext;
    @Autowired
    private SysConfService sysConfService;
    @Autowired
    private QueueServer queueServer;

    @PostConstruct
    public void init() {
        logger.info("start init resys-one cu.");
        // 配置柔性关闭，用于当tomcat关闭时，必须清空队列才能彻底退出，不在内部队列留线程
        initSystemSoftExit();
        // 启动一些在系统启动完之后才能启动的东西
        initPostSeq();
        logger.info("init resys-one cu success.");
    }

    public SysConfService conf() {
        return sysConfService;
    }

    private void initPostSeq() {
        // 启动queue开始执行
        queueServer.start();
    }

    private AtomicBoolean isShuttingDown = new AtomicBoolean(false);

    public void initSystemSoftExit() {
        if (ifRunOnLinux()) {
            logger.info("添加关闭钩子，监听 -15(TERM)信号");
            Signal.handle(new Signal("TERM"), new CuSignalHandler());
        }
        logger.info("在JVM上监听关闭信号");
        Runtime.getRuntime().addShutdownHook(new ShutdownHook());
    }

    public void shutdown(int returnCode) {
        try {
            logger.info("系统开始关闭");
            queueServer.shutdown();
        } catch (Exception e) {
            logger.error("系统关闭失败!", e);
            System.exit(returnCode);
        } finally {
            SpringApplication.exit(applicationContext);
            logger.info("系统关闭成功");
            // System.exit(returnCode);
        }
    }

    @PreDestroy
    private void softShutdown() {
        if (!isShuttingDown.getAndSet(true)) {
            logger.info("系统柔性关闭启动");
            shutdown(0);
            logger.info("柔性关闭成功");
            // 阻塞Runtime关闭问题，否则会影响tomcat重启
            // Runtime.getRuntime().exit(0);
        }
    }

    class ShutdownHook extends Thread {
        public void run() {
            softShutdown();
        };
    }

    class CuSignalHandler implements SignalHandler {
        @Override
        public void handle(Signal signal) {
            logger.info("检测到-15关闭信号，开始关闭系统！");
            softShutdown();
            logger.info("系统关闭完成！");
        }
    }

    /**
     * 判断系统运行的平台
     */
    public boolean ifRunOnLinux() {
        String os = System.getProperty("os.name");
        return !os.toLowerCase().startsWith("win");
    }
}
