package com.uhope.rl.application.filter;

import com.uhope.common.web.util.RequestUtils;
import com.uhope.rl.application.cache.CacheAccess;
import com.uhope.rl.application.constants.SysConstant;
import com.uhope.rl.application.enums.EnumEnvType;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

public class PassportFilter implements Filter {

    private boolean loginRequiredDefault = true;
    private List<Pattern> loginRequiredUrlPatterns = new LinkedList<Pattern>();
    private List<Pattern> loginIgnoredUrlPatterns = new LinkedList<Pattern>();

    @Autowired
    private CacheAccess cacheAccess;
    /**
     * 当前使用的配置文件,dev或pro
     */
    private String env;

    public PassportFilter(String env) {
        this.env = env;
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse res,
                         FilterChain chain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;
        response.setCharacterEncoding(SysConstant.CHAR_SET);
        if (EnumEnvType.DEV.toString().equalsIgnoreCase(env)) {
            chain.doFilter(request, response);
            return;
        }
        /*
         * 开发环境中不需要验证
         *否则需要完整是否登录，是否有权限
         */


//        boolean loginRequired = isLoginRequired(request);
        //TODO 待系统管理模块完成后再完善
        System.out.println("开始验证权限");
        chain.doFilter(request, response);

    }

    @Override
    public void destroy() {
    }


    private boolean isLoginRequired(HttpServletRequest request) {
        String uri = RequestUtils.getRequestURIWithoutContextPath(request);

        //以以下js,res,..文件开头都可以直接访问
        if ("/".equals(uri) || uri.startsWith("/js/")
                || uri.startsWith("/res/") || uri.startsWith("/fr")
                || uri.startsWith("/common/") || uri.startsWith("/commonController/")
                || uri.startsWith("/fileController/fromdfs")
                || uri.startsWith("/tree/")) {
            return false;
        }

        for (Pattern pattern : loginIgnoredUrlPatterns) {
            if (pattern.matcher(uri).matches()) {
                return false;
            }
        }

        return loginRequiredDefault;
    }


}
