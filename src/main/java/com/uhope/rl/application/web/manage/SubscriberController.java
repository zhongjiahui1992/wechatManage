package com.uhope.rl.application.web.manage;

import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.PageInfo;
import com.uhope.common.web.util.ResponseUtils;
import com.uhope.rl.application.constants.Constant;
import com.uhope.rl.application.result.ResponseMsgUtil;
import com.uhope.rl.application.result.Result;
import com.uhope.rl.application.utils.WeiXinUtil;
import com.uhope.rl.wechat.domain.WxGroup;
import com.uhope.rl.wechat.domain.WxSubscribeUser;
import com.uhope.rl.wechat.dto.WxGroupDTO;
import com.uhope.rl.wechat.dto.WxSubscribeUserDTO;
import com.uhope.rl.wechat.service.WxGroupService;
import com.uhope.rl.wechat.service.WxSubscribeUserService;
import me.chanjar.weixin.common.exception.WxErrorException;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.bean.result.WxMpUser;
import me.chanjar.weixin.mp.bean.result.WxMpUserBlacklistGetResult;
import me.chanjar.weixin.mp.bean.result.WxMpUserList;
import me.chanjar.weixin.mp.bean.tag.WxTagListUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import tk.mybatis.mapper.entity.Condition;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * Created by Administrator on 2017/6/5.
 */
@RestController
public class SubscriberController{
    @Autowired
    private WxMpService wxService;
    @Autowired
    private WxSubscribeUserService wxSubscribeUserBO;
    @Autowired
    private WxGroupService wxGroupBO;

    public static final String ADD_TAGS_URL = "https://api.weixin.qq.com/cgi-bin/tags/members/batchtagging";
    public static final String REMOVE_TAGS_URL = "https://api.weixin.qq.com/cgi-bin/tags/members/batchuntagging";


    /**
     * 获取用户列表
     * @return
     */
    @RequestMapping(value = "/get_users", method = {RequestMethod.GET})
    public Result<WxSubscribeUser> getUsers(@RequestParam(defaultValue = "0") int pageNumber,
                                            @RequestParam(defaultValue = "10") int pageSize) {
        JSONObject jsonObject = new JSONObject();

        String appId = wxService.getWxMpConfigStorage().getAppId();

        Condition wxSubscribeUserVO = new Condition(WxSubscribeUser.class);
        wxSubscribeUserVO.createCriteria().andCondition("appid=",appId);
        if(wxSubscribeUserBO.count(wxSubscribeUserVO) > 0){
            PageInfo<WxSubscribeUser> wxSubscribeUserPageInfo = wxSubscribeUserBO.findByCondition(wxSubscribeUserVO,pageSize,pageNumber);
            List<WxSubscribeUser> subscribeUserList = wxSubscribeUserPageInfo.getList();
            for(WxSubscribeUser subscribeUser : subscribeUserList){
                String nickname = subscribeUser.getNickname();
                subscribeUser.setNickname(WeiXinUtil.convertSqlNameToTrueName(nickname));

                String tagids = subscribeUser.getTagids();
                subscribeUser.setTagids(convertTagIdsToTagNames(appId, tagids));
            }
            /*jsonObject.put(Constant.RESULT_RESMSG, Constant.RESULT_MSG_SUCCESS);
            jsonObject.put(Constant.RESULT_RESCODE, Constant.RESULT_CODE_SUCCESS);
            jsonObject.put(Constant.RESULT_DATA, subscribeUserList);*/
            return ResponseMsgUtil.success(wxSubscribeUserPageInfo);
        }else{
            //清空数据库的非拉黑用户
            wxSubscribeUserBO.remove(wxSubscribeUserVO);

            WxMpUser wxMpUser = new WxMpUser();
            WxMpUserList userList = null;
            List<WxSubscribeUser> userInfoList = new ArrayList<WxSubscribeUser>();
            try {
                userList = wxService.getUserService().userList("");
                //userInfoList = wxService.getUserService().userInfoList(userList.getOpenids());
                List<String> openids = userList.getOpenids();
                for(String openid : openids){
                    wxMpUser = wxService.getUserService().userInfo(openid.toString(), "zh_CN");
                    WxSubscribeUser wxSubscribeUser = new WxSubscribeUser();
                    wxSubscribeUser.setAppid(appId);
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
                    wxSubscribeUser.setSubscribeTime(new Date(wxMpUser.getSubscribeTime()));
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

                    /*wxSubscribeUser.setNickname(wxMpUser.getNickname());
                    wxSubscribeUser.setTagids(convertTagIdsToTagNames(appId, wxSubscribeUser.getTagids()));
                    userInfoList.add(wxSubscribeUser);*/
                }
                PageInfo<WxSubscribeUser> wxSubscribeUserPageInfo = wxSubscribeUserBO.findByCondition(wxSubscribeUserVO,pageSize,pageNumber);
                List<WxSubscribeUser> subscribeUserList = wxSubscribeUserPageInfo.getList();
                for(WxSubscribeUser subscribeUser : subscribeUserList){
                    String nickname = subscribeUser.getNickname();
                    subscribeUser.setNickname(WeiXinUtil.convertSqlNameToTrueName(nickname));

                    String tagids = subscribeUser.getTagids();
                    subscribeUser.setTagids(convertTagIdsToTagNames(appId, tagids));
                }
                /*jsonObject.put(Constant.RESULT_RESMSG, Constant.RESULT_MSG_SUCCESS);
                jsonObject.put(Constant.RESULT_RESCODE, Constant.RESULT_CODE_SUCCESS);
                jsonObject.put(Constant.RESULT_DATA, userInfoList);*/
                return ResponseMsgUtil.success(wxSubscribeUserPageInfo);
            } catch (WxErrorException e) {
                e.printStackTrace();
                /*jsonObject.put(Constant.RESULT_RESMSG, Constant.RESULT_MSG_FAILURE);
                jsonObject.put(Constant.RESULT_RESCODE, Constant.RESULT_CODE_FAILURE);*/
                return ResponseMsgUtil.failure();
            }
        }
        //ResponseUtils.writeJsonObject(response, jsonObject);
    }

