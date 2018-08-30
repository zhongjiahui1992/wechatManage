package com.uhope.rl.application.web.common;

import com.uhope.common.web.util.ResponseUtils;
import me.chanjar.weixin.mp.api.WxMpMessageRouter;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.bean.message.WxMpXmlMessage;
import me.chanjar.weixin.mp.bean.message.WxMpXmlOutMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Created by phl on 15-9-9.
 */
@Controller
@RequestMapping("/wx/weChat")
public class WxController{

    @Autowired
    private WxMpService wxService;

    @Autowired
    private WxMpMessageRouter wxMessageRouter;


    /**
     * 开发者模式验证接口
     * @param response
     * @param signature
     * @param timestamp
     * @param nonce
     * @param echostr
     */
    @RequestMapping(method = RequestMethod.GET)
    public void checkSignature(HttpServletResponse response, String signature, String timestamp, String nonce, String echostr) {
        System.out.println("signature:" + signature);
        if (wxService.checkSignature(timestamp, nonce, signature)) {
            ResponseUtils.writeText(response, echostr);
        } else {
            ResponseUtils.writeText(response, "error");
        }
    }

    @RequestMapping(method = RequestMethod.POST)
    public void post(HttpServletRequest request, HttpServletResponse response) {
        System.out.println("post--------------------------------");
        WxMpXmlMessage inMessage = null;
        try {
            inMessage = WxMpXmlMessage.fromXml(request.getInputStream());
            System.out.println("inMessage:" + inMessage.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (inMessage != null) {
            WxMpXmlOutMessage outMessage = wxMessageRouter.route(inMessage);
            if (outMessage != null) {
                ResponseUtils.writeText(response, outMessage.toXml());
            }
        }
    }

}
