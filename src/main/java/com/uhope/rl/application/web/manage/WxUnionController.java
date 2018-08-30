package com.uhope.rl.application.web.manage;

import com.alibaba.fastjson.JSONObject;
import com.uhope.common.web.util.ResponseUtils;
import com.uhope.rl.application.utils.DateUtil;
import com.uhope.rl.application.utils.FileUploadUtil;
import com.uhope.rl.base.core.OrderBy;
import com.uhope.rl.wechat.domain.WxUnion;
import com.uhope.rl.wechat.service.WxUnionService;
import com.uhope.base.result.ResponseMsgUtil;
import com.uhope.base.result.Result;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import me.chanjar.weixin.mp.api.WxMpService;
import org.apache.commons.fileupload.disk.DiskFileItem;
import org.springframework.web.bind.annotation.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.commons.CommonsMultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.lang.String;

/**
 * 微信联盟-Controller类
 *
 * @author zdp on 2018/07/16
 */
@RestController
@RequestMapping("/wxUnion")
public class WxUnionController {
    @Autowired
    private WxUnionService wxUnionService;
    @Autowired
    private WxMpService wxService;


    @PostMapping("/add")

    public Result<WxUnion> add(String title, String description, String thumb, String qrcode) {
        try {
            String appid = wxService.getWxMpConfigStorage().getAppId();
            WxUnion wxUnion = new WxUnion();
            wxUnion.setAppid(appid);
            wxUnion.setTitle(title);
            wxUnion.setThumb(thumb);
            wxUnion.setQrcode(qrcode);
            wxUnion.setDescription(description);
            wxUnion.setCreatetime(new Date());
            wxUnionService.insert(wxUnion);
            return ResponseMsgUtil.success(wxUnion);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseMsgUtil.failure("failure");
        }
    }


    @PostMapping("/delete")
    public Result delete(@RequestParam String id) {
        wxUnionService.remove(id);
        return ResponseMsgUtil.success(null);
    }


    @PostMapping("/update")
    public Result<WxUnion> update(String id, String title, String description, String thumb, String qrcode) {
        WxUnion wxUnion = new WxUnion();
        wxUnion.setId(id);
        wxUnion.setTitle(title);
        wxUnion.setThumb(thumb);
        wxUnion.setQrcode(qrcode);
        wxUnion.setDescription(description);
        wxUnionService.update(wxUnion);
        return ResponseMsgUtil.success(wxUnion);
    }


    @GetMapping("/detail")
    public Result<WxUnion> detail(@RequestParam String id) {
        WxUnion wxUnion = wxUnionService.get(id);
        return ResponseMsgUtil.success(wxUnion);
    }


    @GetMapping("/list")
    public Result<PageInfo> list(@RequestParam(defaultValue = "1") Integer page, @RequestParam(defaultValue = "50") Integer size) {
        PageHelper.startPage(page, size);
        OrderBy orderBy= new OrderBy();
        orderBy.add("createtime",false);
        List<WxUnion> list = wxUnionService.find(orderBy);
        PageInfo pageInfo = new PageInfo(list);
        return ResponseMsgUtil.success(pageInfo);
    }

    @ResponseBody
    @RequestMapping(value = "/coverUpload", method = {RequestMethod.POST})
    public Result<String> coverUpload(HttpServletRequest request, HttpServletResponse response, HttpSession session, @RequestParam(value = "uploadFile", required = true)
    MultipartFile uploadFile) {
        String originFileName = uploadFile.getOriginalFilename();
        String fileName = originFileName.split("\\.")[0];
        if (fileName.length() < 3) {
            fileName = fileName + DateUtil.formatDate(new Date(), DateUtil.DATE_yyyyMMddHHmmss);
        }

        File f = null;
        try {
            String fileType = originFileName.split("\\.")[1];
            f = File.createTempFile(fileName, "." + fileType);
            uploadFile.transferTo(f);
            f.deleteOnExit();
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseMsgUtil.failure("failure");
        }
        try {
            String returnUrl = FileUploadUtil.upload(f)[0];
            return ResponseMsgUtil.success(returnUrl);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseMsgUtil.failure("failure");
        }
    }
}
