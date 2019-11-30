package xmt.resys.util.encrypt.coder;

import javax.crypto.KeyGenerator;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

/**
 * HMAC加密组件
 */
public abstract class HmacCoder {
    public static final String MD2 = "HmacMD2";
    public static final String MD4 = "HmacMD4";
    public static final String MD5 = "HmacMD5";
    public static final String SHA1 = "HmacSHA1";
    public static final String SHA224 = "HmacSHA224";
    public static final String SHA256 = "HmacSHA256";
    public static final String SHA512 = "HmacSHA512";

    /**
     * 初始化Hmac密钥
     * @return
     * @throws Exception
     */
    public static byte[] initHmacKey(String type) throws Exception {
        // 初始化KeyGenerator
        KeyGenerator keyGenerator = KeyGenerator.getInstance(type);
        // 产生秘密密钥
        SecretKey secretKey = keyGenerator.generateKey();
        // 获得密钥
        return secretKey.getEncoded();
    }

    /**
     * Hmac加密
     * @param data 待加密数据
     * @param key 密钥
     * @throws Exception
     */
    public static byte[] encodeHmac(String type,
                                    byte[] data,
                                    byte[] key)
            throws Exception {
        // 还原密钥
        SecretKey secretKey = new SecretKeySpec(key, type);
        // 实例化Mac "SslMacMD5"
        Mac mac = Mac.getInstance(secretKey.getAlgorithm());
        // 初始化Mac
        mac.init(secretKey);
        // 执行消息摘要
        return mac.doFinal(data);
    }

    /**
     * HmacHex消息摘要
     * @param data 待做消息摘要处理的数据
     * @param key 密钥
     * @throws Exception
     */
    public static String encodeHmacHex(String type,
                                       byte[] data,
                                       byte[] key)
            throws Exception {
        // 执行消息摘要
        byte[] b = encodeHmac(type, data, key);
        // 做十六进制转换
        return new String(Hex.encode(b));
    }
}
