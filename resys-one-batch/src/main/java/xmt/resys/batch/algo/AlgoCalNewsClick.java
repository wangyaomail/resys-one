package xmt.resys.batch.algo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import org.apache.commons.cli.CommandLine;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.CellUtil;
import org.apache.hadoop.hbase.CompareOperator;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.filter.BinaryComparator;
import org.apache.hadoop.hbase.filter.Filter;
import org.apache.hadoop.hbase.filter.QualifierFilter;
import org.apache.hadoop.hbase.filter.SubstringComparator;
import org.apache.hadoop.hbase.io.ImmutableBytesWritable;
import org.apache.hadoop.hbase.mapreduce.TableMapReduceUtil;
import org.apache.hadoop.hbase.mapreduce.TableMapper;
import org.apache.hadoop.hbase.mapreduce.TableReducer;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.util.GenericOptionsParser;

import xmt.resys.batch.base.BaseJob;
import xmt.resys.util.set.CountMap;
import xmt.resys.util.set.HBStringUtil;
import xmt.resys.util.time.TimeUtil;

/**
 * 从用户表中拿到每个新闻的点击次数，并统计一段时间内的news的点击次数
 * @algo 遍历用户表，将每个新闻的点击率输出到一个专门的点击率增量表中（为了避免损坏点击率后无法还原），再设计一个计算将增量表的增量计算到总表中
 * @warn 传入的时间必须是2019-11-27-10-10这样的格式，可以精确到分钟
 * @info 合并时间后，对总表的总点击次数就做了不可恢复式的修改了，如果需要可以恢复，可以对qualifier进行再次分层
 */
public class AlgoCalNewsClick extends BaseJob {
    private static byte[] _table_ul_click = Bytes.toBytes("user_list_click");
    private static String _table_nw_click = "news_all";
    private static final byte[] _family = Bytes.toBytes("data");
    private static String _start_time = "";
    private static String _end_time = "";

    static class TheMapper extends TableMapper<Text, IntWritable> {
        CountMap<String> videoClickMap = new CountMap<String>();

        @Override
        protected void setup(Mapper<ImmutableBytesWritable, Result, Text, IntWritable>.Context context)
                throws IOException, InterruptedException {
            super.setup(context);
            Configuration conf = context.getConfiguration();
            try {
                _table_ul_click = getFromConfiguration(conf,
                                                       "ul_click",
                                                       _table_ul_click,
                                                       byte[].class);
            } catch (Exception e) {
                throw new IOException("paramters set error");
            }
        }

        /**
         * @input hbase table "user_list_click"
         * @output videoid \t clicknum
         */
        @Override
        protected void map(ImmutableBytesWritable key,
                           Result columns,
                           Mapper<ImmutableBytesWritable, Result, Text, IntWritable>.Context context)
                throws IOException, InterruptedException {
            for (Cell cell : columns.listCells()) {
                String familyString = Bytes.toString(CellUtil.cloneFamily(cell));
                String qualString = Bytes.toString(CellUtil.cloneQualifier(cell));
                String valueString = Bytes.toString(CellUtil.cloneValue(cell));
                // 只要点击事件，不要列表事件
                logger.info(familyString + ":" + qualString + ":" + valueString);
                if (HBStringUtil.isNotBlank(valueString)) {
                    if (qualString.startsWith("click-")) { // 从用户点击里来的
                        // row: userid-recid String rowString = Bytes.toString(cell.getRowArray());
                        // qual: count-time value: videoid
                        if (HBStringUtil.isNotBlank(valueString)) {
                            videoClickMap.add(valueString);
                        }
                    } else if (qualString.equals("click")) { // 从总点击表中来的
                        Integer clickNum = Integer.parseInt(valueString);
                        if (clickNum > 0) {
                            videoClickMap.add(Bytes.toString(key.get()), clickNum);
                        }
                    }
                }
            }
        }

