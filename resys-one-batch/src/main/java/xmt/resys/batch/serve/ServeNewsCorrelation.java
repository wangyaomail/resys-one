package xmt.resys.batch.serve;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.CellUtil;
import org.apache.hadoop.hbase.CompareOperator;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.filter.BinaryComparator;
import org.apache.hadoop.hbase.filter.FamilyFilter;
import org.apache.hadoop.hbase.filter.Filter;
import org.apache.hadoop.hbase.io.ImmutableBytesWritable;
import org.apache.hadoop.hbase.mapreduce.TableMapReduceUtil;
import org.apache.hadoop.hbase.mapreduce.TableMapper;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.lib.output.NullOutputFormat;
import org.apache.hadoop.util.GenericOptionsParser;

import com.couchbase.client.java.document.JsonDocument;
import com.couchbase.client.java.document.json.JsonArray;
import com.couchbase.client.java.document.json.JsonObject;

import xmt.resys.batch.algo.AlgoNewsCorrelation;
import xmt.resys.batch.base.BaseJob;
import xmt.resys.batch.base.CouchbaseSingleUseHelper;
import xmt.resys.util.set.CountMap;
import xmt.resys.util.set.HBStringUtil;

/**
 * 将相关新闻的结果导出到couchbase中
 * @info 因为相关新闻较多，为了快速导出使用mapreduce实现
 */
public class ServeNewsCorrelation extends BaseJob {
    private static byte[] _table_news = Bytes.toBytes("news_all");
    private static final byte[] _family = Bytes.toBytes("correlation");
    private static String _prefix = "cor_";
    private static String _couchbase_url;
    private static String _couchbase_bucket;
    private static String _couchbase_username;
    private static String _couchbase_pwd;
    private static Integer _couchbase_ttl;

    static class TheMapper extends TableMapper<NullWritable, NullWritable> {
        CouchbaseSingleUseHelper cblinker = null;
        long updateTime = System.currentTimeMillis();

        /**
         * setup内初始化对hbase的连接
         */
        @Override
        protected void setup(Mapper<ImmutableBytesWritable, Result, NullWritable, NullWritable>.Context context)
                throws IOException, InterruptedException {
            super.setup(context);
            Configuration conf = context.getConfiguration();
            try {
                _couchbase_url = getFromConfiguration(conf, "cb_url", _couchbase_url, String.class);
                _couchbase_bucket = getFromConfiguration(conf,
                                                         "cb_bucket",
                                                         _couchbase_bucket,
                                                         String.class);
                _couchbase_username = getFromConfiguration(conf,
                                                           "cb_username",
                                                           _couchbase_username,
                                                           String.class);
                _couchbase_pwd = getFromConfiguration(conf, "cb_pwd", _couchbase_pwd, String.class);
                _couchbase_ttl = getFromConfiguration(conf,
                                                      "cb_ttl",
                                                      _couchbase_ttl,
                                                      Integer.class);
                updateTime = getFromConfiguration(conf, "updateTime", updateTime, Long.class);
            } catch (Exception e) {
                throw new IOException("paramters set error");
            }
            cblinker = new CouchbaseSingleUseHelper(_couchbase_url,
                                                    _couchbase_bucket,
                                                    _couchbase_username,
                                                    _couchbase_pwd,
                                                    _couchbase_ttl);
        }

        /**
         * @input news的标题和正文字段
         */
        @Override
        protected void map(ImmutableBytesWritable key,
                           Result value,
                           Mapper<ImmutableBytesWritable, Result, NullWritable, NullWritable>.Context context)
                throws IOException, InterruptedException {
            // 1.读取correlation的内容
            Cell corCell = value.getColumnLatestCell(_family, Bytes.toBytes("correlation"));
            if (corCell != null) {
                String toks = Bytes.toString(CellUtil.cloneValue(corCell));
                if (HBStringUtil.isNotBlank(toks)) {
                    CountMap<String> wordMap = CountMap.fromString(toks);
                    // 2.写入couchbase
                    JsonObject jobj = JsonObject.create();
                    JsonArray jsarr = JsonArray.create();
                    for (String corkey : wordMap.keySet()) {
                        jsarr.add(corkey);
                    }
                    jobj.put("cors", jsarr);
                    jobj.put("updateTime", updateTime);
                    String id = _prefix + Bytes.toString(key.get());
                    jobj.put("id", id);
                    cblinker.insert(JsonDocument.create(id, jobj));
                }
            }
        }

        /**
         * 关闭各种连接
         */
        @Override
        protected void cleanup(Mapper<ImmutableBytesWritable, Result, NullWritable, NullWritable>.Context context)
                throws IOException, InterruptedException {
            cblinker.close();
            super.cleanup(context);
        }
    }

    public static void main(String[] args) throws Exception {
        Configuration conf = HBaseConfiguration.create();
        String[] otherArgs = new GenericOptionsParser(conf, args).getRemainingArgs();
        CommandLineGeneralParser clgp = new CommandLineGeneralParser("table_news",
                                                                     "cb_url",
                                                                     "cb_bucket",
                                                                     "cb_username",
                                                                     "cb_pwd",
                                                                     "cb_ttl");
        CommandLine cmd = clgp.parseArgs(otherArgs);
        clgp.fillConfiguration(conf, cmd);
        try {
            _table_news = clgp.getOptionValue(cmd, "table_news", byte[].class);
            _couchbase_url = clgp.getOptionValue(cmd, "cb_url", String.class, true);
            _couchbase_bucket = clgp.getOptionValue(cmd, "cb_bucket", String.class, true);
            _couchbase_username = clgp.getOptionValue(cmd, "cb_username", String.class, true);
            _couchbase_pwd = clgp.getOptionValue(cmd, "cb_pwd", String.class, true);
            _couchbase_ttl = clgp.getOptionValue(cmd, "cb_ttl", Integer.class, true);
        } catch (Exception e) {
            System.out.println("传入参数解析失败");
            e.printStackTrace();
            System.exit(-1);
        }
        conf.set("updateTime", System.currentTimeMillis() + "");

        Job job = Job.getInstance(conf, "上传相关新闻");
        job.setJarByClass(AlgoNewsCorrelation.class);

        List<Scan> scans = new ArrayList<Scan>();
        Scan scan = new Scan();
        scan.setCaching(200);
        scan.setCacheBlocks(false);
        scan.setAttribute(Scan.SCAN_ATTRIBUTES_TABLE_NAME, _table_news);
        Filter filter = new FamilyFilter(CompareOperator.EQUAL, new BinaryComparator(_family));
        scan.setFilter(filter);
        scans.add(scan);
        TableMapReduceUtil.initTableMapperJob(scans,
                                              TheMapper.class,
                                              NullWritable.class,
                                              NullWritable.class,
                                              job);
        job.setOutputFormatClass(NullOutputFormat.class);
        job.setNumReduceTasks(0);

        System.exit(job.waitForCompletion(true) ? 0 : 1);
    }

}
