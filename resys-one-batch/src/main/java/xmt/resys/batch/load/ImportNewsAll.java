package xmt.resys.batch.load;

import java.io.IOException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.PosixParser;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.Mutation;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.io.ImmutableBytesWritable;
import org.apache.hadoop.hbase.mapreduce.TableOutputFormat;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.util.GenericOptionsParser;
import org.apache.hbase.thirdparty.org.apache.commons.cli.ParseException;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import com.alibaba.fastjson.JSONObject;

import xmt.resys.batch.base.BaseJob;
import xmt.resys.util.set.HBStringUtil;

/**
 * 从hdfs中将全量的news数据导入hbase数据库
 */
public class ImportNewsAll extends BaseJob {
    static class ImportMapper extends Mapper<LongWritable, Text, ImmutableBytesWritable, Mutation> {
        /**
         * 输入的是json字符串，在map函数中进行解析并写入hbase数据库
         */
        @Override
        public void map(LongWritable offset,
                        Text line,
                        Context context)
                throws IOException {
            try {
                String lineString = line.toString().trim();
                if (HBStringUtil.isNotBlank(lineString)) {
                    JSONObject json = JSONObject.parseObject(lineString);
                    byte[] rowKey = Bytes.toBytes(json.getString("id"));
                    Put put = new Put(rowKey);
                    checkAndSet("title", json, put);
                    checkAndSet("categorys", json, put);
                    checkAndSet("createTime", json, put);
                    checkAndSet("state", json, put);
                    checkAndSet("content", json, put);
                    checkAndSet("tags", json, put);
                    context.write(new ImmutableBytesWritable(rowKey), put);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public void checkAndSet(String key,
                                JSONObject json,
                                Put put) {
            if (json.containsKey(key)) {
                put.addColumn(Bytes.toBytes("data"),
                              Bytes.toBytes(key),
                              Bytes.toBytes(json.getString(key)));
            }
        }
    }

    private static CommandLine parseArgs(String[] args) throws ParseException {
        Options options = new Options();
        Option o = new Option("t", "table", true, "table to import into (must exist)");
        o.setArgName("table-name");
        o.setRequired(true);
        options.addOption(o);
        o = new Option("i", "input", true, "the directory or file to read from");
        o.setArgName("path-in-HDFS");
        o.setRequired(true);
        options.addOption(o);
        CommandLineParser parser = new PosixParser();
        CommandLine cmd = null;
        try {
            cmd = parser.parse(options, args);
        } catch (Exception e) {
            System.err.println("ERROR: " + e.getMessage() + "\n");
            System.exit(-1);
        }
        if (cmd.hasOption("d")) {
            Logger log = Logger.getLogger("mapreduce");
            log.setLevel(Level.DEBUG);
        }
        return cmd;
    }

    public static void main(String[] args) throws Exception {// main方法
        Configuration conf = HBaseConfiguration.create();
        String[] otherArgs = new GenericOptionsParser(conf, args).getRemainingArgs();
        CommandLine cmd = parseArgs(otherArgs);
        String table = cmd.getOptionValue("t");
        String input = cmd.getOptionValue("i");
        Job job = Job.getInstance(conf, "Import from file " + input + " into table " + table);
        job.setJarByClass(ImportNewsAll.class);
        job.setMapperClass(ImportMapper.class);
        job.setOutputFormatClass(TableOutputFormat.class);
        job.getConfiguration().set(TableOutputFormat.OUTPUT_TABLE, table);
        job.setOutputKeyClass(ImmutableBytesWritable.class);
        job.setOutputValueClass(Writable.class);
        job.setNumReduceTasks(0);
        FileInputFormat.addInputPath(job, new Path(input));
        System.exit(job.waitForCompletion(true) ? 0 : 1);
    }
}