    /**
     * 获得某个标签下的用户列表
     * @param tagid
     * @return
     */
    @RequestMapping(value = "/get_tag_user_list", method = {RequestMethod.GET})
    public Result<WxSubscribeUser> getTagUserList(@RequestParam(defaultValue = "0") int pageNumber,
                                                  @RequestParam(defaultValue = "10") int pageSize,
                                                  String tagid){
        JSONObject jsonObject = new JSONObject();
        String appId = wxService.getWxMpConfigStorage().getAppId();

        List<WxSubscribeUser> wxSubscribeUserList = new ArrayList<WxSubscribeUser>();
        WxSubscribeUser wxSubscribeUser = new WxSubscribeUser();
        Condition wxSubscribeUserVO = null;
        List<WxSubscribeUser> tagListUser = new ArrayList<WxSubscribeUser>();
        /*wxSubscribeUserVO.setTagids(tagid);
        List<WxSubscribeUser> tagListUser = wxSubscribeUserBO.find(wxSubscribeUserVO);*/
        WxTagListUser tagListIncludeOpenids = new WxTagListUser();
        try {
            wxSubscribeUserVO = new Condition(WxSubscribeUser.class);
            wxSubscribeUserVO.createCriteria()
                    .andCondition("appid=",appId)
                    .andLike("tagids","%"+tagid+"%");
            PageInfo<WxSubscribeUser> subscribeUserPageInfo = wxSubscribeUserBO.findByCondition(wxSubscribeUserVO,pageSize,pageNumber);
            wxSubscribeUserList = subscribeUserPageInfo.getList();
            for(WxSubscribeUser user : wxSubscribeUserList){
                String nickname = user.getNickname();
                user.setNickname(WeiXinUtil.convertSqlNameToTrueName(nickname));
                user.setTagids(convertTagIdsToTagNames(appId,user.getTagids()));
            }
            return ResponseMsgUtil.success(subscribeUserPageInfo);

            /*tagListIncludeOpenids = wxService.getUserTagService().tagListUser(Long.parseLong(tagid),"");

            if(tagListIncludeOpenids.getCount() == 0){
                return ResponseMsgUtil.success(null);
            }

            List<String> openidList = tagListIncludeOpenids.getData().getOpenidList();
            for(String openid : openidList){
                wxSubscribeUserVO = new Condition(WxSubscribeUser.class);
                wxSubscribeUserVO.createCriteria().andCondition("appid=",appId).andCondition("openid=",openid);
                wxSubscribeUserList = wxSubscribeUserBO.find(wxSubscribeUserVO);
                if(!CollectionUtils.isEmpty(wxSubscribeUserList)){
                    wxSubscribeUser = wxSubscribeUserList.get(0);
                    String nickname = wxSubscribeUser.getNickname();
                    wxSubscribeUser.setNickname(WeiXinUtil.convertSqlNameToTrueName(nickname));
                    wxSubscribeUser.setTagids(convertTagIdsToTagNames(appId,wxSubscribeUser.getTagids()));
                    tagListUser.add(wxSubscribeUser);
                }

            }
            jsonObject.put(Constant.RESULT_RESMSG, Constant.RESULT_MSG_SUCCESS);
            jsonObject.put(Constant.RESULT_RESCODE, Constant.RESULT_CODE_SUCCESS);
            jsonObject.put(Constant.RESULT_DATA, tagListUser);*/
        } catch (Exception e) {
            return ResponseMsgUtil.failure();
        }
        //ResponseUtils.writeJsonObject(response, jsonObject);
    }

