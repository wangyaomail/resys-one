package xmt.resys.batch;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.util.GenericOptionsParser;

import xmt.resys.batch.base.BaseJob;

public class ArgTest extends BaseJob {
    public static void main(String[] args) throws Exception {
        System.out.println(Thread.currentThread().getStackTrace()[0].getClassName());
        for (String remainArg : args) {
            System.out.println(remainArg);
        }
        System.out.println("+++++++++++++");
        Configuration conf = HBaseConfiguration.create();
        String[] otherArgs = new GenericOptionsParser(conf, args).getRemainingArgs();
        for (String remainArg : otherArgs) {
            System.out.println(remainArg);
        }
        CommandLineGeneralParser clgp = new CommandLineGeneralParser("ul_click",
                                                                     "nw_click",
                                                                     "start_time",
                                                                     "end_time");
        System.out.println(clgp.options);
        CommandLine cmd = clgp.parseArgs(otherArgs);
        for (Option opt : cmd.getOptions()) {
            System.out.println(opt);
        }
        System.out.println(cmd.getParsedOptionValue("-ul_click"));
        if (cmd.hasOption("ul_click")) {
            System.out.println("AA" + cmd.getOptionValue("ul_click"));
        } else {
            System.out.println("123");
        }
        // System.out.println(cmd.getOptionProperties("start_time"));
        // System.out.println(cmd.getOptionValue("start_time"));
        // for (Object opt : clgp.options.getOptions()) {
        // String arg = ((Option) opt).getArgName();
        // conf.set(arg, cmd.getOptionValue(arg));
        // }
    }

}
