package xmt.resys.userlog;

import org.apache.hadoop.hbase.TableName;
import org.springframework.stereotype.Repository;

import xmt.resys.common.BaseHBaseDao;

@Repository("userListClickHBaseDao")
public class UserListClickHBaseDao extends BaseHBaseDao {

    @Override
    public TableName getTable() {
        return TableName.valueOf("user_list_click");
    }

    @Override
    public String getFamily() {
        return "data";
    }

}
