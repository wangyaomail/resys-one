package xmt.resys.util.encrypt;

import xmt.resys.util.encrypt.coder.BASE64Encoder;
import xmt.resys.util.encrypt.coder.DESCoder;
import xmt.resys.util.encrypt.coder.HmacCoder;
import xmt.resys.util.encrypt.coder.MDCoder;
import xmt.resys.util.encrypt.coder.RSACoder;
import xmt.resys.util.encrypt.coder.SHACoder;

/**
 * 数据加密辅助类(默认编码UTF-8)， 该方法是静态的，在Service中还要提供一个Security的服务，服务中配置好加解密的各类参数
 */
public final class HBSecurityUtil {
    private HBSecurityUtil() {}

    /**
     * 默认算法密钥
     */
    private static final byte[] ENCRYPT_KEY = { -55, 5, 78, 62, -18, 49, -52, 8 };
    public static final String CHARSET = "UTF-8";

    /**
     * BASE64解码
     * @param str
     * @return
     */
    public static final byte[] decryptBASE64(String str) {
        try {
            return BASE64Encoder.decode(str);
        } catch (Exception e) {
            throw new RuntimeException("解密错误，错误信息：", e);
        }
    }

    /**
     * BASE64编码
     * @param str
     * @return
     */
    public static final String encryptBASE64(byte[] str) {
        try {
            return BASE64Encoder.encode(str);
        } catch (Exception e) {
            throw new RuntimeException("加密错误，错误信息：", e);
        }
    }

    /**
     * 数据解密，算法（DES）
     * @param cryptData 加密数据
     * @return 解密后的数据
     */
    public static final String decryptDes(String cryptData) {
        return decryptDes(cryptData, ENCRYPT_KEY);
    }

    /**
     * 数据加密，算法（DES）
     * @param data 要进行加密的数据
     * @return 加密后的数据
     */
    public static final String encryptDes(String data) {
        return encryptDes(data, ENCRYPT_KEY);
    }

    /**
     * 基于MD5算法的单向加密
     * @param strSrc 明文
     * @return 返回密文
     */
    public static final String encryptMd5(String strSrc) {
        String outString = null;
        try {
            outString = encryptBASE64(MDCoder.encodeMD5(strSrc.getBytes(CHARSET)));
        } catch (Exception e) {
            throw new RuntimeException("加密错误，错误信息：", e);
        }
        return outString;
    }

    /**
     * SHA加密
     * @param data
     * @return
     */
    public static final String encryptSHA(String data) {
        try {
            return encryptBASE64(SHACoder.encodeSHA256(data.getBytes(CHARSET)));
        } catch (Exception e) {
            throw new RuntimeException("加密错误，错误信息：", e);
        }
    }

    /**
     * 数据解密，算法（DES）
     * @param cryptData 加密数据
     * @param key
     * @return 解密后的数据
     */
    public static final String decryptDes(String cryptData,
                                          byte[] key) {
        String decryptedData = null;
        try {
            // 把字符串解码为字节数组，并解密
            decryptedData = new String(DESCoder.decrypt(decryptBASE64(cryptData), key));
        } catch (Exception e) {
            throw new RuntimeException("解密错误，错误信息：", e);
        }
        return decryptedData;
    }

    /**
     * 数据加密，算法（DES）
     * @param data 要进行加密的数据
     * @param key
     * @return 加密后的数据
     */
    public static final String encryptDes(String data,
                                          byte[] key) {
        String encryptedData = null;
        try {
            // 加密，并把字节数组编码成字符串
            encryptedData = encryptBASE64(DESCoder.encrypt(data.getBytes(), key));
        } catch (Exception e) {
            throw new RuntimeException("加密错误，错误信息：", e);
        }
        return encryptedData;
    }

    /**
     * RSA签名
     * @param data 原数据
     * @param privateKey
     * @return
     */
    public static final String signRSA(String data,
                                       String privateKey) {
        try {
            return encryptBASE64(RSACoder.sign(data.getBytes(CHARSET), decryptBASE64(privateKey)));
        } catch (Exception e) {
            throw new RuntimeException("签名错误，错误信息：", e);
        }
    }

    /**
     * RSA验签
     * @param data 原数据
     * @param publicKey
     * @param sign
     * @return
     */
    public static final boolean verifyRSA(String data,
                                          String publicKey,
                                          String sign) {
        try {
            return RSACoder.verify(data.getBytes(CHARSET),
                                   decryptBASE64(publicKey),
                                   decryptBASE64(sign));
        } catch (Exception e) {
            throw new RuntimeException("验签错误，错误信息：", e);
        }
    }

    /**
     * 数据加密，算法（RSA）
     * @param data 数据
     * @param privateKey
     * @return 加密后的数据
     */
    public static final String encryptRSAPrivate(String data,
                                                 String privateKey) {
        try {
            return encryptBASE64(RSACoder.encryptByPrivateKey(data.getBytes(CHARSET),
                                                              decryptBASE64(privateKey)));
        } catch (Exception e) {
            throw new RuntimeException("解密错误，错误信息：", e);
        }
    }

    /**
     * 数据解密，算法（RSA）
     * @param cryptData 加密数据
     * @param publicKey
     * @return 解密后的数据
     */
    public static final String decryptRSAPublic(String cryptData,
                                                String publicKey) {
        try {
            // 把字符串解码为字节数组，并解密
            return new String(RSACoder.decryptByPublicKey(decryptBASE64(cryptData),
                                                          decryptBASE64(publicKey)));
        } catch (Exception e) {
            throw new RuntimeException("解密错误，错误信息：", e);
        }
    }

    /**
     * HMAC加密
     * @param type HmacMD2/HmacMD4/HmacMD5/HmacSHA1/HmacSHA224/HmacSHA256/HmacSHA512
     * @return
     */
    public static final String initHmacKey(String type) {
        try {
            return encryptBASE64(HmacCoder.initHmacKey(type));
        } catch (Exception e) {
            throw new RuntimeException("获取HMAC密钥失败：", e);
        }
    }

    /**
     * HMAC加密
     * @param type HmacMD2/HmacMD4/HmacMD5/HmacSHA1/HmacSHA224/HmacSHA256/HmacSHA512
     * @param data
     * @param key initHmacKey
     * @return
     */
    public static final String encryptHMAC(String type,
                                           String data,
                                           String key) {
        try {
            return HmacCoder.encodeHmacHex(type, data.getBytes(CHARSET), decryptBASE64(key));
        } catch (Exception e) {
            throw new RuntimeException("加密错误，错误信息：", e);
        }
    }

    public static String encryptPassword(String password) {
        return encryptMd5(encryptSHA(password));
    }

}
