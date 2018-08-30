package com.uhope.rl.application.web.manage;

import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.PageInfo;
import com.uhope.rl.application.constants.SysConstant;
import com.uhope.rl.application.result.ResponseMsgUtil;
import com.uhope.rl.application.result.Result;
import com.uhope.rl.application.utils.DateUtil;
import com.uhope.rl.application.utils.FileUploadUtil;
import com.uhope.rl.base.core.OrderBy;
import com.uhope.rl.wechat.domain.WxMaterialFile;
import com.uhope.rl.wechat.domain.WxNewsArticle;
import com.uhope.rl.wechat.dto.WxNewsArticleDTO;
import com.uhope.rl.wechat.service.WxMaterialFileService;
import com.uhope.rl.wechat.service.WxNewsArticleService;
import me.chanjar.weixin.common.exception.WxErrorException;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.bean.material.*;
import org.apache.commons.fileupload.disk.DiskFileItem;
import org.json.JSONArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.commons.CommonsMultipartFile;
import tk.mybatis.mapper.entity.Condition;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Administrator on 2017/6/5.
 */
@RestController
public class MaterialController{
    private static final Logger LOGGER = LoggerFactory
            .getLogger(MaterialController.class);
    @Autowired
    private WxMpService wxService;
    @Autowired
    private WxMaterialFileService wxMaterialFileService;
    @Autowired
    private WxNewsArticleService wxNewsArticleService;


    /**
     * 上传永久素材(image,voice,video,thumb)
     * @param response
     * @param session
     * @param uploadFile
     * @param title
     * @param desc
     */
    @RequestMapping(value = "/upload_material", method = {RequestMethod.POST})
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
            System.out.println("name:" + originFileName);
            /*originFileName = new String(originFileName.getBytes("ISO-8859-1"), "UTF-8");
            System.out.println("originFileName:" + originFileName);*/

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

            WxMpMaterial wxMpMaterial = new WxMpMaterial();
            wxMpMaterial.setName(fileName);
            wxMpMaterial.setFile(f);

            String type = "image";
            if(uploadFile.getContentType().indexOf("audio") != -1){
                type = "voice";
            }
            if(uploadFile.getContentType().indexOf("video") != -1){
                type = "video";
                wxMpMaterial.setVideoTitle(title);
                wxMpMaterial.setVideoIntroduction(desc);
            }
            WxMpMaterialUploadResult wxMpMaterialUploadResult = wxService.getMaterialService().materialFileUpload(type,wxMpMaterial);
            WxMaterialFile wxMaterialFile = new WxMaterialFile();
            wxMaterialFile.setAppid(wxService.getWxMpConfigStorage().getAppId());
            wxMaterialFile.setMediaid(wxMpMaterialUploadResult.getMediaId());
            wxMaterialFile.setType(type);
            wxMaterialFile.setName(fileName+"."+fileType);
            wxMaterialFile.setUrl(wxMpMaterialUploadResult.getUrl());
            wxMaterialFile.setUpdatetime(new Date());

            String dfsUrl = FileUploadUtil.upload(f)[0];
            wxMaterialFile.setDfsUrl(dfsUrl);

            if("video".equals(type)){
                WxMpMaterialVideoInfoResult videoInfo = wxService.getMaterialService().materialVideoInfo(wxMpMaterialUploadResult.getMediaId());
                wxMaterialFile.setVideotitle(title);
                wxMaterialFile.setVideointroduction(desc);
                wxMaterialFile.setUrl(videoInfo.getDownUrl());
            }

