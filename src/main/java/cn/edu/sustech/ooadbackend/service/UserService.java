package cn.edu.sustech.ooadbackend.service;

import cn.edu.sustech.ooadbackend.model.domain.User;
import com.baomidou.mybatisplus.extension.service.IService;
import jakarta.servlet.http.HttpServletRequest;

/**
 * @className UserService
 * @version 1.0
 * @author DYH
 * @since 2023/10/9 12:44
 */
    
public interface UserService extends IService<User> {
    
    /**
     * 用户注册
     * @param userAccount 用户账户
     * @param username 用户姓名
     * @param userPassword 用户密码
     * @param checkPassword 校验密码
     * @return 新用户id
     */
    long userRegister(String userAccount, String username, String userPassword, String checkPassword);
    
    /**
     * 用户登录
     * @param userAccount 用户账户
     * @param userPassword 用户密码
     * @return 脱敏后的用户信息
     */
    User userLogin(String userAccount, String userPassword, HttpServletRequest request);
    
    /**
     * 用户注销
     * @param request HttpServletRequest
     */
    int userLogout(HttpServletRequest request);
    
    /**
     * 获取当前登录的用户
     * @param request HttpServletRequest
     * @return 当前登录的用户
     */
    User getCurrentUser(HttpServletRequest request);

    /**
     * 更新当前用户个人信息
     * @param user 当前用户已修改的个人信息
     * @return 是否修改成功
     */
    Boolean currentUserUpdate(HttpServletRequest request, User user);
}
