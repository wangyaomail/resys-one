package xmt.resys.common;

import java.io.IOException;
import java.util.Date;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.annotation.PostConstruct;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Admin;
import org.apache.hadoop.hbase.client.ColumnFamilyDescriptorBuilder;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.ConnectionFactory;
import org.apache.hadoop.hbase.client.Delete;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.hbase.client.TableDescriptorBuilder;
import org.apache.hadoop.hbase.util.Bytes;

import xmt.resys.common.dao.l1.BaseDao;
import xmt.resys.util.set.HBStringUtil;

/**
 * HBase的驱动类
 * @INFO 每个表可以构造一个子类，连接不冲突
 * @INFO 禁掉专门的family
 */
public abstract class BaseHBaseDao extends BaseDao {

    protected static Configuration _configuration = null;
    protected static Connection _connection = null;
    protected static Admin _admin = null;
    protected AtomicBoolean isShutdown = new AtomicBoolean(false);
    private static Object lock = new Object();

    @PostConstruct
    public boolean init() {
        boolean initState = true;
        if (mainServer.conf().getSwitchOnHbase()) {
            synchronized (lock) {
                if (_connection == null) {
                    try {
                        _configuration = HBaseConfiguration.create();
                        _connection = ConnectionFactory.createConnection(_configuration);
                        _admin = _connection.getAdmin();
                    } catch (Exception e) {
                        logger.error("HBase初始化失败", e);
                        initState = false;
                    }
                }
            }
            if (!initState) {
                mainServer.shutdown(-1);
            }
        }
        return initState;
    }

    public abstract TableName getTable();

    public abstract String getFamily();

    /**
     * 按照类型决定如何添加
     * @INFO 注意除了基本类型之外，其它类型均按照string写入
     */
    public Put addColumn(Put put,
                         String qual,
                         Object val) {
        if (HBStringUtil.isNotBlank(qual) && val != null) {
            if (val instanceof String) {
                return put.addColumn(Bytes.toBytes(getFamily()),
                                     Bytes.toBytes(qual),
                                     Bytes.toBytes((String) val));
            } else if (val instanceof Integer) {
                return put.addColumn(Bytes.toBytes(getFamily()),
                                     Bytes.toBytes(qual),
                                     Bytes.toBytes((Integer) val));
            } else if (val instanceof Long) {
                return put.addColumn(Bytes.toBytes(getFamily()),
                                     Bytes.toBytes(qual),
                                     Bytes.toBytes((Long) val));
            } else if (val instanceof Boolean) {
                return put.addColumn(Bytes.toBytes(getFamily()),
                                     Bytes.toBytes(qual),
                                     Bytes.toBytes((Boolean) val));
            } else if (val instanceof Double) {
                return put.addColumn(Bytes.toBytes(getFamily()),
                                     Bytes.toBytes(qual),
                                     Bytes.toBytes((Double) val));
            } else if (val instanceof Float) {
                return put.addColumn(Bytes.toBytes(getFamily()),
                                     Bytes.toBytes(qual),
                                     Bytes.toBytes((Float) val));
            } else if (val instanceof Date) {
                return put.addColumn(Bytes.toBytes(getFamily()),
                                     Bytes.toBytes(qual),
                                     Bytes.toBytes(((Date) val).getTime()));
            }
        }
        return put;
    }

    public String get(String row,
                      String qual) {
        try (Table table = _connection.getTable(getTable())) {
            if (table != null) {
                Get get = new Get(Bytes.toBytes(row));
                get.addColumn(Bytes.toBytes(getFamily()), Bytes.toBytes(qual));
                Result result = table.get(get);
                byte[] val = result.getValue(Bytes.toBytes(getFamily()), Bytes.toBytes(qual));
                return Bytes.toString(val);
            }
        } catch (Exception e) {
            logger.error("HBase获取插入数据失败", e);
        }
        return null;
    }

    public boolean put(Put put) {
        try (Table table = _connection.getTable(getTable())) {
            table.put(put);
            return true;
        } catch (Exception e) {
            logger.error("HBase插入数据失败", e);
        }
        return false;
    }

    public boolean put(String row,
                       String qual,
                       String val) {
        return put(row, qual, null, val);
    }

    public boolean put(String row,
                       String qual,
                       Long ts,
                       String val) {
        try (Table table = _connection.getTable(getTable())) {
            if (table != null) {
                Put put = new Put(Bytes.toBytes(row));
                if (ts != null) {
                    put.addColumn(Bytes.toBytes(getFamily()),
                                  Bytes.toBytes(qual),
                                  ts,
                                  Bytes.toBytes(val));
                } else {
                    put.addColumn(Bytes.toBytes(getFamily()),
                                  Bytes.toBytes(qual),
                                  Bytes.toBytes(val));
                }
                table.put(put);
                return true;
            }
        } catch (Exception e) {
            logger.error("HBase插入数据失败", e);
        }
        return false;
    }

    public boolean put(String[] rows,
                       String[] quals,
                       long[] ts,
                       String[] vals) {
        try (Table table = _connection.getTable(getTable())) {
            if (table != null) {
                for (String row : rows) {
                    Put put = new Put(Bytes.toBytes(row));
                    int v = 0;
                    for (String qual : quals) {
                        String val = vals[v < vals.length ? v : vals.length - 1];
                        long t = ts[v < ts.length ? v : ts.length - 1];
                        put.addColumn(Bytes.toBytes(getFamily()),
                                      Bytes.toBytes(qual),
                                      t,
                                      Bytes.toBytes(val));
                        v++;
                    }
                    table.put(put);
                }
                return true;
            }
        } catch (Exception e) {
            logger.error("HBase插入数据失败", e);
        }
        return false;
    }

    public boolean remove(String row) {
        return remove(row, null);
    }

    public boolean remove(String row,
                          String qual) {
        try (Table table = _connection.getTable(getTable())) {
            if (table != null) {
                Delete delete = new Delete(Bytes.toBytes(row));
                if (HBStringUtil.isNotBlank(qual)) {
                    delete.addColumn(Bytes.toBytes(getFamily()), Bytes.toBytes(qual));
                }
                table.delete(delete);
                return true;
            }
        } catch (Exception e) {
            logger.error("HBase删除数据失败", e);
        }
        return false;
    }

    public boolean existsTable() {
        try {
            return _admin.tableExists(getTable());
        } catch (IOException e) {
            logger.error("检查HBase表是否存在失败", e);
            return false;
        }
    }

    public boolean createTable(int maxVersions) {
        TableDescriptorBuilder tableDesc = TableDescriptorBuilder.newBuilder(getTable());
        ColumnFamilyDescriptorBuilder familyDesc = ColumnFamilyDescriptorBuilder.newBuilder(Bytes.toBytes(getFamily()));
        familyDesc.setMaxVersions(maxVersions);
        tableDesc.setColumnFamily(familyDesc.build());
        try {
            _admin.createTable(tableDesc.build());
            return true;
        } catch (IOException e) {
            logger.error("HBase建表失败", e);
            return false;
        }
    }

    /**
     * 柔性关闭HBase
     */
    public void shutdown() {
        if (!isShutdown.getAndSet(true)) {
            synchronized (lock) {
                try {
                    // 关闭admin
                    if (_admin != null) {
                        _admin.close();
                        _admin = null;
                    }
                } catch (Exception e) {
                }
                try {
                    // 关闭connection
                    if (_connection != null) {
                        _connection.close();
                        _connection = null;
                    }
                } catch (Exception e) {
                }
            }
        }
    }
}