            wxMaterialFileService.insert(wxMaterialFile);
            jsonObject.put("name",fileName+"."+fileType);
            jsonObject.put("mediaid",wxMaterialFile.getMediaid());
            jsonObject.put("url",wxMaterialFile.getUrl());
            //jsonObject.put("dfsurl", "http://" + SysConstant.FDFS_NGINXSERVER + "/" + dfsUrl);
            jsonObject.put("dfsurl", dfsUrl);
            jsonObject.put("updatetime",wxMaterialFile.getUpdatetime());
            jsonObject.put("videotitle",wxMaterialFile.getVideotitle());
            jsonObject.put("videointroduction",wxMaterialFile.getVideointroduction());
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
     * 获取其他素材列表（图片，语音，视频）
     * @param response
     * @param materialtype
     * @return
     */
    @RequestMapping(value = "/get_materials_files", method = {RequestMethod.GET})
    public Result<WxMaterialFile> getMaterialsFiles(HttpServletResponse response, String materialtype,
                                                    @RequestParam(defaultValue = "0") int pageNumber,
                                                    @RequestParam(defaultValue = "10") int pageSize){
        JSONObject result = new JSONObject();

        String appId = wxService.getWxMpConfigStorage().getAppId();

        Condition wxMaterialFileVO = new Condition(WxMaterialFile.class);
        wxMaterialFileVO.createCriteria().andCondition("appid=",appId).andCondition("type=",materialtype);
        OrderBy orderBy = new OrderBy();
        orderBy.add("updatetime", false);
       /* wxMaterialFileVO.setAppid(appId);
        wxMaterialFileVO.setType(materialtype);*/
        PageInfo<WxMaterialFile> materialFilePageInfo = new PageInfo<>();
        List<WxMaterialFile> materialList = new ArrayList<WxMaterialFile>();
        if(wxMaterialFileService.count(wxMaterialFileVO) > 0){
            materialFilePageInfo = wxMaterialFileService.findByCondition(wxMaterialFileVO,orderBy,pageSize,pageNumber);
            //materialList = materialFilePageInfo.getList();
            return ResponseMsgUtil.success(materialFilePageInfo);
        }else{
            WxMaterialFile wxMaterialFile = new WxMaterialFile();
            WxMpMaterialFileBatchGetResult wxGetResult = new WxMpMaterialFileBatchGetResult();
            try {
                int offset = 0;
                wxGetResult = wxService.getMaterialService().materialFileBatchGet(materialtype,0,20);
                List<WxMpMaterialFileBatchGetResult.WxMaterialFileBatchGetNewsItem> list = wxGetResult.getItems();
                int totalNum = wxGetResult.getTotalCount();
                int number = (int) Math.floor(totalNum/20);
                if(number > 0){
                    for(int i=0;i<number;i++){
                        offset = offset + 20;
                        wxGetResult = wxService.getMaterialService().materialFileBatchGet(materialtype,offset,20);
                        List<WxMpMaterialFileBatchGetResult.WxMaterialFileBatchGetNewsItem> listAfter = wxGetResult.getItems();
                        list.addAll(listAfter);
                    }
                }

                for(WxMpMaterialFileBatchGetResult.WxMaterialFileBatchGetNewsItem i : list){
                    wxMaterialFile.setAppid(appId);
                    wxMaterialFile.setMediaid(i.getMediaId());
                    wxMaterialFile.setType(materialtype);
                    wxMaterialFile.setName(i.getName());
                    wxMaterialFile.setUrl(i.getUrl());
                    wxMaterialFile.setUpdatetime(i.getUpdateTime());
                    if("video".equals(materialtype)){
                        WxMpMaterialVideoInfoResult videoInfo = wxService.getMaterialService().materialVideoInfo(i.getMediaId());
                        wxMaterialFile.setVideotitle(videoInfo.getTitle());
                        wxMaterialFile.setVideointroduction(videoInfo.getDescription());
                        wxMaterialFile.setUrl(videoInfo.getDownUrl());
                    }
                    wxMaterialFileService.insert(wxMaterialFile);
                    materialList.add(wxMaterialFile);
                }
                materialFilePageInfo = wxMaterialFileService.findByCondition(wxMaterialFileVO,orderBy,pageSize,pageNumber);
                //materialList = materialFilePageInfo.getList();
                return ResponseMsgUtil.success(materialFilePageInfo);
            } catch (WxErrorException e) {
                e.printStackTrace();
                return ResponseMsgUtil.failure();
            }
        }
        //ResponseUtils.writeJsonObject(response, result);
    }

