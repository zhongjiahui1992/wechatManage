package com.uhope.rl.application.result;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;

/**
 * @author xiepuyao
 * @date Created on 2018/1/30
 */
public class Result<T> {
    private Integer resCode;
    private String resMsg;
    private Object data;

    public Result() {
    }

    public Integer getResCode() {
        return this.resCode;
    }

    public void setResCode(Integer resCode) {
        this.resCode = resCode;
    }

    public String getResMsg() {
        return this.resMsg;
    }

    public void setResMsg(String resMsg) {
        this.resMsg = resMsg;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public String toJson() {
        return this.data == null ? JSON.toJSONString(this) : this.toJson(new SerializerFeature[]{SerializerFeature.WriteNullListAsEmpty, SerializerFeature.WriteNullStringAsEmpty, SerializerFeature.WriteMapNullValue});
    }

    public String toJson(SerializerFeature... features) {
        return null == features ? this.toJson() : JSON.toJSONString(this, features);
    }

    public String toString() {
        return "Result{resCode=" + this.resCode + ", resMsg=\'" + this.resMsg + '\'' + ", data=" + this.data + '}';
    }
}
