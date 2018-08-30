package com.uhope.rl.application.web.manage;

import com.alibaba.fastjson.JSONObject;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.uhope.common.web.util.ResponseUtils;
import com.uhope.rl.application.constants.Constant;
import com.uhope.rl.application.result.ResponseMsgUtil;
import com.uhope.rl.application.result.Result;
import com.uhope.rl.application.utils.DateUtil;
import com.uhope.rl.base.core.OrderBy;
import com.uhope.rl.wechat.domain.WxAutoReply;
import com.uhope.rl.wechat.domain.WxNewsArticle;
import com.uhope.rl.wechat.dto.WxAutoReplyDTO;
import com.uhope.rl.wechat.dto.WxNewsArticleDTO;
import com.uhope.rl.wechat.service.WxAutoReplyService;
import com.uhope.rl.wechat.service.WxNewsArticleService;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.bean.material.WxMediaImgUploadResult;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.fileupload.disk.DiskFileItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.commons.CommonsMultipartFile;
import tk.mybatis.mapper.entity.Condition;
import tk.mybatis.mapper.entity.Example;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.util.Date;
import java.util.List;

/**
 * Created by zhongjiahui on 2018/1/26.
 */
@RestController
public class AutoReplyController {
    @Autowired
    private WxMpService wxMpService;
    @Autowired
    private WxAutoReplyService wxAutoReplyBO;
    @Autowired
    private WxNewsArticleService wxNewsArticleBO;

    /**
     * 获得自动回复列表
     *
     * @param response
     * @param session
     * @return
     */
    @RequestMapping(value = "/get_auto_reply", method = RequestMethod.GET)
    public Result<WxAutoReply> getAutoReply(HttpServletResponse response, HttpSession session) {
        //WxMpService wxMpService = setWxServiceConfig(session);
        String appid = wxMpService.getWxMpConfigStorage().getAppId();

        WxAutoReplyDTO wxAutoReplyVO = new WxAutoReplyDTO();
        wxAutoReplyVO.setAppid(appid);
        //wxAutoReplyVO.setType(0);
        OrderBy orderBy = new OrderBy();
        orderBy.add("createtime", false);
        Condition condition= new Condition(WxAutoReply.class);
        condition.createCriteria().andCondition("appid=",appid);
        condition.setOrderByClause(orderBy.toString());
        List<WxAutoReply> autoReplyList = wxAutoReplyBO.findByCondition(condition);
        WxNewsArticleDTO wxNewsArticleVO = null;
        List<WxNewsArticle> artList = null;
        String replyType = "";
        for (WxAutoReply autoReply : autoReplyList) {
            replyType = autoReply.getReplytype();
            if (autoReply.getType() == 2 && replyType != null && replyType.equals("news")) {
                wxNewsArticleVO = new WxNewsArticleDTO();
                wxNewsArticleVO.setAppid(appid);
                wxNewsArticleVO.setMediaid(autoReply.getId());
                artList = wxNewsArticleBO.find(wxNewsArticleVO);
                autoReply.setNewsArticleList(artList);
            }
        }
        return ResponseMsgUtil.success(autoReplyList);
        //ResponseUtils.writeJsonObject(response, autoReplyList);
    }

