package com.uhope.rl.application.utils.token;

import java.io.Serializable;

/**
 * @author xiepuyao
 * @date Created on 2017/12/4
 */
public class TokenInfo implements Serializable{
    /**
     * 加密以后的密钥
     */
    private String token;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}

