package xmt.resys.videobase;

import org.apache.hadoop.hbase.TableName;
import org.springframework.stereotype.Repository;

import xmt.resys.common.BaseHBaseDao;

@Repository("articleHBaseDao")
public class ArticleHBaseDao extends BaseHBaseDao {

    @Override
    public TableName getTable() {
        return TableName.valueOf("videobase_all");
    }

    @Override
    public String getFamily() {
        return "data";
    }

}
