package com.uhope.rl.application.web.wx;

import com.uhope.rl.application.utils.WeiXinUtil;
import com.uhope.rl.system.domain.AppUser;
import com.uhope.rl.system.dto.AppUserDTO;
import com.uhope.rl.system.service.AppUserService;
import com.uhope.rl.wechat.domain.WxAutoReply;
import com.uhope.rl.wechat.domain.WxNewsArticle;
import com.uhope.rl.wechat.domain.WxSubscribeUser;
import com.uhope.rl.wechat.dto.WxAutoReplyDTO;
import com.uhope.rl.wechat.dto.WxNewsArticleDTO;
import com.uhope.rl.wechat.service.WxAutoReplyService;
import com.uhope.rl.wechat.service.WxBindInfoService;
import com.uhope.rl.wechat.service.WxNewsArticleService;
import com.uhope.rl.wechat.service.WxSubscribeUserService;
import me.chanjar.weixin.common.exception.WxErrorException;
import me.chanjar.weixin.common.session.WxSessionManager;
import me.chanjar.weixin.mp.api.WxMpMessageHandler;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.bean.message.WxMpXmlMessage;
import me.chanjar.weixin.mp.bean.message.WxMpXmlOutMessage;
import me.chanjar.weixin.mp.bean.message.WxMpXmlOutNewsMessage;
import me.chanjar.weixin.mp.bean.message.WxMpXmlOutTextMessage;
import me.chanjar.weixin.mp.bean.result.WxMpUser;
import me.chanjar.weixin.mp.builder.outxml.NewsBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import tk.mybatis.mapper.entity.Condition;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by phl on 15-8-10.
 */
@Component("subscribeEventHandler")
public class SubscribeEventHandler implements WxMpMessageHandler {
    @Autowired
    private AppUserService appUserBO;
    @Autowired
    private WxSubscribeUserService wxSubscribeUserBO;
    @Autowired
    private WxAutoReplyService wxAutoReplyBO;
    @Autowired
    private WxBindInfoService wxBindInfoBO;
    @Autowired
    private WxNewsArticleService wxNewsArticleBO;

    @Override
    public WxMpXmlOutMessage handle(WxMpXmlMessage wxMpXmlMessage, Map<String, Object> map, WxMpService wxMpService, WxSessionManager wxSessionManager) throws WxErrorException {
        String returnContent = "欢迎关注本公众号！";

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
        //操作用户
        WxMpUser wxMpUser = wxMpService.getUserService().userInfo(wxMpXmlMessage.getFromUser(), "zh_CN");

        insertToAppUserDB(wxMpUser);

        insertToSubscriberDB(appid, wxMpUser);

        WxAutoReplyDTO wxAutoReplyVO = new WxAutoReplyDTO();
        wxAutoReplyVO.setAppid(appid);
        wxAutoReplyVO.setType(2);
        List<WxAutoReply> wxAutoReplyList = wxAutoReplyBO.find(wxAutoReplyVO);
        if(!CollectionUtils.isEmpty(wxAutoReplyList)){
            String key = wxAutoReplyList.get(0).getReplytype();
            if(key != null && "news".equals(key)){
                WxNewsArticleDTO wxNewsArticleVO = new WxNewsArticleDTO();
                wxNewsArticleVO.setAppid(appid);
                wxNewsArticleVO.setMediaid(wxAutoReplyList.get(0).getId());
                List<WxNewsArticle> articles = wxNewsArticleBO.find(wxNewsArticleVO);
                WxMpXmlOutNewsMessage.Item item = null;
                NewsBuilder newsBuilder = new NewsBuilder();
                for(WxNewsArticle art : articles){
                    item = new WxMpXmlOutNewsMessage.Item();
                    item.setTitle(art.getTitle());
                    item.setUrl(art.getUrl());
                    item.setDescription(art.getDigest());
                    item.setPicUrl(art.getThumburl());
                    newsBuilder.addArticle(item);
                }
                return newsBuilder
                        .fromUser(wxMpXmlMessage.getToUser())
                        .toUser(wxMpXmlMessage.getFromUser())
                        .build();
            }else{
                returnContent = wxAutoReplyList.get(0).getContent();
            }
        }

        //返回消息
        WxMpXmlOutTextMessage m = WxMpXmlOutMessage.TEXT().content(returnContent).fromUser(wxMpXmlMessage.getToUser()).toUser(wxMpXmlMessage.getFromUser()).build();
        return m;
    }

    private void insertToAppUserDB(WxMpUser wxMpUser){
        AppUserDTO appUserVO = new AppUserDTO();
        AppUser appUser = new AppUser();
        appUser.setNickname(wxMpUser.getNickname());
        appUser.setHeadportrait(wxMpUser.getHeadImgUrl());
        appUser.setOpenid(wxMpUser.getOpenId());
        appUser.setAccount(wxMpUser.getOpenId());
        appUser.setPassword(wxMpUser.getOpenId());
        appUser.setGender((byte)wxMpUser.getSexId().intValue());
        appUser.setUnionid(wxMpUser.getUnionId());
        appUser.setStatus((byte)1);
        appUser.setUsertype((byte)2);
        appUser.setRegisterdate(new Date());

        Condition condition = new Condition(AppUser.class);
        condition.createCriteria().andCondition("openid=", wxMpUser.getOpenId());
        appUserVO.setOpenid(wxMpUser.getOpenId());
        if (appUserBO.count(appUserVO) == 0) {
            //新增
            appUserBO.insert(appUser);
        } else {
            //更新
            appUserBO.update(appUser, condition);
        }
    }

    private void insertToSubscriberDB(String appid, WxMpUser wxMpUser){
        WxSubscribeUser wxSubscribeUser = new WxSubscribeUser();
        wxSubscribeUser.setAppid(appid);
        wxSubscribeUser.setOpenid(wxMpUser.getOpenId());

        String nickName = wxMpUser.getNickname();
        wxSubscribeUser.setNickname(WeiXinUtil.convertTrueNameToSqlName(nickName));

        wxSubscribeUser.setSubscribe(wxMpUser.getSubscribe()==true?1:0);
        wxSubscribeUser.setSex(wxMpUser.getSexId());
        wxSubscribeUser.setLanguage(wxMpUser.getLanguage());
        wxSubscribeUser.setCity(wxMpUser.getCity());
        wxSubscribeUser.setProvince(wxMpUser.getProvince());
        wxSubscribeUser.setCountry(wxMpUser.getCountry());
        wxSubscribeUser.setHeadimgurl(wxMpUser.getHeadImgUrl());
        wxSubscribeUser.setSubscribeTime(new Date());
        wxSubscribeUser.setRemark(wxMpUser.getRemark());
        wxSubscribeUser.setGroupid(wxMpUser.getGroupId());
        wxSubscribeUser.setIsblack(0);//不是拉黑用户

        Long[] tagids = wxMpUser.getTagIds();
        String[] tagidssql = new String[tagids.length];
        for(int i=0;i<tagids.length;i++){
            tagidssql[i] = String.valueOf(tagids[i]);
        }
        String tagidstring = WeiXinUtil.convertArrayToString(tagidssql);
        wxSubscribeUser.setTagids(tagidstring.substring(1,tagidstring.length()-1));

        wxSubscribeUserBO.insert(wxSubscribeUser);
    }
}
