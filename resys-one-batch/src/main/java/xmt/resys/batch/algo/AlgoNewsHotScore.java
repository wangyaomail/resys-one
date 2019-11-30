package xmt.resys.batch.algo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.cli.CommandLine;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.CellUtil;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.ConnectionFactory;
import org.apache.hadoop.hbase.client.Mutation;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
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
import xmt.resys.util.algo.nlp.trie.Forest;
import xmt.resys.util.algo.nlp.trie.GetWord;
import xmt.resys.util.algo.nlp.trie.Library;
import xmt.resys.util.algo.nlp.trie.NlpWord;

/**
 * 计算热度
 * @info 写入hbase的news_all表hot_score字段，但不涉及排序
 */
public class AlgoNewsHotScore extends BaseJob {
    private static byte[] _table_news_all = Bytes.toBytes("news_all");
    private static final byte[] _family = Bytes.toBytes("data");
    private static String _table_tags_all = "tag_all";
    // 计算热度的各个数据源重要性权重，各个参数只注重比例关系
    private static double _w_title_tag = 0.4; // 新闻标题标签
    private static double _w_content_tag = 0.03; // 新闻内容标签
    private static double _w_timeliness = 0.5; // 时效性
    private static double _w_clickNum = 0.9; // 点击量
    private static double _w_quality = 0.5; // 质量评分

    static class TheMapper extends TableMapper<ImmutableBytesWritable, Mutation> {
        // 词典森林，如果要用到分词需要对forest进行填充
        private Forest forest;
        private long currentTime = System.currentTimeMillis();

        @Override
        protected void setup(Mapper<ImmutableBytesWritable, Result, ImmutableBytesWritable, Mutation>.Context context)
                throws IOException, InterruptedException {
            super.setup(context);
            Configuration conf = context.getConfiguration();
            try {
                _table_tags_all = getFromConfiguration(conf,
                                                       "tags_all",
                                                       _table_tags_all,
                                                       String.class);
            } catch (Exception e) {
                throw new IOException("paramters set error");
            }
            // 从tag的hbase库中拿到所有的tag，送入分词树
            Configuration configuration = HBaseConfiguration.create();
            Connection connection = ConnectionFactory.createConnection(configuration);
            Table table = connection.getTable(TableName.valueOf(_table_tags_all));
            Scan scan = new Scan();
            scan.addColumn(_family, Bytes.toBytes("w"));
            ResultScanner scanner = table.getScanner(scan);
            List<NlpWord> nlpWords = new ArrayList<NlpWord>(200);
            for (Result res : scanner) {
                NlpWord word = new NlpWord();
                word.setName(Bytes.toString(res.getRow()));
                Cell weightCell = res.getColumnLatestCell(_family, Bytes.toBytes("w"));
                if (weightCell != null) {
                    String weightString = Bytes.toString(CellUtil.cloneValue(weightCell));
                    word.setWeight(Double.parseDouble(weightString));
                } else {
                    word.setWeight(0.1); // 设置为默认值0.1
                }
                nlpWords.add(word);
            }
            try {
                forest = Library.makeForest(nlpWords);
            } catch (Exception e) {
                e.printStackTrace();
            }
            scanner.close();
            table.close();
            connection.close();
        }

        public Map<String, NlpWord> getWordMap(String content) {
            GetWord udg = forest.getWord(content);
            Map<String, NlpWord> wordMap = new HashMap<>();
            if (udg != null) {
                String temp = null;
                while ((temp = udg.getAllWords()) != null) {
                    NlpWord word = wordMap.get(temp);
                    if (word != null) {
                        word.setNum(word.getNum() + 1);
                    } else {
                        wordMap.put(temp, udg.getParam().newWord().numPP()); // 注意这里拿到的是新的词，我们可以在这些词上进行各种操作而不需要担心会影响到分词词典
                    }
                }
            }
            return wordMap;
        }

