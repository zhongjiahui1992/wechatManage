package com.uhope.rl.application.utils.generator;

/**
 * 生成id,所有的id生成必须调用该类的下的方法
 *
 * @author xiepuyao
 * @date Created on 2017/12/8
 */
public class GeneratorIDFactory {
    private GeneratorIDFactory() {
    }

    public static String generatorId(Generator generator) {
        return generator.generateID();
    }

    /**
     * 生成UserId
     *
     * @return
     */
    public static String generatorUserId() {
        return generatorId(UserIdGenerator.getInstance());
    }

    /**
     * 生成32位的UUID
     *
     * @return
     */
    public static String generatorUUID() {
        return generatorId(UUIDGenerator.getInstance());
    }

}
