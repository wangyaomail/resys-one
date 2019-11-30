package xmt.resys.batch.algo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.cli.CommandLine;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.CompareOperator;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.ConnectionFactory;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.hbase.filter.Filter;
import org.apache.hadoop.hbase.filter.FilterList;
import org.apache.hadoop.hbase.filter.SubstringComparator;
import org.apache.hadoop.hbase.filter.ValueFilter;
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
import xmt.resys.util.algo.nlp.trie.Forest;
import xmt.resys.util.algo.nlp.trie.GetWord;
import xmt.resys.util.algo.nlp.trie.Library;
import xmt.resys.util.algo.nlp.trie.NlpWord;
import xmt.resys.util.set.CountMap;
import xmt.resys.util.set.FixedTreeMap;
import xmt.resys.util.set.HBCollectionUtil;

/**
 * 计算新闻的倒排表，后来把分词和建倒排表分开了，请使用AlgoNewsTokenGen和AlgoNewsInvertedWithoutToken
 * @info 计算新闻的内容和标题分词后写入hbase的倒排表中
 * @info 需要在map端加载全局词典，而不是仅有标签的词典（需要预先做到hbase中）
 * @info 这一步的reduce过程注意需要对倒排表进行清洗，删除召回高的作用词
 */
@Deprecated
public class AlgoNewsInvertedReToken extends BaseJob {
    private static byte[] _table_vb = Bytes.toBytes("news_all");
    private static String _table_vb_invert = "news_invert";
    private static final byte[] _family = Bytes.toBytes("data");
    private static String _table_dic = "dic_all";
    private static int _word_max_threshold = 1000; // 一个词最多出现的次数，超出删掉
    private static int _word_min_threshold = 3; // 一个词最少出现的次数，少于删掉
    private static int _recall_limit = 1000; // 一个词最多召回多少新闻

    static class TheMapper extends TableMapper<Text, Text> {
        // 词典森林，如果要用到分词需要对forest进行填充
        private Forest forest;

        @Override
        protected void setup(Mapper<ImmutableBytesWritable, Result, Text, Text>.Context context)
                throws IOException, InterruptedException {
            super.setup(context);
            // 从tag的hbase库中拿到所有的tag，送入分词树
            Configuration configuration = HBaseConfiguration.create();
            Connection connection = ConnectionFactory.createConnection(configuration);
            Table table = connection.getTable(TableName.valueOf(_table_dic));
            Scan scan = new Scan();
            scan.addColumn(_family, Bytes.toBytes("nature"));
            // 只要名词和动词
            List<Filter> filters = new ArrayList<Filter>();
            Filter filter1 = new ValueFilter(CompareOperator.EQUAL, new SubstringComparator("n"));
            filters.add(filter1);
            Filter filter2 = new ValueFilter(CompareOperator.EQUAL, new SubstringComparator("v"));
            filters.add(filter2);
            FilterList filterList = new FilterList(FilterList.Operator.MUST_PASS_ONE, filters);
            scan.setFilter(filterList);
            ResultScanner scanner = table.getScanner(scan);
            List<NlpWord> nlpWords = new LinkedList<>();
            for (Result res : scanner) {
                NlpWord word = new NlpWord();
                word.setName(Bytes.toString(res.getRow()));
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
            Cell titleCell = value.getColumnLatestCell(_family, Bytes.toBytes("title"));
            if (titleCell != null) {
                GetWord udg = forest.getWord(Bytes.toString(titleCell.getValueArray()));
                if (udg != null) {
                    String temp = null;
                    while ((temp = udg.getAllWords()) != null) {
                        revertMap.add(temp);
                    }
                }
            }
            // 2.计算正文的倒排
            Cell contentCell = value.getColumnLatestCell(_family, Bytes.toBytes("content"));
            if (contentCell != null) {
                GetWord udg = forest.getWord(Bytes.toString(titleCell.getValueArray()));
                if (udg != null) {
                    String temp = null;
                    while ((temp = udg.getAllWords()) != null) {
                        revertMap.add(temp);
                    }
                }
            }
            // 3.拿到文章的发布日期，如果在reduce中需要做截断，以发布日期作为截断的依据
            Cell publishCell = value.getColumnLatestCell(_family, Bytes.toBytes("publishTime"));
            long time = 0;
            if (contentCell != null) {
                time = Bytes.toLong(publishCell.getValueArray());
            }
            // last.向reduce送入倒排结果
            // 格式：word \t newsid \t publishTime
            for (Entry<String, Integer> entry : revertMap.entrySet()) {
                context.write(new Text(entry.getKey()),
                              new Text(Bytes.toString(key.get()) + "\t" + entry.getValue() + "\t"
                                      + time));
            }
        }
    }

    static class TheReducer extends TableReducer<Text, Text, ImmutableBytesWritable> {
        @SuppressWarnings("unused")
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
        CommandLineGeneralParser clgp = new CommandLineGeneralParser("table-vb",
                                                                     "table-vb-revert",
                                                                     "table-dic",
                                                                     "max",
                                                                     "min");
        CommandLine cmd = clgp.parseArgs(otherArgs);
        _table_vb = clgp.getOptionValue(cmd, "table-vb", byte[].class);
        _table_vb_invert = clgp.getOptionValue(cmd, "table-vb-invert", String.class);
        _table_dic = clgp.getOptionValue(cmd, "table-dic", String.class);
        _word_max_threshold = clgp.getOptionValue(cmd, "word-max-thres", Integer.class);
        _word_min_threshold = clgp.getOptionValue(cmd, "word-min-thres", Integer.class);
        _recall_limit = clgp.getOptionValue(cmd, "recall-limit", Integer.class);

        Job job = Job.getInstance(conf, "计算倒排表");
        job.setJarByClass(AlgoNewsInvertedReToken.class);

        List<Scan> scans = new ArrayList<Scan>();
        Scan scan = new Scan();
        scan.setCaching(200);
        scan.setCacheBlocks(false);
        scan.setAttribute(Scan.SCAN_ATTRIBUTES_TABLE_NAME, _table_vb);
        scans.add(scan);
        TableMapReduceUtil.initTableMapperJob(scans, TheMapper.class, Text.class, Text.class, job);
        TableMapReduceUtil.initTableReducerJob(_table_vb_invert, TheReducer.class, job);

        System.exit(job.waitForCompletion(true) ? 0 : 1);
    }
}