    /**
     * 上传永久图文素材
     * @param response
     * @param session
     * @param wxNewsArticleString
     */
    @RequestMapping(value = "/upload_news", method = {RequestMethod.POST})
    public Result<String> uploadNews(HttpServletResponse response,
                           HttpSession session,
                             @RequestBody String wxNewsArticleString){
        String result = "";
        WxMpMaterialNews wxMpMaterialNewsMultiple = new WxMpMaterialNews();

        WxMpMaterialNews.WxMpMaterialNewsArticle articleOne = null;
        List<WxMpMaterialNews.WxMpMaterialNewsArticle> articles = new ArrayList();
        JSONArray jsonArray = new JSONArray(wxNewsArticleString);
        for(int i=0;i<jsonArray.length();i++){
            articleOne = new WxMpMaterialNews.WxMpMaterialNewsArticle();
            org.json.JSONObject art = jsonArray.getJSONObject(i);

            articleOne.setTitle(art.getString("title"));
            articleOne.setAuthor(art.getString("author"));
            articleOne.setThumbMediaId(art.getString("thumb_media_id"));
            articleOne.setContent(art.getString("content"));
            articleOne.setShowCoverPic(false);
            articleOne.setDigest(art.getString("digest"));

            wxMpMaterialNewsMultiple.addArticle(articleOne);
        }

        try {
            WxMpMaterialUploadResult wxMpMaterialUploadResult = wxService.getMaterialService().materialNewsUpload(wxMpMaterialNewsMultiple);
            String newsMediaId = wxMpMaterialUploadResult.getMediaId();
            String currentAppid = wxService.getWxMpConfigStorage().getAppId();

            WxMpMaterialNews materialNewsInfo = wxService.getMaterialService().materialNewsInfo(newsMediaId);
            articles = materialNewsInfo.getArticles();
            for(WxMpMaterialNews.WxMpMaterialNewsArticle a : articles){
                WxNewsArticle wxNewsArticle = new WxNewsArticle();
                wxNewsArticle.setAppid(currentAppid);
                wxNewsArticle.setMediaid(newsMediaId);
                wxNewsArticle.setThumbmediaid(a.getThumbMediaId());
                wxNewsArticle.setThumburl(a.getThumbUrl());
                wxNewsArticle.setTitle(a.getTitle());
                wxNewsArticle.setAuthor(a.getAuthor());
                wxNewsArticle.setContent(a.getContent());
                wxNewsArticle.setContentsourceurl(a.getContentSourceUrl());
                wxNewsArticle.setDigest(a.getDigest());
                wxNewsArticle.setShowcoverpic(a.isShowCoverPic()?1:0);
                wxNewsArticle.setUrl(a.getUrl());
                wxNewsArticleService.insert(wxNewsArticle);
            }

            WxMaterialFile wxMaterialFile = new WxMaterialFile();
            wxMaterialFile.setAppid(currentAppid);
            wxMaterialFile.setMediaid(newsMediaId);
            wxMaterialFile.setType("news");
            wxMaterialFile.setUpdatetime(new Date());
            wxMaterialFileService.insert(wxMaterialFile);

            return ResponseMsgUtil.success();
        } catch (WxErrorException e) {
            e.printStackTrace();
            return ResponseMsgUtil.failure();
        }
        //ResponseUtils.writeText(response,result);
    }

