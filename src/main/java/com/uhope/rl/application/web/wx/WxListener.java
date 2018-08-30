package com.uhope.rl.application.web.wx;

import me.chanjar.weixin.common.api.WxConsts;
import me.chanjar.weixin.mp.api.WxMpMessageRouter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

/**
 * @author zhongjiahui.
 * @date Created on 2018/2/5.
 */
@Component
public class WxListener implements ApplicationListener<ContextRefreshedEvent> {
    @Autowired
    private WxMpMessageRouter wxMessageRouter;
    //文本处理
    @Autowired
    private DefaultTextHandler defaultTextHandler;
    //事件处理
    @Autowired
    private SubscribeEventHandler subscribeEventHandler;
    @Autowired
    private UnSubscribeEventHandler unSubscribeEventHandler;
    @Autowired
    private MassSendJobHandler massSendJobHandler;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        System.out.println("初始化");
        wxMessageRouter
                .rule().async(false).event(WxConsts.EVT_SCAN).handler(null).end()
                .rule().async(false).event(WxConsts.EVT_MASS_SEND_JOB_FINISH).handler(massSendJobHandler).end()
                .rule().async(false).event(WxConsts.EVT_SUBSCRIBE).handler(subscribeEventHandler).end()
                .rule().async(false).event(WxConsts.EVT_UNSUBSCRIBE).handler(unSubscribeEventHandler).end()
                .rule().async(false).handler(defaultTextHandler).end();
    }
}
