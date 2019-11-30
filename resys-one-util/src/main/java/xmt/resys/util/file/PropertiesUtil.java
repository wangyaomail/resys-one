package xmt.resys.util.file;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * 基础配置文件解析类
 * @INFO 在springboot下该配置文件暂时不需要，但暂时保留
 */
public class PropertiesUtil {
    private static Map<String, Properties> propertiesMap = new HashMap<String, Properties>();

    public static String get(String propertiesFileName,
                             String key,
                             String defaultVal) {
        if (null != propertiesFileName && 0 < propertiesFileName.trim().length()) {
            if (propertiesMap.containsKey(propertiesFileName)) {
                String res = propertiesMap.get(propertiesFileName).getProperty(key);
                return res == null || res.length() == 0 ? defaultVal : res;
            } else {
                Properties properties = new Properties();
                try {
                    properties.load(PropertiesUtil.class.getClassLoader()
                                                        .getResourceAsStream(propertiesFileName.trim()));
                    propertiesMap.put(propertiesFileName, properties);
                    return PropertiesUtil.get(propertiesFileName, key, defaultVal);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        System.out.println(key + "not found in " + propertiesFileName);
        return defaultVal;
    }

    public static String get(String propertiesFileName,
                             String key) {
        return get(propertiesFileName, key, null);
    }

    public static int getInt(String propertiesFileName,
                             String key,
                             int defaultValue) {
        return Integer.parseInt(get(propertiesFileName, key, String.valueOf(defaultValue)));
    }

    public static double getDouble(String propertiesFileName,
                                   String key,
                                   double defaultValue) {
        return Double.parseDouble(get(propertiesFileName, key, String.valueOf(defaultValue)));
    }

    public static boolean getBoolean(String propertiesFileName,
                                     String key,
                                     boolean defaultValue) {
        return Boolean.parseBoolean(get(propertiesFileName, key, defaultValue + ""));
    }

    public static long getLong(String propertiesFileName,
                               String key,
                               long defaultValue) {
        return Long.parseLong(get(propertiesFileName, key, defaultValue + ""));
    }

    public static String getFromFile(String path,
                                     String key) {
        String defaultVal = "";
        if (null != path && 0 < path.trim().length()) {
            Properties properties = new Properties();
            try {
                properties.load(new FileInputStream(path));
                return properties.getProperty(key);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return defaultVal;
    }

    public static List<String> getAllKeys(String path) {
        List<String> defaultVal = new ArrayList<>();
        if (null != path && 0 < path.trim().length()) {
            Properties properties = new Properties();
            try {
                properties.load(new FileInputStream(path));
                Enumeration<?> propertyNames = properties.propertyNames();
                while (propertyNames.hasMoreElements()) {
                    Object nextElement = propertyNames.nextElement();
                    defaultVal.add((String) nextElement);
                }
                return defaultVal;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return defaultVal;
    }

    // 将spring的配置文件转成SysConfService能用的格式
    public static void main(String[] args) throws Exception {
        BufferedReader reader = new BufferedReader(new InputStreamReader(PropertiesUtil.class.getClassLoader()
                                                                                             .getResourceAsStream("application.properties")));
        String line = null;
        while ((line = reader.readLine()) != null) {
            try {
                if (line.contains("=")) {
                    char[] ks = line.split("=")[0].toCharArray();
                    StringBuilder sb = new StringBuilder();
                    for (int p = 0; p < ks.length; p++) {
                        if (ks[p] == '.') {
                            p++;
                            sb.append(Character.toUpperCase(ks[p]));
                        } else {
                            sb.append(ks[p]);
                        }
                    }
                    System.out.println(sb.toString());
                }
            } finally {
            }
        }
        reader.close();
    }
}