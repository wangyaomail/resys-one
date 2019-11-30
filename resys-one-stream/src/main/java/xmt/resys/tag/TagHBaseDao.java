package xmt.resys.tag;

import org.apache.hadoop.hbase.TableName;
import org.springframework.stereotype.Repository;

import xmt.resys.common.BaseHBaseDao;

@Repository("tagHBaseDao")
public class TagHBaseDao extends BaseHBaseDao {

    @Override
    public TableName getTable() {
        return TableName.valueOf("tag_all");
    }

    @Override
    public String getFamily() {
        return "data";
    }

}
