package cn.edu.sustech.ooadbackend.constant;

/**
 * 用户常量
 * @author DYH
 * @version 1.0
 * @className UserConstant
 * @since 2023/10/9 15:31
 */
public interface UserConstant {
    
    /**
     * 用户密码加密盐值，混淆密码
     */
    String SALT = "ooad";
    
    /**
     * 用户登录态键
     */
    String USER_LOGIN_STATE = "userLoginState";
    
    // -------- 权限 --------
    /**
     * 默认权限
     */
    int DEFAULT_ROLE = 0;
    /**
     * 管理员权限
     */
    int ADMIN_ROLE = 1;
}
