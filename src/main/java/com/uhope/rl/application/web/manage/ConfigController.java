package com.uhope.rl.application.web.manage;

import com.alibaba.fastjson.JSONObject;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.uhope.common.web.util.ResponseUtils;
import com.uhope.rl.application.cache.CacheAccess;
import com.uhope.rl.application.constants.Constant;
import com.uhope.rl.application.result.ResponseMsgUtil;
import com.uhope.rl.application.result.Result;
import com.uhope.rl.application.utils.WeiXinUtil;
import com.uhope.rl.wechat.domain.WxBindInfo;
import com.uhope.rl.wechat.dto.WxBindInfoDTO;
import com.uhope.rl.wechat.service.WxBindInfoService;
import me.chanjar.weixin.mp.api.WxMpInMemoryConfigStorage;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.api.impl.WxMpServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Controller;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.Date;
import java.util.List;

/**
 * Created by zhongjiahui on 2018/1/26.
 */
@RestController
public class ConfigController{
    @Autowired
    private WxBindInfoService wxBindInfoBO;
    @Autowired
    private WxMpService wxMpService;

    /**
     * 验证是否已经绑定公众号
     * @return
     */
    @GetMapping("/config")
    public Result<WxBindInfo> configWechat(){
        List<WxBindInfo> wxBindInfoList = wxBindInfoBO.find();
        if(!CollectionUtils.isEmpty(wxBindInfoList)){
            WxBindInfo wxBindInfo = wxBindInfoList.get(0);

            WxMpInMemoryConfigStorage wxMpInMemoryConfigStorage = new WxMpInMemoryConfigStorage();
            wxMpInMemoryConfigStorage.setAppId(wxBindInfo.getWxappid());
            wxMpInMemoryConfigStorage.setSecret(wxBindInfo.getWxappsecret());
            wxMpInMemoryConfigStorage.setToken(wxBindInfo.getWxtoken());
            wxMpInMemoryConfigStorage.setAesKey(wxBindInfo.getWxaeskey());
            wxMpService.setWxMpConfigStorage(wxMpInMemoryConfigStorage);

            return ResponseMsgUtil.success(wxBindInfo);
        }else{
            return ResponseMsgUtil.failure("未绑定微信公众号");
        }
    }

    /**
     * 获得绑定的公众号信息
     * @return
     */
    @GetMapping("/get_bind_info")
    public Result<WxBindInfo> getBindInfo(){
        try{
            List<WxBindInfo> wxBindInfoList = wxBindInfoBO.find();
            if(!CollectionUtils.isEmpty(wxBindInfoList)){
                WxBindInfo wxBindInfo = wxBindInfoList.get(0);

                return ResponseMsgUtil.success(wxBindInfo);
            }else{
                return ResponseMsgUtil.success();
            }
        }catch (Exception e){
            e.printStackTrace();
            return ResponseMsgUtil.failure();
        }
    }

    /**
     * 绑定微信公众号
     * @param wxname
     * @param wxinitid
     * @param wxappid
     * @param wxappsecret
     * @param wxtoken
     * @param wxaeskey
     * @param basedomain
     * @param id
     * @return
     */
    @RequestMapping(value = "/bind_wechat", method = RequestMethod.POST)
    public Result<String> bindWeChat(@RequestParam String wxname,
                                     @RequestParam String wxinitid,
                                     @RequestParam String wxappid,
                                     @RequestParam String wxappsecret,
                                     @RequestParam String wxtoken,
                                     @RequestParam String wxaeskey,
                                     @RequestParam String basedomain,
                                     @RequestParam(required = false) String id){
        JSONObject wxConfig = new JSONObject();
        try {
            wxConfig = WeiXinUtil.getToken(wxappid,wxappsecret);
            wxConfig.getString("access_token");

            WxBindInfo wxBindInfo = new WxBindInfo();
            wxBindInfo.setWxappid(wxappid);
            wxBindInfo.setWxappsecret(wxappsecret);
            wxBindInfo.setWxtoken(wxtoken);
            wxBindInfo.setWxaeskey(wxaeskey);
            wxBindInfo.setWxname(wxname);
            wxBindInfo.setWxoriginalid(wxinitid);
            wxBindInfo.setIscurrent(1);
            wxBindInfo.setBindtime(new Date());
            wxBindInfo.setBasedomain(basedomain);
            if(!StringUtils.isEmpty(id)){
                wxBindInfo.setId(id);
                wxBindInfoBO.update(wxBindInfo);
            }else {
                wxBindInfoBO.insert(wxBindInfo);
            }

            WxMpInMemoryConfigStorage wxMpInMemoryConfigStorage = new WxMpInMemoryConfigStorage();
            wxMpInMemoryConfigStorage.setAppId(wxappid);
            wxMpInMemoryConfigStorage.setSecret(wxappsecret);
            wxMpInMemoryConfigStorage.setToken(wxtoken);
            wxMpInMemoryConfigStorage.setAesKey(wxaeskey);
            wxMpService.setWxMpConfigStorage(wxMpInMemoryConfigStorage);

            return ResponseMsgUtil.success();
            //session.setAttribute("wxName", wxname);
        } catch (Exception e) {
            String error = "获取token失败,错误："+wxConfig.getInteger("errcode")+",错误信息："+ wxConfig.getString("errmsg");
            return ResponseMsgUtil.failure(error);
        }
        //ResponseUtils.writeJsonObject(response, result);
    }
}
