package cn.edu.sustech.ooadbackend.service;

import cn.edu.sustech.ooadbackend.model.domain.User;
import cn.edu.sustech.ooadbackend.model.request.CurrentUserUpdateRequest;
import cn.edu.sustech.ooadbackend.model.request.UserInsertRequest;
import cn.edu.sustech.ooadbackend.model.request.UserUpdateRequest;
import com.baomidou.mybatisplus.extension.service.IService;
import jakarta.servlet.http.HttpServletRequest;

import java.util.Date;
import java.util.List;

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
     * 为使用邮箱登录的用户发送验证码
     * @param mailAddress 用户的邮箱地址
     * @param request HttpServletRequest
     * @return 一个随机生成的验证码
     */
    boolean userLoginMailSend(String mailAddress, HttpServletRequest request);
    
    /**
     * 用户邮箱验证码登录
     * @param mailAddress 邮箱地址
     * @param verificationCode 验证码
     * @return 脱敏后的用户信息
     */
    User userLoginMailCheck(String mailAddress, String verificationCode, HttpServletRequest request);
    
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
     * @param currentUserRequest 包含当前用户已修改的个人信息请求
     * @return 是否修改成功
     */
    Boolean currentUserUpdate(HttpServletRequest request, CurrentUserUpdateRequest currentUserRequest);
    /**
     * 获取教师列表
     * @param request HttpServletRequest
     * @return 教师列表
     */
    List<User> listTeacher(HttpServletRequest request);

    /**
     * 获取教师助理列表
     * @param request HttpServletRequest
     * @return 教师助理列表
     */
    List<User> listTa(HttpServletRequest request);

    /**
     * 根据参数获取用户列表
     * @param userAccount
     * @param userRole
     * @param age
     * @param gender
     * @param email
     * @param avatarUrl
     * @param startTime
     * @param endTime
     * @return 用户列表
     */
    List<User> listByParam(HttpServletRequest request, String userAccount, String name, Integer userRole, Integer age, Byte gender, String email, String avatarUrl, String startTime, String endTime);
    /**
     * 获取学生列表
     * @param request HttpServletRequest
     * @return 学生列表
     */
    List<User> listStudent(HttpServletRequest request);
    /**
     * 新增用户信息
     * @param userInsertRequest
     * @return 新增用户ID
     */
    Long insertUser(UserInsertRequest userInsertRequest);
    /**
     * 删除用户信息
     * @param userId 用户ID
     * @return 是否成功删除
     */
    Boolean deleteUser(Long userId);

    Boolean updateUser(UserUpdateRequest userUpdateRequest);



}
