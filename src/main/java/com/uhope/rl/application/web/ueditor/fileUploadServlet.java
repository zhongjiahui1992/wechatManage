package com.uhope.rl.application.web.ueditor;

import com.alibaba.fastjson.JSONObject;
import com.uhope.common.web.util.ResponseUtils;
import com.uhope.rl.application.utils.DateUtil;
import me.chanjar.weixin.common.exception.WxErrorException;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.bean.material.WxMediaImgUploadResult;
import org.apache.commons.fileupload.disk.DiskFileItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.commons.CommonsMultipartFile;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.Iterator;

/**
 * Created by zhongjiahui on 2017/6/23.
 */
@RestController
public class fileUploadServlet {
    private static final long serialVersionUID = 1L;

    @Autowired
    private WxMpService wxService;
    /*@Autowired
    private EhAccessoryBO ehAccessoryBO;*/

    /*public fileUploadServlet() {
        super();
        // TODO Auto-generated constructor stub
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // TODO Auto-generated method stub
    }*/

    /**
     * 富文本编辑器上传文件
     * @param request
     * @param response
     * @return
     * @throws ServletException
     * @throws IOException
     */
    @RequestMapping(value = "/fileUploadServlet")
    protected JSONObject doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        JSONObject jsonObject = new JSONObject();

        String queryString = request.getQueryString();
        CommonsMultipartResolver multipartResolver=new CommonsMultipartResolver(request.getSession().getServletContext());
        //检查form中是否有enctype="multipart/form-data"
        if(multipartResolver.isMultipart(request)) {
            //将request变成多部分request
            MultipartHttpServletRequest multiRequest = (MultipartHttpServletRequest)request;
            //获取multiRequest 中所有的文件名
            Iterator<String> it = multiRequest.getFileNames();
            //遍历文件
            while(it.hasNext()) {
                MultipartFile file=multiRequest.getFile(it.next().toString());
                String originFileName = file.getOriginalFilename();
                /*CommonsMultipartFile cf= (CommonsMultipartFile)file;
                DiskFileItem fi = (DiskFileItem)cf.getFileItem();*/

                String fileName = originFileName.split("\\.")[0];
                if(fileName.length() < 3){
                    fileName = fileName + DateUtil.formatDate(new Date(), DateUtil.DATE_yyyyMMddHHmmss);
                }
                //String fileName = DateUtil.formatDate(new Date(), DateUtil.DATE_yyyyMMddHHmmss);
                String fileType = originFileName.split("\\.")[1];
                File f = null;
                try {
                    f = File.createTempFile(fileName, "."+fileType);
                    file.transferTo(f);
                    f.deleteOnExit();
                } catch (IOException e) {
                    e.printStackTrace();
                    jsonObject.put("state","error");
                    jsonObject.put("url",e.getMessage());
                }
                WxMediaImgUploadResult wxMediaImgUploadResult = new WxMediaImgUploadResult();
                try {
                    String returnUrl = "";
                    String albumid = "";
                    if(queryString!=null && queryString.indexOf("isNotice")!=-1){
                        /*String[] urls = FileUploadUtil.upload(f);
                        returnUrl = "http://" + Constants.NGINX_SERVER + "/" + urls[0];
                        EhAccessory ehAccessory = new EhAccessory();
                        ehAccessory.setAccessoryname(fileName);
                        ehAccessory.setAccessoryurl(urls[0]);
                        ehAccessory.setCreatetime(new Date());
                        ehAccessory.setEventid("");
                        ehAccessory.setAccessorytype(1);
                        ehAccessoryBO.insert(ehAccessory);
                        albumid = ehAccessory.getId();*/
                    }else{
                        wxMediaImgUploadResult = wxService.getMaterialService().mediaImgUpload(f);
                        returnUrl = wxMediaImgUploadResult.getUrl();
                    }
                    jsonObject.put("state","SUCCESS");
                    jsonObject.put("url",returnUrl);
                    jsonObject.put("albumid", albumid);
                } catch (WxErrorException e) {
                    e.printStackTrace();
                    jsonObject.put("state","error");
                    jsonObject.put("url",e.getMessage());
                }
                jsonObject.put("original",originFileName);
                jsonObject.put("size",file.getSize());
                jsonObject.put("title",originFileName);
                jsonObject.put("type",fileType);
                //ResponseUtils.writeJsonObject(response,jsonObject);
            }
        }
        return jsonObject;

        /*FileItemStream fileStream = null;
        ServletFileUpload upload = new ServletFileUpload(
                new DiskFileItemFactory());
        try {
            FileItemIterator iterator = upload.getItemIterator(request);
            while (iterator.hasNext()) {
                fileStream = iterator.next();

                if (!fileStream.isFormField())
                    break;
                fileStream = null;
            }
        } catch (FileUploadException e) {
            e.printStackTrace();
        }
        File f = null;
        String fileName = fileStream.getName().split("\\.")[0];
        String fileType = fileStream.getName().split("\\.")[1];
        f = File.createTempFile(fileName, "."+fileType);
        f.deleteOnExit();

        WxMediaImgUploadResult wxMediaImgUploadResult = new WxMediaImgUploadResult();
        try {
            wxMediaImgUploadResult = wxService.getMaterialService().mediaImgUpload(f);
            jsonObject.put("state","success");
            jsonObject.put("url",wxMediaImgUploadResult.getUrl());
        } catch (WxErrorException e) {
            e.printStackTrace();
            jsonObject.put("state","error");
            jsonObject.put("url",e.getMessage());
        }
        jsonObject.put("original",fileStream.getName());
        jsonObject.put("size",1024);
        jsonObject.put("title",fileStream.getName());
        jsonObject.put("type",fileType);

        ResponseUtils.writeJsonObject(response,jsonObject);*/
    }
}
