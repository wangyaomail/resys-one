package xmt.resys.batch.algo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map.Entry;

import org.apache.commons.cli.CommandLine;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.CellUtil;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.filter.ColumnPrefixFilter;
import org.apache.hadoop.hbase.io.ImmutableBytesWritable;
import org.apache.hadoop.hbase.mapreduce.TableMapReduceUtil;
import org.apache.hadoop.hbase.mapreduce.TableMapper;
import org.apache.hadoop.hbase.mapreduce.TableReducer;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.util.GenericOptionsParser;

import xmt.resys.batch.base.BaseJob;
import xmt.resys.util.set.CountMap;
import xmt.resys.util.set.FixedTreeMap;
import xmt.resys.util.set.HBCollectionUtil;
import xmt.resys.util.set.HBStringUtil;

/**
 * 计算新闻的倒排表
 * @info 这个建立在已经分词了的基础上
 * @info 计算新闻的内容和标题分词后写入hbase的倒排表中
 * @info 需要在map端加载全局词典，而不是仅有标签的词典（需要预先做到hbase中）
 * @info 这一步的reduce过程注意需要对倒排表进行清洗，删除召回高的作用词
 */
public class AlgoNewsInvertedWithoutToken extends BaseJob {
    private static byte[] _table_news = Bytes.toBytes("news_all");
    private static byte[] _table_news_invert = Bytes.toBytes("news_invert");
    private static final byte[] _family = Bytes.toBytes("data");
    private static int _word_max_threshold = 1000; // 一个词最多出现的次数，超出删掉
    private static int _word_min_threshold = 3; // 一个词最少出现的次数，少于删掉
    private static int _recall_limit = 1000; // 一个词最多召回多少新闻

    static class TheMapper extends TableMapper<Text, Text> {

        /**
         * @input video的title和正文
         * @output 倒排表
         */
        @Override
        protected void map(ImmutableBytesWritable key,
                           Result value,
                           Mapper<ImmutableBytesWritable, Result, Text, Text>.Context context)
                throws IOException, InterruptedException {
            CountMap<String> revertMap = new CountMap<String>();
            // 1.计算标题的倒排
            Cell titleCell = value.getColumnLatestCell(_family, Bytes.toBytes("titleTok"));
            if (titleCell != null) {
                String toks = Bytes.toString(CellUtil.cloneValue(titleCell));
                if (HBStringUtil.isNotBlank(toks)) {
                    revertMap.addAll(CountMap.fromString(toks));
                }
            }
            // 2.计算正文的倒排
            Cell contentCell = value.getColumnLatestCell(_family, Bytes.toBytes("contentTok"));
            if (contentCell != null) {
                String toks = Bytes.toString(CellUtil.cloneValue(contentCell));
                if (HBStringUtil.isNotBlank(toks)) {
                    revertMap.addAll(CountMap.fromString(toks));
                }
            }
            // 3.拿到文章的发布日期，如果在reduce中需要做截断，以发布日期作为截断的依据
            Cell createTimeCell = value.getColumnLatestCell(_family, Bytes.toBytes("createTime"));
            long time = 0;
            if (createTimeCell != null) {
                time = Bytes.toLong(CellUtil.cloneValue(createTimeCell));
            }
            // last.向reduce送入倒排结果
            // @outkey: word
            // @outvalue: newsid \t count \t createTime
            for (Entry<String, Integer> entry : revertMap.entrySet()) {
                context.write(new Text(entry.getKey()),
                              new Text(Bytes.toString(key.get()) + "\t" + entry.getValue() + "\t"
                                      + time));
            }
        }
    }

    static class TheReducer extends TableReducer<Text, Text, ImmutableBytesWritable> {
        @Override
        public void reduce(Text key,
                           Iterable<Text> values,
                           Context context)
                throws IOException, InterruptedException {
            String rowKey = key.toString();
            // treemap默认以时间有小到大排序，需要做一下倒置
            FixedTreeMap<Long, String> newsMap = new FixedTreeMap<Long, String>(new Comparator<Long>() {
                @Override
                public int compare(Long o1,
                                   Long o2) {
                    return (int) (o2 - o1);
                }
            }, _recall_limit);
            @SuppressWarnings("unused")
            int totalNum = 0;
            int totalNumUniq = 0;
            for (Text v : values) {
                try {
                    String[] toks = v.toString().trim().split("\t");
                    if (toks.length < 3)
                        continue;
                    // 我们假定没有两个new的发表时间完全一样，而且在一个词下，概率太小了
                    newsMap.put(Long.parseLong(toks[2]), toks[0]);
                    totalNum += Integer.parseInt(toks[1]);
                    totalNumUniq += 1;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (totalNumUniq > _word_min_threshold && totalNumUniq < _word_max_threshold) {
                Put put = new Put(Bytes.toBytes(rowKey));
                put.addColumn(_family,
                              Bytes.toBytes("invert"),
                              Bytes.toBytes(HBCollectionUtil.listToString(newsMap.values())));
                context.write(new ImmutableBytesWritable(Bytes.toBytes(rowKey)), put);
            }
        }
    }

    public static void main(String[] args) throws Exception {// main方法
        Configuration conf = HBaseConfiguration.create();
        String[] otherArgs = new GenericOptionsParser(conf, args).getRemainingArgs();
        CommandLineGeneralParser clgp = new CommandLineGeneralParser("table_news",
                                                                     "table_news_invert",
                                                                     "table_dic",
                                                                     "word_max_threshold",
                                                                     "word_min_threshold",
                                                                     "recall_limit");
        CommandLine cmd = clgp.parseArgs(otherArgs);
        _table_news = clgp.getOptionValue(cmd, "table_news", byte[].class);
        _table_news_invert = clgp.getOptionValue(cmd, "table_news_invert", byte[].class);
        _word_max_threshold = clgp.getOptionValue(cmd, "word_max_threshold", Integer.class);
        _word_min_threshold = clgp.getOptionValue(cmd, "word_min_threshold", Integer.class);
        _recall_limit = clgp.getOptionValue(cmd, "recall_limit", Integer.class);

        Job job = Job.getInstance(conf, "计算倒排表");
        job.setJarByClass(AlgoNewsInvertedWithoutToken.class);

        List<Scan> scans = new ArrayList<Scan>();
        Scan scan = new Scan();
        scan.setCaching(200);
        scan.setCacheBlocks(false);
        scan.setAttribute(Scan.SCAN_ATTRIBUTES_TABLE_NAME, _table_news);
        scans.add(scan);
        TableMapReduceUtil.initTableMapperJob(scans, TheMapper.class, Text.class, Text.class, job);
        TableMapReduceUtil.initTableReducerJob(Bytes.toString(_table_news_invert),
                                               TheReducer.class,
                                               job);

        System.exit(job.waitForCompletion(true) ? 0 : 1);
    }
}
