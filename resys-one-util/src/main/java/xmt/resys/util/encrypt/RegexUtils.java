package xmt.resys.util.encrypt;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 提供一些正则表达式校验的方法，支持验证邮箱、身份证、电话号码、密码、ip、网址、邮编、手机号、大小写、汉字或英文
 */
public class RegexUtils {
    private RegexUtils() {
        throw new AssertionError();
    }

    /**
     * 验证邮箱
     * @param 待验证的字符串
     * @return 如果是符合的字符串,返回 true,否则为 false
     */
    public static boolean isEmail(String str) {
        String regex = "^([\\w-\\.]+)@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.)|(([\\w-]+\\.)+))([a-zA-Z]{2,4}|[0-9]{1,3})(\\]?)$";
        return match(regex, str);
    }

    /**
     * 验证身份证
     * @param str 待验证的字符串
     * @return 如果是符合的字符串,返回 true,否则为 false
     */
    public static boolean isUserIdentity(String str) {
        String regex = "^((1[1-5])|(2[1-3])|(3[1-7])|(4[1-6])|(5[0-4])|(6[1-5])|71|(8[12])|91)\\d{4}(((((19|20)((\\d{2}(0[13-9]|1[012])(0[1-9]|[12]\\d|30))|(\\d{2}(0[13578]|1[02])31)|(\\d{2}02(0[1-9]|1\\d|2[0-8]))|(([13579][26]|[2468][048]|0[48])0229)))|20000229)\\d{3}(\\d|X|x))|(((\\d{2}(0[13-9]|1[012])(0[1-9]|[12]\\d|30))|(\\d{2}(0[13578]|1[02])31)|(\\d{2}02(0[1-9]|1\\d|2[0-8]))|(([13579][26]|[2468][048]|0[48])0229))\\d{3}))$";
        return match(regex, str);
    }

    /**
     * 验证电话号码
     * @param 待验证的字符串
     * @return 如果是符合格式的字符串,返回 true,否则为 false
     */
    public static boolean isTelephone(String str) {
        String regex = "^1[3|4|5|7|8]\\d{9}$";
        return match(regex, str);
    }

    /**
     * 验证输入密码条件(字符与数据同时出现)
     * @param 待验证的字符串
     * @return 如果是符合格式的字符串,返回 true,否则为 false
     */
    public static boolean isPassword(String str) {
        String regex = "((?=.*\\d)(?=.*\\D)|(?=.*[a-zA-Z])(?=.*[^a-zA-Z]))^.{6,20}$";
        return match(regex, str);
    }

    /**
     * 验证IP地址
     * @param 待验证的字符串
     * @return 如果是符合格式的字符串,返回 true,否则为 false
     */
    public static boolean isIP(String str) {
        String num = "(25[0-5]|2[0-4]\\d|[0-1]\\d{2}|[1-9]?\\d)";
        String regex = "^" + num + "\\." + num + "\\." + num + "\\." + num + "$";
        return match(regex, str);
    }

    /**
     * 验证网址Url
     * @param 待验证的字符串
     * @return 如果是符合格式的字符串,返回 true,否则为 false
     */
    public static boolean isUrl(String str) {
        String regex = "http(s)?://([\\w-]+\\.)+[\\w-]+(/[\\w- ./?%&=]*)?";
        return match(regex, str);
    }

    /**
     * 验证输入密码长度 (6-18位)
     * @param 待验证的字符串
     * @return 如果是符合格式的字符串,返回 true,否则为 false
     */
    public static boolean isPasswLength(String str) {
        String regex = "[0-9a-zA-Z]{6,16}";
        return match(regex, str);
    }

    /**
     * 验证输入邮政编号
     * @param 待验证的字符串
     * @return 如果是符合格式的字符串,返回 true,否则为 false
     */
    public static boolean isPostalcode(String str) {
        String regex = "^\\d{6}$";
        return match(regex, str);
    }

    /**
     * 验证输入手机号码
     * @param 待验证的字符串
     * @return 如果是符合格式的字符串,返回 true,否则为 false
     */
    public static boolean isHandset(String str) {
        String regex = "^[1]+[3,5]+\\d{9}$";
        return match(regex, str);
    }

