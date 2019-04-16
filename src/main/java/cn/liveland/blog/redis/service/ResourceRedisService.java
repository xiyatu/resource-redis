package cn.liveland.blog.redis.service;

import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.serializer.JdkSerializationRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author xiyatu
 * @date 2019/4/1 10:58
 * Description  Redis资源并发处理器  暂不支持Redis集群模式
 */
@Service
public class ResourceRedisService extends ListRedisService<Integer> {

    /**
     * 定义Redis key
     */
    private static final String RESOURCE_ID_KEY = "list_resource_id";

    /**
     * 将id进队
     *
     * @param ids 入队列表
     */
    private void leftPush(List<Integer> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            throw new RuntimeException("id列表不能为空");
        }
        //将资源重新放入队列
        leftPushList(RESOURCE_ID_KEY, ids);
    }

    /**
     * 获取key下num个资源id
     * list不支持一次多个出队，只能一个一个的出队，
     * 为了保证原子性，使用lua脚本进行获取，保证操作的原子性
     *
     * @param num 资源数量
     * @return List<Integer>  id列表
     */
    private List<Integer> rightPop(int num) {
        DefaultRedisScript<List> defaultRedisScript = new DefaultRedisScript<>();
        //脚本逻辑：
        // 先判断队列中的资源个数，如果资源个数小于需要的个数则直接返回空数组，不占有资源
        // 队列中资源个数足够则进行出队，最后返回数组
        String lua = " local num = tonumber(ARGV[1]) " +
                " local key = KEYS[1]" +
                " local array = {}" +
                " local size = redis.call('LLEN',key)" +
                " if size < num" +
                " then " +
                " return array " +
                " end " +
                "for i=1,num " +
                "do " +
                "   local id = redis.call ('RPOP',key)" +
                "   if id " +
                "   then" +
                "     array[i]=id " +
                "   end " +
                "end " +
                "return array";
        defaultRedisScript.setScriptText(lua);
        defaultRedisScript.setResultType(List.class);
        //设置参数序列化方式
        RedisSerializer<String> paramSerializer = new StringRedisSerializer();
        //设置返回参数序列化方式
        RedisSerializer resultSerializer = new JdkSerializationRedisSerializer();
        List<String> keys = new ArrayList<>(1);
        keys.add(RESOURCE_ID_KEY);
        //获取资源列表
        List<Integer> ids = this.redisTemplate.execute(defaultRedisScript, paramSerializer, resultSerializer, keys, String.valueOf(num));
        if (CollectionUtils.isEmpty(ids)) {
            return null;
        } else {
            return ids;
        }
    }

    /**
     * 业务处理
     */
    public void handleBiz(int num) throws InterruptedException {
        //获取资源
        List<Integer> ids = rightPop(num);
        //如果获取不到足够的资源则自旋，每3s再去获取资源。
        int emptyCount = 1;
        while (CollectionUtils.isEmpty(ids)) {
            //超过5次则直接失败
            if (emptyCount >= 5) {
                throw new RuntimeException("获取资源失败");
            }
            emptyCount++;
            Thread.sleep(3000L);
            ids = rightPop(num);
        }
        try {
            System.out.println(ids);
            for (Integer id : ids) {
                //Todo
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            //将资源归队
            leftPush(ids);
        }
    }

    public void pushResource() {
        Integer[] ids = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
        List<Integer> list = Arrays.asList(ids);
        leftPush(list);
    }


}