    /**
     * 获取图文消息素材列表
     * @param response
     * @param materialtype
     * @return
     */
    @RequestMapping(value = "/get_materials_news", method = {RequestMethod.GET})
    public Result<WxMaterialFile> getMaterialsNews(HttpServletResponse response, String materialtype,
                                                   @RequestParam(defaultValue = "0") int pageNumber,
                                                   @RequestParam(defaultValue = "10") int pageSize){
        JSONObject result = new JSONObject();

        String wxAppid = wxService.getWxMpConfigStorage().getAppId();

        materialtype = "news";
        Condition wxMaterialFileVO = new Condition(WxMaterialFile.class);
        wxMaterialFileVO.createCriteria().andCondition("appid=",wxAppid).andCondition("type=", materialtype);
        OrderBy orderBy = new OrderBy();
        orderBy.add("updatetime", false);
        /*wxMaterialFileVO.setAppid(wxAppid);
        wxMaterialFileVO.setType("news");*/
        List<WxMaterialFile> materialList = new ArrayList<WxMaterialFile>();
        List<WxNewsArticle> wxNewsArticleList = new ArrayList<WxNewsArticle>();
        PageInfo<WxMaterialFile> materialFilePageInfo = new PageInfo<WxMaterialFile>();
        if(wxMaterialFileService.count(wxMaterialFileVO) > 0){
            materialFilePageInfo = wxMaterialFileService.findByCondition(wxMaterialFileVO,orderBy,pageSize,pageNumber);
            materialList = materialFilePageInfo.getList();
            WxNewsArticleDTO wxNewsArticleVO = new WxNewsArticleDTO();
            for(WxMaterialFile news : materialList){
                //wxNewsArticleVO.createCriteria().andCondition("appid=",wxAppid).andCondition("mediaid=",news.getMediaid());
                wxNewsArticleVO.setAppid(wxAppid);
                wxNewsArticleVO.setMediaid(news.getMediaid());
                wxNewsArticleList = wxNewsArticleService.find(wxNewsArticleVO);
                news.setNewsArticleList(wxNewsArticleList);
            }
            return ResponseMsgUtil.success(materialList);
        }else{
            WxMaterialFile wxMaterialFile = null;
            WxMpMaterialNewsBatchGetResult wxNewsBatchGetResult = new WxMpMaterialNewsBatchGetResult();
            try {
                int offset = 0;
                wxNewsBatchGetResult = wxService.getMaterialService().materialNewsBatchGet(0,20);
                List<WxMpMaterialNewsBatchGetResult.WxMaterialNewsBatchGetNewsItem> list = wxNewsBatchGetResult.getItems();
                int totalNum = wxNewsBatchGetResult.getTotalCount();
                int number = (int) Math.floor(totalNum/20);
                if(number > 0){
                    for(int i=0;i<number;i++){
                        offset = offset + 20;
                        wxNewsBatchGetResult = wxService.getMaterialService().materialNewsBatchGet(offset,20);
                        List<WxMpMaterialNewsBatchGetResult.WxMaterialNewsBatchGetNewsItem> listAfter = wxNewsBatchGetResult.getItems();
                        list.addAll(listAfter);
                    }
                }

                for(WxMpMaterialNewsBatchGetResult.WxMaterialNewsBatchGetNewsItem i : list){
                    wxMaterialFile = new WxMaterialFile();
                    wxMaterialFile.setAppid(wxAppid);
                    wxMaterialFile.setMediaid(i.getMediaId());
                    wxMaterialFile.setType("news");
                    wxMaterialFile.setUpdatetime(i.getUpdateTime());
                    wxMaterialFileService.insert(wxMaterialFile);

                    WxMpMaterialNews wxNews = i.getContent();
                    List<WxMpMaterialNews.WxMpMaterialNewsArticle> articles = wxNews.getArticles();
                    wxNewsArticleList = new ArrayList<WxNewsArticle>();
                    for(WxMpMaterialNews.WxMpMaterialNewsArticle a : articles){
                        WxNewsArticle wxNewsArticle = new WxNewsArticle();
                        wxNewsArticle.setAppid(wxAppid);
                        wxNewsArticle.setMediaid(i.getMediaId());
                        wxNewsArticle.setThumbmediaid(a.getThumbMediaId());
                        wxNewsArticle.setThumburl(a.getThumbUrl());
                        wxNewsArticle.setAuthor(a.getAuthor());
                        wxNewsArticle.setTitle(a.getTitle());
                        wxNewsArticle.setContentsourceurl(a.getContentSourceUrl());
                        wxNewsArticle.setContent(a.getContent());
                        wxNewsArticle.setDigest(a.getDigest());
                        wxNewsArticle.setShowcoverpic(a.isShowCoverPic()?1:0);
                        wxNewsArticle.setUrl(a.getUrl());
                        wxNewsArticleService.insert(wxNewsArticle);
                        //wxNewsArticleList.add(wxNewsArticle);
                    }
                    //wxMaterialFile.setNewsArticleList(wxNewsArticleList);
                    //materialList.add(wxMaterialFile);
                }
                //分页查询
                materialFilePageInfo = wxMaterialFileService.findByCondition(wxMaterialFileVO,orderBy,pageSize,pageNumber);
                materialList = materialFilePageInfo.getList();
                WxNewsArticleDTO wxNewsArticleVO = new WxNewsArticleDTO();
                for(WxMaterialFile news : materialList){
                    //wxNewsArticleVO.createCriteria().andCondition("appid=",wxAppid).andCondition("mediaid=",news.getMediaid());
                    wxNewsArticleVO.setAppid(wxAppid);
                    wxNewsArticleVO.setMediaid(news.getMediaid());
                    wxNewsArticleList = wxNewsArticleService.find(wxNewsArticleVO);
                    news.setNewsArticleList(wxNewsArticleList);
                }
                return ResponseMsgUtil.success(materialList);
            } catch (WxErrorException e) {
                e.printStackTrace();
                return ResponseMsgUtil.failure();
            }
        }
        //ResponseUtils.writeJsonObject(response, result);
    }

