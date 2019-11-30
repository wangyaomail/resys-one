package xmt.resys.common.service;

import java.lang.reflect.Field;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import lombok.Getter;
import xmt.resys.util.set.HBStringUtil;

/**
 * 所有配置文件内的程序用到的配置项都从这里走，这样整个系统的调用是干净的，可以很快知道哪个配置项有没有用到
 * @INFO 使用PropertiesUtil线下生成
 * @INFO 将来会改为这里是默认值，如果需要改动再配置到property文件中的方式
 */
@Getter
@Service
public class SysConfService {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    @Autowired
    private Environment environment;

    /**
     * 从这里对所有配置项进行填入
     */
    @PostConstruct
    public void init() {
        Field[] fields = SysConfService.class.getDeclaredFields();
        boolean goon = false;
        for (Field field : fields) {
            if (field.getName().equals("runOnDev")) {
                // 从这个配置开示
                goon = true;
            }
            if (goon) {
                try {
                    char[] keyChars = field.getName().toCharArray();
                    StringBuilder sb = new StringBuilder();
                    for (char c : keyChars) {
                        if (Character.isUpperCase(c)) {
                            sb.append(".").append(Character.toLowerCase(c));
                        } else {
                            sb.append(c);
                        }
                    }
                    String propertyValue = environment.getProperty(sb.toString());
                    if (HBStringUtil.isNotBlank(propertyValue)) {
                        field.setAccessible(true);
                        switch (field.getType().getName()) {
                        case "java.lang.Boolean":
                            field.set(this, Boolean.parseBoolean(propertyValue));
                            break;
                        case "java.lang.Integer":
                            field.set(this, Integer.parseInt(propertyValue));
                            break;
                        case "java.lang.Long":
                            field.set(this, Long.parseLong(propertyValue));
                            break;
                        case "java.lang.String":
                            field.set(this, propertyValue);
                        default:
                            break;
                        }
                    }
                    logger.info("配置项【" + sb.toString() + "】=" + field.get(this));
                } catch (Exception e) {
                    logger.error("配置项【" + field.getName() + "】读取出错", e);
                }
            }
        }
    }

