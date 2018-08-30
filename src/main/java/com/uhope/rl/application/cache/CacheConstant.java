package com.uhope.rl.application.cache;

/**
 * 缓存常量
 *
 * @author xiepuyao
 * @date Created on 2017/11/28
 */
public final class CacheConstant {
    /**
     * 下划线
     */
    public final static String UNDERLINE_SEPARATOR = ":";

    public final static String DETAIL = "detail";

    public final static String ROLE = "role";

    public final static String ROLE_DETAIL = ROLE + UNDERLINE_SEPARATOR + DETAIL;

    public final static String MENUS = "menus";

    /**
     * 与角色相关的，所有的key以role为前缀,角色对应的菜单
     */
    public final static String ROLE_MENUS = ROLE + UNDERLINE_SEPARATOR + MENUS;

    public final static String USER = "user";

    public final static String USER_ROLE = USER + UNDERLINE_SEPARATOR + ROLE;

    /**
     * 用户详情
     */
    public final static String USER_DETAIL = USER + UNDERLINE_SEPARATOR + DETAIL;

    /**
     * 图形验证码
     */
    public final static String IMAGE_CODE = "image_code";

    static class Keys {
        /**
         * 角色详情-key
         * 规则:appId+":"+"role:detail"+":"+roleId
         *
         * @param appId
         * @param roleId
         * @return
         */
        public static String getRoleDetailKey(String appId, String roleId) {
            return appId + UNDERLINE_SEPARATOR + ROLE_DETAIL + UNDERLINE_SEPARATOR + roleId;
        }


        /**
         * 角色对应的权限菜单-key
         * 规则appId + ":" + "role:menus"+":"+roleId
         *
         * @param appId
         * @param roleId
         * @return
         */
        public static String getRoleMenusKey(String appId, String roleId) {
            return appId + UNDERLINE_SEPARATOR + ROLE_MENUS + UNDERLINE_SEPARATOR + roleId;
        }

        /**
         * 用户对应的角色 - key
         * 规则:appId + ":" + "user:role"+ ":" + userId
         *
         * @param appId
         * @param userId
         * @return
         */
        public static String getUserRolesKey(String appId, String userId) {
            return getUserRolesKeyPrefixes(appId) + UNDERLINE_SEPARATOR + userId;
        }
        
        public static String getUserRolesKeyPrefixes(String appId){
        	return appId + UNDERLINE_SEPARATOR + USER_ROLE;
        }

        /**
         * 图形验证码对应的keyy
         * 规则:appId + ":" + "image_code" + ":" + imageCodeId
         *
         * @param appId
         * @param imageCodeId
         * @return
         */
        public static String getImageCodeKey(String appId, String imageCodeId) {
            return appId + UNDERLINE_SEPARATOR + IMAGE_CODE + UNDERLINE_SEPARATOR + imageCodeId;
        }


    }
}