    /**
     * 修改用户备注
     * @param response
     * @param session
     * @param openid
     * @param remark
     */
    @RequestMapping(value = "/update_user_remark", method = {RequestMethod.GET})
    public Result<WxSubscribeUser> updateUserRemark(HttpServletResponse response, HttpSession session, String openid, String remark){
        JSONObject result = new JSONObject();
        try {
            wxService.getUserService().userUpdateRemark(openid,remark);
            WxSubscribeUser appUser = new WxSubscribeUser();
            appUser.setRemark(remark);
            Condition appUserVO = new Condition(WxSubscribeUser.class);
            appUserVO.createCriteria().andCondition("appid=",wxService.getWxMpConfigStorage().getAppId())
                    .andCondition("openid=", openid);
            /*appUserVO.setAppid(wxService.getWxMpConfigStorage().getAppId());
            appUserVO.setOpenid(openid);*/
            wxSubscribeUserBO.update(appUser,appUserVO);

            return ResponseMsgUtil.success();
        } catch (WxErrorException e) {
            e.printStackTrace();
            return ResponseMsgUtil.failure();
        }
        //ResponseUtils.writeJsonObject(response, result);
    }

    /**
     * 批量给用户添加标签
     * @param response
     * @param session
     * @param openids
     * @param tagids
     */
    @RequestMapping(value = "/batch_tag_users", method = {RequestMethod.POST})
    public Result<WxSubscribeUser> batchTagForUsers(HttpServletResponse response,
                                 HttpSession session,
                                 @RequestParam(value = "openids") String[] openids,
                                 @RequestParam(value = "tagids") String[] tagids){
        JSONObject jsonObject = new JSONObject();
        String appId = wxService.getWxMpConfigStorage().getAppId();
        try {
            WxGroup wxGroup = new WxGroup();
            Condition wxGroupVO = null;

            WxSubscribeUser wxSubscribeUser = new WxSubscribeUser();
            Condition wxSubscribeUserVO = null;
            List<WxSubscribeUser> wxSubscribeUserList = new ArrayList<WxSubscribeUser>();

            List<WxGroup> wxGroupList = new ArrayList<WxGroup>();
            Integer preCount = 0;
            for(String tagid : tagids){
                WeiXinUtil.postUrlByOpenidsAndTagids(wxService ,ADD_TAGS_URL,openids,tagid);
                //更新数据库用户分组表的count
                for(String openid : openids){
                    wxSubscribeUserVO = new Condition(WxSubscribeUser.class);
                    wxSubscribeUserVO.createCriteria().andCondition("appid=",appId).andCondition("openid=",openid);
                    /*wxSubscribeUserVO.setAppid(appId);
                    wxSubscribeUserVO.setOpenid(openid);*/
                    wxSubscribeUserList = wxSubscribeUserBO.findByCondition(wxSubscribeUserVO);
                    if(!CollectionUtils.isEmpty(wxSubscribeUserList)){
                        wxSubscribeUser = wxSubscribeUserList.get(0);
                        String usertagids = wxSubscribeUser.getTagids();

                        if(!usertagids.contains(tagid)){
                            wxGroupVO = new Condition(WxGroup.class);
                            wxGroupVO.createCriteria()
                                    .andCondition("appid=",appId)
                                    .andCondition("groupid=",Integer.parseInt(tagid));
                            wxGroupList = wxGroupBO.findByCondition(wxGroupVO);
                            if(!CollectionUtils.isEmpty(wxGroupList)){
                                wxGroup = wxGroupList.get(0);
                                preCount = wxGroup.getCount();
                                wxGroup.setCount(preCount+1);
                                wxGroupBO.update(wxGroup,wxGroupVO);
                            }
                        }
                    }
                }
            }

            //更新数据库内关注者用户表的tagids
            for(String openid : openids){
                wxSubscribeUserVO = new Condition(WxSubscribeUser.class);
                wxSubscribeUserVO.createCriteria().andCondition("appid=",appId).andCondition("openid=",openid);
                /*wxSubscribeUserVO.setAppid(appId);
                wxSubscribeUserVO.setOpenid(openid);*/
                wxSubscribeUserList = wxSubscribeUserBO.findByCondition(wxSubscribeUserVO);
                if(!CollectionUtils.isEmpty(wxSubscribeUserList)){
                    wxSubscribeUser = wxSubscribeUserList.get(0);

                    String usertagids = wxSubscribeUser.getTagids();

                    String tagidstring = WeiXinUtil.convertArrayToString(tagids);
                    if("".equals(usertagids)){
                        wxSubscribeUser.setTagids(tagidstring.substring(1,tagidstring.length()-1));
                    }else{
                        usertagids = usertagids +","+ tagidstring.substring(1,tagidstring.length()-1);
                        String[] afterRemove = WeiXinUtil.removeSameString(usertagids);
                        String afterRemoveString = WeiXinUtil.convertArrayToString(afterRemove);
                        wxSubscribeUser.setTagids(afterRemoveString.substring(1,afterRemoveString.length()-1));
                    }
                    wxSubscribeUserBO.update(wxSubscribeUser,wxSubscribeUserVO);
                }

            }
            return ResponseMsgUtil.success();
        } catch (WxErrorException e) {
            e.printStackTrace();
            return ResponseMsgUtil.failure();
        }
        //ResponseUtils.writeJsonObject(response, jsonObject);
    }

