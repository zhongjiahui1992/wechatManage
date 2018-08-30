package com.uhope.rl.application.web.manage;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.uhope.rl.application.constants.Constant;
import com.uhope.rl.application.result.ResponseMsgUtil;
import com.uhope.rl.application.result.Result;
import com.uhope.rl.application.utils.WeiXinUtil;
import com.uhope.rl.wechat.domain.WxGroup;
import com.uhope.rl.wechat.domain.WxSubscribeUser;
import com.uhope.rl.wechat.service.WxGroupService;
import com.uhope.rl.wechat.service.WxSubscribeUserService;
import me.chanjar.weixin.common.exception.WxErrorException;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.bean.tag.WxUserTag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import tk.mybatis.mapper.entity.Condition;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author zhongjiahui.
 * @date Created on 2018/2/1.
 */
@RestController
public class GroupController {
    private static final Logger LOGGER = LoggerFactory.getLogger(GroupController.class);

    @Autowired
    private WxMpService wxMpService;
    @Autowired
    private WxGroupService wxGroupService;
    @Autowired
    private WxSubscribeUserService wxSubscribeUserService;


    /**
     * 获取微信公众号分组信息
     * @param response
     */
    @RequestMapping(value = "/get_tags", method = {RequestMethod.GET})
    public Result<WxGroup> getTags(HttpServletResponse response) {
        //WxMpService wxMpService = setWxServiceConfig(session);
        com.alibaba.fastjson.JSONObject jsonObject = new com.alibaba.fastjson.JSONObject();

        String appId = wxMpService.getWxMpConfigStorage().getAppId();
        Condition condition = new Condition(WxGroup.class);
        condition.createCriteria().andCondition("appid =  ", appId);

        if(wxGroupService.count(condition) > 0){
            List<WxGroup> wxGroups = wxGroupService.findByCondition(condition);

             return ResponseMsgUtil.success(wxGroups);
            /*jsonObject.put(Constant.RESULT_RESCODE, Constant.RESULT_CODE_SUCCESS);
            jsonObject.put(Constant.RESULT_RESMSG, Constant.RESULT_MSG_SUCCESS);
            jsonObject.put(Constant.RESULT_DATA, wxGroups);*/
        }else{
            List<WxGroup> wxGroupList = new ArrayList<WxGroup>();
            try {
                String token = wxMpService.getAccessToken();
                JSONArray groups = WeiXinUtil.getGroupByToken(token);
                /*if(groups == null){
                    return wxGroupList;
                }*/
                wxGroupService.remove(condition);
                WxGroup dbWxGroup = new WxGroup();
                Iterator<Object> it = groups.iterator();
                while (it.hasNext()) {
                    JSONObject group=(JSONObject)it.next();
                    dbWxGroup.setAppid(appId);
                    dbWxGroup.setGroupid(group.getInteger("id"));
                    dbWxGroup.setName(group.getString("name"));
                    dbWxGroup.setCount(group.getInteger("count"));
                    dbWxGroup.setId(null);
                    wxGroupService.insert(dbWxGroup);

                    wxGroupList.add(dbWxGroup);
                }
                /*jsonObject.put(Constant.RESULT_RESCODE, Constant.RESULT_CODE_SUCCESS);
                jsonObject.put(Constant.RESULT_RESMSG, Constant.RESULT_MSG_SUCCESS);
                jsonObject.put(Constant.RESULT_DATA, groups);*/
                 return ResponseMsgUtil.success(wxGroupList);
            } catch (WxErrorException e) {
                e.printStackTrace();
                return ResponseMsgUtil.failure();
            }
        }
        //ResponseUtils.writeJsonObject(response, jsonObject);
    }

