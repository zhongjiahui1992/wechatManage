package com.uhope.rl.application.utils.token;

import com.uhope.rl.application.constants.SysConstant;
import io.jsonwebtoken.*;

import java.util.Date;
import java.util.Map;

/**
 * @author xiepuyao
 * @date Created on 2017/12/5
 */
public class JWT {

    private String secret;

    protected JWT() {
        this(SysConstant.TOKEN_SECRET);
    }

    protected JWT(String secret) {
        this.secret = secret;
    }

    /**
     * 生成token
     *
     * @param payload    载体
     * @param expiration 过期时间,单位：毫秒
     * @return
     */
    public String createToken(Map payload, Long expiration) {
        String token = Jwts.builder()
                .setClaims(payload)
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(SignatureAlgorithm.HS256, secret)
                .compact();
        return token;
    }

    /**
     * 生成token，永不过期
     *
     * @param payload 载体
     * @return
     */
    public String createToken(Map payload) {
        String token = Jwts.builder()
                .setClaims(payload)
                .signWith(SignatureAlgorithm.HS256, secret)
                .compact();
        return token;
    }

    /**
     * 解密，返回payload
     *
     * @param token
     * @return
     * @throws SignatureException 解密失败，会抛出该异常
     */
    public Claims getPayLoad(String token) throws SignatureException {
        Claims claims = Jwts.parser()
                .setSigningKey(secret)
                .parseClaimsJws(token)
                .getBody();
        return claims;
    }

    /**
     * 解密，返回header
     *
     * @param token
     * @return
     * @throws SignatureException 解密失败，会抛出该异常
     */
    public JwsHeader getHeader(String token) throws SignatureException {
        JwsHeader header = Jwts.parser()
                .setSigningKey(secret)
                .parseClaimsJws(token)
                .getHeader();
        return header;
    }

}