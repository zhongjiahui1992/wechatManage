package com.uhope.rl.application.cache;

import com.uhope.uip.redis.client.RedisClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static com.uhope.rl.application.cache.CacheConstant.*;


/**
 * <pre>
 *  该类用于集中调用缓存操作，避免每个用户独立调用时Key的组合错误和重复编写
 * 	方法中的INlltarget对象，等于null时，使用默认的数据库查询,否则使用调用者自定义的查询
 * </pre>
 *
 * @author xiepuyao
 * @date Created on 2017/11/25
 */
@Component("cacheAccess")
public class CacheAccessImpl implements CacheAccess {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(CacheAccessImpl.class);


    @Autowired
    private RedisClient redisClient;

    /**
     * 删除时，设置过期时间，3秒
     */
    private static int DELETE_TIME = 3;

    private String appId;


    @Override
    public void setImageCode(String imageCodeId, String code, int seconds)
            throws Exception {
        redisClient.set(Keys.getImageCodeKey(appId, imageCodeId), code, seconds);
    }

    @Override
    public String getImageCode(String imageCodeId) throws Exception {
        return redisClient.get(Keys.getImageCodeKey(appId, imageCodeId));
    }

    @Override
    public void flushDB() {
        redisClient.flushDB();
    }

    @Override
    public void flushAll() {
        redisClient.flushAll();
    }

    @Override
    public void setObject(String key, Object object) throws Exception{
        redisClient.set(key ,object);
    }

    @Override
    public Object getObject(String key) throws Exception{
        return redisClient.get(key);
    }

    @Override
    public Boolean existsKey(String key){
        return redisClient.existsKey(key);
    }
}
