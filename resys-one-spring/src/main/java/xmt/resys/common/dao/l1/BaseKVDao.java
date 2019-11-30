package xmt.resys.common.dao.l1;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;

import xmt.resys.common.server.MainServer;

/**
 * 最简单的key-value数据库
 */
@DependsOn("sysConfService")
public abstract class BaseKVDao<K,V> {
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    protected MainServer mainServer;

    public BaseKVDao() {}

    /**
     * 注意，如果使用new进行对象创建，mainServer必须手动指定
     */
    public BaseKVDao(MainServer mainServer) {
        this.mainServer = mainServer;
    }

}
