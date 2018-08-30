package com.uhope.rl.application.constants;

import com.uhope.rl.application.utils.ConfigUtil;

/**
 * 常量类
 *
 * @author xiepuyao
 * @date Created on 2017/11/24
 */
public final class SysConstant {


    /**
     * 生成Token的密钥
     */
    public static final String TOKEN_SECRET = ConfigUtil.getConfig().get("sys.token.secret");

    /**
     * Token默认过期时间,单位秒，默认30分钟
     */
    public static final int TOKEN_EXPIRATION = ConfigUtil.getConfig().getInt("sys.token.expiration");

    /**
     * 验证码默认长度
     */
    public static final int CODE_LENGTH = ConfigUtil.getConfig().getInt("sys.code.length");

    /**
     * 验证码默认失效时间,单位：秒
     */
    public static final int CODE_EXPIRATION = ConfigUtil.getConfig().getInt("sys.code.expiration");

    /**
     * 同一个手机号码在某段时间内验证码发送的限制次数，比如limitTime=600，limitNumber=5，则表示600秒(10分钟)内最多发送5次
     * 单位:秒
     */
    public static final int CODE_LIMIT_TIME = ConfigUtil.getConfig().getInt("sys.code.limitTime");

    /**
     * 特定时间内，验证码发送的限制次数
     */
    public static final int CODE_LIMIT_NUMBER = ConfigUtil.getConfig().getInt("sys.code.limitNumber");

    /**
     * 发送次数达到上限后，等待时间
     */
    public static final int CODE_WAIT_TIME = ConfigUtil.getConfig().getInt("sys.code.waitTime");
    /**
     * 同一个用户在某段时间内尝试登陆次数限制，比如limitTime=1800，limitNumber=5，则表示1800秒(30分钟)内最多5次
     * 单位：秒
     */
    public static final int LOGIN_LIMIT_TIME = ConfigUtil.getConfig().getInt("sys.login.limitTime");

    /**
     * 特定时间内，验证码发送的限制次数
     */
    public static final int LOGIN_LIMIT_NUMBER = ConfigUtil.getConfig().getInt("sys.login.limitNumber");
    /**
     * 密码错误达到上限后，等待时间，单位：秒
     */
    public static final int LOGIN_WAIT_TIME = ConfigUtil.getConfig().getInt("sys.login.waitTime");


    /**
     * 默认字符编码
     */
    public static final String CHAR_SET = ConfigUtil.getConfig().get("sys.char.set");

    /**
     * 超级管理员登录名
     */
    public static final String SUPER_ADMIN = ConfigUtil.getConfig().get("sys.user.admin");


    //public static final String FDFS_NGINXSERVER = ConfigUtil.getConfig().get("fdfs.nginxserver");
}
