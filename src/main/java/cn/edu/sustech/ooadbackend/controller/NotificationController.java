package cn.edu.sustech.ooadbackend.controller;

import cn.edu.sustech.ooadbackend.common.BaseResponse;
import cn.edu.sustech.ooadbackend.common.StatusCode;
import cn.edu.sustech.ooadbackend.constant.UserConstant;
import cn.edu.sustech.ooadbackend.exception.BusinessException;
import cn.edu.sustech.ooadbackend.model.domain.Notification;
import cn.edu.sustech.ooadbackend.model.domain.User;
import cn.edu.sustech.ooadbackend.model.domain.UserNotification;
import cn.edu.sustech.ooadbackend.model.request.NotificationDeleteRequest;
import cn.edu.sustech.ooadbackend.model.request.NotificationInsertRequest;
import cn.edu.sustech.ooadbackend.model.response.NotificationInfoResponse;
import cn.edu.sustech.ooadbackend.service.NotificationService;
import cn.edu.sustech.ooadbackend.service.UserNotificationService;
import cn.edu.sustech.ooadbackend.utils.ResponseUtils;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import jakarta.annotation.Resource;
import jakarta.annotation.Resources;
import jakarta.servlet.http.HttpServletRequest;
import org.jetbrains.annotations.NotNull;
import org.springframework.web.bind.annotation.*;

/**
 * @className: NotificationController
 * @Package: cn.edu.sustech.ooadbackend.controller
 * @Description:
 * @Author: Daniel
 * @Date: 2023/10/31 17:09
 * @Version: 1.0
 */
@RestController
@RequestMapping("/notification")
public class NotificationController {

    @Resource
    UserNotificationService userNotificationService;

    @Resource
    NotificationService notificationService;

    @GetMapping("")
    public BaseResponse<NotificationInfoResponse> getNotificationInfo(HttpServletRequest request, @RequestParam Long notificationId) {

        User currentUser = (User) request.getSession().getAttribute(UserConstant.USER_LOGIN_STATE);

        if (currentUser == null) throw new BusinessException(StatusCode.NOT_LOGIN);

        return ResponseUtils.success(notificationService.getNotificationInfo(request, notificationId));
    }
    @GetMapping("/list")
    public BaseResponse<Notification[]> getNotification(@NotNull HttpServletRequest request){

        // 获取用户登录态
        User currentUser = (User) request.getSession().getAttribute(UserConstant.USER_LOGIN_STATE);

        if (currentUser == null) throw new BusinessException(StatusCode.NOT_LOGIN);

        return ResponseUtils.success(notificationService.listNotification(request));

    }

}
