package xmt.resys.batch.algo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.CellUtil;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.ConnectionFactory;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.Mutation;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.hbase.io.ImmutableBytesWritable;
import org.apache.hadoop.hbase.mapreduce.TableMapReduceUtil;
import org.apache.hadoop.hbase.mapreduce.TableMapper;
import org.apache.hadoop.hbase.mapreduce.TableOutputFormat;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.util.GenericOptionsParser;

import xmt.resys.batch.base.BaseJob;
import xmt.resys.util.set.CountMap;
import xmt.resys.util.set.HBCollectionUtil;
import xmt.resys.util.set.HBStringUtil;

/**
 * 计算新闻的相关性分数
 * @info 利用倒排表查询计算每个文章的相关文章（需要先加载倒排表中的黑名单词进行过滤）
 * @info 对每个文章的所有召回文章统一计算分数并排序，截断后存入news_all库
 */
public class AlgoNewsCorrelation extends BaseJob {
    private static byte[] _table_news = Bytes.toBytes("news_all");
    private static String _table_news_invert = "news_invert";
    private static final byte[] _family = Bytes.toBytes("data");
    private static int _recall_word_limit = 1000; // 最多使用多少单词进行召回
    private static int _recall_news_limit = 30; // 最多让一个新闻查找多少条相关新闻

    static class TheMapper extends TableMapper<ImmutableBytesWritable, Mutation> {
        Connection connection = null;
        Table table = null;

        /**
         * setup内初始化对hbase的连接
         */
        @Override
        protected void setup(Mapper<ImmutableBytesWritable, Result, ImmutableBytesWritable, Mutation>.Context context)
                throws IOException, InterruptedException {
            super.setup(context);
            Configuration conf = context.getConfiguration();
            try {
                _table_news = getFromConfiguration(conf, "table_news", _table_news, byte[].class);
                _table_news_invert = getFromConfiguration(conf,
                                                          "table_news_invert",
                                                          _table_news_invert,
                                                          String.class);
                _recall_word_limit = getFromConfiguration(conf,
                                                          "recall_word_limit",
                                                          _recall_word_limit,
                                                          Integer.class);
                _recall_news_limit = getFromConfiguration(conf,
                                                          "recall_news_limit",
                                                          _recall_news_limit,
                                                          Integer.class);
            } catch (Exception e) {
                throw new IOException("paramters set error");
            }
            Configuration configuration = HBaseConfiguration.create();
            connection = ConnectionFactory.createConnection(configuration);
            table = connection.getTable(TableName.valueOf(_table_news_invert));
        }

        /**
         * @input news的标题和正文字段
         */
        @Override
        protected void map(ImmutableBytesWritable key,
                           Result value,
                           Mapper<ImmutableBytesWritable, Result, ImmutableBytesWritable, Mutation>.Context context)
                throws IOException, InterruptedException {
            CountMap<String> wordMap = new CountMap<String>();
            // 1.查询标题的所有召回，注意这里对标题的分词结果进行提权，确保标题对正文分词的优先程度
            Cell titleCell = value.getColumnLatestCell(_family, Bytes.toBytes("titleTok"));
            if (titleCell != null) {
                String toks = Bytes.toString(CellUtil.cloneValue(titleCell));
                if (HBStringUtil.isNotBlank(toks)) {
                    wordMap.addAll(CountMap.fromString(toks).boost(3));
                }
            }
            // 2.拿到正文的召回
            Cell contentCell = value.getColumnLatestCell(_family, Bytes.toBytes("contentTok"));
            if (contentCell != null) {
                String toks = Bytes.toString(CellUtil.cloneValue(contentCell));
                if (HBStringUtil.isNotBlank(toks)) {
                    wordMap.addAll(CountMap.fromString(toks).boost(1));
                }
            }
            if (wordMap.size() > 0) {
                // 3.对wordMap排序并做截断
                wordMap.cut(_recall_word_limit, true);
                // 4.按照wordMap从倒排表中召回需要的新闻
                byte[] qualifier = Bytes.toBytes("invert");
                List<Get> gets = new ArrayList<Get>();
                for (String word : wordMap.keySet()) {
                    Get get = new Get(Bytes.toBytes(word));
                    get.addColumn(_family, qualifier);
                    gets.add(get);
                }
                Result[] results = table.get(gets);
                CountMap<String> corNewsMap = new CountMap<String>();
                for (Result result : results) {
                    Cell cell = result.getColumnLatestCell(_family, qualifier);
                    if (cell != null) {
                        List<String> newsList = HBCollectionUtil.stringToList(Bytes.toString(CellUtil.cloneValue(cell)));
                        if (HBCollectionUtil.isNotEmpty(newsList)) {
                            corNewsMap.addAll(newsList);
                        }
                    }
                }
                // 5.对召回的新闻排序并做截断，注意corNewsMap中的新闻得到的结果是非排序的
                corNewsMap.cut(_recall_news_limit, true);
                // @WARN!!!这里没有排序意味着相关性的召回结果排序可以另做（比如按照时间排序，或进行一下随机排序之类的）
                // last.向news写入相关新闻字段
                Put put = new Put(key.get());
                put.addColumn(Bytes.toBytes("correlation"),
                              Bytes.toBytes("correlation"),
                              Bytes.toBytes(corNewsMap.toString()));
                context.write(new ImmutableBytesWritable(key), put);
            }
        }

        /**
         * 关闭各种连接
         */
        @Override
        protected void cleanup(Mapper<ImmutableBytesWritable, Result, ImmutableBytesWritable, Mutation>.Context context)
                throws IOException, InterruptedException {
            table.close();
            connection.close();
            super.cleanup(context);
        }
    }

    public static void main(String[] args) throws Exception {
        Configuration conf = HBaseConfiguration.create();
        String[] otherArgs = new GenericOptionsParser(conf, args).getRemainingArgs();
        CommandLineGeneralParser clgp = new CommandLineGeneralParser("table_news",
                                                                     "table_news_invert",
                                                                     "recall_word_limit",
                                                                     "recall_news_limit");
        CommandLine cmd = clgp.parseArgs(otherArgs);
        _table_news = clgp.getOptionValue(cmd, "table_news", byte[].class);
        _table_news_invert = clgp.getOptionValue(cmd, "table_news_invert", String.class);
        _recall_word_limit = clgp.getOptionValue(cmd, "recall_word_limit", Integer.class);
        _recall_news_limit = clgp.getOptionValue(cmd, "recall_news_limit", Integer.class);

        Job job = Job.getInstance(conf, "计算相关新闻");
        job.setJarByClass(AlgoNewsCorrelation.class);

        List<Scan> scans = new ArrayList<Scan>();
        Scan scan = new Scan();
        scan.setCaching(200);
        scan.setCacheBlocks(false);
        scan.setAttribute(Scan.SCAN_ATTRIBUTES_TABLE_NAME, _table_news);
        scans.add(scan);
        TableMapReduceUtil.initTableMapperJob(scans,
                                              TheMapper.class,
                                              ImmutableBytesWritable.class,
                                              Mutation.class,
                                              job);

        job.setOutputFormatClass(TableOutputFormat.class);
        job.getConfiguration().set(TableOutputFormat.OUTPUT_TABLE, Bytes.toString(_table_news));
        job.setNumReduceTasks(0);

        System.exit(job.waitForCompletion(true) ? 0 : 1);
    }

}