    /**
     * 获取单个图文素材的详细信息
     * @param response
     * @param session
     * @param newsMediaId
     * @return
     */
    @RequestMapping(value = "/get_one_news", method = {RequestMethod.POST})
    public Result<WxMaterialFile> getOneNews(HttpServletResponse response,
                                     HttpSession session,
                                     @RequestParam(value = "newsMediaId", required = false) String newsMediaId){
        JSONObject result = new JSONObject();
        try{
            String currAppid = wxService.getWxMpConfigStorage().getAppId();

            Condition wxMaterialFileVO = new Condition(WxMaterialFile.class);
            wxMaterialFileVO.createCriteria()
                    .andCondition("appid=",currAppid)
                    .andCondition("mediaid=",newsMediaId)
                    .andCondition("type=","news");
            /*wxMaterialFileVO.setAppid(currAppid);
            wxMaterialFileVO.setMediaid(newsMediaId);
            wxMaterialFileVO.setType("news");*/
            List<WxMaterialFile> wxMaterialFileList = wxMaterialFileService.findByCondition(wxMaterialFileVO);
            if(!CollectionUtils.isEmpty(wxMaterialFileList)){
                WxMaterialFile wxMaterialFile = wxMaterialFileList.get(0);

                Condition wxNewsArticleVO = new Condition(WxNewsArticle.class);
                wxNewsArticleVO.createCriteria().andCondition("appid=", currAppid).andCondition("mediaid=", newsMediaId);
           /* wxNewsArticleVO.setAppid(currAppid);
            wxNewsArticleVO.setMediaid(newsMediaId);*/
                List<WxNewsArticle> newsArticles = wxNewsArticleService.findByCondition(wxNewsArticleVO);

                wxMaterialFile.setNewsArticleList(newsArticles);

                return ResponseMsgUtil.success(wxMaterialFile);
            }else{
                return ResponseMsgUtil.failure("无效mediaid！");
            }

        }catch (Exception e){
            e.printStackTrace();
            return ResponseMsgUtil.failure();
        }
        //ResponseUtils.writeJsonObject(response, result);
    }

