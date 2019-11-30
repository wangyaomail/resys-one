package xmt.resys.util.encrypt;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

public class MD5Util {
    private static char hexDigits[] = new char[] { '0',
                                                   '1',
                                                   '2',
                                                   '3',
                                                   '4',
                                                   '5',
                                                   '6',
                                                   '7',
                                                   '8',
                                                   '9',
                                                   'a',
                                                   'b',
                                                   'c',
                                                   'd',
                                                   'e',
                                                   'f' };

    private static char[] byteToHexString(byte[] tmp) {
        char str[] = new char[32];
        int k = 0;
        byte byte0;
        for (int i = 0; i < 16; i++) {
            byte0 = tmp[i];
            str[k++] = hexDigits[byte0 >>> 4 & 0xf];
            str[k++] = hexDigits[byte0 & 0xf];
        }
        return str;
    }

    private static char[] zero32chars() {
        char zeroCHARs[] = new char[32];
        for (int i = 0; i < 32; i++)
            zeroCHARs[i] = '0';
        return zeroCHARs;
    }

    public synchronized static char[] EncodeByMd5(String str) {
        return EncodeByMd5(str, "utf-8");
    }

    public static char[] EncodeByMd5(String str,
                                     String encoding) {
        if (str == null)
            return zero32chars();
        try {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            byte[] digest = md5.digest(str.getBytes(encoding));
            if (digest == null || digest.length != 16)
                return zero32chars();
            char[] md5chars = byteToHexString(digest);
            return md5chars;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return zero32chars();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return zero32chars();
        }
    }

    public static char[] EncodeByMd5(byte[] bytes,
                                     int offset,
                                     int len) {
        byte[] buff = new byte[len];
        System.arraycopy(bytes, offset, buff, 0, len);
        return EncodeByMd5(buff);
    }

    public static char[] EncodeByMd5(byte[] bytes) {
        if (bytes == null)
            return zero32chars();
        try {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            byte[] digest = md5.digest(bytes);
            if (digest == null || digest.length != 16)
                return zero32chars();
            char[] md5chars = byteToHexString(digest);
            return md5chars;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return zero32chars();
        }
    }

    public static String getRandomMD5Code() {
        return new String(EncodeByMd5("" + new Random().nextLong()));
    }

    public static String getRandomMD5Code(String token) {
        return new String(EncodeByMd5(token + new Random().nextLong()));
    }

    public static void main(String[] args) {
        System.out.println(getRandomMD5Code());
    }
}