        @Override
        protected void cleanup(Mapper<ImmutableBytesWritable, Result, Text, IntWritable>.Context context)
                throws IOException, InterruptedException {
            super.cleanup(context);
            for (Entry<String, Integer> entry : videoClickMap.entrySet()) {
                // 把videoid向后传递
                context.write(new Text(entry.getKey()), new IntWritable(entry.getValue()));
            }
        }
    }

    static class TheReducer extends TableReducer<Text, IntWritable, ImmutableBytesWritable> {
        /**
         * 覆盖式写入
         * @input videoid \t clicknum
         * @output news_click: videoid \t clicknum
         */
        @Override
        public void reduce(Text key,
                           Iterable<IntWritable> values,
                           Context context)
                throws IOException, InterruptedException {
            int totalClick = 0;
            for (IntWritable clickNum : values) {
                totalClick += clickNum.get();
            }
            Put put = new Put(key.getBytes());
            put.addColumn(_family, Bytes.toBytes("click"), Bytes.toBytes(totalClick + ""));
            context.write(new ImmutableBytesWritable(key.getBytes()), put);
        }
    }

    /**
     * 遍历两个表，进行合并，合并结果写到总表内
     */
    public static void main(String[] args) throws Exception {// main方法
        Configuration conf = HBaseConfiguration.create();
        String[] otherArgs = new GenericOptionsParser(conf, args).getRemainingArgs();
        CommandLineGeneralParser clgp = new CommandLineGeneralParser("ul_click",
                                                                     "nw_click",
                                                                     "start_time",
                                                                     "end_time");
        CommandLine cmd = clgp.parseArgs(otherArgs);
        clgp.fillConfiguration(conf, cmd);
        _table_ul_click = clgp.getOptionValue(cmd, "ul_click", byte[].class);
        _table_nw_click = clgp.getOptionValue(cmd, "nw_click", String.class);
        _start_time = clgp.getOptionValue(cmd, "start_time", String.class);
        _end_time = clgp.getOptionValue(cmd, "end_time", String.class);
        // 检测start-time和end-time的格式，转换成天或小时
        long minStamp = TimeUtil.checkAndParse(_start_time).getTime();
        long maxStamp = TimeUtil.checkAndParse(_end_time).getTime();
        System.out.println("time range from " + minStamp + " to " + maxStamp);

        Job job = genAGeneralJob(conf, "计算新闻的点击次数");

        List<Scan> scans = new ArrayList<Scan>();
        {
            Scan scan = new Scan();
            scan.setCaching(200);
            scan.setCacheBlocks(false);
            scan.setAttribute(Scan.SCAN_ATTRIBUTES_TABLE_NAME, _table_ul_click);
            // 只统计Qualifier是click-开头的
            Filter filter = new QualifierFilter(CompareOperator.EQUAL,
                                                new SubstringComparator("click-"));
            scan.setFilter(filter);
            // 且按照时间戳进行筛选，这个时间戳需要在程序外面送进来
            scan.setTimeRange(minStamp, maxStamp);
            scans.add(scan);
        }
        {
            Scan scan = new Scan();
            scan.setCaching(200);
            scan.setCacheBlocks(false);
            scan.setAttribute(Scan.SCAN_ATTRIBUTES_TABLE_NAME, Bytes.toBytes(_table_nw_click));
            // 只统计Qualifier是click
            Filter filter = new QualifierFilter(CompareOperator.EQUAL,
                                                new BinaryComparator(Bytes.toBytes("click")));
            scan.setFilter(filter);
            scans.add(scan);
        }
        TableMapReduceUtil.initTableMapperJob(scans,
                                              TheMapper.class,
                                              Text.class,
                                              IntWritable.class,
                                              job);
        TableMapReduceUtil.initTableReducerJob(_table_nw_click, TheReducer.class, job);

        System.exit(job.waitForCompletion(true) ? 0 : 1);
    }

}
