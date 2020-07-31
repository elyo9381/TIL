package me.elyowon.springbootredisdemo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

public class RedisRunner implements ApplicationRunner {

    @Autowired
    RedisTemplate redisTemplate;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        ValueOperations<String, String> value = redisTemplate.opsForValue();
        value.set("elyo","won");
        value.set("hello","world");
        value.set("123","456");
    }
}