package com.uhope.rl.application.web.manage;

import com.alibaba.fastjson.JSONObject;
import com.uhope.rl.application.utils.ApplicationContextUtil;
import com.uhope.rl.wechat.domain.WxTemplateMsg;
import com.uhope.rl.wechat.domain.WxTemplateUser;
import com.uhope.rl.wechat.service.WxTemplateMsgService;
import com.uhope.rl.wechat.service.WxTemplateUserService;
import com.uhope.rl.wechat.service.impl.WxTemplateMsgServiceImpl;
import com.uhope.rl.wechat.service.impl.WxTemplateUserServiceImpl;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.api.impl.WxMpServiceImpl;
import me.chanjar.weixin.mp.bean.template.WxMpTemplateData;
import me.chanjar.weixin.mp.bean.template.WxMpTemplateMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import tk.mybatis.mapper.entity.Condition;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhengdepiao on 2018/2/7.
 */
@Configuration
@EnableScheduling   //启用定时任务
public class SchedulingController {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    WxTemplateMsgService wxTemplateMsgService = (WxTemplateMsgService) ApplicationContextUtil.getBean("wxTemplateMsgService");
    WxTemplateUserService wxTemplateUserService=(WxTemplateUserService) ApplicationContextUtil.getBean("wxTemplateUserService");
    WxMpService wxService=(WxMpService) ApplicationContextUtil.getBean("wxMpService");

    /**
     * 发送模板消息
     * 定时扫描wxTemplateMsg表，若有未发送消息，则发送
     */
//    @Scheduled(cron = "0/60 * * * * ?") // 每20秒执行一次

    /*public void sendMsg() {
        Condition condition = new Condition(WxTemplateMsg.class);
        //条件查询未发送的模板消息
        condition.createCriteria().andCondition("status=", 0);
        List<WxTemplateMsg> wxTemplateMsgs = wxTemplateMsgService.findByCondition(condition);
        if (wxTemplateMsgs.size() > 0) {
            //查询每条模板消息的接收者
            Condition condition1 = new Condition(WxTemplateUser.class);
            for (WxTemplateMsg wxTemplateMsg : wxTemplateMsgs) {
                //通过消息条件查询
                condition1.createCriteria().andCondition("msgid=", wxTemplateMsg.getId());
                //得到接收者
                WxTemplateUser wxTemplateUser = wxTemplateUserService.findByCondition(condition).get(0);
                if (wxTemplateUser != null) {
                    //获取消息记录的data
                    String data = wxTemplateMsg.getData();
                    // 转换成jsonObject对象
                    JSONObject jsonObject = JSONObject.parseObject(data);
                    //模板消息
                    try {
                        WxMpTemplateMessage wxMpTemplateMessage = new WxMpTemplateMessage();
                        wxMpTemplateMessage.setTemplateId(wxTemplateMsg.getTemplateid());
                        wxMpTemplateMessage.setToUser(wxTemplateUser.getTouser());
                        WxMpTemplateData first = new WxMpTemplateData("first", jsonObject.getString("first"), wxTemplateMsg.getColor());
                        WxMpTemplateData key0 = new WxMpTemplateData("keyword1", jsonObject.getString("keyword1"), wxTemplateMsg.getColor());
                        WxMpTemplateData key1 = new WxMpTemplateData("keyword2", jsonObject.getString("keyword2"), wxTemplateMsg.getColor());
                        WxMpTemplateData key2 = new WxMpTemplateData("keyword3", jsonObject.getString("keyword3"), wxTemplateMsg.getColor());
                        WxMpTemplateData remark = new WxMpTemplateData("remark", jsonObject.getString("remark"), wxTemplateMsg.getColor());
                        List<WxMpTemplateData> datas = new ArrayList();
                        datas.add(first);
                        datas.add(remark);
                        datas.add(key0);
                        datas.add(key1);
                        datas.add(key2);
                        wxMpTemplateMessage.setData(datas);
                        wxService.getTemplateMsgService().sendTemplateMsg(wxMpTemplateMessage);
                        logger.info("模板消息发送成功给用户：" + wxTemplateUser.getTouser());
                        //修改消息的状态
                        wxTemplateMsg.setStatus((byte)1);
                        wxTemplateMsgService.update(wxTemplateMsg);
                    } catch (Exception e) {
                        e.printStackTrace();
                        logger.info("模板消息发送失败");
                    }

                }
            }
        }

    }*/
}

