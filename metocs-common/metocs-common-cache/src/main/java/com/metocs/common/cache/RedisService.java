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

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
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

    public String getString(String key){
        Object data = redisTemplate.opsForValue().get(key);
        return data == null?"":data.toString();
    }


    public void setString(String key,String data){
        this.setStringAndTTL(key,data,500L);
    }

    public void setStringAndTTL(String key,String data,Long ttl){
        redisTemplate.opsForValue().set(key,data,ttl);
    }

    public void set(String key,Object data,Long ttl){
        redisTemplate.opsForValue().set(key,data, Duration.of(ttl, ChronoUnit.SECONDS));
    }

    public Object get(String key){
        return redisTemplate.opsForValue().get(key);
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

    public String lpopString(String key) {
        Object data = redisTemplate.opsForList().leftPop(key);
        return data == null?"":data.toString();
    }

    public Object rpop(String key) {
        return redisTemplate.opsForList().rightPop(key);
    }

    public String rpopString(String key) {
        Object data = redisTemplate.opsForList().rightPop(key);
        return data == null?"":data.toString();
    }

    public void sadd(String key,Object... data) {
        redisTemplate.opsForSet().add(key,data);
    }

    public Object spop(String key) {
        return redisTemplate.opsForSet().pop(key);
    }

    public String spopString(String key) {
        Object pop = redisTemplate.opsForSet().pop(key);
        return pop == null?"":pop.toString();
    }


    public void hset(String key,String filed,String data) {
        redisTemplate.opsForHash().put(key,filed,data);
    }

    public Object hget(String key,String filed) {
        return redisTemplate.opsForHash().get(key,filed);
    }

    public String hgetString(String key,String filed) {
        Object data = redisTemplate.opsForHash().get(key, filed);
        return data == null?"":data.toString();
    }

    public Map<Object, Object> hgetAll(String key) {
        return redisTemplate.opsForHash().entries(key);
    }


    public void subscribe(String channel,MessageListener messageListener){
        MessageListenerAdapter messageListenerAdapter = new MessageListenerAdapter(messageListener);
        redisMessageListenerContainer.addMessageListener(messageListenerAdapter, ChannelTopic.of(channel));
    }

    public void publish(String channel,String message){
        redisTemplate.convertAndSend(channel,message);
    }
}
