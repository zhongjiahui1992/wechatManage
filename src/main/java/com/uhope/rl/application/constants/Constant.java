package com.uhope.rl.application.constants;

import com.uhope.rl.application.utils.ConfigUtil;

/**
 * 常量类
 *
 * @author xiepuyao
 * @date Created on 2017/11/24
 */
public final class Constant {


    /**
     * 请求头token
     */
    String HTTP_HEADER_ACCESS_TOKEN = "Access-Token";

    /**
     * @Fields RESULT_CODE_SUCCESS : 返回码成功
     */
    public static final int RESULT_CODE_SUCCESS = 1;

    /**
     * @Fields RESULT_CODE_FAILURE : 返回码失败
     */
    public static final int RESULT_CODE_FAILURE = 0;
    /**
     * @Fields RESULT_CODE_EXCEPTION : 请求抛出异常
     */
    public static final int RESULT_CODE_EXCEPTION = 1002;

    /**
     * @Fields RESULT_CODE_NOT_LOGIN : 未登陆状态
     */
    public static final int RESULT_CODE_NOT_LOGIN = 1003;

    /**
     * @Fields RESULT_CODE_NO_EXISTS : 查询结果为空
     */
    public static final int RESULT_CODE_NO_EXISTS = 1004;

    /**
     * @Fields RESULT_CODE_NOT_AUTHORIZED : 无操作权限
     */
    public static final int RESULT_CODE_NOT_AUTHORIZED = 1005;

    /**
     * @Fields RESULT_CODE_USER_OR_PASSWORD_ERROR : 返回码 用户名或密码错误
     */
    public static final int RESULT_CODE_USER_OR_PASSWORD_ERROR = 1006;

    //
    /**
     * @Fields RESULT_CODE_MAX_PASSWORD_ERROR_COUNT : 返回码 密码错误次数达到上限
     */
    public static final int RESULT_CODE_MAX_PASSWORD_ERROR_COUNT = 1007;

    /**
     * @Fields RESULT_CODE_ILLEGAL_REQUEST : 非法请求返回码 未登录 或跳过登陆的请求
     */
    public static final int  RESULT_CODE_ILLEGAL_REQUEST = 1008;

    /**
     * @Fields RESULT_CODE_PARAMS_EMPTY : 返回码 必填参数为空
     */
    public static final int  RESULT_CODE_PARAMS_EMPTY = 1009;


    /**
     * @Fields RESULT_CODE_MODULE_REGISTER_MODULE_ALREADY_EXISTED : 模块注册返回码： 模块已存在
     */
    public static final int RESULT_CODE_MODULE_REGISTER_MODULE_ALREADY_EXISTED = 2001;

    /**
     * @Fields RESULT_CODE_MODULE_REGISTER_ILLEGAL_REQUEST : 模块注册返回码：非法请求
     */
    public static final int RESULT_CODE_MODULE_REGISTER_ILLEGAL_REQUEST = 2002;

    /**
     * @Fields RESULT_CODE_MODULE_REGISTER_GET_MODULE_CONFIG_FAILURE : 模块注册返回码：获取模块配置信息失败
     */
    public static final int RESULT_CODE_MODULE_REGISTER_GET_MODULE_CONFIG_FAILURE = 2003;

    /**
     * @Fields RESULT_CODE_MODULE_REGISTER_GET_OMS_APP_CONFIG_FAILURE : 模块注册返回码：获取OMS App配置信息失败
     */
    public static final int RESULT_CODE_MODULE_REGISTER_GET_OMS_APP_CONFIG_FAILURE = 2004;

    /**
     * @Fields RESULT_MSG_SUCCESS : 返回消息 成功
     */
    public static final String RESULT_MSG_SUCCESS = "Success";

    /**
     * @Fields RESULT_MSG_FAILURE : 返回消息 失败
     */
    public static final String RESULT_MSG_FAILURE = "Failure";

    /**
     * @Fields RESULT_MSG_USER_OR_PASSWORD_ERROR : 返回消息  用户名或密码错误
     */
    public static final String RESULT_MSG_USER_OR_PASSWORD_ERROR = "username or password Invalid";

    /**
     * @Fields RESULT_MSG_MAX_PASSWORD_ERROR_COUNT : 返回消息 密码错误次数达到上限
     */
    public static final String RESULT_MSG_MAX_PASSWORD_ERROR_COUNT = "upper limit for password error";

    /**
     * @Fields RESULT_MSG_ILLEGAL_REQUEST : 非法请求返回码 未登录 或跳过登陆的请求
     */
    public static final String  RESULT_MSG_ILLEGAL_REQUEST = "Illegal request";

    /**
     * @Fields RESULT_MSG_EXCEPTION : 返回消息 请求抛出异常
     */
    public static final String RESULT_MSG_EXCEPTION = "request exception";

    /**
     * @Fields RESULT_MSG_PARAMS_EMPTY : 返回消息 必填参数为空
     */
    public static final String  RESULT_MSG_PARAMS_EMPTY = "the input parameter is null";

    public static final String RESULT_DATA = "data";
    public static final String RESULT_RESCODE = "resCode";
    public static final String RESULT_RESMSG = "resMsg";


}
