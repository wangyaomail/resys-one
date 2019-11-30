package xmt.resys.common.server;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Service;

import xmt.resys.common.service.SysConfService;

/**
 * 用于针对后台线程队列生成线程，修改于Executors.DefaultThreadFactory，默认情况下所有线程都是daemon
 * 注意类中的ThreadGroup，可能导致隐形的同步问题，后期需要再调整
 */
@Service
@DependsOn("sysConfService")
public class HBThreadFactory implements ThreadFactory {
    private static final AtomicInteger poolNumber = new AtomicInteger(1);
    private final ThreadGroup group;
    private final AtomicInteger threadNumber = new AtomicInteger(1);
    private final String namePrefix;
    @Autowired
    private SysConfService sysConfService;

    public HBThreadFactory() {
        SecurityManager s = System.getSecurityManager();
        group = (s != null) ? s.getThreadGroup() : Thread.currentThread().getThreadGroup();
        namePrefix = "pool-" + poolNumber.getAndIncrement() + "-thread-";
    }

    public Thread newThread(Runnable r) {
        Thread t = new Thread(group, r, namePrefix + threadNumber.getAndIncrement(), 0);
        if ((!t.isDaemon()) && sysConfService.getThreadFactoryMakeDaemon())
            t.setDaemon(true);
        if (t.getPriority() != Thread.NORM_PRIORITY)
            t.setPriority(Thread.NORM_PRIORITY);
        return t;
    }
}