package xmt.resys.batch.algo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.CellUtil;
import org.apache.hadoop.hbase.CompareOperator;
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
import org.apache.hadoop.hbase.filter.Filter;
import org.apache.hadoop.hbase.filter.FilterList;
import org.apache.hadoop.hbase.filter.QualifierFilter;
import org.apache.hadoop.hbase.filter.SubstringComparator;
import org.apache.hadoop.hbase.filter.ValueFilter;
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
import xmt.resys.util.set.CountMap;

/**
 * 生成新闻的全量分词
 */
public class AlgoNewsTokenGen extends BaseJob {
    private static byte[] _table_vb = Bytes.toBytes("news_all");
    private static final byte[] _family = Bytes.toBytes("data");
    private static String _table_dic = "dic_all";

    static class TheMapper extends TableMapper<ImmutableBytesWritable, Mutation> {
        // 词典森林，如果要用到分词需要对forest进行填充
        private Forest forest;

        @Override
        protected void setup(Mapper<ImmutableBytesWritable, Result, ImmutableBytesWritable, Mutation>.Context context)
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

        /**
         * @input video的title和正文
         * @output word1:count1,word2:count2,...
         */
        @Override
        protected void map(ImmutableBytesWritable key,
                           Result value,
                           Mapper<ImmutableBytesWritable, Result, ImmutableBytesWritable, Mutation>.Context context)
                throws IOException, InterruptedException {
            // 1.计算标题的倒排
            Cell titleCell = value.getColumnLatestCell(_family, Bytes.toBytes("title"));
            if (titleCell != null) {
                GetWord udg = forest.getWord(Bytes.toString(CellUtil.cloneValue(titleCell)));
                if (udg != null) {
                    CountMap<String> words = new CountMap<>();
                    String temp = null;
                    while ((temp = udg.getAllWords()) != null) {
                        words.add(temp);
                    }
                    Put put = new Put(value.getRow());
                    put.addColumn(Bytes.toBytes("data"),
                                  Bytes.toBytes("titleTok"),
                                  Bytes.toBytes(words.toString()));
                    context.write(key, put);
                }
            }
            // 2.计算正文的倒排
            Cell contentCell = value.getColumnLatestCell(_family, Bytes.toBytes("content"));
            if (contentCell != null) {
                GetWord udg = forest.getWord(Bytes.toString(CellUtil.cloneValue(contentCell)));
                if (udg != null) {
                    CountMap<String> words = new CountMap<>();
                    String temp = null;
                    while ((temp = udg.getAllWords()) != null) {
                        words.add(temp);
                    }
                    Put put = new Put(value.getRow());
                    put.addColumn(Bytes.toBytes("data"),
                                  Bytes.toBytes("contentTok"),
                                  Bytes.toBytes(words.toString()));
                    context.write(key, put);
                }
            }
        }
    }

    public static void main(String[] args) throws Exception {
        Configuration conf = HBaseConfiguration.create();
        String[] otherArgs = new GenericOptionsParser(conf, args).getRemainingArgs();
        CommandLineGeneralParser clgp = new CommandLineGeneralParser("table_vb", "table_dic");
        CommandLine cmd = clgp.parseArgs(otherArgs);
        _table_vb = clgp.getOptionValue(cmd, "table_vb", byte[].class);
        _table_dic = clgp.getOptionValue(cmd, "table_dic", String.class);

        Job job = Job.getInstance(conf, "计算全量分词");
        job.setJarByClass(AlgoNewsTokenGen.class);

        List<Scan> scans = new ArrayList<Scan>();
        Scan scan = new Scan();
        scan.setCaching(200);
        scan.setCacheBlocks(false);
        scan.setAttribute(Scan.SCAN_ATTRIBUTES_TABLE_NAME, _table_vb);
        // 只需要content和title
        List<Filter> filters = new ArrayList<Filter>();
        filters.add(new QualifierFilter(CompareOperator.EQUAL, new SubstringComparator("title")));
        filters.add(new QualifierFilter(CompareOperator.EQUAL, new SubstringComparator("content")));
        FilterList filterList = new FilterList(FilterList.Operator.MUST_PASS_ONE, filters);
        scan.setFilter(filterList);
        scans.add(scan);
        TableMapReduceUtil.initTableMapperJob(scans,
                                              TheMapper.class,
                                              ImmutableBytesWritable.class,
                                              Mutation.class,
                                              job);
        job.setOutputFormatClass(TableOutputFormat.class);
        job.getConfiguration().set(TableOutputFormat.OUTPUT_TABLE, Bytes.toString(_table_vb));
        job.setNumReduceTasks(0);
        System.exit(job.waitForCompletion(true) ? 0 : 1);
    }
}
