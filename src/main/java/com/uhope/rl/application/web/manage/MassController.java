package com.uhope.rl.application.web.manage;

import com.alibaba.fastjson.JSONObject;
import com.uhope.rl.application.result.ResponseMsgUtil;
import com.uhope.rl.application.result.Result;
import com.uhope.rl.application.utils.DateUtil;
import com.uhope.rl.wechat.domain.WxSubscribeUser;
import com.uhope.rl.wechat.dto.WxSubscribeUserDTO;
import com.uhope.rl.wechat.service.WxSubscribeUserService;
import me.chanjar.weixin.common.bean.result.WxMediaUploadResult;
import me.chanjar.weixin.common.exception.WxErrorException;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.bean.WxMpMassOpenIdsMessage;
import me.chanjar.weixin.mp.bean.WxMpMassTagMessage;
import me.chanjar.weixin.mp.bean.WxMpMassVideo;
import me.chanjar.weixin.mp.bean.result.WxMpMassSendResult;
import me.chanjar.weixin.mp.bean.result.WxMpMassUploadResult;
import org.apache.commons.fileupload.disk.DiskFileItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.commons.CommonsMultipartFile;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by zhongjiahui on 2017/6/6.
 */
@RestController
public class MassController{
    private static final Logger LOGGER = LoggerFactory
            .getLogger(MassController.class);

    @Autowired
    private WxMpService wxService;
    @Autowired
    private WxSubscribeUserService wxSubscribeUserBO;


    /**
     * 上传临时素材作为群发材料
     * @param response
     * @param session
     * @param uploadFile
     * @param title
     * @param desc
     */
    @RequestMapping(value = "/upload_mass_material", method = {RequestMethod.POST})
    public Result<JSONObject> uploadMaterial(HttpServletResponse response,
                                                 HttpSession session,
                                                 @RequestParam(value = "uploadFile", required = true) MultipartFile uploadFile,
                                                 @RequestParam(value = "title", required = false) String title,
                                                 @RequestParam(value = "desc", required = false) String desc){
        JSONObject jsonObject = new JSONObject();
        try {
            /*CommonsMultipartFile cf= (CommonsMultipartFile)uploadFile;
            DiskFileItem fi = (DiskFileItem)cf.getFileItem();*/
            String originFileName = uploadFile.getOriginalFilename();

            String fileName = originFileName.split("\\.")[0];
            if(fileName.length() < 3){
                fileName = fileName + DateUtil.formatDate(new Date(), DateUtil.DATE_yyyyMMddHHmmss);
            }
            //String fileName = DateUtil.formatDate(new Date(), DateUtil.DATE_yyyyMMddHHmmss);
            String fileType = originFileName.split("\\.")[1];
            File f = null;
            f = File.createTempFile(fileName, "."+fileType);
            uploadFile.transferTo(f);
            f.deleteOnExit();

            String massMediaId = "";
            String type = "image";
            if(uploadFile.getContentType().indexOf("audio") != -1){
                type = "voice";
            }
            if(uploadFile.getContentType().indexOf("video") != -1){
                type = "video";
            }
            WxMediaUploadResult uploadMediaRes = wxService.getMaterialService().mediaUpload(type,f);
            massMediaId = uploadMediaRes.getMediaId();

            if("video".equals(type)){
                // 把视频变成可被群发的媒体
                WxMpMassVideo video = new WxMpMassVideo();
                video.setTitle(title);
                video.setDescription(desc);
                video.setMediaId(uploadMediaRes.getMediaId());
                WxMpMassUploadResult uploadResult = wxService.massVideoUpload(video);
                massMediaId = uploadResult.getMediaId();
            }

            jsonObject.put("filename",fileName+"."+fileType);
            jsonObject.put("massmediaid",massMediaId);
            return ResponseMsgUtil.success(jsonObject);
        } catch (WxErrorException e) {
            LOGGER.error(e.getMessage());
            return ResponseMsgUtil.failure();
        } catch(IOException ioe){
            LOGGER.error(ioe.getMessage());
            return ResponseMsgUtil.failure();
        }
        //ResponseUtils.writeJsonObject(response,jsonObject);
    }