    /**
     * 添加关键词回复
     *
     * @param roleName
     * @param type
     * @param keyword
     * @param content
     * @param id
     * @return
     */
    @RequestMapping(value = "/add_rule", method = RequestMethod.POST)
    public Result<WxAutoReply> addRule(@RequestParam String roleName,
                                       @RequestParam Integer type,
                                       @RequestParam String keyword,
                                       @RequestParam String content,
                                       @RequestParam(required = false) String id) {
        JSONObject result = new JSONObject();
        try {
            //WxMpService wxMpService = setWxServiceConfig(session);
            String appid = wxMpService.getWxMpConfigStorage().getAppId();

            /*System.out.println(ruleString);
            JsonParser jsonParser = new JsonParser();
            JsonObject jsonObject = jsonParser.parse(ruleString).getAsJsonObject();
            String roleName = jsonObject.get("roleName").getAsString();
            Integer type = jsonObject.get("type").getAsInt();
            String keyword = jsonObject.get("keyword").getAsString();
            String content = jsonObject.get("desc").getAsString();*/

            WxAutoReply wxAutoReply = new WxAutoReply();
            wxAutoReply.setAppid(appid);
            wxAutoReply.setType(0);
            wxAutoReply.setName(roleName);
            wxAutoReply.setKeyword(keyword);
            wxAutoReply.setContent(content);
            wxAutoReply.setMode(type);
            wxAutoReply.setReplytype("text");
            wxAutoReply.setCreatetime(new Date());

            if (!StringUtils.isEmpty(id)) {
                wxAutoReply.setId(id);
                wxAutoReplyBO.update(wxAutoReply);
            } else {
                Condition condition = new Condition(WxAutoReply.class);
                condition.createCriteria().andCondition("name=", roleName).orCondition("keyword=",keyword);
                List<WxAutoReply> wxAutoReplies = wxAutoReplyBO.findByCondition(condition);
                //wxAutoReplyBO.findByNameOrKey(roleName,keyword);
                if (CollectionUtils.isEmpty(wxAutoReplies)) {
                    wxAutoReplyBO.insert(wxAutoReply);
                }else{
                    return ResponseMsgUtil.failure("规则名称或关键词重复！");
                }
            }

            return ResponseMsgUtil.success();
        } catch (Exception e) {
            return ResponseMsgUtil.failure();
        }
        //ResponseUtils.writeJsonObject(response, result);
    }

    /**
     * 删除关键词回复
     *
     * @param response
     * @param session
     * @param id
     * @return
     */
    @RequestMapping(value = "/delete_rule", method = RequestMethod.GET)
    public Result<WxAutoReply> deleteRule(HttpServletResponse response, HttpSession session, @RequestParam String id) {
        JSONObject result = new JSONObject();
        try {
            //WxMpService wxMpService = setWxServiceConfig(session);
            String appid = wxMpService.getWxMpConfigStorage().getAppId();

            wxAutoReplyBO.remove(id);

            return ResponseMsgUtil.success();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseMsgUtil.failure();
        }
        //ResponseUtils.writeJsonObject(response, result);
    }

    /**
     * 添加自动回复（未匹配回复、首次关注文字回复）
     *
     * @param response
     * @param session
     * @param content
     * @param type
     * @return
     */
    @RequestMapping(value = "/add_auto_reply", method = RequestMethod.GET)
    public Result<WxAutoReply> addNokeyReply(HttpServletResponse response, HttpSession session,
                                             @RequestParam String content, @RequestParam Integer type) {
        JSONObject result = new JSONObject();
        try {
            //WxMpService wxMpService = setWxServiceConfig(session);
            String appid = wxMpService.getWxMpConfigStorage().getAppId();

            WxAutoReply wxAutoReply = new WxAutoReply();
            wxAutoReply.setAppid(appid);
            wxAutoReply.setType(type);
            wxAutoReply.setContent(content);
            wxAutoReply.setCreatetime(new Date());
            wxAutoReply.setReplytype("text");

            WxAutoReplyDTO wxAutoReplyVO = new WxAutoReplyDTO();
            wxAutoReplyVO.setAppid(appid);
            wxAutoReplyVO.setType(type);
            if (wxAutoReplyBO.count(wxAutoReplyVO) > 0) {
                wxAutoReplyBO.remove(wxAutoReplyVO);
            }
            wxAutoReplyBO.insert(wxAutoReply);

            return ResponseMsgUtil.success();
        } catch (Exception e) {
            return ResponseMsgUtil.failure();
        }
        //ResponseUtils.writeJsonObject(response, result);
    }

    /**
     * 删除自带回复（未匹配回复、首次关注文字回复）
     *
     * @param response
     * @param session
     * @param type
     * @return
     */
    @RequestMapping(value = "/del_auto_reply", method = RequestMethod.GET)
    public Result<WxAutoReply> delAutoReply(HttpServletResponse response, HttpSession session, @RequestParam Integer type) {
        JSONObject result = new JSONObject();
        try {
            //WxMpService wxMpService = setWxServiceConfig(session);
            String appid = wxMpService.getWxMpConfigStorage().getAppId();

            WxAutoReplyDTO wxAutoReplyVO = new WxAutoReplyDTO();
            wxAutoReplyVO.setAppid(appid);
            wxAutoReplyVO.setType(type);

            wxAutoReplyBO.remove(wxAutoReplyVO);

            return ResponseMsgUtil.success();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseMsgUtil.failure();
        }
        //ResponseUtils.writeJsonObject(response, result);
    }

