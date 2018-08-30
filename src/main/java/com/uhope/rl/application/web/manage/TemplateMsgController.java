package com.uhope.rl.application.web.manage;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.uhope.rl.application.result.ResponseMsgUtil;
import com.uhope.rl.application.result.Result;
import com.uhope.rl.application.utils.DateUtil;
import com.uhope.rl.application.utils.WeiXinUtil;
import com.uhope.rl.event.domain.EhSuggest;
import com.uhope.rl.event.service.EhSuggestService;
import com.uhope.rl.wechat.domain.WxTemplate;
import com.uhope.rl.wechat.domain.WxTemplateMsg;
import com.uhope.rl.wechat.domain.WxTemplateUser;
import com.uhope.rl.wechat.dto.WxMsgFormDTO;
import com.uhope.rl.wechat.service.WxTemplateMsgService;
import com.uhope.rl.wechat.service.WxTemplateService;
import com.uhope.rl.wechat.service.WxTemplateUserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import me.chanjar.weixin.common.exception.WxErrorException;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.bean.template.WxMpTemplateData;
import me.chanjar.weixin.mp.bean.template.WxMpTemplateMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import tk.mybatis.mapper.entity.Condition;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

/**
 * Created by zhengdepiao on 2017/6/7.
 */

@Controller
public class TemplateMsgController {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private WxTemplateService wxTemplateService;
    @Autowired
    private WxTemplateMsgService wxTemplateMsgService;
    @Autowired
    private WxTemplateUserService wxTemplateUserService;
    @Autowired
    private WxMpService wxService;
    @Autowired
    private EhSuggestService ehSuggestService;

    @ApiOperation(value = "同步微信模板消息")
    /**
     * 同步公众号模板消息的模板
     *
     * @param response
     * @param session
     * @return
     */
    @ResponseBody
    @GetMapping(value = "/get_template")
    public Result<WxTemplate> getTemplate() {
        //得到appId
        String appId = wxService.getWxMpConfigStorage().getAppId();
        //条件查询
        Condition condition = new Condition(WxTemplateMsg.class);
        condition.createCriteria().andCondition("appid=", appId);
        //初始化容器
        List<WxTemplate> templateList = new ArrayList<WxTemplate>();
        //查询数据库中是否有
        if (wxTemplateService.findByCondition(condition).size() > 0) {
            //若有，则删除
            wxTemplateService.remove(condition);
        }
        WxTemplate wxTemplate = new WxTemplate();
        String token = "";
        try {
            //获取token
            token = wxService.getAccessToken();
        } catch (WxErrorException e) {
            e.printStackTrace();
        }
        //获取微信模板
        JSONArray templateId = WeiXinUtil.getTemplateIdByToken(token);
        if (templateId == null) {
            return ResponseMsgUtil.failure(null);
        }
        Iterator<Object> it = templateId.iterator();
        while (it.hasNext()) {
            JSONObject msgTemplate = (JSONObject) it.next();
            wxTemplate.setAppid(appId);
            wxTemplate.setTemplateid(msgTemplate.getString("template_id"));
            wxTemplate.setTitle(msgTemplate.getString("title"));
            wxTemplate.setPrimaryindustry(msgTemplate.getString("primary_industry"));
            wxTemplate.setDeputyindustry(msgTemplate.getString("deputy_industry"));
            wxTemplate.setContent(msgTemplate.getString("content"));
            wxTemplateService.insert(wxTemplate);
            templateList.add(wxTemplate);
        }
        return ResponseMsgUtil.success(templateList);
    }

    /**
     * 创建模板消息
     *
     * @param wxmsgform
     * @param request
     * @param session
     * @return
     */
    @ApiOperation(value = " ")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "que" +
                    "ry", name = "first", value = "前言", required = true, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "keyword1", value = "标题", required = true, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "keyword3", value = "消息内容", required = true, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "remark", value = "备注", required = true, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "color", value = "消息字体颜色", required = true, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "id", value = "建言献策id", required = true, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "creatuser", value = "创建者", required = true, dataType = "String")
    })
    @RequestMapping(value = "/hzzCreate_templateMsg", method = RequestMethod.POST)
    public Result<String> hzzCreate(@ModelAttribute("wxmsgform") WxMsgFormDTO wxmsgform,
                                    String remark,
                                    String id,
                                    HttpServletRequest request,
                                    HttpSession session) {
        WxTemplate wxTemplate = wxTemplateService.get("lXkZi7o0AvCrMogK5REiggTzGuUHXnWl0go-zJgHVnQ");
        if (wxTemplate != null) {
            //SmWeiXinMsg smWeiXinMsg = new SmWeiXinMsg();
            WxTemplateMsg wxTemplateMsg = new WxTemplateMsg();
            String content = "";
            String first = "";
            String key1 = "";
            String key2 = "";
            String key3 = "";
            String key4 = "";
            String key5 = "";
            remark = "";
            if (wxmsgform.getFirst() != null) {
                first = "first:" + wxmsgform.getFirst();
            }
            if (wxmsgform.getKeyword1() != null) {
                key1 = "," + "keyword1:" + wxmsgform.getKeyword1();
            }
            key2 = "," + "keyword2:" + DateUtil.formatDate(System.currentTimeMillis(), DateUtil.DATE_yyyyMMddHHmmss_china);

            if (wxmsgform.getKeyword3() != null) {
                key3 = "," + "keyword3:" + wxmsgform.getKeyword3();
            }
            if (wxmsgform.getKeyword4() != null) {
                key4 = "," + "keyword4:" + wxmsgform.getKeyword4();
            }
            if (wxmsgform.getKeyword5() != null) {
                key5 = "," + "keyword5:" + wxmsgform.getKeyword5();
            }
            if (remark != null) {
                key5 = "," + "remark:" + remark;
            }
            content = "{" + first + key1 + key2 + key3 + key4 + key5 + remark + "}";
            //写入消息和消息颜色，模板id，跳转链接
            wxTemplateMsg.setData(content);
            wxTemplateMsg.setColor(wxmsgform.getColor());
            wxTemplateMsg.setTemplateid(wxTemplate.getTemplateid());
            if (wxmsgform.getUrl() != null) {
                wxTemplateMsg.setUrl(wxmsgform.getUrl());
            }
            //初始化消息状态
            wxTemplateMsg.setStatus((byte) 0);
            //创建人，创建时间
            wxTemplateMsg.setCreateuser(wxmsgform.getCreateuser());
            wxTemplateMsg.setCreatetime(new Date());
            try {
                //插入模板消息表
                wxTemplateMsgService.insert(wxTemplateMsg);
                //通过建言献策id得到ehSuggest
                EhSuggest ehSuggest = ehSuggestService.get(id);
                //消息id插入表wx_tempplate_user中
                WxTemplateUser wxTemplateUser = new WxTemplateUser();
                wxTemplateUser.setMsgid(wxTemplateMsg.getId());
                //接收者
                wxTemplateUser.setTouser(ehSuggest.getUserid());
                wxTemplateUserService.insert(wxTemplateUser);
            } catch (Exception e) {
                e.printStackTrace();
                return ResponseMsgUtil.failure("failure");
            }
            return ResponseMsgUtil.success("success");
        }
        return ResponseMsgUtil.success("null");
    }


}