    /**
     * 新建微信分组
     * @param response
     * @param name
     */
    @RequestMapping(value = "/create_tag", method = {RequestMethod.GET})
    public Result<WxGroup> createTag(HttpServletResponse response, String name){
        //WxMpService wxMpService = setWxServiceConfig(session);
        WxUserTag wxTag = new WxUserTag();
        WxGroup wxGroup = new WxGroup();
        JSONObject jsonObject = new JSONObject();
        try {
            wxTag = wxMpService.getUserTagService().tagCreate(name);
            wxTag.setCount(0);
            wxGroup.setAppid(wxMpService.getWxMpConfigStorage().getAppId());
            wxGroup.setGroupid(wxTag.getId().intValue());
            wxGroup.setName(wxTag.getName());
            wxGroup.setCount(wxTag.getCount());
            wxGroupService.insert(wxGroup);
            return ResponseMsgUtil.success(wxGroup);
        } catch (WxErrorException e) {
            e.printStackTrace();
            return ResponseMsgUtil.failure(Constant.RESULT_MSG_EXCEPTION);
        }
    }

    /**
     * 修改微信分组名称
     * @param response
     * @param groupid
     * @param name
     * @param count
     */
    @RequestMapping(value = "/update_tag", method = {RequestMethod.GET})
    public Result<String> updateTag(HttpServletResponse response, String groupid, String name, String count){
        //WxMpService wxMpService = setWxServiceConfig(session);
        String appId = wxMpService.getWxMpConfigStorage().getAppId();
        WxGroup wxGroup = new WxGroup();
        Condition wxGroupVO = new Condition(WxGroup.class);
        try {
            wxMpService.getUserTagService().tagUpdate(Long.parseLong(groupid),name);

            wxGroup.setAppid(appId);
            wxGroup.setGroupid(Integer.parseInt(groupid));
            wxGroup.setName(name);
            wxGroup.setCount(Integer.parseInt(count));

            wxGroupVO.createCriteria().andCondition("appid=", appId).andCondition("groupid=", groupid);
            /*wxGroupVO.setAppid(appId);
            wxGroupVO.setId(Long.parseLong(id));*/

            wxGroupService.update(wxGroup, wxGroupVO);
            return ResponseMsgUtil.success();
        } catch (WxErrorException e) {
            e.printStackTrace();
            return ResponseMsgUtil.failure();
        }
    }

    /**
     * 删除微信标签
     * @param response
     * @param session
     * @param openids
     * @param tagid
     */
    @RequestMapping(value = "/delete_tag", method = {RequestMethod.POST})
    public Result<String> deleteTag(HttpServletResponse response,
                          HttpSession session,
                          @RequestParam(required = false, value = "openids[]") String[] openids,
                          @RequestParam(value = "tagid") String tagid){
        //WxMpService wxMpService = setWxServiceConfig(session);
        String appId = wxMpService.getWxMpConfigStorage().getAppId();
        Condition wxGroupVO = new Condition(WxGroup.class);
        try {
            if(openids == null){
                openids = new String[0];
            }
            wxMpService.getUserTagService().tagDelete(Long.parseLong(tagid));
            //weiXinUnit.postUrlByOpenidsAndTagids(REMOVE_TAGS_URL,openids.toString(),tagid);
            //删除数据库分组id
            wxGroupVO.createCriteria().andCondition("appid=", appId).andCondition("groupid=", tagid);
            /*wxGroupVO.setAppid(appId);
            wxGroupVO.setId(Long.parseLong(tagid));*/
            wxGroupService.remove(wxGroupVO);

            //修改数据库关注者用户表的tagids
            List<WxSubscribeUser> wxSubscribeUser = new ArrayList<WxSubscribeUser>();
            //WxSubscribeUser user = new WxSubscribeUser();
            Condition wxSubscribeUserVO = new Condition(WxSubscribeUser.class);
            wxSubscribeUserVO.createCriteria().andCondition("appid=", appId).andLike("tagids", "%"+tagid+"%");
                /*wxSubscribeUserVO.setAppid(appId);
                wxSubscribeUserVO.setOpenid(openid);*/
            wxSubscribeUser = wxSubscribeUserService.findByCondition(wxSubscribeUserVO);
            for(WxSubscribeUser user : wxSubscribeUser){
                String tagidinsql = user.getTagids();
                String context = ","+tagid+"|"+tagid+","+"|"+tagid;
                user.setTagids(tagidinsql.replaceAll(context,""));

                wxSubscribeUserService.update(user,wxSubscribeUserVO);
            }
            //user = wxSubscribeUser.get(0);


            return ResponseMsgUtil.success();
        } catch (WxErrorException e) {
            e.printStackTrace();
            return ResponseMsgUtil.failure();
        }
    }


}
