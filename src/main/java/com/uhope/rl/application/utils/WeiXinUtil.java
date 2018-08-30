package com.uhope.rl.application.utils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.uhope.rl.application.model.MyX509TrustManager;
import me.chanjar.weixin.common.exception.WxErrorException;
import me.chanjar.weixin.mp.api.WxMpService;
import org.apache.commons.codec.binary.Base64;
import org.springframework.stereotype.Component;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import java.io.*;
import java.net.ConnectException;
import java.net.URL;
import java.util.*;

/**
 * Created by zhongjiahui on 2017/5/16.
 */
@Component("weiXinUnit")
public class WeiXinUtil {

    public static final String GET_TOKEN_URL = "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid=APPID&secret=APPSECRET";
    public static final String GET_GROUP_URL = "https://api.weixin.qq.com/cgi-bin/groups/get?access_token=ACCESS_TOKEN";
    public static final String CLEAR_QUOTA_URL = "https://api.weixin.qq.com/cgi-bin/clear_quota?access_token=ACCESS_TOKEN";
    public static final String GET_TEMPLATE_URL = "https://api.weixin.qq.com/cgi-bin/template/get_all_private_template?access_token=ACCESS_TOKEN";
    public static final String GET_MENU_URL = "https://api.weixin.qq.com/cgi-bin/get_current_selfmenu_info?access_token=ACCESS_TOKEN";


    public static JSONObject getToken(String appid, String appsecret){
        String requestUrl = GET_TOKEN_URL.replace("APPID", appid).replace("APPSECRET", appsecret);
        JSONObject jsonObject = httpsRequest(requestUrl, "GET", null);

        return jsonObject;
    }

    public static JSONObject getMenuByToken(String token){
        String requestUrl = GET_MENU_URL.replace("ACCESS_TOKEN", token);
        JSONObject jsonObject = httpsRequest(requestUrl, "GET", null);

        return jsonObject;
    }

    public static JSONArray getGroupByToken(String token){
        String requestUrl = GET_GROUP_URL.replace("ACCESS_TOKEN", token);
        JSONObject jsonObject = httpsRequest(requestUrl, "GET", null);
        JSONArray result = null;
        if(null != jsonObject){
            try{
                result = jsonObject.getJSONArray("groups");
            }catch(Exception e){
                e.printStackTrace();
            }
        }
        return result;
    }

    public static JSONArray getTemplateIdByToken(String token){
        String requestUrl = GET_TEMPLATE_URL.replace("ACCESS_TOKEN", token);
        JSONObject jsonObject = httpsRequest(requestUrl, "GET", null);
        JSONArray result = null;
        if(null != jsonObject){
            try{
                result = jsonObject.getJSONArray("template_list");
            }catch(Exception e){
                System.out.println("获取token失败,错误是 "+jsonObject.getInteger("errcode")+",错误信息是"+ jsonObject.getString("errmsg"));
            }
        }
        return result;
    }

