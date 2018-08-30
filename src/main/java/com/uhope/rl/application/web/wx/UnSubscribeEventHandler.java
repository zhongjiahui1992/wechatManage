package com.uhope.rl.application.web.wx;

import com.uhope.rl.system.domain.AppUser;
import com.uhope.rl.system.dto.AppUserDTO;
import com.uhope.rl.system.service.AppUserService;
import com.uhope.rl.wechat.dto.WxSubscribeUserDTO;
import com.uhope.rl.wechat.service.WxBindInfoService;
import com.uhope.rl.wechat.service.WxSubscribeUserService;
import me.chanjar.weixin.common.exception.WxErrorException;
import me.chanjar.weixin.common.session.WxSessionManager;
import me.chanjar.weixin.mp.api.WxMpInMemoryConfigStorage;
import me.chanjar.weixin.mp.api.WxMpMessageHandler;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.bean.message.WxMpXmlMessage;
import me.chanjar.weixin.mp.bean.message.WxMpXmlOutMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import tk.mybatis.mapper.entity.Condition;

import java.util.Map;

/**
 * Created by phl on 15-8-10.
 */
@Component("unSubscribeEventHandler")
public class UnSubscribeEventHandler implements WxMpMessageHandler {
    @Autowired
    private AppUserService appUserBO;
    @Autowired
    private WxSubscribeUserService wxSubscribeUserBO;
    @Autowired
    private WxBindInfoService wxBindInfoBO;

    @Override
    public WxMpXmlOutMessage handle(WxMpXmlMessage wxMpXmlMessage, Map<String, Object> map, WxMpService wxMpService, WxSessionManager wxSessionManager) throws WxErrorException {
        String wxId = wxMpXmlMessage.getToUser();
        /*WxBindInfoVO wxBindInfoVO = new WxBindInfoVO();
        wxBindInfoVO.setWxoriginalid(wxId);
        WxBindInfo wxBindInfo = wxBindInfoBO.get(wxBindInfoVO);

        if(wxBindInfo != null){
            WxMpInMemoryConfigStorage wxMpInMemoryConfigStorage = new WxMpInMemoryConfigStorage();
            wxMpInMemoryConfigStorage.setAppId(wxBindInfo.getWxappid());
            wxMpInMemoryConfigStorage.setSecret(wxBindInfo.getWxappsecret());
            wxMpInMemoryConfigStorage.setToken(wxBindInfo.getWxtoken());
            wxMpInMemoryConfigStorage.setAesKey(wxBindInfo.getWxaeskey());
            wxMpService.setWxMpConfigStorage(wxMpInMemoryConfigStorage);
        }*/

        String appid = wxMpService.getWxMpConfigStorage().getAppId();
        //更新用户状态到0，失效状态
        Condition appUserVO = new Condition(AppUser.class);
        appUserVO.createCriteria().andCondition("openid=", wxMpXmlMessage.getFromUser());
        //appUserVO.setOpenid(wxMpXmlMessage.getFromUser());
        AppUser appUser = new AppUser();
        appUser.setStatus((byte)0);
        appUserBO.update(appUser,appUserVO);

        WxSubscribeUserDTO wxSubscribeUserVO = new WxSubscribeUserDTO();
        wxSubscribeUserVO.setAppid(appid);
        wxSubscribeUserVO.setOpenid(wxMpXmlMessage.getFromUser());
        wxSubscribeUserBO.remove(wxSubscribeUserVO);

        System.out.println("----------delete user------------");
        return null;
    }
}