    /**
     * 批量给用户取消标签
     * @param response
     * @param openids
     * @param tagids
     */
    @RequestMapping(value = "/batch_untag_users", method = {RequestMethod.POST})
    public void batchUnTagForUsers(HttpServletResponse response,
                                   @RequestParam(value = "openids") String[] openids,
                                   @RequestParam(value = "tagids") String[] tagids){
        try {
            for(String tagid : tagids){
                wxService.getUserTagService().batchUntagging(Long.parseLong(tagid),openids);
            }
        } catch (WxErrorException e) {
            e.printStackTrace();
        }
    }

    /**
     * 获得黑名单用户
     * @param session
     * @param response
     * @return
     */
    @RequestMapping(value = "/get_black_list", method = {RequestMethod.GET})
    public Result<WxSubscribeUser> getBlackList(HttpSession session, HttpServletResponse response){
        JSONObject result = new JSONObject();
        String appId = wxService.getWxMpConfigStorage().getAppId();

        Condition appUserVO = null;
        String nickName = "";
        WxSubscribeUser blackUser = new WxSubscribeUser();
        WxMpUserBlacklistGetResult wxMpUserBlacklistGetResult = null;
        List<WxSubscribeUser> wxBlackUserList = new ArrayList<WxSubscribeUser>();
        List<WxSubscribeUser> wxSubscribeUserList = new ArrayList<WxSubscribeUser>();
        try {
            wxMpUserBlacklistGetResult = wxService.getBlackListService().getBlacklist("");
            List<String> openids = wxMpUserBlacklistGetResult.getOpenidList();
            for(String openid : openids){
                appUserVO = new Condition(WxSubscribeUser.class);
                appUserVO.createCriteria().andCondition("appid=",appId).andCondition("openid=",openid);
                /*appUserVO.setAppid(appId);
                appUserVO.setOpenid(openid);*/
                WxSubscribeUser wxBlackUser = new WxSubscribeUser();
                wxBlackUser.setIsblack(1);
                wxSubscribeUserBO.update(wxBlackUser,appUserVO);

                wxSubscribeUserList = wxSubscribeUserBO.findByCondition(appUserVO);
                if(!CollectionUtils.isEmpty(wxSubscribeUserList)){
                    blackUser = wxSubscribeUserList.get(0);

                    nickName = blackUser.getNickname();
                    blackUser.setNickname(WeiXinUtil.convertSqlNameToTrueName(nickName));
                    blackUser.setTagids(convertTagIdsToTagNames(appId, blackUser.getTagids()));
                    wxBlackUserList.add(blackUser);
                }
            }
            return ResponseMsgUtil.success(wxBlackUserList);
        } catch (WxErrorException e) {
            return ResponseMsgUtil.failure();
        }
        //ResponseUtils.writeJsonObject(response, result);
    }

