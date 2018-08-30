package com.uhope.rl.application.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class ServiceFilter implements Filter {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceFilter.class);

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain)
            throws IOException, ServletException {
        HttpServletResponse resp = (HttpServletResponse) response;
        HttpServletRequest req = (HttpServletRequest) request;

        // 解决页面跨域访问问题
        resp.setHeader("Access-Control-Allow-Origin", "*");
        resp.setHeader("Access-Control-Allow-Credentials", "true");
        resp.setHeader("Access-Control-Allow-Methods", "*");
        resp.setHeader("Access-Control-Allow-Headers", "Content-Type,x_requested_with,Access-Token");
        //x_requested_with 用于ueditor跨域上传图片文件
        resp.setHeader("Access-Control-Expose-Headers", "*");

        req.setCharacterEncoding("UTF-8");

        filterChain.doFilter(req, resp);


    }


    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void destroy() {
    }


}
