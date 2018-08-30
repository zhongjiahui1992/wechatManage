package com.uhope.rl.application.exception;

import com.uhope.rl.application.constants.Constant;
import com.uhope.rl.application.result.ResponseMsgUtil;
import com.uhope.rl.application.result.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.NoHandlerFoundException;

import javax.servlet.http.HttpServletRequest;

/**
 * 全局异常处理.
 * 一般情况下，方法都有异常处理机制，但不能排除有个别异常没有处理，导致返回到前台，因此在这里做一个异常拦截，统一处理那些未被处理过的异常
 *
 * @author xiepuyao
 * @date Created on 2017/11/21
 */
@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);


    /**
     * 特别说明： 可以配置指定的异常处理,这里处理所有
     *
     * @param request
     * @param e
     * @return
     */
    @ExceptionHandler(value = Exception.class)
    public Result defaultErrorHandler(HttpServletRequest request, Exception e) {
        LOGGER.error("request Exception:", e);
        //404
        if (e instanceof NoHandlerFoundException) {
            return ResponseMsgUtil.builderResponse(Constant.RESULT_CODE_NO_EXISTS, "请求的资源不存在!", null);
        }
        return ResponseMsgUtil.exception();
    }
}
