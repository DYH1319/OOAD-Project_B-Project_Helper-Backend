package cn.edu.sustech.ooadbackend.controller;

import cn.edu.sustech.ooadbackend.common.BaseResponse;
import cn.edu.sustech.ooadbackend.common.StatusCode;
import cn.edu.sustech.ooadbackend.constant.UserConstant;
import cn.edu.sustech.ooadbackend.exception.BusinessException;
import cn.edu.sustech.ooadbackend.model.domain.User;
import cn.edu.sustech.ooadbackend.model.request.CurrentUserUpdateRequest;
import cn.edu.sustech.ooadbackend.model.request.UserLoginRequest;
import cn.edu.sustech.ooadbackend.model.request.UserLoginWithMailRequest;
import cn.edu.sustech.ooadbackend.model.request.UserRegisterRequest;
import cn.edu.sustech.ooadbackend.service.UserService;
import cn.edu.sustech.ooadbackend.utils.ResponseUtils;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

/**
 * @author DYH
 * @version 1.0
 * @className UserController
 * @since 2023/10/9 15:30
 */
@RestController
@RequestMapping("/user")
public class UserController {
    
    @Autowired
    private UserService userService;
    
    @PostMapping("/register")
    public BaseResponse<Long> userRegister(@RequestBody(required = false) UserRegisterRequest userRegisterRequest, HttpServletRequest request) {
        if (userRegisterRequest == null) {
            throw new BusinessException(StatusCode.PARAMS_ERROR);
        }
        String userAccount = userRegisterRequest.getUserAccount();
        String username = userRegisterRequest.getUsername();
        String userPassword = userRegisterRequest.getUserPassword();
        String checkPassword = userRegisterRequest.getCheckPassword();
        if (StringUtils.isAnyBlank(userAccount, username, userPassword, checkPassword)) {
            throw new BusinessException(StatusCode.PARAMS_ERROR, "参数为空");
        }
        long result = userService.userRegister(userAccount, username, userPassword, checkPassword);
        return ResponseUtils.success(result, "注册成功!");
    }
    
    @PostMapping("/login")
    public BaseResponse<User> userLogin(@RequestBody(required = false) UserLoginRequest userLoginRequest, HttpServletRequest request) {
        if (userLoginRequest == null) {
            throw new BusinessException(StatusCode.PARAMS_ERROR);
        }
        String userAccount = userLoginRequest.getUserAccount();
        String userPassword = userLoginRequest.getUserPassword();
        if (StringUtils.isAnyBlank(userAccount, userPassword)) {
            throw new BusinessException(StatusCode.PARAMS_ERROR, "参数为空");
        }
        User user = userService.userLogin(userAccount, userPassword, request);
        return ResponseUtils.success(user, "登录成功!");
    }
    
    @PostMapping("/login/mail/send")
    public BaseResponse<Boolean> userLoginMailSend(@RequestBody(required = false) String mailAddress, HttpServletRequest request) {
        if (StringUtils.isAnyBlank(mailAddress)) {
            throw new BusinessException(StatusCode.PARAMS_ERROR);
        }
        boolean res = userService.userLoginMailSend(mailAddress, request);
        return ResponseUtils.success(res, "成功发送验证码");
    }
    
    @PostMapping("/login/mail/check")
    public BaseResponse<User> userLoginMailCheck(@RequestBody(required = false) UserLoginWithMailRequest userLoginWithMailRequest, HttpServletRequest request) {
        if (userLoginWithMailRequest == null) {
            throw new BusinessException(StatusCode.PARAMS_ERROR);
        }
        String mailAddress = userLoginWithMailRequest.getMailAddress();
        String verificationCode = userLoginWithMailRequest.getVerificationCode();
        if (StringUtils.isAnyBlank(mailAddress, verificationCode)) {
            throw new BusinessException(StatusCode.PARAMS_ERROR, "参数为空");
        }
        User user = userService.userLoginMailCheck(mailAddress, verificationCode, request);
        return ResponseUtils.success(user, "登录成功!");
    }
    
