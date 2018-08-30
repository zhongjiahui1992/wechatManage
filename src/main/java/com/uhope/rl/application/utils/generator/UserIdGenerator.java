package com.uhope.rl.application.utils.generator;

import java.util.UUID;

/**
 * 用户id生成器
 *
 * @author xiepuyao
 * @date Created on 2017/12/8
 */
public class UserIdGenerator implements Generator {

    private static Generator generator = new UserIdGenerator();

    private UserIdGenerator() {
    }

    /**
     * 获取实例
     * @return
     * @see GeneratorIDFactory
     */
    protected static Generator getInstance() {
        return generator;
    }

    @Override
    public String generateID() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}
