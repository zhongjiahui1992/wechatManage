package com.uhope.rl.application.utils.generator;

import java.util.UUID;

/**
 * 生成32位UUID
 *
 * @author xiepuyao
 * @date Created on 2017/11/9
 */
public class UUIDGenerator implements Generator {

    private static Generator generator = new UUIDGenerator();

    private UUIDGenerator() {
    }

    protected static Generator getInstance() {
        return generator;
    }

    /**
     * 将随机生成的UUID字符串"-"替换为""后输出
     *
     * @return String
     */
    @Override
    public String generateID() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}
