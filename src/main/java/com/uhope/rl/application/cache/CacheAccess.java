package com.uhope.rl.application.cache;


/**
 * <pre>
 *  该类用于集中调用缓存操作，避免每个用户独立调用时Key的组合错误和重复编写
 * 	方法中的INlltarget对象，等于null时，使用默认的数据库查询,否则使用调用者自定义的查询
 * </pre>
 *
 * @author xiepuyao
 * @date Created on 2017/11/25
 */
public interface CacheAccess {

    /**
     * 将图形验证码放入缓存中
     *
     * @param imageCodeId
     * @param code
     * @param seconds
     * @throws Exception
     */
    public void setImageCode(String imageCodeId, String code, int seconds)
            throws Exception;

    /**
     * 从缓存中取出验证码
     *
     * @param imageCodeId
     * @return
     * @throws Exception
     */
    public String getImageCode(String imageCodeId) throws Exception;

    /**
     * 清空当前数据库中的所有 key 该操作是不可逆的，慎用
     */
    public void flushDB();

    /**
     * 清空整个 Redis 服务器的数据(删除所有数据库的所有 key ) 该操作是不可逆的，慎用
     */
    public void flushAll();

    public void setObject(String key, Object object) throws Exception;

    public Object getObject(String key) throws Exception;

    public Boolean existsKey(String key);
}
