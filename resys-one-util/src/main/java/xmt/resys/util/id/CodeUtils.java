package xmt.resys.util.id;

import java.io.UnsupportedEncodingException;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;

import xmt.resys.util.set.HBStringUtil;

/**
 * 编码、解码 工具类
 */
public class CodeUtils {
    public static String MD5Encode(String code) {
        if (StringUtils.isEmpty(code)) {
            return "";
        }
        return DigestUtils.md5Hex(code);
    }

    public static String MD5Encode(String code,
                                   String charset) {
        String md5 = "";
        if (StringUtils.isEmpty(code)) {
            return "";
        }
        try {
            md5 = DigestUtils.md5Hex(code.getBytes(charset));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return md5;
    }

    public static String MD5EncodeStrof8bitRandom() {
        double tempt = (1 + Math.random()) * 10000000;
        return DigestUtils.md5Hex((long) tempt + "");
    }

    public static String Base64Encode(String code) {
        if (StringUtils.isEmpty(code)) {
            return "";
        }
        return new String(Base64.encodeBase64(code.getBytes()));
    }

    public static String Base64Decode(String code) {
        if (StringUtils.isEmpty(code)) {
            return "";
        }
        return new String(Base64.decodeBase64(code));
    }

    /**
     * 将字符串的编码格式转换为utf-8
     * @param str
     * @return Name = new String(Name.getBytes("ISO-8859-1"), "utf-8");
     */
    public static String toUTF8(String str) {
        if (HBStringUtil.isBlank(str)) {
            return "";
        }
        try {
            if (str.equals(new String(str.getBytes("GB2312"), "GB2312"))) {
                str = new String(str.getBytes("GB2312"), "utf-8");
                return str;
            }
        } catch (Exception exception) {
        }
        try {
            if (str.equals(new String(str.getBytes("ISO-8859-1"), "ISO-8859-1"))) {
                str = new String(str.getBytes("ISO-8859-1"), "GBK");
                return str;
            }
        } catch (Exception exception1) {
        }
        try {
            if (str.equals(new String(str.getBytes("GBK"), "GBK"))) {
                str = new String(str.getBytes("GBK"), "utf-8");
                return str;
            }
        } catch (Exception exception3) {
        }
        return str;
    }

    public static void main(String[] args) {
        String str = "353347060711312";
        String base64Encode = CodeUtils.Base64Encode(str);
        System.out.println(base64Encode);
        String base64Decode = CodeUtils.Base64Decode(base64Encode);
        System.out.println(base64Decode);
        // String devKey = "EA02D3DF-9A25-425D-9FD1-38FDC29F1A3B";
        String apiKey = CodeUtils.MD5Encode("11", "UTF-8");
        System.out.println(apiKey);
    }
}