    /**
     * 根据标签进行群发
     * @param response
     * @param session
     * @param massMediaId
     * @param massType
     * @param massContent
     * @param massTag
     */
    @RequestMapping(value = "/send_mass_group", method = {RequestMethod.POST})
    public Result<String> sendMassGroup(HttpServletResponse response,
                              HttpSession session,
                              @RequestParam(value = "massMediaId", required = false) String massMediaId,
                              @RequestParam(value = "massType", required = true) String massType,
                              @RequestParam(value = "massContent", required = false) String massContent,
                              @RequestParam(value = "massTag", required = false) String massTag){
        JSONObject result = new JSONObject();
        WxMpMassTagMessage wxMpMassGroupMessage = new WxMpMassTagMessage();
        WxMpMassSendResult wxMpMassSendResult = new WxMpMassSendResult();
        try {
            wxMpMassGroupMessage.setMediaId(massMediaId);
            wxMpMassGroupMessage.setMsgType(massType);
            wxMpMassGroupMessage.setContent(massContent);
            wxMpMassGroupMessage.setTagId(Long.parseLong(massTag));
            wxMpMassSendResult = wxService.massGroupMessageSend(wxMpMassGroupMessage);

            /*SmWeiXinMsg weiXinMsg = new SmWeiXinMsg();
            weiXinMsg.setType("mass_msg");
            weiXinMsg.setTitle(wxMpMassSendResult.getMsgId());
            weiXinMsg.setAuther(wxMpService.getWxMpConfigStorage().getAppId());
            weiXinMsg.setSendtime(new Date());
            weiXinMsg.setStatus("sending...");*/

            /*org.json.JSONObject jsonObject = new org.json.JSONObject();
            jsonObject.put("msg_id", wxMpMassSendResult.getMsgId());
            String msgResult = wxMpService.post("https://api.weixin.qq.com/cgi-bin/message/mass/get", jsonObject.toString());
            org.json.JSONObject msgresultObj = new org.json.JSONObject(msgResult);
            weiXinMsg.setStatus(msgresultObj.getString("msg_status"));*/
            //smWeiXinMsgBO.insert(weiXinMsg);

            return ResponseMsgUtil.success(wxMpMassSendResult.getErrorMsg());
        } catch (WxErrorException e) {
            e.printStackTrace();
            return ResponseMsgUtil.failure();
        }
        //ResponseUtils.writeJsonObject(response, result);
    }

    /**
     * 根据open id 进行群发
     * @param response
     * @param session
     * @param massMediaId
     * @param massType
     * @param massContent
     * @param massSex
     */
    @RequestMapping(value = "/send_mass_openid", method = {RequestMethod.POST})
    public Result<String> sendMassOpenId(HttpServletResponse response,
                               HttpSession session,
                               @RequestParam(value = "massMediaId", required = false) String massMediaId,
                               @RequestParam(value = "massType", required = true) String massType,
                               @RequestParam(value = "massContent", required = false) String massContent,
                               @RequestParam(value = "massSex", required = false) String massSex){
        JSONObject result = new JSONObject();
        String appId = wxService.getWxMpConfigStorage().getAppId();
        List<String> toUsers = new ArrayList();
        WxMpMassOpenIdsMessage wxMpMassOpenIdsMessage = new WxMpMassOpenIdsMessage();
        WxMpMassSendResult wxMpMassSendResult = new WxMpMassSendResult();
        WxSubscribeUserDTO wxSubscribeUserVO = new WxSubscribeUserDTO();
        List<WxSubscribeUser> subscribeUserList = new ArrayList<WxSubscribeUser>();
        try {
            wxSubscribeUserVO.setAppid(appId);
            if(!"0".equals(massSex)){
                wxSubscribeUserVO.setSex(Integer.parseInt(massSex));
            }
            subscribeUserList = wxSubscribeUserBO.find(wxSubscribeUserVO);
            for(WxSubscribeUser user : subscribeUserList){
                toUsers.add(user.getOpenid());
            }

            wxMpMassOpenIdsMessage.setToUsers(toUsers);
            wxMpMassOpenIdsMessage.setMediaId(massMediaId);
            wxMpMassOpenIdsMessage.setMsgType(massType);
            wxMpMassOpenIdsMessage.setContent(massContent);
            wxMpMassSendResult = wxService.massOpenIdsMessageSend(wxMpMassOpenIdsMessage);

            /*SmWeiXinMsg weiXinMsg = new SmWeiXinMsg();
            weiXinMsg.setType("mass_msg");
            weiXinMsg.setTitle(wxMpMassSendResult.getMsgId());
            weiXinMsg.setAuther(appId);
            weiXinMsg.setSendtime(new Date());
            weiXinMsg.setStatus("sending...");*/

            /*org.json.JSONObject jsonObject = new org.json.JSONObject();
            jsonObject.put("msg_id", wxMpMassSendResult.getMsgId());
            String msgResult = wxMpService.post("https://api.weixin.qq.com/cgi-bin/message/mass/get", jsonObject.toString());
            org.json.JSONObject msgresultObj = new org.json.JSONObject(msgResult);
            weiXinMsg.setStatus(msgresultObj.getString("msg_status"));*/
            //smWeiXinMsgBO.insert(weiXinMsg);

            return ResponseMsgUtil.success(wxMpMassSendResult.getErrorMsg());
        } catch (WxErrorException e) {
            e.printStackTrace();
            return ResponseMsgUtil.failure();
        }
        //ResponseUtils.writeJsonObject(response, result);
    }
}
