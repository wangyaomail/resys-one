package xmt.resys.common.service;

import javax.annotation.PostConstruct;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import lombok.Data;
import xmt.resys.util.time.TimeUtil;

/**
 * 系统的标准化时间服务
 */
@Data
@Service
public class TimerService {
    // 以2018-08-02-09-40时间节点为示例的起始时间
    public static String now_to_day; // 今日天，2018-08-02
    public static String las_to_day; // 昨日天，2018-08-01
    public static String la3_to_day; // 三天前，2018-07-31
    public static String la4_to_day; // 四天前，2018-07-30
    public static String nxt_to_day; // 明日天，2018-08-03
    public static String now_to_hour; // 当前小时，2018-08-02-09
    public static String las_to_hour; // 上一小时，2018-08-02-08
    public static String nxt_to_hour; // 下一小时，2018-08-02-10
    public static String now_to_min; // 当前分钟，2018-08-02-09-40
    public static String las_to_min; // 上一分钟，2018-08-02-09-39
    public static String nxt_to_min; // 下一分钟，2018-08-02-09-41
    public static long now_to_timestamp; // 当前时间戳，long

    @PostConstruct
    public void init() {
        resetMinClock();
        resetHourClock();
        resetDayClock();
    }

    /**
     * 每分钟更新一次分钟时间
     */
    @Scheduled(cron = "0 * * * * *")
    public void resetMinClock() {
        long nowTime = System.currentTimeMillis();
        now_to_min = TimeUtil.getStringFromFreq(nowTime, "minute");
        las_to_min = TimeUtil.getStringFromFreq(nowTime - 60l * 1000, "minute");
        nxt_to_min = TimeUtil.getStringFromFreq(nowTime + 60l * 1000, "minute");
        now_to_timestamp = nowTime;
    }

    /**
     * 每小时更新一次小时级时间
     */
    @Scheduled(cron = "0 0 * * * *")
    public void resetHourClock() {
        long nowTime = System.currentTimeMillis();
        now_to_hour = TimeUtil.getStringFromFreq(nowTime, "hour");
        las_to_hour = TimeUtil.getStringFromFreq(nowTime - 60l * 60 * 1000, "hour");
        nxt_to_hour = TimeUtil.getStringFromFreq(nowTime + 60l * 60 * 1000, "hour");
        now_to_timestamp = nowTime;
    }

    /**
     * 每天更新一次天级时间
     */
    @Scheduled(cron = "0 0 0 * * *")
    public void resetDayClock() {
        long nowTime = System.currentTimeMillis();
        now_to_day = TimeUtil.getStringFromFreq(nowTime, "day");
        las_to_day = TimeUtil.getStringFromFreq(nowTime - 24l * 3600 * 1000, "day");
        la3_to_day = TimeUtil.getStringFromFreq(nowTime - 24l * 3600 * 1000 * 2, "day");
        la4_to_day = TimeUtil.getStringFromFreq(nowTime - 24l * 3600 * 1000 * 3, "day");
        nxt_to_day = TimeUtil.getStringFromFreq(nowTime + 24l * 3600 * 1000, "day");
        now_to_timestamp = nowTime;
    }
}
