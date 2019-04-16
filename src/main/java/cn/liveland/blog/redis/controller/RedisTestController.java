package cn.liveland.blog.redis.controller;

import cn.liveland.blog.redis.service.ResourceRedisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author xiyatu
 * @date 2019/3/14 14:12
 * Description
 */
@RestController
@RequestMapping("/redis")
public class RedisTestController {

    @Autowired
    private ResourceRedisService resourceRedisService;


    @GetMapping("/handleBiz")
    public void handleBiz() throws InterruptedException {
        resourceRedisService.handleBiz(3);
    }

    @GetMapping("/pushResource")
    public void pushResource() {
        resourceRedisService.pushResource();
    }
}
