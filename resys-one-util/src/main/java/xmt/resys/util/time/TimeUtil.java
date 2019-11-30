package xmt.resys.util.time;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * provide simple time funcs
 */
public class TimeUtil {
    private static Logger logger = LoggerFactory.getLogger(TimeUtil.class);
    public static long A_MINUTE = 60l * 1000;
    public static long AN_HOUR = 3600l * 1000;
    public static long A_DAY = 24l * 3600 * 1000;

    public static int getIntervalDays(Date startday,
                                      Date endday) {
        if (startday.after(endday)) {
            Date cal = startday;
            startday = endday;
            endday = cal;
        }
        long sl = startday.getTime();
        long el = endday.getTime();
        long ei = el - sl;
        return (int) (ei / (1000l * 60 * 60 * 24));
    }

    private static HashMap<String, SimpleDateFormat> sdfx = new HashMap<>();
    static {
        sdfx.put("year", new SimpleDateFormat("yyyy"));
        sdfx.put("month", new SimpleDateFormat("yyyy-MM"));
        sdfx.put("month-underline", new SimpleDateFormat("yyyy_MM"));
        sdfx.put("day", new SimpleDateFormat("yyyy-MM-dd"));
        sdfx.put("hour", new SimpleDateFormat("yyyy-MM-dd-HH"));
        sdfx.put("hh", new SimpleDateFormat("HH"));
        sdfx.put("minute", new SimpleDateFormat("yyyy-MM-dd-HH-mm"));
        sdfx.put("second", new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss"));
        sdfx.put("linux", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));
        sdfx.put("excel", new SimpleDateFormat("yyyy/MM/dd"));
    }

    /**
     * 这个函数传进来的一定是yyyy到yyyy-MM-dd-HH-mm-ss这样格式中的一个，其它格式不支持
     * @throws ParseException
     */
    public static Date checkAndParse(String date) throws ParseException {
        String[] toks = date.split("-");
        switch (toks.length) {
        case 1:
            return sdfx.get("year").parse(date);
        case 2:
            return sdfx.get("month").parse(date);
        case 3:
            return sdfx.get("day").parse(date);
        case 4:
            return sdfx.get("hour").parse(date);
        case 5:
            return sdfx.get("minute").parse(date);
        case 6:
            return sdfx.get("second").parse(date);
        default:
            throw new ParseException("时间格式解析失败", -1);
        }
    }

    public static String getStringFromFreq(Date date,
                                           String freq) {
        SimpleDateFormat sdf = sdfx.get(freq);
        if (sdf != null) {
            return sdf.format(date);
        }
        return null;
    }

    public static String getStringFromFreq(long date,
                                           String freq) {
        return getStringFromFreq(new Date(date), freq);
    }

    public static Date getDateFromFreq(String date,
                                       String freq)
            throws ParseException {
        SimpleDateFormat sdf = sdfx.get(freq);
        if (sdf != null) {
            return sdf.parse(date);
        }
        return null;
    }

    public static String getStringFromStringYMDHMS(String date,
                                                   String srcFreq,
                                                   String outFreg) {
        try {
            Date time = getDateFromFreq(date, srcFreq);
            return getStringFromFreq(time, outFreg);
        } catch (Exception e) {
            logger.error("time parse error...", e);
            return null;
        }
    }

