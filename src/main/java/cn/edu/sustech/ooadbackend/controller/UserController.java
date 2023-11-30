package cn.edu.sustech.ooadbackend.controller;

import cn.edu.sustech.ooadbackend.common.BaseResponse;
import cn.edu.sustech.ooadbackend.common.StatusCode;
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

}
