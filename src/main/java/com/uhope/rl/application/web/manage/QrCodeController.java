package com.uhope.rl.application.web.manage;

import com.alibaba.fastjson.JSONObject;
import com.uhope.common.web.util.ResponseUtils;
import com.uhope.rl.application.constants.Constant;
import com.uhope.rl.application.result.ResponseMsgUtil;
import com.uhope.rl.application.result.Result;
import com.uhope.rl.wechat.domain.WxBindInfo;
import com.uhope.rl.wechat.service.WxBindInfoService;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.bean.result.WxMpQrCodeTicket;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.List;

/**
 * Created by zhongjiahui on 2018/1/27.
 */
@RestController
public class QrCodeController{
    @Autowired
    private WxMpService wxMpService;
    @Autowired
    private WxBindInfoService wxBindInfoService;

    /**
     * 获得微信公众号的二维码
     * @param session
     * @param response
     * @return
     */
    @RequestMapping(value = "/getQrcode", method = RequestMethod.GET)
    public Result<JSONObject> getQrcode(HttpSession session, HttpServletResponse response){
        JSONObject jsonObject = new JSONObject();
        try{
            //WxMpService wxMpService = setWxServiceConfig(session);
            WxMpQrCodeTicket wxMpQrCodeTicket = wxMpService.getQrcodeService().qrCodeCreateLastTicket(9999);
            String qrUrl = wxMpService.getQrcodeService().qrCodePictureUrl(wxMpQrCodeTicket.getTicket());
            System.out.println(qrUrl);
            jsonObject.put("qrCode", qrUrl);

            List<WxBindInfo> wxBindInfoList = wxBindInfoService.find();
            if(!CollectionUtils.isEmpty(wxBindInfoList)){
                WxBindInfo wxBindInfo = wxBindInfoList.get(0);
                jsonObject.put("wxInfo", wxBindInfo);
            }else{
                jsonObject.put("wxInfo", "");
            }

            return ResponseMsgUtil.success(jsonObject);
        }catch (Exception e){
            e.printStackTrace();
            return ResponseMsgUtil.failure();
        }
        //ResponseUtils.writeJsonObject(response, jsonObject);
    }
}
