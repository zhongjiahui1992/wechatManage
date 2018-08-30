package com.uhope.rl.application.web.common;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.base.Strings;
import com.uhope.rl.application.result.ResponseMsgUtil;
import com.uhope.rl.application.result.Result;
import com.uhope.rl.application.utils.FileUploadUtil;
import me.chanjar.weixin.common.bean.WxJsapiSignature;
import me.chanjar.weixin.common.exception.WxErrorException;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.bean.result.WxMpOAuth2AccessToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.io.File;

/**
 * @author zhongjiahui.
 * @date Created on 2018/2/7.
 */
@RestController
@RequestMapping("/wx/common")
public class WeChatCommonUtil {
    @Autowired
    private WxMpService wxMpService;


    /**
     * 根据code换取微信用户信息
     * @param code
     * @return
     */
    @GetMapping("/openid")
    public Result<String> getOpenId(@RequestParam(required = false) String code){
        System.out.println("code:" + code);
        //code = "aaa";
        WxMpOAuth2AccessToken wxMpOAuth2AccessToken = null;
        try{
            if (!Strings.isNullOrEmpty(code)) {
                wxMpOAuth2AccessToken = wxMpService.oauth2getAccessToken(code);
            }
            if (wxMpOAuth2AccessToken != null) {
                return ResponseMsgUtil.success(wxMpOAuth2AccessToken.getOpenId());
            }else{
                return ResponseMsgUtil.failure();
            }
            //return ResponseMsgUtil.success("oJGYl1NEdCORGfA40XbiLGPzgFGQ");//测试用
        }catch (WxErrorException e){
            e.printStackTrace();
            return ResponseMsgUtil.failure(e.getError().getJson());
        }
    }

    /**
     * 验证url，用户前端页面使用jssdk
     * @param url
     * @return
     */
    @GetMapping("/signature")
    public Result<WxJsapiSignature> checkWxJsApiSignature(@RequestParam String url){
        System.out.println("验证signature========================");
        try {
            //String url = request.getRequestURL() + "";
            /*if (request.getQueryString() != null) {
                url += "?" + request.getQueryString();
            }*/
            System.out.println("url:" + url);
            WxJsapiSignature wxJsapiSignature = wxMpService.createJsapiSignature(url);
            System.out.println("signature：" + JSON.toJSONString(wxJsapiSignature));
            return ResponseMsgUtil.success(wxJsapiSignature);
            //model.addAttribute("signature", wxJsapiSignature);

            //model.addAttribute("appid", appid);
        } catch (WxErrorException e) {
            e.printStackTrace();
            return ResponseMsgUtil.failure();
        }
    }

    /**
     * 素材下载接口
     * @param images
     * @return
     */
    @GetMapping("/mediaDownload")
    public Result<JSONObject> mediaDownload(@RequestParam(value = "files") String[] images){
        System.out.println("------start download image----------");
        JSONObject jsonObject = new JSONObject();
        if (images != null && images.length > 0) {
            System.out.println("------"+images.length+"---------");
            File file = null;
            String[] urls = new String[images.length];
            try{
                for(int i=0; i<images.length; i++){
                    System.out.println("------"+images[i]+"---------");
                    file = wxMpService.getMaterialService().mediaDownload(images[i]);
                    System.out.println("--file---:" + file.getAbsolutePath());
                    urls[i] = FileUploadUtil.upload(file)[0];
                    //urls[i] = dfsService.upload(file)[0];
                }
                jsonObject.put("urls", urls);
                return ResponseMsgUtil.success(jsonObject);
            }catch (Exception e){
                e.printStackTrace();
                return ResponseMsgUtil.failure(JSON.toJSONString(e));
            }
        }else{
            return ResponseMsgUtil.failure();
        }
    }
}
