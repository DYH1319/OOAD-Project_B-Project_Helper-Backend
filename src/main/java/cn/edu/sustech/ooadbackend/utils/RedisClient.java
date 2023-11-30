package cn.edu.sustech.ooadbackend.utils;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * @author DYH
 * @version 1.0
 * @className RedisClient
 * @since 2023/11/30 19:35
 */
@Component
public class RedisClient {
    
    @Autowired
    private StringRedisTemplate redisTemplate;
    
    /**
     * 设置缓存（没有时间限制）
     */
    public void set(String key, String value) {
        redisTemplate.opsForValue().set(key, value);
    }
    
    /**
     * 设置缓存（有时间限制，单位为秒）
     */
    public void set(String key, String value, long timeout) {
        redisTemplate.opsForValue().set(key, value, timeout, TimeUnit.SECONDS);
    }
    
    /**
     * 删除缓存，并返回是否删除成功
     */
    public boolean delete(String key) {
        redisTemplate.delete(key);
        // 如果还存在这个 key 就证明删除失败
        // 不存在就证明删除成功
        return !Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }
    
    /**
     * 取出缓存
     */
    public String get(String key) {
        if (Boolean.TRUE.equals(redisTemplate.hasKey(key))) {
            return redisTemplate.opsForValue().get(key);
        } else {
            return null;
        }
    }
    
    /**
     * 获取失效时间（-2：失效或不存在, -1：没有时间限制）
     */
    public long getExpire(String key) {
        // 判断是否存在
        if (Boolean.TRUE.equals(redisTemplate.hasKey(key))) {
            Long expire = redisTemplate.getExpire(key);
            assert expire != null;
            return expire;
        } else {
            return Long.parseLong(-2 + "");
        }
    }
}
