package com.uhope.rl.application.web.wx;

import com.uhope.rl.wechat.domain.WxAutoReply;
import com.uhope.rl.wechat.dto.WxAutoReplyDTO;
import com.uhope.rl.wechat.service.WxAutoReplyService;
import com.uhope.rl.wechat.service.WxBindInfoService;
import me.chanjar.weixin.common.exception.WxErrorException;
import me.chanjar.weixin.common.session.WxSessionManager;
import me.chanjar.weixin.mp.api.WxMpMessageHandler;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.bean.message.WxMpXmlMessage;
import me.chanjar.weixin.mp.bean.message.WxMpXmlOutMessage;
import me.chanjar.weixin.mp.bean.message.WxMpXmlOutTextMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * Created by phl on 15-8-10.
 */
@Component("defaultTextHandler ")
public class DefaultTextHandler implements WxMpMessageHandler {
    @Autowired
    private WxAutoReplyService wxAutoReplyBO;

    @Override
    public WxMpXmlOutMessage handle(WxMpXmlMessage wxMpXmlMessage, Map<String, Object> map, WxMpService wxMpService, WxSessionManager wxSessionManager) throws WxErrorException {
        String returnContent = "";
        boolean isKeyword = false;
        if(!"text".equals(wxMpXmlMessage.getMsgType())){
            return null;
            //return WxMpXmlOutMessage.TEXT().content("此公众号暂未提供该服务！").fromUser(wxMpXmlMessage.getToUser()).toUser(wxMpXmlMessage.getFromUser()).build();
        }
        String message = wxMpXmlMessage.getContent();
        /*String wxId = wxMpXmlMessage.getToUser();
        WxBindInfoVO wxBindInfoVO = new WxBindInfoVO();
        wxBindInfoVO.setWxoriginalid(wxId);
        WxBindInfo wxBindInfo = wxBindInfoBO.get(wxBindInfoVO);*/

        WxAutoReplyDTO wxAutoReplyVO = new WxAutoReplyDTO();
        wxAutoReplyVO.setType(0);
        wxAutoReplyVO.setAppid(wxMpService.getWxMpConfigStorage().getAppId());
        List<WxAutoReply> autoReplyList = wxAutoReplyBO.find(wxAutoReplyVO);
        for(WxAutoReply autoReply : autoReplyList){
            String keyword = autoReply.getKeyword();
            Integer mode = autoReply.getMode();
            if(mode == 0){
                if(message.indexOf(keyword) != -1){
                    isKeyword = true;
                    returnContent = autoReply.getContent();
                    break;
                }
            }else{
                if(message.equals(keyword)){
                    isKeyword = true;
                    returnContent = autoReply.getContent();
                    break;
                }
            }

        }

        if(!isKeyword){
            wxAutoReplyVO.setType(1);
            List<WxAutoReply> wxAutoReply = wxAutoReplyBO.find(wxAutoReplyVO);
            if(wxAutoReply.size() > 0){
                returnContent = wxAutoReply.get(0).getContent();
            }
        }

        if("".equals(returnContent)){
            return null;
        }

        WxMpXmlOutTextMessage m = WxMpXmlOutMessage.TEXT().content(returnContent).fromUser(wxMpXmlMessage.getToUser()).toUser(wxMpXmlMessage.getFromUser()).build();
        //WxMpXmlOutTextMessage m = WxMpXmlOutMessage.TEXT().content("助力治水").fromUser(wxMpXmlMessage.getToUser()).toUser(wxMpXmlMessage.getFromUser()).build();
        return m;
    }
}