    /**
     * 拉黑用户
     * @param response
     * @param session
     * @param openids
     */
    @RequestMapping(value = "/push_to_black", method = {RequestMethod.POST})
    public Result<WxSubscribeUser> pushToBlacklist(HttpServletResponse response,
                                HttpSession session,
                                @RequestParam(value = "openids")  String[] openids){
        JSONObject result = new JSONObject();
        try {
            List<String> openidList = Arrays.asList(openids);
            wxService.getBlackListService().pushToBlacklist(openidList);

            WxSubscribeUser wxBlackUser = new WxSubscribeUser();
            Condition wxSubscribeUserVO = null;
            for(String openid : openids){
                wxSubscribeUserVO = new Condition(WxSubscribeUser.class);
                wxSubscribeUserVO
                        .createCriteria()
                        .andCondition("appid=",wxService.getWxMpConfigStorage().getAppId())
                        .andCondition("openid=",openid);
                /*wxSubscribeUserVO.setAppid(wxService.getWxMpConfigStorage().getAppId());
                wxSubscribeUserVO.setOpenid(openid);*/
                wxBlackUser.setIsblack(1);
                wxSubscribeUserBO.update(wxBlackUser,wxSubscribeUserVO);
            }
            return ResponseMsgUtil.success();
        } catch (WxErrorException e) {
            e.printStackTrace();
            return ResponseMsgUtil.failure();
        }
        //ResponseUtils.writeJsonObject(response, result);
    }

    /**
     * 取消拉黑用户
     * @param response
     * @param session
     * @param openids
     */
    @RequestMapping(value = "/pull_from_black", method = {RequestMethod.POST})
    public Result<WxSubscribeUser> pullFromBlacklist(HttpServletResponse response,
                                  HttpSession session,
                                  @RequestParam(value = "openids")  String[] openids){
        JSONObject result = new JSONObject();
        try {
            List<String> openidList = Arrays.asList(openids);
            wxService.getBlackListService().pullFromBlacklist(openidList);

            WxSubscribeUser wxBlackUser = new WxSubscribeUser();
            Condition wxSubscribeUserVO = null;
            for(String openid : openids){
                wxSubscribeUserVO = new Condition(WxSubscribeUser.class);
                wxSubscribeUserVO
                        .createCriteria()
                        .andCondition("appid=", wxService.getWxMpConfigStorage().getAppId())
                        .andCondition("openid=", openid);
                wxBlackUser.setIsblack(0);
                wxSubscribeUserBO.update(wxBlackUser,wxSubscribeUserVO);
            }
            return ResponseMsgUtil.success();
        } catch (WxErrorException e) {
            e.printStackTrace();
            return ResponseMsgUtil.failure();
        }
        //ResponseUtils.writeJsonObject(response, result);
    }

