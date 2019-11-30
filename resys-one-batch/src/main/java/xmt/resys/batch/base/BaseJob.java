package xmt.resys.batch.base;

import java.io.IOException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.PosixParser;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hbase.thirdparty.org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BaseJob {
    public static Logger logger = LoggerFactory.getLogger(BaseJob.class);

    /**
     * 创建一个hadoop用的通用的job对象
     */
    public static Job genAGeneralJob(Configuration conf,
                                     String desc)
            throws IOException, ClassNotFoundException {
        Job job = Job.getInstance(conf, desc);
        job.setJarByClass(Class.forName(Thread.currentThread().getStackTrace()[1].getClassName()));
        return job;
    }

    public static <T> T getFromConfiguration(Configuration conf,
                                             String key,
                                             T defaultValue,
                                             Class<T> clazz)
            throws Exception {
        T result = defaultValue;
        String val = conf.get(key);
        if (val != null) {
            result = CommandLineGeneralParser.parseOptionValue(val, clazz);
        }
        logger.info(key + ": " + val);
        return result;
    }

    public static class CommandLineGeneralParser {
        public CommandLineParser parser = new PosixParser();
        public Options options = new Options();

        public CommandLineGeneralParser(String... args) {
            if (args != null) {
                for (String arg : args) {
                    Option option = new Option(arg, true, arg);
                    option.setArgName(arg);
                    options.addOption(option);
                    System.out.println("add argument: " + arg);
                }
            }
        }

        public CommandLine parseArgs(String[] args) throws ParseException {
            CommandLine cmd = null;
            try {
                cmd = parser.parse(options, args);
            } catch (Exception e) {
                System.err.println("ERROR: " + e.getMessage() + "\n");
                System.exit(-1);
            }
            return cmd;
        }

        public <T> T getOptionValue(CommandLine cmd,
                                    String key,
                                    Class<T> clazz,
                                    boolean output)
                throws Exception {
            T result = getOptionValue(cmd, key, clazz);
            if (output) {
                System.out.println("paramter[" + key + "]=" + result);
            }
            return result;
        }

        public <T> T getOptionValue(CommandLine cmd,
                                    String key,
                                    Class<T> clazz)
                throws Exception {
            String arg = cmd.getOptionValue(key);
            if (arg != null) {
                return parseOptionValue(arg, clazz);
            } else {
                throw new Exception("could not find parameter:" + key);
            }
        }

        @SuppressWarnings("unchecked")
        public static <T> T parseOptionValue(String arg,
                                             Class<T> clazz)
                throws Exception {
            if (arg != null) {
                switch (clazz.getTypeName()) {
                case "int":
                case "java.lang.Integer":
                    return (T) new Integer(arg);
                case "long":
                case "java.lang.Long":
                    return (T) new Long(arg);
                case "double":
                case "java.lang.Double":
                    return (T) new Double(arg);
                case "byte[]":
                    return (T) Bytes.toBytes(arg);
                case "java.lang.String":
                default:
                    return (T) arg;
                }
            } else {
                throw new Exception("arg is null.");
            }
        }

        /**
         * 把应用启动时候传入的参数设置到configuration配置中，这里添加的都是String
         */
        public void fillConfiguration(Configuration conf,
                                      CommandLine cmd,
                                      String... args) {
            if (args != null) {
                for (String arg : args) {
                    conf.set(arg, cmd.getOptionValue(arg));
                }
            }
        }

        /**
         * 如果不设置参数，那么就是把配置的option中每一个参数都做进去
         */
        public void fillConfiguration(Configuration conf,
                                      CommandLine cmd) {
            for (Object opt : options.getOptions()) {
                String arg = ((Option) opt).getArgName();
                conf.set(arg, cmd.getOptionValue(arg));
            }
        }

    }

}