    public static JSONObject httpsRequest(String requestUrl, String requestMethod, String outputStr) {
        JSONObject jsonObject = null;
        try{
            TrustManager[] tm = { new MyX509TrustManager() };
            SSLContext sslContext = SSLContext.getInstance("SSL", "SunJSSE");
            sslContext.init(null, tm, new java.security.SecureRandom());
            SSLSocketFactory ssf = sslContext.getSocketFactory();

            URL url = new URL(requestUrl);
            HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
            conn.setSSLSocketFactory(ssf);

            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setUseCaches(false);

            conn.setRequestMethod(requestMethod);

            if (null != outputStr) {
                OutputStream outputStream = conn.getOutputStream();
                // 注意编码格式
                outputStream.write(outputStr.getBytes("UTF-8"));
                outputStream.close();
            }
            // 从输入流读取返回内容
            InputStream inputStream = conn.getInputStream();
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "utf-8");
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            String str = null;
            StringBuffer buffer = new StringBuffer();
            while ((str = bufferedReader.readLine()) != null) {
                buffer.append(str);
            }
            // 释放资源
            bufferedReader.close();
            inputStreamReader.close();
            inputStream.close();
            inputStream = null;
            conn.disconnect();
            jsonObject = JSONObject.parseObject(buffer.toString());
        }catch(ConnectException ce){
            ce.printStackTrace();
        }catch(Exception e){
            e.printStackTrace();
        }
        return jsonObject;
    }

    public static String convertTrueNameToSqlName(String name) {
        try {
            name = Base64.encodeBase64String(name.getBytes("utf-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return name;
    }

    public static String convertSqlNameToTrueName(String name){
        try {
            name = new String(Base64.decodeBase64(name.getBytes()), "utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return name;
    }



    //求两个数组的交集
    public static String[] intersect(String[] arr1, String[] arr2) {
        Map<String, Boolean> map = new HashMap<String, Boolean>();
        LinkedList<String> list = new LinkedList<String>();
        for (String str : arr1) {
            if (!map.containsKey(str)) {
                map.put(str, Boolean.FALSE);
            }
        }
        for (String str : arr2) {
            if (map.containsKey(str)) {
                map.put(str, Boolean.TRUE);
            }
        }

        for (Map.Entry<String, Boolean> e : map.entrySet()) {
            if (e.getValue().equals(Boolean.TRUE)) {
                list.add(e.getKey());
            }
        }

        String[] result = {};
        return list.toArray(result);
    }

    //求两个数组的差集
    public static String[] minus(String[] arr1, String[] arr2) {
        LinkedList<String> list = new LinkedList<String>();
        LinkedList<String> history = new LinkedList<String>();
        String[] longerArr = arr1;
        String[] shorterArr = arr2;
        //找出较长的数组来减较短的数组
        if (arr1.length > arr2.length) {
            longerArr = arr2;
            shorterArr = arr1;
        }
        for (String str : longerArr) {
            if (!list.contains(str)) {
                list.add(str);
            }
        }
        for (String str : shorterArr) {
            if (list.contains(str)) {
                history.add(str);
                list.remove(str);
            } else {
                if (!history.contains(str)) {
                    list.add(str);
                }
            }
        }

        String[] result = {};
        return list.toArray(result);
    }

    public static String convertArrayToString(Object[] a){
        if (a == null)
            return "null";

        int iMax = a.length - 1;
        if (iMax == -1)
            return "[]";

        StringBuilder b = new StringBuilder();
        b.append('[');
        for (int i = 0; ; i++) {
            b.append(String.valueOf(a[i]));
            if (i == iMax)
                return b.append(']').toString();
            b.append(",");
        }
    }

    public static String[] removeSameString(String str){
        Set<String> mLinkedSet = new LinkedHashSet<String>();
        String[] strArray = str.split(",");
        List list = Arrays.asList(strArray);
        Set set = new HashSet(list);
        String[] rid = (String[])set.toArray(new String[0]);
        return rid;
    }

    public static String postUrlByOpenidsAndTagids(WxMpService wxMpService, String url, String[] openids, String tagid) throws WxErrorException {
        Gson gson = new Gson();
        String openidsjsonString = gson.toJson(openids);

        JsonParser parser = new JsonParser();
        JsonObject json = new JsonObject();
        json.addProperty("tagid", Long.parseLong(tagid));

        JsonElement jsonElement = parser.parse(openidsjsonString);
        json.add("openid_list", jsonElement);
        //wxService.getUserTagService().batchTagging(Long.parseLong(tagid),openids);
        wxMpService.post(url, json.toString());
        return "success";
    }

    public static String getFileExt(String contentType) {
        String fileExt = "";
        if ("image/jpeg".equals(contentType))
            fileExt = ".jpg";
        else if ("audio/mpeg".equals(contentType))
            fileExt = ".mp3";
        else if ("audio/amr".equals(contentType))
            fileExt = ".amr";
        else if ("video/mp4".equals(contentType))
            fileExt = ".mp4";
        else if ("video/mpeg4".equals(contentType))
            fileExt = ".mp4";
        return fileExt;
    }


}
