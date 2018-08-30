package com.uhope.rl.application.web.manage;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.uhope.common.web.util.ResponseUtils;
import com.uhope.rl.application.constants.Constant;
import com.uhope.rl.application.result.ResponseMsgUtil;
import com.uhope.rl.application.result.Result;
import com.uhope.rl.application.utils.WeiXinUtil;
import com.uhope.rl.wechat.domain.WxMenuFunc;
import com.uhope.rl.wechat.service.WxMenuFuncService;
import me.chanjar.weixin.common.bean.menu.WxMenu;
import me.chanjar.weixin.common.bean.menu.WxMenuButton;
import me.chanjar.weixin.common.exception.WxErrorException;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.bean.menu.WxMpMenu;
import org.apache.commons.codec.net.URLCodec;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2017/6/5.
 */
@RestController
public class MenuController{
    @Autowired
    private WxMpService wxService;
    @Autowired
    private WxMenuFuncService wxMenuFuncBO;


    /**
     * 获得自定义菜单
     * @param session
     * @param response
     */
    @RequestMapping(value = "/get_menu", method = {RequestMethod.GET})
    public Result<WxMpMenu> getMenu(HttpSession session, HttpServletResponse response){
        /*if(session.getAttribute("menuList")!=null){
            return (WxMpMenu)session.getAttribute("menuList");
        }*/
        com.alibaba.fastjson.JSONObject jsonObject = new com.alibaba.fastjson.JSONObject();
        WxMpMenu wxMenu = new WxMpMenu();
        try {
            List<WxMenuButton> button = new ArrayList<WxMenuButton>();

            String token = wxService.getAccessToken();
            JSONObject menu = WeiXinUtil.getMenuByToken(token);

            JSONObject menuInfo = null;
            if(menu != null && menu.containsKey("selfmenu_info")){
                menuInfo = menu.getJSONObject("selfmenu_info");
                JSONArray buttons = null;
                if(menuInfo != null && menuInfo.containsKey("button")){
                    buttons = menuInfo.getJSONArray("button");

                    JSONObject one = null;
                    String name = "";
                    WxMenuButton wxButton = null;
                    JSONObject subBtn = null;
                    List<WxMenuButton> wxMenuButtonList = null;
                    JSONArray subList = null;
                    for(int i=0; i<buttons.size(); i++){
                        one = buttons.getJSONObject(i);
                        name = one.getString("name");

                        wxButton = new WxMenuButton();
                        wxButton.setName(name);

                        subBtn = one.getJSONObject("sub_button");
                        if(subBtn !=null && subBtn.size() > 0){
                            wxMenuButtonList = new ArrayList<WxMenuButton>();
                            wxButton.setSubButtons(wxMenuButtonList);

                            JSONObject subOne = null;
                            WxMenuButton subWxBtn = null;
                            String type = "";
                            subList = subBtn.getJSONArray("list");
                            for(int j=0; j<subList.size(); j++){
                                subOne = subList.getJSONObject(j);
                                subWxBtn = new WxMenuButton();
                                type = subOne.getString("type");
                                subWxBtn.setType(type);
                                if("view".equals(type)){
                                    subWxBtn.setUrl(subOne.getString("url"));
                                }else if("click".equals(type)){
                                    subWxBtn.setKey(subOne.getString("key"));
                                }
                                subWxBtn.setName(subOne.getString("name"));

                                wxMenuButtonList.add(subWxBtn);
                            }
                        }else{
                            String type = one.getString("type");
                            wxButton.setType(type);
                            if("view".equals(type)){
                                wxButton.setUrl(one.getString("url"));
                            }else if("click".equals(type)){
                                wxButton.setKey(one.getString("key"));
                            }
                        }
                        button.add(wxButton);
                    }
                }
            }

            WxMpMenu.WxMpConditionalMenu menu1 = new WxMpMenu.WxMpConditionalMenu();
            menu1.setButtons(button);
            wxMenu.setMenu(menu1);
            //wxMenu = wxService.getMenuService().menuGet();

            return ResponseMsgUtil.success(wxMenu);
        } catch (WxErrorException e) {
            return ResponseMsgUtil.failure();
        }
        //ResponseUtils.writeJsonObject(response,jsonObject);
    }

    /**
     * 创建自定义菜单
     * @param response
     * @param session
     * @param wxMenuList
     */
    @RequestMapping(value = "/create_menu", method = {RequestMethod.POST})
    public Result<String> createMenu(HttpServletResponse response, HttpSession session, @RequestBody String wxMenuList){
        JSONObject jsonObject = new JSONObject();
        try {
            wxMenuList = "{\"button\":"+wxMenuList+"}";
            String changeSubButton = "subButtons";
            wxMenuList = wxMenuList.replaceAll(changeSubButton,"sub_button");
            String changeAppid = "appId";
            wxMenuList = wxMenuList.replaceAll(changeAppid,"appid");
            String changeMediaid = "mediaId";
            wxMenuList = wxMenuList.replaceAll(changeMediaid,"media_id");
            String changePagepath = "pagePath";
            wxMenuList = wxMenuList.replaceAll(changePagepath,"pagepath");

            wxService.getMenuService().menuCreate(wxMenuList);

            return ResponseMsgUtil.success();
        } catch (WxErrorException e) {
            e.printStackTrace();
            return ResponseMsgUtil.failure();
        }
        //ResponseUtils.writeJsonObject(response, jsonObject);
    }

    /**
     * 删除自定义菜单
     * @param response
     */
    @RequestMapping(value = "/del_menu", method = {RequestMethod.GET})
    public void delMenu(HttpServletResponse response){
        try {
            wxService.getMenuService().menuDelete();
        } catch (WxErrorException e) {
            ResponseUtils.writeText(response, e.getError().toString());
        }
    }


    /**
     * 获取所有WxMenuFunc,得到url以及相对应的菜单名称
     */
    @RequestMapping(value = "/list_url", method = {RequestMethod.GET})
    public Result<WxMenuFunc> listMenuFunc(HttpServletResponse response) {
        JSONObject result = new JSONObject();
        List<WxMenuFunc> wxMenuFuncLists = new ArrayList<WxMenuFunc>();
        try {
            wxMenuFuncLists = wxMenuFuncBO.find();

            return ResponseMsgUtil.success(wxMenuFuncLists);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseMsgUtil.failure();
        }
        //ResponseUtils.writeJsonObject(response, result);
    }

    //拼写需要的地址
    @RequestMapping(value = "/menu_url", method = {RequestMethod.GET})
    public Result<String> modifyUrl(@RequestParam String url, @RequestParam String type, @RequestParam String basedomain, HttpServletRequest request) {
        String appId = wxService.getWxMpConfigStorage().getAppId();

        /*int end= request.getRequestURL().indexOf("modifyUrl");
        String str= request.getRequestURL().substring(0,end);
        System.out.println(str);*/
        String modifyUrl = "";
        if ("0".equals(type)) {
            modifyUrl = basedomain + url;
        } else if ("1".equals(type)) {
            URLCodec urlCodec = new URLCodec();
            try {
                String second = urlCodec.encode(basedomain + url);
                modifyUrl = "https://open.weixin.qq.com/connect/oauth2/authorize?appid=" + appId +
                        "&redirect_uri=" + second +
                        "&response_type=code&scope=snsapi_base&state=123#wechat_redirect";
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        System.out.println(modifyUrl);
        return ResponseMsgUtil.success(modifyUrl);
        //ResponseUtils.writeJsonObject(response, modifyUrl);

    }
}
