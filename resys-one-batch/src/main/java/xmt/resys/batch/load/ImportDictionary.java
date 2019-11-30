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

import xmt.resys.batch.base.BaseJob;
import xmt.resys.util.set.HBStringUtil;

/**
 * 从hdfs中将词典导入hbase
 */
public class ImportDictionary extends BaseJob {
    static class ImportMapper extends Mapper<LongWritable, Text, ImmutableBytesWritable, Mutation> {
        /**
         * @input word \t nature \t num
         */
        @Override
        public void map(LongWritable offset,
                        Text line,
                        Context context)
                throws IOException {
            try {
                String lineString = line.toString().trim();
                if (HBStringUtil.isNotBlank(lineString)) {
                    String[] toks = lineString.split("\t");
                    if (HBStringUtil.isNotBlank(toks[0])) {
                        Put put = new Put(Bytes.toBytes(toks[0]));
                        put.addColumn(Bytes.toBytes("data"),
                                      Bytes.toBytes("name"),
                                      Bytes.toBytes(toks[0]));
                        if (toks.length > 1 && HBStringUtil.isNotBlank(toks[1])) {
                            put.addColumn(Bytes.toBytes("data"),
                                          Bytes.toBytes("nature"),
                                          Bytes.toBytes(toks[1]));
                        }
                        context.write(new ImmutableBytesWritable(Bytes.toBytes(toks[0])), put);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
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
        job.setJarByClass(ImportDictionary.class);
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