    /**
     * 验证输入两位小数
     * @param 待验证的字符串
     * @return 如果是符合格式的字符串,返回 true,否则为 false
     */
    public static boolean isDecimal(String str) {
        String regex = "^[0-9]+(.[0-9]{2})?$";
        return match(regex, str);
    }

    /**
     * 验证输入一年的12个月
     * @param 待验证的字符串
     * @return 如果是符合格式的字符串,返回 true,否则为 false
     */
    public static boolean isMonth(String str) {
        String regex = "^(0?[[1-9]|1[0-2])$";
        return match(regex, str);
    }

    /**
     * 验证输入一个月的31天
     * @param 待验证的字符串
     * @return 如果是符合格式的字符串,返回 true,否则为 false
     */
    public static boolean isDay(String str) {
        String regex = "^((0?[1-9])|((1|2)[0-9])|30|31)$";
        return match(regex, str);
    }

    /**
     * 验证日期时间
     * @param 待验证的字符串
     * @return 如果是符合网址格式的字符串,返回 true,否则为 false
     */
    public static boolean isDate(String str) {
        String regex = "^((((1[6-9]|[2-9]\\d)\\d{2})-(0?[13578]|1[02])-(0?[1-9]|[12]\\d|3[01]))|(((1[6-9]|[2-9]\\d)\\d{2})-(0?[13456789]|1[012])-(0?[1-9]|[12]\\d|30))|(((1[6-9]|[2-9]\\d)\\d{2})-0?2-(0?[1-9]|1\\d|2[0-8]))|(((1[6-9]|[2-9]\\d)(0[48]|[2468][048]|[13579][26])|((16|[2468][048]|[3579][26])00))-0?2-29-)) (20|21|22|23|[0-1]?\\d):[0-5]?\\d:[0-5]?\\d$";
        return match(regex, str);
    }

    /**
     * 验证数字输入
     * @param 待验证的字符串
     * @return 如果是符合格式的字符串,返回 true,否则为 false
     */
    public static boolean isNumber(String str) {
        String regex = "^[0-9]*$";
        return match(regex, str);
    }

    /**
     * 验证非零的正整数
     * @param 待验证的字符串
     * @return 如果是符合格式的字符串,返回 true,否则为 false
     */
    public static boolean isIntNumber(String str) {
        String regex = "^\\+?[1-9][0-9]*$";
        return match(regex, str);
    }

    /**
     * 验证大写字母
     * @param 待验证的字符串
     * @return 如果是符合格式的字符串,返回 true,否则为 false
     */
    public static boolean isUpChar(String str) {
        String regex = "^[A-Z]+$";
        return match(regex, str);
    }

    /**
     * 验证小写字母
     * @param 待验证的字符串
     * @return 如果是符合格式的字符串,返回 true,否则为 false
     */
    public static boolean isLowChar(String str) {
        String regex = "^[a-z]+$";
        return match(regex, str);
    }

    /**
     * 验证验证输入字母
     * @param 待验证的字符串
     * @return 如果是符合格式的字符串,返回 true,否则为 false
     */
    public static boolean isLetter(String str) {
        String regex = "^[A-Za-z]+$";
        return match(regex, str);
    }

    /**
     * 验证验证输入汉字
     * @param 待验证的字符串
     * @return 如果是符合格式的字符串,返回 true,否则为 false
     */
    public static boolean isChinese(String str) {
        String regex = "^[\\u4e00-\\u9fa5]+$";
        return match(regex, str);
    }

    /**
     * 验证验证输入字符串
     * @param 待验证的字符串
     * @return 如果是符合格式的字符串,返回 true,否则为 false
     */
    public static boolean isLength(String str) {
        String regex = "^.{8,}$";
        return match(regex, str);
    }

    /**
     * @param regex 正则表达式字符串
     * @param str 要匹配的字符串
     * @return 如果str 符合 regex的正则表达式格式,返回true, 否则返回 false;
     */
    private static boolean match(String regex,
                                 String str) {
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(str);
        return matcher.matches();
    }
}
