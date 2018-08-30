package com.uhope.rl.application.result;


import com.github.pagehelper.PageInfo;
import com.uhope.rl.application.constants.Constant;

import java.util.List;

/**
 * @author xiepuyao
 * @date Created on 2018/1/30
 */
public class ResponseMsgUtil {

    /**
     * @throws
     * @Title: builderResponse
     * @Description: 根据 返回码、返回消息、返回数据 构建 响应消息
     * @param: @param code
     * @param: @param msg
     * @param: @param data
     * @param: @return
     * @return: CommonResponse
     */
    public static <T> Result<T> builderResponse(int code, String msg, T data) {
        Result<T> res = new Result<>();
        res.setResCode(code);
        res.setResMsg(msg);
        res.setData(data);
        return res;
    }

    public static <T> Result<T> success() {
        return builderResponse(Constant.RESULT_CODE_SUCCESS, Constant.RESULT_MSG_SUCCESS, null);
    }

    /**
     * @throws
     * @Title: success
     * @Description: 成功消息体模板
     * @param: @param data
     * @param: @return
     * @return: CommonResponse
     */
    public static <T> Result<T> success(T data) {
        return builderResponse(Constant.RESULT_CODE_SUCCESS, Constant.RESULT_MSG_SUCCESS, data);
    }

    public static <T> Result<T> success(PageInfo<T> data) {
        Result<T> res = new Result<>();
        res.setResCode(Constant.RESULT_CODE_SUCCESS);
        res.setResMsg(Constant.RESULT_MSG_SUCCESS);
        res.setData(data);
        return res;
        //return builderResponse(Constant.RESULT_CODE_SUCCESS, Constant.RESULT_MSG_SUCCESS, data);
    }

    public static <T> Result<T> success(List<T> data){
        Result<T> res = new Result<>();
        res.setResCode(Constant.RESULT_CODE_SUCCESS);
        res.setResMsg(Constant.RESULT_MSG_SUCCESS);
        res.setData(data);
        return res;
    }

    /**
     * @throws
     * @Title: success
     * @Description: 成功消息体模板
     * @param: @param data
     * @param: @return
     * @return: CommonResponse
     */
    public static <T> Result<T> success(String msg, T data) {
        return builderResponse(Constant.RESULT_CODE_SUCCESS, msg, data);
    }

    /**
     * @throws
     * @Title: failure
     * @Description: 失败消息体模板
     * @param: @return
     * @return: CommonResponse
     */
    public static <T> Result<T> failure() {
        return builderResponse(Constant.RESULT_CODE_FAILURE, Constant.RESULT_MSG_FAILURE, null);
    }

    /**
     * @throws
     * @Title: failure
     * @Description: 失败消息体模板
     * @param: @return
     * @return: CommonResponse
     */
    public static <T> Result<T> failure(String msg) {
        return builderResponse(Constant.RESULT_CODE_FAILURE, msg, null);
    }

    /**
     * @Title: loginFailure
     * @Description: 用户名或密码错误, 登陆失败返回消息
     * @param: @return
     * @return: CommonResponse
     * @throws
     */
/*	public static Result loginFailure(){
      return builderResponse(Constant.RESULT_CODE_USER_OR_PASSWORD_ERROR,Constant.RESULT_MSG_USER_OR_PASSWORD_ERROR,null);
	}*/

    /**
     * @Title: maxPasswordErrorCount
     * @Description: 用户密码错误次数达到上限
     * @param: @return
     * @return: CommonResponse
     * @throws
     */
/*	public static CommonResponse reachMaxPasswordErrorCount(){
        CommonResponse res = new CommonResponse();
		res.setResCode(Constant.RESULT_CODE_MAX_PASSWORD_ERROR_COUNT);
		res.setResMsg(Constant.RESULT_MSG_MAX_PASSWORD_ERROR_COUNT);
		res.setData(null);
		return res;
	}*/

    /**
     * @throws
     * @Title: illegalRequest
     * @Description: 非法请求的返回消息
     * @param: @return
     * @return: CommonResponse
     */
    public static <T> Result<T> illegalRequest() {
        return builderResponse(Constant.RESULT_CODE_ILLEGAL_REQUEST, Constant.RESULT_MSG_ILLEGAL_REQUEST, null);
    }

    /**
     * @throws
     * @Title: exception
     * @Description: 请求异常返回结果
     * @param: @return
     * @return: CommonResponse
     */
    public static <T> Result<T> exception() {
        return builderResponse(Constant.RESULT_CODE_EXCEPTION, Constant.RESULT_MSG_EXCEPTION, null);
    }

    /**
     * @throws
     * @Title: paramsEmpty
     * @Description: 参数为空时的响应消息
     * @param: @return
     * @return: CommonResponse
     */
    public static <T> Result<T> paramsEmpty() {
        return builderResponse(Constant.RESULT_CODE_PARAMS_EMPTY, Constant.RESULT_MSG_PARAMS_EMPTY, null);
    }
}
