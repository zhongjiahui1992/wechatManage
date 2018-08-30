package com.uhope.rl.application.web.manage;

import com.alibaba.fastjson.JSONObject;
import com.uhope.rl.application.result.ResponseMsgUtil;
import com.uhope.rl.application.result.Result;
import com.uhope.rl.application.utils.DateUtil;
import com.uhope.rl.wechat.domain.WxSubscribeUser;
import com.uhope.rl.wechat.dto.WxSubscribeUserDTO;
import com.uhope.rl.wechat.service.WxSubscribeUserService;
import me.chanjar.weixin.mp.api.WxMpService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author zhongjiahui.
 * @date Created on 2018/3/2.
 */
@RestController
public class StatisticsController {
    @Autowired
    private WxMpService wxMpService;
    @Autowired
    private WxSubscribeUserService wxSubscribeUserBO;
    @Autowired
    private WxMpService wxService;


    /**
     * 统计接口
     * @param dayNumber
     * @param timestamp
     * @return
     */
    @GetMapping("/statistics")
    public Result<JSONObject> statistics(@RequestParam(required = false,defaultValue = "30") int dayNumber,@RequestParam(required = false) String timestamp){
        System.out.println("timestamp: " + timestamp);
        String appId = wxService.getWxMpConfigStorage().getAppId();
        JSONObject jsonObject = new JSONObject();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date timeStampDate = null;
        try {
            if (timestamp != null && timestamp.equals("0")) {
                System.out.println("46");
                timeStampDate = new Date();
                String tmp = sdf.format(timeStampDate);
                timeStampDate = sdf.parse(tmp);
                System.out.println(timeStampDate);
            }
            else {
                System.out.printf(timestamp);
                timeStampDate = sdf.parse(timestamp);
            }
        }
        catch (Exception e){
            return  ResponseMsgUtil.failure("时间获取失败");
        }
        String[] functions = new String[1];
        functions[0] = "count";
        String[] columns = new String[1];
        columns[0] = "subscribe_time";
        jsonObject = wxSubscribeUserBO.GetDayNumberPeopleNumberFromNow(functions, columns, dayNumber,timeStampDate,appId);
        return  ResponseMsgUtil.success(jsonObject);
    }

    /**
     * 关注用户统计接口
     * @return
     */
    @GetMapping("/userNumber")
    public Result<JSONObject> userNumber(){
        JSONObject jsonObject= new JSONObject();
        try{
            String appid = wxMpService.getWxMpConfigStorage().getAppId();
            String today = DateUtil.formatDate(new Date(), DateUtil.DATE_yyyy_MM_dd);

            WxSubscribeUserDTO wxSubscribeUserVO = new WxSubscribeUserDTO();
            wxSubscribeUserVO.setAppid(appid);
            List<WxSubscribeUser> subscribeUserList = wxSubscribeUserBO.find(wxSubscribeUserVO);
            String subscribeDay = "";
            int newUser = 0;
            for(WxSubscribeUser subscribeUser : subscribeUserList){
                subscribeDay = DateUtil.formatDate(subscribeUser.getSubscribeTime(),DateUtil.DATE_yyyy_MM_dd);
                if(subscribeDay.equals(today)){
                    newUser ++;
                }
            }
            jsonObject.put("newUser", newUser);
            jsonObject.put("allUser", subscribeUserList.size());

            return ResponseMsgUtil.success(jsonObject);
        }catch (Exception e){
            e.printStackTrace();
            return ResponseMsgUtil.exception();
        }
    }
}
