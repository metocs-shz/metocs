package com.metocs.common.cache.config;

import com.github.benmanes.caffeine.cache.Cache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;

/**
 * @author metocs
 * @date 2024/1/21 16:10
 */
public class CaffeineSubscribe implements MessageListener {

    private final static Logger logger = LoggerFactory.getLogger(CaffeineSubscribe.class);

    private final Cache<String, Object> cache;

    public CaffeineSubscribe(Cache<String, Object> cache) {
        this.cache = cache;
    }

    @Override
    public void onMessage(Message messageData, byte[] pattern) {
        String channel = new String(messageData.getChannel());
        String message = new String(messageData.getBody());
        logger.info("接收到清理Caffeine缓存消息 订阅通道： {} ，消息内容为： {}",channel,message);
        cache.invalidate(message);
    }
}
