package com.uhope.rl.application.web.wx;

import com.uhope.rl.wechat.domain.WxMassMessage;
import com.uhope.rl.wechat.service.WxBindInfoService;
import com.uhope.rl.wechat.service.WxMassMessageService;
import me.chanjar.weixin.common.exception.WxErrorException;
import me.chanjar.weixin.common.session.WxSessionManager;
import me.chanjar.weixin.mp.api.WxMpMessageHandler;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.bean.message.WxMpXmlMessage;
import me.chanjar.weixin.mp.bean.message.WxMpXmlOutMessage;
import me.chanjar.weixin.mp.bean.message.WxMpXmlOutTextMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.Map;

/**
 * Created by zhongjiahui on 2017/11/21.
 */
@Component("massSendJobHandler")
public class MassSendJobHandler implements WxMpMessageHandler {
    @Autowired
    private WxMassMessageService wxMassMessageService;
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

        WxMassMessage wxMassMessage = new WxMassMessage();
        wxMassMessage.setId(wxMpXmlMessage.getMsgId().toString());
        wxMassMessage.setAppid(appid);
        wxMassMessage.setTousername(wxMpXmlMessage.getToUser());
        wxMassMessage.setFromusername(wxMpXmlMessage.getFromUser());
        wxMassMessage.setStatus(wxMpXmlMessage.getStatus());
        wxMassMessage.setTotalcount(wxMpXmlMessage.getTotalCount());
        wxMassMessage.setFiltercount(wxMpXmlMessage.getFilterCount());
        wxMassMessage.setSentcount(wxMpXmlMessage.getSentCount());
        wxMassMessage.setErrorcount(wxMpXmlMessage.getErrorCount());
        wxMassMessage.setCreatetime(new Date(wxMpXmlMessage.getCreateTime()));

        wxMassMessageService.insert(wxMassMessage);

        return new WxMpXmlOutTextMessage();
    }
}