        /**
         * @input news中的数据
         * @output 视频计算的热度分数
         */
        @Override
        protected void map(ImmutableBytesWritable key,
                           Result value,
                           Mapper<ImmutableBytesWritable, Result, ImmutableBytesWritable, Mutation>.Context context)
                throws IOException, InterruptedException {
            double hotScore = 0.0;
            // 1.计算标题中标签所占权重
            Cell titleCell = value.getColumnLatestCell(_family, Bytes.toBytes("title"));
            if (titleCell != null) {
                Map<String, NlpWord> titleWords = getWordMap(Bytes.toString(CellUtil.cloneValue(titleCell)));
                for (NlpWord word : titleWords.values()) {
                    hotScore += _w_title_tag * word.getWeight();
                }
            }
            // 2.计算正文中标签所占权重
            Cell contentCell = value.getColumnLatestCell(_family, Bytes.toBytes("content"));
            if (contentCell != null) {
                Map<String, NlpWord> contentWords = getWordMap(Bytes.toString(CellUtil.cloneValue(contentCell)));
                for (NlpWord word : contentWords.values()) {
                    hotScore += _w_content_tag * word.getWeight();
                }
            }
            // 3.计算时效性，时效性以发表时间作为判定依据（根据业务特点也可以换做updateTime），以天为单位
            Cell createTimeCell = value.getColumnLatestCell(_family, Bytes.toBytes("createTime"));
            if (createTimeCell != null) {
                long createTime = Bytes.toLong(CellUtil.cloneValue(createTimeCell));
                if (createTime > 0 && createTime < currentTime) {
                    hotScore += _w_timeliness * (currentTime - createTime) / (24l * 3600 * 1000);
                }
            }
            // 4.计算点击率
            Cell clickNumCell = value.getColumnLatestCell(_family, Bytes.toBytes("click"));
            if (clickNumCell != null) {
                String clickString = Bytes.toString(CellUtil.cloneValue(clickNumCell));
                if (clickString != null) {
                    Integer clickNum = Integer.parseInt(clickString);
                    if (clickNum > 0) {
                        hotScore += _w_clickNum * clickNum / 10000;
                    }
                }
            }
            // 5.计算质量分
            Cell qualityCell = value.getColumnLatestCell(_family, Bytes.toBytes("qualityScore"));
            if (qualityCell != null) {
                double qualityScore = Bytes.toDouble(CellUtil.cloneValue(qualityCell));
                if (qualityScore > 0.001) {
                    hotScore += _w_quality * qualityScore;
                }
            }
            // 6.将hotScore写入HBase
            Put put = new Put(value.getRow());
            put.addColumn(Bytes.toBytes("data"),
                          Bytes.toBytes("hotScore"),
                          Bytes.toBytes(hotScore + ""));
            context.write(key, put);
        }
    }

    public static void main(String[] args) throws Exception {// main方法
        Configuration conf = HBaseConfiguration.create();
        String[] otherArgs = new GenericOptionsParser(conf, args).getRemainingArgs();
        CommandLineGeneralParser clgp = new CommandLineGeneralParser("news_all", "tags_all");

        CommandLine cmd = clgp.parseArgs(otherArgs);
        clgp.fillConfiguration(conf, cmd);
        _table_news_all = clgp.getOptionValue(cmd, "news_all", byte[].class);
        _table_tags_all = clgp.getOptionValue(cmd, "tags_all", String.class);

        Job job = Job.getInstance(conf, "计算所有视频的热度分数");
        job.setJarByClass(AlgoNewsHotScore.class);

        List<Scan> scans = new ArrayList<Scan>();
        Scan scan = new Scan();
        scan.setCaching(200);
        scan.setCacheBlocks(false);
        scan.setAttribute(Scan.SCAN_ATTRIBUTES_TABLE_NAME, _table_news_all);
        scans.add(scan);
        TableMapReduceUtil.initTableMapperJob(scans,
                                              TheMapper.class,
                                              ImmutableBytesWritable.class,
                                              Mutation.class,
                                              job);

        job.setOutputFormatClass(TableOutputFormat.class);
        job.getConfiguration().set(TableOutputFormat.OUTPUT_TABLE, Bytes.toString(_table_news_all));
        job.setNumReduceTasks(0);

        System.exit(job.waitForCompletion(true) ? 0 : 1);
    }
}
