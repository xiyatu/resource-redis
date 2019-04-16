package cn.liveland.blog.redis.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author xiyatu
 * @date 2019/4/1 10:52
 * Description
 */
public class ListRedisService<T> {

    @Resource
    private ListOperations<String, T> listOperations;

    @Autowired
    protected RedisTemplate<String, Object> redisTemplate;


    public T rightPop(String key) {
        return listOperations.rightPop(key);
    }

    public void leftPush(String key, T value) {
        listOperations.leftPush(key, value);
    }

    public void leftPushList(String key, List<T> values) {
        listOperations.leftPushAll(key, values);
    }


}
