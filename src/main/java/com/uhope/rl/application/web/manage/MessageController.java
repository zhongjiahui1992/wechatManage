package com.uhope.rl.application.web.manage;

import com.github.pagehelper.Page;
import com.uhope.rl.application.result.ResponseMsgUtil;
import com.uhope.rl.application.result.Result;
import com.uhope.rl.base.core.OrderBy;
import com.uhope.rl.wechat.domain.WxMassMessage;
import com.uhope.rl.wechat.service.WxMassMessageService;
import me.chanjar.weixin.mp.api.WxMpService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import tk.mybatis.mapper.entity.Condition;

/**
 * @author zhongjiahui.
 * @date Created on 2018/2/5.
 */
@RestController
@RequestMapping("/message")
public class MessageController {
    @Autowired
    private WxMpService wxMpService;
    @Autowired
    private WxMassMessageService wxMassMessageService;

    /**
     * 获取群发消息记录列表
     * @param pageNumber
     * @param pageSize
     * @return
     */
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public Result<WxMassMessage> massList(@RequestParam(defaultValue = "0") int pageNumber,
                                          @RequestParam(defaultValue = "10") int pageSize){
        try{
            String appid = wxMpService.getWxMpConfigStorage().getAppId();

            Condition condition = new Condition(WxMassMessage.class);
            condition.createCriteria().andCondition("appid=",appid);
            //增加排序
            OrderBy orderBy = new OrderBy();
            orderBy.add("createtime", false);

            return ResponseMsgUtil.success(wxMassMessageService.findByCondition(condition, orderBy, pageSize, pageNumber));
        }catch (Exception e){
            e.printStackTrace();
            return ResponseMsgUtil.failure();
        }
    }

    /**
     * 删除一条群发记录
     * @param massid
     * @return
     */
    @RequestMapping(value = "/delete", method = RequestMethod.GET)
    public Result<String> deleteMass(@RequestParam String massid){
        try{
            wxMassMessageService.remove(massid);
            return ResponseMsgUtil.success();
        }catch (Exception e){
            e.printStackTrace();
            return ResponseMsgUtil.failure();
        }
    }
}
