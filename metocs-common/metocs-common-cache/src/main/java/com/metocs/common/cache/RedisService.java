package com.metocs.common.cache;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * @author metocs
 * @date 2024/1/21 15:40
 */

@Component
public class RedisService {

    private final static Logger logger = LoggerFactory.getLogger(RedisService.class);


    @Autowired
    private RedisTemplate<String,Object> redisTemplate;

    @Autowired
    private RedisMessageListenerContainer redisMessageListenerContainer;


    public Boolean exists(String key){
        return redisTemplate.hasKey(key);
    }

    public void set(String key,Object data){
        redisTemplate.opsForValue().set(key,data, Duration.ofSeconds(300L));
    }


    public void set(String key,Object data,Long ttl){
        redisTemplate.opsForValue().set(key,data, Duration.of(ttl, ChronoUnit.SECONDS));
    }

    public Object get(String key){
        return redisTemplate.opsForValue().get(key);
    }

    public List<Object> mGet(String... key) {
        return redisTemplate.opsForValue().multiGet(List.of(key));
    }

    public Boolean setNx(String key,String data){
       return redisTemplate.opsForValue().setIfAbsent(key, data, Duration.ofSeconds(300L));
    }

    public Boolean setNxTTL(String key, String data,Long ttl){
        return redisTemplate.opsForValue().setIfAbsent(key, data, Duration.ofSeconds(ttl));
    }

    public void setExpire(String key, long time) {
        redisTemplate.expire(key,Duration.ofSeconds(time));
    }

    public Long incrBy(String key,Long data){
        return redisTemplate.opsForValue().increment(key,data);
    }

    public Long decrBy(String key,Long data){
        return redisTemplate.opsForValue().decrement(key,data);
    }

    public void del(String... key) {
        redisTemplate.delete(Arrays.asList(key));
    }


    public void lpush(String key, Object data) {
        redisTemplate.opsForList().leftPush(key,data);
    }

    public Object lpop(String key) {
        return redisTemplate.opsForList().leftPop(key);
    }


    public Object rpop(String key) {
        return redisTemplate.opsForList().rightPop(key);
    }


    public void sadd(String key,Object... data) {
        redisTemplate.opsForSet().add(key,data);
    }

    public Object spop(String key) {
        return redisTemplate.opsForSet().pop(key);
    }


    public void hset(String key,String filed,String data) {
        redisTemplate.opsForHash().put(key,filed,data);
    }

    public Object hget(String key,String filed) {
        return redisTemplate.opsForHash().get(key,filed);
    }

    public Map<Object, Object> hgetAll(String key) {
        return redisTemplate.opsForHash().entries(key);
    }


    public void subscribe(String channel,MessageListener messageListener){
        Assert.notNull(messageListener,"listener is null ！");
        Assert.hasText(channel,"channel is cant be empty ！");
        if (logger.isDebugEnabled()){
            logger.debug("开始进行消息订阅 频道：{} 订阅者：{}",channel,messageListener.getClass().getName());
        }
        MessageListenerAdapter messageListenerAdapter = new MessageListenerAdapter(messageListener);
        redisMessageListenerContainer.addMessageListener(messageListenerAdapter, ChannelTopic.of(channel));
    }

    public void publish(String channel,String message){
        Assert.hasText(channel,"channel is cant be empty ！");
        Assert.hasText(message,"message is cant be empty ！");
        if (logger.isDebugEnabled()){
            logger.debug("消息发布 频道: {} 消息内容： {}",channel,message);
        }
        redisTemplate.convertAndSend(channel,message);
    }
}
