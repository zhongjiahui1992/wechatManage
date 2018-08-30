package com.uhope.rl.application.utils.token;

import com.uhope.rl.application.constants.SysConstant;
import org.apache.commons.lang3.StringUtils;

/**
 * @author xiepuyao
 * @date Created on 2017/12/6
 */
public class JWTFactory {
    /**
     * 默认的JWT加密解密
     */
    private static final JWT DEFAULT_JWT;

    static {
        DEFAULT_JWT = new JWT();
    }

    private JWTFactory() {
    }

    public static JWT getInstance() {
        return DEFAULT_JWT;
    }

    public static JWT getInstance(String secret) {
        if (StringUtils.isBlank(secret) || secret.equals(SysConstant.TOKEN_SECRET)) {
            return getInstance();
        }
        return new JWT(secret);
    }

}