    // 从这里开始都是配置文件内的变量，如果需要赋值默认值直接后面设置就行
    // @WARN 注意这里所书写的配置项，除了点隔开的地方要大写，其它地方不要大写
    private Boolean runOnDev = true;
    private String springProfilesActive;
    private String serverHost = "localhost";
    private Integer serverPort = 9001;
    private Boolean springDevtoolsRestartEnabled;
    // mongo的配置，spring开头的配置必须在配置文件中写
    private Boolean switchOnMongo = false;
    private String springDataMongodbHost;
    private Integer springDataMongodbPort;
    private String springDataMongodbDatabase;
    private String springDataMongodbAuthenticationDatabase;
    private String springDataMongodbUsername;
    private String springDataMongodbPassword;
    private String mongoHomeLoc = "/data/xmt/libs/mongo";
    private String apiPrefixName = "resys";
    private String apiVersion = "v1";
    private String springApplicationName = "resys-one";
    // 特殊名称
    private String anonymouslyModuleName = "no_auth"; // 系统模块中匿名的模块
    private String anonymousUser = "匿名用户";
    // 队列相关
    private Boolean switchOnQueue = false;
    private String scheduledThreadPoolSize;
    private Integer queueJobSize;
    private Integer queueSlaveSize;
    private Integer queueExitWaitSecond;
    private Boolean threadFactoryMakeDaemon;
    private String loggingLevelRoot;
    private String loggingLevelHb;
    private String loggingLevelSpringfoxDocumentationSpringWeb;
    private String loggingLevelHbBkUserAuthFilterMyFilterSecurityInterceptor;
    private String loggingLevelOrgSpringframeworkWebServletMvcMethodAnnotationRequestMappingHandlerMapping;
    private String loggingLevelOrgSpringframeworkDataMongodbCoreMappingBasicMongoPersistentProperty;
    private String loggingPatternConsole;
    private String loggingPatternFile;
    private Boolean useSystemLog = false; // 是否使用系统日志，主动记录log到mongo中的时候用到
    private String sysLogDir;
    // 账户配置
    private String tokenHeader = "jwttoken"; // 秘钥在用户浏览器的header中保留的头数据
    private Long sessionExpiration = 10800000l; // 用户session过期时间（默认3小时）
    private Long tokenExpiration = 172800000l; // 登陆秘钥过期时间（默认2天）
    private String tokenPrefix = "token_"; // 秘钥的前缀
    private String tokenSaltKey = "resys-ancient-one";
    private String tokenFstAesSalt = "02b48fa08edb14a017baac4888065e1f"; // 秘钥第一段加秘的key
    private String tokenSndAesSalt = "8c78c93bca36d479ea00a33308af1243"; // 秘钥第二段加密的key
    private String defaultUserInitPwd = "Abc123456";
    // couchbase配置
    private Boolean switchOnCouchbase = false; // 是否打开Couchbase
    private String couchbaseUrl = "xmt"; // couchbase的ip地址，如果是couchbase集群，这里应使用逗号分开
    private String couchbaseBkt = "resys"; // couchbase分桶名，类似mongo的数据库，我们所有的数据都保存在一个桶下，以不同的前缀区分
    private String couchbaseUsername = "root";
    private String couchbasePassword = "123456";
    private Integer couchbaseRecArticleUserTTL = 3 * 24 * 3600; // 给用户推荐的feed的保存时间3day
    private String couchbaseRecArticleUserPrefix = "rau_"; // 用户feed的前缀名
    private Integer couchbaseRecArticleCorrelationTTL = 3 * 24 * 3600; // 相关新闻的保存时间3day
    private String couchbaseRecArticleCorrelationPrefix = "cor_"; // 相关新闻的前缀名
    private Integer couchbaseRecArticleHotTTL = 3 * 24 * 3600; // 热度的保存时间3day
    private String couchbaseRecArticleHotPrefix = "hot_"; // 热度的前缀名
    // kafka配置
    private Boolean switchOnKafka = false; // 是否打开kafka
    private String kafkaUrl = "xmt:9092"; // kafka的ip地址，如果是集群用逗号隔开
    private String kafkaUserListClickTopicName = "user_list_click"; // 用户点击一条新闻向kafka发送的点击消息
    private Integer kafkaUserListClickTopicPartition = 1; // topic对应的分区数
    private String kafkaGroupId = "resys"; // groupid对所有consumer都一样
    // zookeeper配置
    private String zookeeperConnector = "xmt:2181"; // zookeeper连接地址，如果有多个zookeeper那么用逗号分隔开
    // hadoop配置
    private Boolean switchOnHadoop = false; // 是否打开hadoop
    // hbase配置
    private Boolean switchOnHbase = false; // 是否打开hbase
    // hbase配置
    // activemq配置
    private Boolean switchOnActivemq = false; // 是否打开activemq
    private String activemqUser = "admin";
    private String activemqPwd = "admin";
    private String activemqUrl = "tcp://xmt:61616";// 如果是主从，url用逗号分隔
    // private Boolean activemqTransacted = true;
    // private Boolean activemqIsQueue = true; // topic信息消费后不删掉
    private Boolean activemqTopicIfDurable = true; // 如果是topic，是否要保持
    // private String activemqQueueName = "videobase-add"; // 队列名
    // public String activemqTopicDurableName = "resys-videobase-add-duration";
    public Integer activemqReceiveCount = -1; // if sync, set max allowed msgs
    public Integer activemqListeningConsumers = 1;
    // 推荐接口的几个参数
    private Integer feedNumberDefault = 15; // 单次最多返回的推荐条数
    private Integer feedNumberMax = 50; // 单次最多返回的推荐条数
    private Integer feedHistoryMaxLength = 1000; // 用户的历史记录最多保存多少条
    // 关于videobase
    private String videobaseAllPath = "/xmt/data/export"; // 每日全量导出的数据库文件位置
}
