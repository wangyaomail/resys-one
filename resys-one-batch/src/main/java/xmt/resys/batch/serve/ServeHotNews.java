package xmt.resys.batch.serve;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import org.apache.commons.cli.CommandLine;

import com.couchbase.client.java.document.JsonDocument;
import com.couchbase.client.java.document.json.JsonArray;
import com.couchbase.client.java.document.json.JsonObject;

import xmt.resys.batch.base.BaseJob;
import xmt.resys.batch.base.CouchbaseSingleUseHelper;

/**
 * 将计算的热度新闻上传到couchbase上，因为热度文件较小，该程序不需要mapreduce
 */
public class ServeHotNews extends BaseJob {
    private static String _couchbase_url;
    private static String _couchbase_bucket;
    private static String _couchbase_username;
    private static String _couchbase_pwd;
    private static Integer _couchbase_ttl;
    private static String _local_hotfile;
    private static String _couchbase_general;
    private static String _couchbase_key;

    public static void main(String[] args) {
        // 1.读取输入的配置
        CommandLineGeneralParser clgp = new CommandLineGeneralParser("cb_url",
                                                                     "cb_bucket",
                                                                     "cb_username",
                                                                     "cb_pwd",
                                                                     "cb_ttl",
                                                                     "local_hotfile",
                                                                     "cb_general",
                                                                     "cb_key");
        try {
            CommandLine cmd = clgp.parseArgs(args);
            _couchbase_url = clgp.getOptionValue(cmd, "cb_url", String.class, true);
            _couchbase_bucket = clgp.getOptionValue(cmd, "cb_bucket", String.class, true);
            _couchbase_username = clgp.getOptionValue(cmd, "cb_username", String.class, true);
            _couchbase_pwd = clgp.getOptionValue(cmd, "cb_pwd", String.class, true);
            _couchbase_ttl = clgp.getOptionValue(cmd, "cb_ttl", Integer.class, true);
            _local_hotfile = clgp.getOptionValue(cmd, "local_hotfile", String.class, true);
            _couchbase_general = clgp.getOptionValue(cmd, "cb_general", String.class, true);
            _couchbase_key = clgp.getOptionValue(cmd, "cb_key", String.class, true);
        } catch (Exception e) {
            System.out.println("传入参数解析失败");
            e.printStackTrace();
            System.exit(-1);
        }
        // 3.将本地文件封装为jsonDocument上传到couchbase上
        JsonDocument doc_key = null;
        JsonDocument doc_gen = null;
        File localHotFile = new File(_local_hotfile);
        if (localHotFile.exists() && localHotFile.isFile() && localHotFile.canRead()) {
            // hotfile的格式是：id \t score
            try (BufferedReader br = new BufferedReader(new FileReader(localHotFile))) {
                JsonArray newsId = JsonArray.create();
                JsonArray newsScore = JsonArray.create();
                String line = null;
                while ((line = br.readLine()) != null) {
                    try {
                        String[] toks = line.trim().split("\t");
                        if (toks.length > 1) {
                            newsId.add(toks[0]);
                            newsScore.add(new Double(toks[1]));
                        }
                    } catch (Exception e) {
                        System.out.println("解析改行失败:" + line);
                    }
                }
                JsonObject object = JsonObject.create();
                object.put("id", _couchbase_key);
                object.put("hot", newsId);
                object.put("score", newsScore);
                object.put("updateTime", System.currentTimeMillis());
                doc_key = JsonDocument.create(_couchbase_key, object);
                doc_gen = JsonDocument.create(_couchbase_general, object);
            } catch (Exception e) {
                e.printStackTrace();
                System.exit(-1);
            }
            if (doc_key == null) {
                System.out.println("热度文件构造失败");
                System.exit(-1);
            }
            // 3.建立对couchbase的连接并发送结果
            try (CouchbaseSingleUseHelper cblinker = new CouchbaseSingleUseHelper(_couchbase_url,
                                                                                  _couchbase_bucket,
                                                                                  _couchbase_username,
                                                                                  _couchbase_pwd,
                                                                                  _couchbase_ttl)) {
                cblinker.insert(doc_key);
                cblinker.insert(doc_gen);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("本地文件读取失败，请检查文件：" + _local_hotfile);
        }

    }

}
