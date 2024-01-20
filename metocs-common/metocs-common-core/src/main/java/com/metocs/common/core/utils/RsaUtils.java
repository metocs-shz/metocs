package com.metocs.common.core.utils;

import com.metocs.common.core.exception.CommonException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

public class RsaUtils {

    private static final Logger logger = LoggerFactory.getLogger(RsaUtils.class);

    private static final String PUBLIC_KEY = "";

    private static final String PRIVATE_KEY = "";


    public static String decryptByPrivateKey(String content){
        logger.debug("进入RSA工具解密方法，请求方法值为: {}",content);
        byte[] result = null;
        PKCS8EncodedKeySpec pkcs8EncodedKeySpec5 = new PKCS8EncodedKeySpec(Base64Utils.base64Decode(PRIVATE_KEY).getBytes());
        KeyFactory keyFactory = null;
        try {
            keyFactory = KeyFactory.getInstance("RSA");
            PrivateKey privateKey = keyFactory.generatePrivate(pkcs8EncodedKeySpec5);
            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.DECRYPT_MODE, privateKey);
            result = cipher.doFinal(Base64Utils.base64Encode(content).getBytes());
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | IllegalBlockSizeException |
                 InvalidKeySpecException | BadPaddingException | InvalidKeyException e) {

            logger.error("数据解密失败！: {}",e.getMessage(),e);

            throw new CommonException("数据解密失败！");
        }
        return new String(result);
    }

    @Deprecated
    public static String encryptByPublicKey(String text) {
        X509EncodedKeySpec x509EncodedKeySpec2 = new X509EncodedKeySpec(Base64Utils.base64Encode(PUBLIC_KEY).getBytes());
        byte[] result = null;
        try {
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            PublicKey publicKey = keyFactory.generatePublic(x509EncodedKeySpec2);
            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);
            result = cipher.doFinal(text.getBytes());
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | IllegalBlockSizeException |
                 InvalidKeySpecException | BadPaddingException | InvalidKeyException e) {

            logger.error("数据加密失败！: {}",e.getMessage(),e);

            throw new CommonException("数据加密失败！");
        }
        return Base64Utils.base64Encode(result);
    }

}
