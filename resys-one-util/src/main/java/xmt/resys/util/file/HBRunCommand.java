package xmt.resys.util.file;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HBRunCommand {
    private static final Logger logger = LoggerFactory.getLogger(HBRunCommand.class);

    public static boolean runCmdForBoolean(String command) {
        try {
            System.out.println(Arrays.asList(command));
            ProcessBuilder pb = new ProcessBuilder(command);
            pb.directory(new File("/"));
            ArrayList<String> commands = new ArrayList<>();
            commands.add("/bin/bash");
            commands.add("-c");
            commands.add("PATH=$PATH:/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin: && "
                    + command);
            pb.command(commands);
            Process p = pb.start();
            p.waitFor();
            ByteArrayOutputStream result = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int length;
            while ((length = p.getInputStream().read(buffer)) != -1) {
                result.write(buffer, 0, length);
            }
            String resultStr = result.toString("UTF-8");
            logger.info(resultStr);
            return p.exitValue() == 0;
        } catch (Exception e) {
            logger.error("执行系统指令失败", e);
            return false;
        }
    }

    public static String runCmdForString(String command) {
        try {
            System.out.println(Arrays.asList(command));
            ProcessBuilder pb = new ProcessBuilder(command);
            pb.directory(new File("/"));
            ArrayList<String> commands = new ArrayList<>();
            commands.add("/bin/bash");
            commands.add("-c");
            commands.add("PATH=$PATH:/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin: && "
                    + command);
            pb.command(commands);
            Process p = pb.start();
            p.waitFor();
            ByteArrayOutputStream result = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int length;
            while ((length = p.getInputStream().read(buffer)) != -1) {
                result.write(buffer, 0, length);
            }
            String resultStr = result.toString("UTF-8");
            logger.info(resultStr);
            return resultStr;
        } catch (Exception e) {
            logger.error("执行系统指令失败", e);
            return "执行系统指令失败";
        }
    }

    /**
     * 获取传入目录大小
     */
    public static String getDirSize(String path) {
        if (StringUtils.isNotEmpty(path)) {
            File dir = new File(path);
            if (dir.exists() && dir.isDirectory()) {
                String result = runCmdForString("du -sh " + path);
                return StringUtils.isNotEmpty(result) ? result.trim().split("\t")[0] : null;
            }
        }
        return null;
    }

    // 删除路径
    public static boolean rm(String absolutePath) {
        // 检查是否是空
        if (StringUtils.isNotBlank(absolutePath)) {
            String trimPath = absolutePath.trim();
            // 检查路径是否是一级路径或相对路径，而且只能删除/data文件夹下的文件，同时不能有//这样的路径
            String[] splitPath = trimPath.split("/");
            if (splitPath.length >= 3 && StringUtils.isBlank(splitPath[0])
                    && "data".equals(splitPath[1]) && StringUtils.isNotBlank(splitPath[2])) {
                return runCmdForBoolean("rm -rf " + absolutePath);
            }
        }
        return false;
    }
}