    /**
     * 获取单个文章的详情
     * @param id
     * @return
     */
    @RequestMapping(value = "/get_one_art", method = {RequestMethod.GET})
    public Result<WxNewsArticle> getOneArt(@RequestParam String id){
        try{
            WxNewsArticle wxNewsArticle = wxNewsArticleService.get(id);
            return ResponseMsgUtil.success(wxNewsArticle);
        }catch (Exception e){
            e.printStackTrace();
            return ResponseMsgUtil.exception();
        }
    }

    /**
     * 根据mediaid更新图文消息素材
     * @param response
     * @param session
     * @param mediaid
     * @param wxNewsArticleString
     */
    @RequestMapping(value = "/update_news/{mediaid}", method = {RequestMethod.POST})
    public Result<String> updateNews(HttpServletResponse response,
                           HttpSession session,
                           @PathVariable("mediaid") String mediaid,
                           @RequestBody String wxNewsArticleString){
        JSONObject result = new JSONObject();
        String appId = wxService.getWxMpConfigStorage().getAppId();
        try {
            WxMpMaterialNews.WxMpMaterialNewsArticle articleOne = null;
            WxMpMaterialArticleUpdate wxMpMaterialArticleUpdate = null;
            JSONArray jsonArray = new JSONArray(wxNewsArticleString);
            for(int i=0;i<jsonArray.length();i++){
                articleOne = new WxMpMaterialNews.WxMpMaterialNewsArticle();
                org.json.JSONObject art = jsonArray.getJSONObject(i);

                articleOne.setTitle(art.getString("title"));
                articleOne.setAuthor(art.getString("author"));
                articleOne.setThumbMediaId(art.getString("thumb_media_id"));
                articleOne.setContent(art.getString("content"));
                articleOne.setDigest(art.getString("digest"));

                wxMpMaterialArticleUpdate = new WxMpMaterialArticleUpdate();
                wxMpMaterialArticleUpdate.setMediaId(mediaid);
                wxMpMaterialArticleUpdate.setIndex(i);
                wxMpMaterialArticleUpdate.setArticles(articleOne);

                wxService.getMaterialService().materialNewsUpdate(wxMpMaterialArticleUpdate);
            }

            Condition wxNewsArticleVO = new Condition(WxNewsArticle.class);
            wxNewsArticleVO.createCriteria().andCondition("appid=",appId).andCondition("mediaid=",mediaid);
            /*wxNewsArticleVO.setAppid(appId);
            wxNewsArticleVO.setMediaid(mediaid);*/
            wxNewsArticleService.remove(wxNewsArticleVO);

            WxMpMaterialNews materialNewsInfo = wxService.getMaterialService().materialNewsInfo(mediaid);
            List<WxMpMaterialNews.WxMpMaterialNewsArticle> articles = materialNewsInfo.getArticles();
            for(WxMpMaterialNews.WxMpMaterialNewsArticle a : articles){
                WxNewsArticle wxNewsArticle = new WxNewsArticle();
                wxNewsArticle.setAppid(appId);
                wxNewsArticle.setMediaid(mediaid);
                wxNewsArticle.setThumbmediaid(a.getThumbMediaId());
                wxNewsArticle.setThumburl(a.getThumbUrl());
                wxNewsArticle.setTitle(a.getTitle());
                wxNewsArticle.setAuthor(a.getAuthor());
                wxNewsArticle.setContent(a.getContent());
                wxNewsArticle.setContentsourceurl(a.getContentSourceUrl());
                wxNewsArticle.setDigest(a.getDigest());
                wxNewsArticle.setShowcoverpic(a.isShowCoverPic()?1:0);
                wxNewsArticle.setUrl(a.getUrl());
                wxNewsArticleService.insert(wxNewsArticle);
            }
            return ResponseMsgUtil.success();
        } catch (WxErrorException e) {
            e.printStackTrace();
            return ResponseMsgUtil.failure();
        }
        //ResponseUtils.writeJsonObject(response, result);
    }

