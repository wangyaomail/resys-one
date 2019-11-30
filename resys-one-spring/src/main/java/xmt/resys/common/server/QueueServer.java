package xmt.resys.common.server;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.springframework.stereotype.Service;

import xmt.resys.common.bean.job.QueueJob;
import xmt.resys.common.service.BaseService;
import xmt.resys.util.time.TimeUtil;

@Service
public class QueueServer extends BaseService {
    private BlockingQueue<QueueJob> jobQueue = null;
    private QueueSlave[] slaves = null;
    public AtomicBoolean isShutdown = new AtomicBoolean(false);

    @PostConstruct
    public void init() {
        if (sysConfService.getSwitchOnQueue()) {
            logger.info("开始初始化任务队列");
            jobQueue = new ArrayBlockingQueue<QueueJob>(sysConfService.getQueueJobSize());
            slaves = new QueueSlave[sysConfService.getQueueSlaveSize()];
            logger.info("任务队列初始化成功");
        }
    }

    public void addAJob(QueueJob job) {
        if (sysConfService.getSwitchOnQueue()) {
            try {
                jobQueue.put(job);
            } catch (InterruptedException e) {
                logger.error("向任务队列添加任务失败", e);
            }
        }
    }

    public QueueJob takeAJob() {
        try {
            return jobQueue.take();
        } catch (InterruptedException e) {
            logger.error("线程[" + Thread.currentThread().getName() + "]获取任务过程中被中断");
        }
        return null;
    }

    public void start() {
        if (sysConfService.getSwitchOnQueue()) {
            logger.info("queue start parsing. thread size=" + sysConfService.getQueueSlaveSize());
            for (int i = 0; i < sysConfService.getQueueSlaveSize(); i++) {
                slaves[i] = new QueueSlave(i);
                slaves[i].start();
            }
        }
    }

    /**
     * 柔性关闭queue
     */
    @PreDestroy
    public void shutdown() {
        if (!isShutdown.getAndSet(true)) {
            logger.info("正在关闭系统队列，关闭监听时间：" + sysConfService.getQueueExitWaitSecond() + "秒");
            int stopCount = 0;
            while (!isEmpty() && stopCount++ < sysConfService.getQueueExitWaitSecond()) {
                logger.info("正在等待队列处理完毕，当前job队列长度=" + jobQueue.size() + "，剩余等待秒数="
                        + (sysConfService.getQueueExitWaitSecond() - stopCount));
                TimeUtil.waitOneSec();
            }
            for (int i = 0; i < sysConfService.getQueueSlaveSize(); i++) {
                if (slaves[i] != null) {
                    slaves[i].die();
                }
            }
        }
    }

    public boolean isEmpty() {
        return jobQueue.isEmpty();
    }

    class QueueSlave implements Runnable {
        private AtomicBoolean slaveLive = new AtomicBoolean(true);
        private int slaveId = -1;
        private Thread slaveThread = null;

        public QueueSlave(int id) {
            slaveId = id;
            slaveThread = new Thread(this);
            slaveThread.setDaemon(true); // 设置为守护进程，杜绝关闭问题
        }

        public void start() {
            slaveThread.start();
        }

        public void die() {
            slaveLive.set(false);
            slaveThread.interrupt();
        }

        @Override
        public void run() {
            while (slaveLive.get()) {
                QueueJob job = null;
                try {
                    job = takeAJob();
                    if (job != null) {
                        if (job.execute()) {
                            logger.info("队列任务[" + job + "]执行成功");
                        } else {
                            logger.error("队列任务[" + job + "]执行失败");
                        }
                    }
                } catch (Exception e) {
                    logger.error("队列任务[" + job + "]执行失败", e);
                }
            }
            logger.info("队列任务执行线程[" + slaveId + "]现在死掉了");
        }
    }

    public int getQueueSize() {
        return jobQueue.size();
    }

    public boolean isQueueEnable() {
        return sysConfService.getSwitchOnQueue();
    }
}
