package com.uhope.rl.application.config;

import me.chanjar.weixin.mp.api.WxMpInMemoryConfigStorage;
import me.chanjar.weixin.mp.api.WxMpMessageRouter;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.api.impl.WxMpServiceImpl;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

/**
 * @author zhongjiahui.
 * @date Created on 2018/2/1.
 */
@Configuration
@PropertySource("classpath:wx.properties")
public class WxConfig {
    @Value("${wx.appId}")
    private String appId;

    @Value("${wx.appSecret}")
    private String appSecret;

    @Value("${wx.token}")
    private String token;

    @Value("${wx.aesKey}")
    private String aesKey;

    @Bean
    @ConditionalOnMissingBean
    public WxMpInMemoryConfigStorage wxMpInMemoryConfigStorage() {
        WxMpInMemoryConfigStorage configStorage = new WxMpInMemoryConfigStorage();
        /*configStorage.setAppId(properties.getAppId());
        configStorage.setSecret(properties.getAppSecret());
        configStorage.setToken(properties.getToken());
        configStorage.setAesKey(properties.getAesKey());*/
        configStorage.setAppId(appId);
        configStorage.setSecret(appSecret);
        configStorage.setToken(token);
        configStorage.setAesKey(aesKey);
        return configStorage;
    }

    @Bean
    @ConditionalOnMissingBean
    public WxMpService wxMpService(WxMpInMemoryConfigStorage wxMpInMemoryConfigStorage) {
        WxMpService wxMpService = new WxMpServiceImpl();
        wxMpService.setWxMpConfigStorage(wxMpInMemoryConfigStorage);
        return wxMpService;
    }

    @Bean
    @ConditionalOnMissingBean
    public WxMpMessageRouter wxMpMessageRouter(WxMpService wxMpService){
        return new WxMpMessageRouter(wxMpService);
    }
}
