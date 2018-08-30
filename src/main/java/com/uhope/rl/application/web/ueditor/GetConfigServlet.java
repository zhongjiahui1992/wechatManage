package com.uhope.rl.application.web.ueditor;

import com.uhope.rl.application.utils.ueditor.ActionEnter;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Created by zhongjiahui on 2017/6/23.
 */
@RestController
public class GetConfigServlet {

    /**
     * 富文本编辑器config接口
     * @param request
     * @param response
     * @throws ServletException
     * @throws IOException
     */
    @RequestMapping(value = "/GetConfigServlet")
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        //这里就是把controller.jsp代码copy下来
        //request.setCharacterEncoding( "utf-8" );
        response.setHeader("Content-Type" , "text/html");

        String roolPath = request.getSession().getServletContext().getRealPath("/");
        String action = request.getParameter("action");
        //记得把config.json放到/Test/WEB-INF/下
        String configStr = new ActionEnter(request, roolPath).exec();
        /*用于在线管理图片显示*/
        /*if( action!=null &&
                (action.equals("listfile") || action.equals("listimage") ) ){
            roolPath = roolPath.replace("\\", "/");
            configStr = configStr.replaceAll(roolPath, "/");
        }*/
        System.out.println(roolPath);
        response.getWriter().write(configStr);
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    }
}