    /**
     * 上传首次关注时的图片到微信服务器
     *
     * @param response
     * @param session
     * @param uploadFile
     * @return
     */
    @RequestMapping(value = "/upload_news_reply", method = {RequestMethod.POST})
    public Result<String> uploadNewsReply(HttpServletResponse response,
                                          HttpSession session,
                                          @RequestParam(value = "uploadFile", required = true) MultipartFile uploadFile) {
        JSONObject jsonObject = new JSONObject();
        try {
            //WxMpService wxMpService = setWxServiceConfig(session);
            /*CommonsMultipartFile cf= (CommonsMultipartFile)uploadFile;
            DiskFileItem fi = (DiskFileItem)cf.getFileItem();*/
            String originFileName = uploadFile.getOriginalFilename();

            //String fileName = fi.getName().split("\\.")[0];
            String fileName = DateUtil.formatDate(new Date(), DateUtil.DATE_yyyyMMddHHmmss);
            String fileType = originFileName.split("\\.")[1];
            File f = null;
            f = File.createTempFile(fileName, "." + fileType);
            uploadFile.transferTo(f);
            f.deleteOnExit();

            WxMediaImgUploadResult result = wxMpService.getMaterialService().mediaImgUpload(f);

            return ResponseMsgUtil.success(result.getUrl());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseMsgUtil.failure();
        }
        //ResponseUtils.writeJsonObject(response, jsonObject);
    }

    /**
     * 添加首次关注的图文消息回复
     *
     * @param response
     * @param session
     * @param artString
     * @return
     */
    @RequestMapping(value = "/add_news_reply", method = RequestMethod.POST)
    public Result<String> addNewsReply(HttpServletResponse response, HttpSession session, @RequestBody String artString) {
        JSONObject result = new JSONObject();
        try {
            //WxMpService wxMpService = setWxServiceConfig(session);
            String appid = wxMpService.getWxMpConfigStorage().getAppId();

            WxAutoReply wxAutoReply = null;
            WxAutoReplyDTO wxAutoReplyVO = new WxAutoReplyDTO();
            wxAutoReplyVO.setAppid(appid);
            wxAutoReplyVO.setType(2);
            if (wxAutoReplyBO.count(wxAutoReplyVO) > 0) {
                wxAutoReplyBO.remove(wxAutoReplyVO);
            }
            wxAutoReply = new WxAutoReply();
            wxAutoReply.setAppid(appid);
            wxAutoReply.setType(2);
            //wxAutoReply.setKeyword("isNewsReply");
            wxAutoReply.setReplytype("news");
            wxAutoReply.setCreatetime(new Date());
            wxAutoReplyBO.insert(wxAutoReply);

            JsonParser jsonParser = new JsonParser();
            JsonArray jsonArray = jsonParser.parse(artString).getAsJsonArray();
            JsonObject artJson = null;
            WxNewsArticle wxNewsArticle = null;
            for (int i = 0; i < jsonArray.size(); i++) {
                artJson = jsonArray.get(i).getAsJsonObject();

                wxNewsArticle = new WxNewsArticle();
                wxNewsArticle.setAppid(appid);
                wxNewsArticle.setTitle(artJson.get("title").getAsString());
                wxNewsArticle.setThumburl(artJson.get("picurl").getAsString());
                wxNewsArticle.setUrl(artJson.get("url").getAsString());
                wxNewsArticle.setDigest(artJson.get("desc").getAsString());
                wxNewsArticle.setMediaid(wxAutoReply.getId());

                wxNewsArticleBO.insert(wxNewsArticle);
            }
            return ResponseMsgUtil.success();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseMsgUtil.failure();
        }
        //ResponseUtils.writeJsonObject(response, result);
    }
}