    /**
     * 根据mediaid删除素材
     * @param response
     * @param session
     * @param mediaid
     */
    @RequestMapping(value = "/delete_material", method = {RequestMethod.GET})
    public Result<String> deleteMaterial(HttpServletResponse response, HttpSession session, @RequestParam String mediaid){
        String appId = wxService.getWxMpConfigStorage().getAppId();
        JSONObject jsonObject = new JSONObject();
        try {
            List<WxMaterialFile> wxMaterialFileList = new ArrayList<WxMaterialFile>();
            WxMaterialFile wxMaterialFile = new WxMaterialFile();
            Condition wxMaterialFileVO = new Condition(WxMaterialFile.class);
            boolean isSuccess = false;
            isSuccess = wxService.getMaterialService().materialDelete(mediaid);
            if(isSuccess){
                wxMaterialFileVO.createCriteria().andCondition("appid=",appId).andCondition("mediaid=",mediaid);
                /*wxMaterialFileVO.setAppid(appId);
                wxMaterialFileVO.setMediaid(mediaid);*/
                wxMaterialFileList = wxMaterialFileService.findByCondition(wxMaterialFileVO);
                if(wxMaterialFileList != null){
                    wxMaterialFile = wxMaterialFileList.get(0);
                    if("news".equals(wxMaterialFile.getType())){
                        Condition wxNewsArticleVO = new Condition(WxNewsArticle.class);
                        wxNewsArticleVO.createCriteria().andCondition("appid=",appId).andCondition("mediaid=",mediaid);
                        /*wxNewsArticleVO.setAppid(appId);
                        wxNewsArticleVO.setMediaid(mediaid);*/
                        wxNewsArticleService.remove(wxNewsArticleVO);
                    }
                    wxMaterialFileService.remove(wxMaterialFileVO);
                }
            }
            return ResponseMsgUtil.success(mediaid);
        } catch (WxErrorException e) {
            e.printStackTrace();
            return ResponseMsgUtil.failure();
        }
        //ResponseUtils.writeJsonObject(response, jsonObject);
    }

    /**
     * 下载图片或语音素材
     * @param response
     * @param session
     * @param mediaid
     * @param filename
     */
    @RequestMapping(value = "/download_material", method = {RequestMethod.POST})
    public Result<String> downloadMaterial(HttpServletResponse response,
                                 HttpSession session,
                                 @RequestParam String mediaid,
                                 @RequestParam String filename){
        String result = "";
        JSONObject jsonObject = new JSONObject();
        try {
            InputStream voiceFile = wxService.getMaterialService().materialImageOrVoiceDownload(mediaid);
            byte[] buffer = new byte[1024];
            int len = 0;
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            try {
                while((len = voiceFile.read(buffer)) != -1) {
                    bos.write(buffer, 0, len);
                }
                bos.close();
                byte[] getData = bos.toByteArray();
                String tempPath = System.getProperty("java.io.tmpdir");
                File saveDir = new File(tempPath + File.separator+ "tempWxImg");
                if(!saveDir.exists()){
                    saveDir.mkdir();
                }
                File file = new File(saveDir + File.separator + filename);
                FileOutputStream fos = new FileOutputStream(file);
                fos.write(getData);
                if(fos!=null){
                    fos.close();
                }
                if(voiceFile!=null){
                    voiceFile.close();
                }
                String[] urls = FileUploadUtil.upload(file);
                //String returnUrl = "http://" + SysConstant.FDFS_NGINXSERVER + "/" + urls[0];
                String returnUrl = urls[0];

                return ResponseMsgUtil.success(returnUrl);
            } catch (IOException e) {
                e.printStackTrace();
                return ResponseMsgUtil.exception();
            }
        } catch (WxErrorException e) {
            e.printStackTrace();
            return ResponseMsgUtil.exception();
        }

    }
}