    // return range fix time from tiemPoint
    public static long getFixMin(long timePoint) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(timePoint);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTimeInMillis();
    }

    // get start time back to given time range
    public static String getFixTimeStringStartFromNow(long range) {
        return sdfx.get("linux").format(new Date(System.currentTimeMillis() - range));
    }

    /**
     * get num interval
     * @param timeStart
     * @param timeEnd
     * @param type      1-day, 2-hour, 3-minute
     * @return time num interval
     */
    public static int getTimeNumBetweenTwoTime(long timeStart,
                                               long timeEnd,
                                               int type) {
        if (timeStart > timeEnd) {
            return 0;
        }
        switch (type) {
        case 1:
            return (int) ((timeEnd - timeStart) / (24l * 3600 * 1000));
        case 2:
            return (int) ((timeEnd - timeStart) / (3600l * 1000));
        case 3:
            return (int) ((timeEnd - timeStart) / (60l * 1000));
        default:
            return 1000;
        }
    }

    /**
     * generate a time period list by start timestamp and end timestamp
     * @param freq contains day,hour,minute,second
     */
    public static List<String> generateATimeList(long startTime,
                                                 long endTime,
                                                 String freq) {
        if (StringUtils.isBlank(freq) || endTime < startTime) {
            return null;
        } else {
            List<String> timeList = new ArrayList<>();
            long tick = endTime;
            switch (freq) {
            case "day":
                while (tick > startTime) {
                    timeList.add(getStringFromFreq(tick, "day"));
                    tick -= 24l * 3600 * 1000;
                }
                break;
            case "hour":
                while (tick > startTime) {
                    timeList.add(getStringFromFreq(tick, "hour"));
                    tick -= 3600l * 1000;
                }
                break;
            case "minute":
                while (tick > startTime) {
                    timeList.add(getStringFromFreq(tick, "minute"));
                    tick -= 60l * 1000;
                }
                break;
            case "second":
                while (tick > startTime) {
                    timeList.add(getStringFromFreq(tick, "second"));
                    tick -= 1000l;
                }
                break;
            default:
                break;
            }
            return timeList;
        }
    }

    /**
     * get today 00:00:00 time unit
     */
    public static long getTodayStartTS() {
        return getTodayStartDate().getTime();
    }

    public static Date getTodayStartDate() {
        Calendar c = new GregorianCalendar();
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        return c.getTime();
    }

    /**
     * get today 23:59:59 time unit
     */
    public static long getTodayEndTS() {
        return getTodayStartDate().getTime();
    }

    public static Date getTodayEndDate() {
        Calendar c = new GregorianCalendar();
        c.set(Calendar.HOUR_OF_DAY, 23);
        c.set(Calendar.MINUTE, 59);
        c.set(Calendar.SECOND, 59);
        return c.getTime();
    }

    // wait func
    public static void waitTime(long time) {
        try {
            Thread.sleep(time);
        } catch (Exception e) {
            logger.error("time throws exception while sleeping...", e);
        }
    }

    public static void waitUpToTime(long time_used,
                                    long time_whole) {
        try {
            if (time_used < time_whole) {
                Thread.sleep(time_whole - time_used);
            }
        } catch (Exception e) {
            logger.error("time throws exception while sleeping...", e);
        }
    }

    public static void waitTenMs() {
        try {
            Thread.sleep(10);
        } catch (Exception e) {
            logger.error("time throws exception while sleeping...", e);
        }
    }

    public static void waitAHundredMs() {
        try {
            Thread.sleep(100);
        } catch (Exception e) {
            logger.error("time throws exception while sleeping...", e);
        }
    }

    public static void waitOneSec() {
        try {
            Thread.sleep(1000);
        } catch (Exception e) {
            logger.error("time throws exception while sleeping...", e);
        }
    }

    public static void waitTenSec() {
        try {
            Thread.sleep(10000);
        } catch (Exception e) {
            logger.error("time throws exception while sleeping...", e);
        }
    }

    public static void waitUpToOneSec(long time_used) {
        try {
            if (time_used < 1000) {
                Thread.sleep(1000 - time_used);
            }
        } catch (Exception e) {
            logger.error("time throws exception while sleeping...", e);
        }
    }

    public static void waitUpToTenSec(long time_used) {
        try {
            if (time_used < 10000) {
                Thread.sleep(10000 - time_used);
            }
        } catch (Exception e) {
            logger.error("time throws exception while sleeping...", e);
        }
    }

    public static void waitOneMin() {
        try {
            Thread.sleep(60 * 1000);
        } catch (Exception e) {
            logger.error("time throws exception while sleeping...", e);
        }
    }

    public static void waitThreeMin() {
        try {
            Thread.sleep(3 * 60 * 1000);
        } catch (Exception e) {
            logger.error("time throws exception while sleeping...", e);
        }
    }

    public static void waitTenMin() {
        try {
            Thread.sleep(5 * 60 * 1000);
        } catch (Exception e) {
            logger.error("time throws exception while sleeping...", e);
        }
    }

    public static void waitUpToOneMin(long time_used) {
        try {
            if (time_used < 60 * 1000) {
                Thread.sleep(60 * 1000 - time_used);
            }
        } catch (Exception e) {
            logger.error("time throws exception while sleeping...", e);
        }
    }

    public static void waitUpToTenMin(long time_used) {
        try {
            if (time_used < 60 * 1000) {
                Thread.sleep(10 * 60 * 1000 - time_used);
            }
        } catch (Exception e) {
            logger.error("time throws exception while sleeping...", e);
        }
    }

    public static void waitHalfHour() {
        try {
            Thread.sleep(30 * 60 * 1000);
        } catch (Exception e) {
            logger.error("time throws exception while sleeping...", e);
        }
    }

    public static void waitUpToHalfHour(long time_used) {
        try {
            if (time_used < 30 * 60 * 1000) {
                Thread.sleep(30 * 60 * 1000 - time_used);
            }
        } catch (Exception e) {
            logger.error("time throws exception while sleeping...", e);
        }
    }

    public static void waitOneHour() {
        try {
            Thread.sleep(60 * 60 * 1000);
        } catch (Exception e) {
            logger.error("time throws exception while sleeping...", e);
        }
    }

    public static void waitUpToOneHour(long time_used) {
        try {
            if (time_used < 60 * 60 * 1000) {
                Thread.sleep(60 * 60 * 1000 - time_used);
            }
        } catch (Exception e) {
            logger.error("time throws exception while sleeping...", e);
        }
    }
}
