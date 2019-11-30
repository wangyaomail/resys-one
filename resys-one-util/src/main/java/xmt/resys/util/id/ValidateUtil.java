package xmt.resys.util.id;

import java.util.Collection;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * 用于做一些通用的检验
 */
@SuppressWarnings("rawtypes")
public class ValidateUtil {
    public static boolean isBlank(String s) {
        return StringUtils.isBlank(s);
    }

    public static boolean isBlank(StringBuffer s) {
        if (s == null) {
            return true;
        }
        return StringUtils.isBlank(s.toString());
    }

    public static boolean isBlank(Collection c) {
        return CollectionUtils.isEmpty(c);
    }

    public static boolean isNotBlank(Collection c) {
        return !CollectionUtils.isEmpty(c);
    }

    public static boolean isNotBlank(Map m) {
        return !isBlank(m);
    }

    public static boolean isBlank(Map m) {
        return m == null || m.isEmpty();
    }

    public static <T> boolean isBlank(T[] arguments) {
        if (arguments == null || arguments.length == 0) {
            return true;
        }
        return false;
    }

    public static boolean isLetterOrDigit(String arg0) {
        if (ValidateUtil.isBlank(arg0)) {
            return false;
        }
        for (int i = 0; i < arg0.length(); i++) {
            if (!isDigit(arg0.charAt(i)) && !isLetter(arg0.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    public static boolean isDigit(char arg0) {
        if (arg0 >= '0' && arg0 <= '9') {
            return true;
        }
        return false;
    }

    public static boolean isNullOrEmpty(String value) {
        if (value == null || "".equals(value) || "".equals(value.trim()))
            return true;
        return false;
    }

    public static boolean onlyOneIsNullOrEmpty(String value1,
                                               String value2) {
        int i = 0;
        if (value1 == null || "".equals(value1) || "".equals(value1.trim()))
            i++;
        if (value2 == null || "".equals(value2) || "".equals(value2.trim()))
            i++;
        if (i == 1)
            return true;
        return false;
    }

    public static boolean isNullOrEmpty(Collection list) {
        if (list == null || list.size() == 0)
            return true;
        return false;
    }

    /**
     * Judge if the char can be converted to the Digit<BR>
     */
    static public boolean isDigital(char c) {
        if ((c < '0') || ('9' < c))
            return false;
        return true;
    }

    /**
     * Judge if the String can be converted to the Digit<BR>
     */
    static public boolean isDigit(String str) {
        if (str == null || "".equals(str))
            return false;
        for (int i = 0; i < str.length(); i = i + 1) {
            if (!isDigital(str.charAt(i)))
                return false;
        }
        return true;
    }

    static public boolean isDouble(String str) {
        if (str == null || "".equals(str))
            return false;
        for (int i = 0; i < str.length(); i = i + 1) {
            if (!isDigital(str.charAt(i))) {
                if (str.charAt(i) == '.')
                    ;
                else
                    return false;
            }
        }
        return true;
    }

    /**
     * Judge if the char is Lower Case<BR>
     */
    static public boolean isLowerCase(char c) {
        if (('a' <= c) && (c <= 'z')) {
            return true;
        }
        return false;
    }

    /**
     * Judge if the char is Captial Case<BR>
     */
    static public boolean isCapitalLetter(char c) {
        if (('A' <= c) && (c <= 'Z')) {
            return true;
        }
        return false;
    }

    /**
     * Judge if the char is letter<BR>
     */
    static public boolean isLetter(char c) {
        if (isLowerCase(c) || isCapitalLetter(c)) {
            return true;
        }
        return false;
    }

    /**
     * Judge if the char is letter<BR>
     */
    static public boolean isLetter(String str) {
        if (str == null || "".equals(str))
            return false;
        for (int i = 0; i < str.length(); i = i + 1) {
            if (!isLetter(str.charAt(i)))
                return false;
        }
        return true;
    }

    /**
     * Judge if the char is letter<BR>
     */
    static public boolean isLetterOrNum(String str) {
        if (str == null || "".equals(str))
            return false;
        for (int i = 0; i < str.length(); i = i + 1) {
            if (!isLetter(str.charAt(i)) && !isDigital(str.charAt(i)))
                return false;
        }
        return true;
    }

    /**
     * Judge if the char is Chinese<BR>
     */
    static public boolean isChinese(char c) {
        if (c > 255)
            return true;
        return false;
    }

    /**
     * Judge if the String is Chinese<BR>
     */
    static public boolean isChinese(String str) {
        for (int i = 0; i < str.length(); i++)
            if (!isChinese(str.charAt(i)))
                return false;
        return true;
    }

    /**
     * Judge if the String is Chinese<BR>
     */
    static public boolean isContainsChinese(String str) {
        for (int i = 0; i < str.length(); i++)
            if (isChinese(str.charAt(i)))
                return true;
        return false;
    }

    /**
     * 验证是否为中文
     * @param charStr
     * @return
     */
    public static boolean isChineseNew(char charStr) {
        String regEx = "[\u4e00-\u9fa5]";
        if (Pattern.matches(regEx, String.valueOf(charStr))) {
            return true;
        }
        return false;
    }

    /**
     * 判断字符串中是否有中文
     * @param charStr
     * @return
     */
    public static boolean isContainsChineseNew(String str) {
        for (int i = 0; i < str.length(); i++)
            if (isChineseNew(str.charAt(i)))
                return true;
        return false;
    }

    public static String getVerify(String eightcardid) {
        // wi =2(n-1)(mod 11)
        int[] wi = { 7, 9, 10, 5, 8, 4, 2, 1, 6, 3, 7, 9, 10, 5, 8, 4, 2, 1 };
        // verify digit
        int[] vi = { 1, 0, 'X', 9, 8, 7, 6, 5, 4, 3, 2 };
        int[] ai = new int[18];
        int remaining = 0;
        if (eightcardid.length() == 18)
            eightcardid = eightcardid.substring(0, 17);
        if (eightcardid.length() == 17) {
            int sum = 0;
            for (int i = 0; i < 17; i++) {
                String k = eightcardid.substring(i, i + 1);
                ai[i] = Integer.parseInt(k);
            }
            for (int i = 0; i < 17; i++) {
                sum = sum + wi[i] * ai[i];
            }
            remaining = sum % 11;
        }
        return remaining == 2 ? "X" : String.valueOf(vi[remaining]);
    }

    public static boolean isEmptyOrSplit(char c) {
        if (c == 32 || c == 47)
            return true;
        return false;
    }

    /**
     * @param str
     * @return
     */
    static public boolean isInt(String str) {
        try {
            for (int i = 0; i < str.length(); i = i + 1) {
                char c = str.charAt(i);
                if (c == '-' || c == ',')
                    continue;
                if (!ValidateUtil.isDigital(c))
                    return false;
            }
            Integer.parseInt(str);
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    /**
     * @param str
     * @return
     */
    static public boolean isEmail(String str) {
        try {
            if (str == null || "".equals(str))
                return false;
            int n = str.lastIndexOf("@");
            int m = str.lastIndexOf(".");
            if (n == -1 && m == -1)
                return false;
            String userName = "";
            String serverName = "";
            StringTokenizer token = new StringTokenizer(str, "@");
            while (token.hasMoreTokens()) {
                userName = token.nextToken();
                serverName = token.nextToken();
            }
            if (userName.length() < 2)
                return false;
            if (serverName.length() < 6)
                return false;
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    /**
     * @param str
     * @return
     */
    static public boolean isPhoneNumber(String str) {
        try {
            if (str == null || "".equals(str))
                return false;
            if (str.length() < 3 || str.length() > 20)
                return false;
            for (int i = 0; i < str.length(); i = i + 1) {
                char c = str.charAt(i);
                if (c == '-' || c == '(' || c == ')')
                    continue;
                if (!ValidateUtil.isDigital(str.charAt(i)))
                    return false;
            }
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    // 检查手机号是否是正确的
    public static boolean isChinaPhoneNum(String phoneNum) {
        // String regex =
        // "^((13[0-9])|(14[5|7])|(15([0-3]|[5-9]))|(17[013678])|(18[0-2,5-9]))\\d{8}$";
        String regex = "^((13[0-9])|(14[579])|(15([0-3]|[5-9]))|(16[6])|(17[0135678])|(18[0-9])|(19[89]))\\d{8}$";
        if (StringUtils.isEmpty(phoneNum) || phoneNum.length() != 11) {
            return false;
        } else {
            Pattern p = Pattern.compile(regex);
            Matcher m = p.matcher(phoneNum);
            return m.matches();
        }
    }

    /**
     * @param str
     * @return
     */
    static public boolean isZip(String str) {
        try {
            if (str == null || "".equals(str))
                return false;
            if (str.length() != 6)
                return false;
            for (int i = 0; i < str.length(); i = i + 1) {
                if (!ValidateUtil.isDigital(str.charAt(i)))
                    return false;
            }
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    /** 两个可为空的字符串是否相等 */
    public static boolean isNullableStringEqual(String pass1,
                                                String pass2) {
        // 如果两个字符串都为NULL，返回TRUE
        if (ValidateUtil.isBlank(pass1) && ValidateUtil.isBlank(pass2))
            return true;
        // 如果只有字符串都为NULL，返回false
        if (ValidateUtil.isBlank(pass1) || ValidateUtil.isBlank(pass2))
            return false;
        // 判断两个字符串的内容是否相等
        if (pass1.equals(pass2))
            return true;
        return false;
    }

    /**
     * 判断字符串数组中是否包含该字符串
     * @param arrays
     * @param s
     * @return
     */
    public static boolean isArrayContains(String[] arrays,
                                          String s) {
        try {
            if (arrays == null || ValidateUtil.isBlank(s))
                return false;
            for (String ss : arrays) {
                if (s.equalsIgnoreCase(ss)) {
                    return true;
                }
            }
        } catch (Exception e) {
        }
        return false;
    }

    public static boolean isArrayContains(Integer[] arrays,
                                          Integer s) {
        try {
            if (arrays == null || s == null)
                return false;
            for (Integer ss : arrays) {
                if (ss.intValue() == s.intValue()) {
                    return true;
                }
            }
        } catch (Exception e) {
        }
        return false;
    }
}