    /**
     * 给单个用户修改标签
     * @param response
     * @param session
     * @param openids
     * @param pretagids
     * @param aftertagids
     */
    @RequestMapping(value = "/modify_tag_for_one", method = {RequestMethod.POST})
    public Result<WxSubscribeUser> modifyTagForOne(HttpServletResponse response,
                                HttpSession session,
                                 @RequestParam(value = "openids") String openids,
                                 @RequestParam(value = "pretagids") String[] pretagids,
                                 @RequestParam(value = "aftertagids") String[] aftertagids){
        JSONObject result = new JSONObject();
        String appId = wxService.getWxMpConfigStorage().getAppId();
        try {
            if(pretagids.length == 1 && "isNull".equals(pretagids[0])){
                pretagids = new String[0];
            }
            if(aftertagids.length == 1 && "isNull".equals(aftertagids[0])){
                aftertagids = new String[0];
            }

            String[] sameids = WeiXinUtil.intersect(pretagids,aftertagids);
            String[] addtags = WeiXinUtil.minus(aftertagids,sameids);
            String[] removetags = WeiXinUtil.minus(pretagids,sameids);

            List<WxGroup> wxGroupList = new ArrayList<WxGroup>();
            Integer preCount = 0;
            WxGroup wxGroup = new WxGroup();
            Condition wxGroupVO = null;
            String[] openid = new String[1];
            openid[0] = openids;
            for(String tagid : addtags){
                WeiXinUtil.postUrlByOpenidsAndTagids(wxService, ADD_TAGS_URL, openid,tagid);
                //更新数据库用户分组表的count
                wxGroupVO = new Condition(WxGroup.class);
                wxGroupVO.createCriteria()
                        .andCondition("appid=",appId)
                        .andCondition("groupid=",Integer.parseInt(tagid));
                wxGroupList = wxGroupBO.findByCondition(wxGroupVO);
                if(!CollectionUtils.isEmpty(wxGroupList)){
                    wxGroup = wxGroupList.get(0);
                    preCount = wxGroup.getCount();
                    wxGroup.setCount(preCount+1);
                    wxGroupBO.update(wxGroup,wxGroupVO);
                }
            }

            for(String tagid : removetags){
                WeiXinUtil.postUrlByOpenidsAndTagids(wxService,REMOVE_TAGS_URL,openid,tagid);
                //更新数据库用户分组表的count
                wxGroupVO = new Condition(WxGroup.class);
                wxGroupVO.createCriteria()
                        .andCondition("appid=",appId)
                        .andCondition("groupid=",Integer.parseInt(tagid));
                /*wxGroupVO.setAppid(appId);
                wxGroupVO.setId(Long.parseLong(tagid));*/
                wxGroupList = wxGroupBO.findByCondition(wxGroupVO);
                if(!CollectionUtils.isEmpty(wxGroupList)){
                    wxGroup = wxGroupList.get(0);
                    preCount = wxGroup.getCount();
                    wxGroup.setCount(preCount-1);
                    wxGroupBO.update(wxGroup,wxGroupVO);
                }
            }

            //更新数据库内关注者用户表的tagids
            WxSubscribeUser wxSubscribeUser = new WxSubscribeUser();
            List<WxSubscribeUser> wxSubscribeUserList = new ArrayList<WxSubscribeUser>();
            Condition wxSubscribeUserVO = new Condition(WxSubscribeUser.class);
            wxSubscribeUserVO.createCriteria()
                    .andCondition("appid=",appId)
                    .andCondition("openid=",openids);
            wxSubscribeUserList = wxSubscribeUserBO.findByCondition(wxSubscribeUserVO);
            if(!CollectionUtils.isEmpty(wxSubscribeUserList)){
                wxSubscribeUser = wxSubscribeUserList.get(0);

                String usertagids = wxSubscribeUser.getTagids();

                String tagidstring = WeiXinUtil.convertArrayToString(aftertagids);
                if("".equals(usertagids)){
                    wxSubscribeUser.setTagids(tagidstring.substring(1,tagidstring.length()-1));
                }else{
                    usertagids = tagidstring.substring(1,tagidstring.length()-1);
                    wxSubscribeUser.setTagids(usertagids);
                }
                wxSubscribeUserBO.update(wxSubscribeUser,wxSubscribeUserVO);
            }

            return ResponseMsgUtil.success();
        } catch (WxErrorException e) {
            return ResponseMsgUtil.failure();
        }
        //ResponseUtils.writeJsonObject(response, result);
    }


    public String convertTagIdsToTagNames(String appid, String tagids){
        WxGroupDTO wxGroupVO = new WxGroupDTO();
        List<WxGroup> wxGroupList = new ArrayList<WxGroup>();
        String tagname = "";
        if(!"".equals(tagids)){
            String[] tag = tagids.split(",");
            String[] tagnamelist = new String[tag.length];
            for(int i=0;i<tag.length;i++){
                wxGroupVO.setAppid(appid);
                wxGroupVO.setGroupid(Integer.parseInt(tag[i].trim()));
                wxGroupList = wxGroupBO.find(wxGroupVO);
                if(!CollectionUtils.isEmpty(wxGroupList)){
                    tagname = wxGroupList.get(0).getName();
                    tagnamelist[i] = tagname;
                }
            }
            String tagnamestring = WeiXinUtil.convertArrayToString(tagnamelist);
            return tagnamestring.substring(1,tagnamestring.length()-1);
        }
        return "";
    }
}
