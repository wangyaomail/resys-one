package xmt.resys.util.set;

import java.util.List;
import java.util.Random;
import java.util.UUID;

/**
 * RandomUtils工具类：提供一些生成随机数的方法
 */
public final class RandomUtils {
    /** 用于随机选的字符和数字 */
    public static final String ALLCHAR = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
    /** 用于随机选的字符 */
    public static final String LETTERCHAR = "abcdefghijkllmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
    /** 用于随机选的数字 */
    public static final String NUMBERCHAR = "0123456789";

    private RandomUtils() {
        throw new AssertionError();
    }

    /**
     * 生成制定范围内的随机数
     * @param scopeMin 最小数
     * @param scoeMax 最大数
     * @return 随机数
     */
    public static int integer(int scopeMin,
                              int scoeMax) {
        Random random = new Random();
        return random.nextInt(scoeMax) % (scoeMax - scopeMin + 1) + scopeMin;
    }

    /**
     * 生成制定范围内的随机数
     * @param scopeMin 最小数
     * @param scoeMax 最大数
     * @return 随机数
     */
    public static long randLong(long scopeMin,
                                long scoeMax) {
        Random random = new Random();
        return random.nextLong() % ((scoeMax - scopeMin + 1) / 2) + scopeMin
                + ((scoeMax - scopeMin + 1) / 2);
    }

    /**
     * 返回固定长度的数字
     * @param length 长度
     * @return 随机数
     */
    public static String number(int length) {
        StringBuilder sb = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < length; i++) {
            sb.append(NUMBERCHAR.charAt(random.nextInt(NUMBERCHAR.length())));
        }
        return sb.toString();
    }

    /**
     * 返回一个定长的随机字符串(只包含大小写字母、数字)
     * @param length 随机字符串长度
     * @return 随机字符串
     */
    public static String getString(int length) {
        StringBuilder sb = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < length; i++) {
            sb.append(ALLCHAR.charAt(random.nextInt(ALLCHAR.length())));
        }
        return sb.toString();
    }

    /**
     * 返回一个定长的随机纯字母字符串(只包含大小写字母)
     * @param length 随机字符串长度
     * @return 随机字符串
     */
    public static String getMixString(int length) {
        StringBuilder sb = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < length; i++) {
            sb.append(ALLCHAR.charAt(random.nextInt(LETTERCHAR.length())));
        }
        return sb.toString();
    }

    /**
     * 返回一个定长的随机纯大写字母字符串(只包含大小写字母)
     * @param length 随机字符串长度
     * @return 随机字符串
     */
    public static String getLowerString(int length) {
        return getMixString(length).toLowerCase();
    }

    /**
     * 返回一个定长的随机纯小写字母字符串(只包含大小写字母)
     * @param length 随机字符串长度
     * @return 随机字符串
     */
    public static String getUpperString(int length) {
        return getMixString(length).toUpperCase();
    }

    /**
     * 生成一个定长的纯0字符串
     * @param length 字符串长度
     * @return 纯0字符串
     */
    public static String getZeroString(int length) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sb.append('0');
        }
        return sb.toString();
    }

    /**
     * 根据数字生成一个定长的字符串，长度不够前面补0
     * @param num 数字
     * @param fixdlenth 字符串长度
     * @return 定长的字符串
     */
    public static String toFixdLengthString(long num,
                                            int fixdlenth) {
        StringBuilder sb = new StringBuilder();
        String strNum = String.valueOf(num);
        if (fixdlenth - strNum.length() >= 0) {
            sb.append(getZeroString(fixdlenth - strNum.length()));
        } else {
            throw new RuntimeException("将数字" + num + "转化为长度为" + fixdlenth + "的字符串发生异常！");
        }
        sb.append(strNum);
        return sb.toString();
    }

    /**
     * 根据数字生成一个定长的字符串，长度不够前面补0
     * @param num 数字
     * @param fixdlenth 字符串长度
     * @return 定长的字符串
     */
    public static String toFixdLengthString(int num,
                                            int fixdlenth) {
        StringBuilder sb = new StringBuilder();
        String strNum = String.valueOf(num);
        if (fixdlenth - strNum.length() >= 0) {
            sb.append(getZeroString(fixdlenth - strNum.length()));
        } else {
            throw new RuntimeException("将数字" + num + "转化为长度为" + fixdlenth + "的字符串发生异常！");
        }
        sb.append(strNum);
        return sb.toString();
    }

    /**
     * 每次生成的len位数都不相同
     * @param param
     * @return 定长的数字
     */
    public static int getNotSimple(int[] param,
                                   int len) {
        Random rand = new Random();
        for (int i = param.length; i > 1; i--) {
            int index = rand.nextInt(i);
            int tmp = param[index];
            param[index] = param[i - 1];
            param[i - 1] = tmp;
        }
        int result = 0;
        for (int i = 0; i < len; i++) {
            result = result * 10 + param[i];
        }
        return result;
    }

    /**
     * 从指定的数组中随机数组中的某个元素
     * @param param 指定的数组
     * @return 随机元素
     */
    @SuppressWarnings("unchecked")
    public static <T> T randomItem(T... param) {
        int index = integer(0, param.length);
        return param[index];
    }

    public static <T> T randomItem(List<T> param) {
        int index = integer(0, param.size());
        return param.get(index);
    }

    /**
     * 从指定的数组中随机数组中的某个元素
     * @param ratio 指定元素的比例
     * @param param 指定的数组
     * @return 随机元素
     */
    public static <T> T randomItem(Integer[] ratio,
                                   T[] param) {
        return param[randomIndexInIntArr(ratio)];
    }

    /**
     * 按照int数组的值选择一个index下标，要求传入的都是非负整数
     */
    public static int randomIndexInIntArr(Integer[] ratio) {
        int number = integer(0, HBCollectionUtil.sumIntArr(ratio));
        int sum = 0, i = 0;
        for (i = 0; i < ratio.length; i++) {
            if (sum <= number && sum + ratio[i] > number) {
                break;
            }
            sum += ratio[i];
        }
        return i;
    }

    /**
     * 实现一个简单的字符串乘法
     * @param str 字符串
     * @param multiplication 乘法数量
     * @return
     */
    @SuppressWarnings("unused")
    private static String strMultiplication(String str,
                                            int multiplication) {
        StringBuilder buffer = new StringBuilder();
        for (int i = 0; i < multiplication; i++) {
            buffer.append(str);
        }
        return buffer.toString();
    }

    /**
     * 返回一个UUID编码 UUID:通用唯一识别码 (Universally Unique Identifier)
     * @return 小写的UUID
     */
    public static String uuid() {
        UUID uuid = UUID.randomUUID();
        String s = uuid.toString();
        return s.substring(0, 8) + s.substring(9, 13) + s.substring(14, 18) + s.substring(19, 23)
                + s.substring(24);
    }

    /**
     * 返回一个UUID编码 UUID:通用唯一识别码 (Universally Unique Identifier)
     * @return 大写的UUID
     */
    public static String getUUID() {
        UUID uuid = UUID.randomUUID();
        String s = uuid.toString();
        String temp = s.substring(0, 8) + s.substring(9, 13) + s.substring(14, 18)
                + s.substring(19, 23) + s.substring(24);
        return temp.toUpperCase();
    }
}
