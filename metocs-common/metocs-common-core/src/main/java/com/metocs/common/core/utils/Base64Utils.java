package com.metocs.common.core.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Base64 编码工具
 */
public class Base64Utils {

    private static final Logger logger = LoggerFactory.getLogger(Base64Utils.class);

    private static final Base64.Decoder decoder = Base64.getDecoder();
    private static final Base64.Encoder encoder = Base64.getEncoder();

    public static String base64Encode(String text) {
        logger.debug("base64 编码: {}",text);
        byte[] textByte = text.getBytes(StandardCharsets.UTF_8);
        return encoder.encodeToString(textByte);
    }

    public static String base64Encode(byte[] text) {
        logger.debug("base64 编码: {}",new String(text));
        return encoder.encodeToString(text);
    }

    public static String base64Decode(String encodedText) {
        logger.debug("base64 解码: {}",encodedText);
        return new String(decoder.decode(encodedText), StandardCharsets.UTF_8);
    }
}