    @PostMapping("/logout")
    public BaseResponse<Integer> userLogout(HttpServletRequest request) {
        if (request == null) {
            throw new BusinessException(StatusCode.PARAMS_ERROR);
        }
        int result = userService.userLogout(request);
        if (result != 0) {
            throw new BusinessException(StatusCode.UNKNOWN_ERROR, "注销失败");
        }
        return ResponseUtils.success(result, "注销成功!");
    }
    
    @GetMapping("/current")
    public BaseResponse<User> getCurrentUser(HttpServletRequest request) {
        if (request == null) {
            throw new BusinessException(StatusCode.PARAMS_ERROR);
        }
        User safetyUser = userService.getCurrentUser(request);
        return ResponseUtils.success(safetyUser);
    }

    @PostMapping("/current/update")
    public BaseResponse<Boolean> currentUserUpdate(@RequestBody(required = false) CurrentUserUpdateRequest currentUserRequest, HttpServletRequest request){
        if (currentUserRequest == null) throw new BusinessException(StatusCode.PARAMS_ERROR);
        Boolean isUpdated = userService.currentUserUpdate(request, currentUserRequest);

        return ResponseUtils.success(isUpdated);
    }
    /**
     * 获取教师列表
     * @param request HttpServletRequest
     * @return 教师列表
     */
    @GetMapping("/listAllTeacherName")
    public BaseResponse<User[]> listAllTeacherName(HttpServletRequest request){
        if (request == null) throw new BusinessException(StatusCode.SYSTEM_ERROR);
        // 获取用户登录态
        User currentUser = (User) request.getSession().getAttribute(UserConstant.USER_LOGIN_STATE);
        // 确认用户是否登录
        if (currentUser == null) throw new BusinessException(StatusCode.NOT_LOGIN);
        if(currentUser.getUserRole() != UserConstant.ADMIN_ROLE) throw new BusinessException(StatusCode.NO_AUTH, "非管理员用户不能查看所有教师列表");

        List<User> teacherList = userService.listTeacher(request);
        return ResponseUtils.success(teacherList.toArray(User[] :: new));
    }
    /**
     * 获取教师助理列表
     * @param request HttpServletRequest
     * @return 教师助理列表
     */
    @GetMapping("/listAllTaName")
    public BaseResponse<User[]> listAllTaName(HttpServletRequest request){

        if (request == null) throw new BusinessException(StatusCode.SYSTEM_ERROR);
        // 获取用户登录态
        User currentUser = (User) request.getSession().getAttribute(UserConstant.USER_LOGIN_STATE);
        // 确认用户是否登录
        if (currentUser == null) throw new BusinessException(StatusCode.NOT_LOGIN);
        if(currentUser.getUserRole() != UserConstant.TEACHER_ROLE) throw new BusinessException(StatusCode.NO_AUTH, "非教师用户不能查看所有助教列表");



        List<User> taList = userService.listTa(request);
        return ResponseUtils.success(taList.toArray(User[] :: new));
    }
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

    @GetMapping("/listByParams")
    public BaseResponse<User[]> listByParams(
            HttpServletRequest request,
            @RequestParam(name = "userAccount", required = false) String userAccount,
            @RequestParam(name = "userRole", required = false) Integer userRole,
            @RequestParam(name = "age", required = false) Integer age,
            @RequestParam(name = "gender", required = false) Byte gender,
            @RequestParam(name = "email", required = false) String email,
            @RequestParam(name = "avatarUrl", required = false) String avatarUrl,
            @RequestParam(name = "startTime", required = false) Date startTime,
            @RequestParam(name = "endTime", required = false) Date endTime
    ) {
        if (request == null) throw new BusinessException(StatusCode.SYSTEM_ERROR);
        // 获取用户登录态
        User currentUser = (User) request.getSession().getAttribute(UserConstant.USER_LOGIN_STATE);
        // 确认用户是否登录
        if (currentUser == null) throw new BusinessException(StatusCode.NOT_LOGIN);
        if(currentUser.getUserRole() != UserConstant.ADMIN_ROLE) throw new BusinessException(StatusCode.NO_AUTH, "非管理员用户不能查看该用户列表");
        List<User> userList = userService.listByParam( request,  userAccount,  userRole,  age,  gender,  email,  avatarUrl,  startTime,  endTime);
        return ResponseUtils.success(userList.toArray(User[] :: new ));
    }




}
