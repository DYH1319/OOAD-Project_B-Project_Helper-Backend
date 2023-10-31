package cn.edu.sustech.ooadbackend.service.impl;

import cn.edu.sustech.ooadbackend.common.StatusCode;
import cn.edu.sustech.ooadbackend.constant.UserConstant;
import cn.edu.sustech.ooadbackend.exception.BusinessException;
import cn.edu.sustech.ooadbackend.mapper.NotificationMapper;
import cn.edu.sustech.ooadbackend.model.domain.*;
import cn.edu.sustech.ooadbackend.model.response.NotificationInfoResponse;
import cn.edu.sustech.ooadbackend.service.*;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @className NotificationServiceImpl
 * @version 1.0
 * @author DYH
 * @since 2023/10/9 12:42
 */
    
@Service
public class NotificationServiceImpl extends ServiceImpl<NotificationMapper, Notification> implements NotificationService{

    @Resource
    UserNotificationService userNotificationService;
    @Resource
    ProjectService projectService;
    @Resource
    CourseService courseService;
    @Resource
    UserService userService;

    @Override
    public NotificationInfoResponse getNotificationInfo(HttpServletRequest request, Long notificationId) {

        User currentUser = (User) request.getSession().getAttribute(UserConstant.USER_LOGIN_STATE);

        QueryWrapper<UserNotification> userNotificationQueryWrapper = new QueryWrapper<>();
        userNotificationQueryWrapper.eq("receiver_id", currentUser.getId());
        userNotificationQueryWrapper.and(wrapper -> wrapper.eq("notification_id", notificationId));

        UserNotification notification = userNotificationService.getOne(userNotificationQueryWrapper);
        if (notification == null) throw new BusinessException(StatusCode.PARAMS_ERROR, "未能从当前用户通知库中获取该通知信息");


        QueryWrapper<Notification> notificationQueryWrapper = new QueryWrapper<>();
        notificationQueryWrapper.eq("id", notificationId);

        Notification currentNotification = this.getOne(notificationQueryWrapper);
        if (currentNotification == null) throw new BusinessException(StatusCode.PARAMS_ERROR, "查询不到该通知的信息");

        User sender = userService.getById(currentNotification.getSenderId());
        if (sender == null) throw new BusinessException(StatusCode.SYSTEM_ERROR, "通知发送方信息缺失");

        Course course = courseService.getById(currentNotification.getCourseId());
        if (course == null) throw new BusinessException(StatusCode.SYSTEM_ERROR, "通知所属课程信息缺失");

        Project project = projectService.getById(currentNotification.getProjectId());

        NotificationInfoResponse response = new NotificationInfoResponse();

        response.setMessage(currentNotification.getMessage());
        response.setId(currentNotification.getId());
        response.setIsRead(notification.getIsRead());
        response.setTitle(currentNotification.getTitle());
        response.setSendTime(currentNotification.getCreateTime());
        response.setSenderName(sender.getUsername());
        response.setCourseName(course.getCourseName());
        if (project != null) {
            response.setProjectName(project.getProjectName());
        }
        return response;
    }

    @Override
    public Notification[] listNotification(HttpServletRequest request) {

        User currentUser = (User) request.getSession().getAttribute(UserConstant.USER_LOGIN_STATE);

        if (currentUser.getUserRole() == UserConstant.TEACHER_ROLE || currentUser.getUserRole() == UserConstant.TEACHER_ASSISTANT_ROLE){

            // 获取当前用户作为接收方的所有通知
            QueryWrapper<UserNotification> notificationQueryWrapper = new QueryWrapper<>();
            notificationQueryWrapper.eq("receiver_id", currentUser.getId());
            List<UserNotification> notifications = userNotificationService.list(notificationQueryWrapper);
            List<Long> notificationsIdList = notifications.stream().map(UserNotification::getNotificationId).toList();

            // 获取当前用户作为发送方的所有通知
            QueryWrapper<Notification> notifyWrapper = new QueryWrapper<>();
            notifyWrapper.eq("sender_id", currentUser.getId());

            if (!notificationsIdList.isEmpty()){
                notifyWrapper.or(wrapper -> wrapper.in("id", notificationsIdList));
            }

            List<Notification> notificationsList = this.list(notifyWrapper);

            if (notificationsList.isEmpty()) throw new BusinessException(StatusCode.PARAMS_ERROR, "未找到有关当前用户的任何通知");

            return notificationsList.stream().map(this::getSimplifiedNotification).toArray(Notification[]::new);

        } else if (currentUser.getUserRole() == UserConstant.ADMIN_ROLE) {

            // 获取所有通知
            List<Notification> notificationList = this.list();

            if (notificationList.isEmpty()) throw new BusinessException(StatusCode.PARAMS_ERROR, "未找到任何通知");

            return notificationList.stream().map(this::getSimplifiedNotification).toArray(Notification[]::new);

        } else if (currentUser.getUserRole() == UserConstant.STUDENT_ROLE){

            // 获取当前用户作为接收方的所有通知
            QueryWrapper<UserNotification> notificationQueryWrapper = new QueryWrapper<>();
            notificationQueryWrapper.eq("receiver_id", currentUser.getId());
            List<UserNotification> notifications = userNotificationService.list(notificationQueryWrapper);
            List<Long> notificationsIdList = notifications.stream().map(UserNotification::getNotificationId).toList();

            if (notificationsIdList.isEmpty()) throw new BusinessException(StatusCode.PARAMS_ERROR, "未找到有关当前用户的任何信息");

            QueryWrapper<Notification> notifyWrapper = new QueryWrapper<>();
            notifyWrapper.in("id", notificationsIdList);
            List<Notification> notificationList = this.list(notifyWrapper);

            if (notificationList.isEmpty()) throw new BusinessException(StatusCode.PARAMS_ERROR, "未找到有关当前用户的任何信息");

            return notificationList.stream().map(this::getSimplifiedNotification).toArray(Notification[]::new);

        } else {
            throw new BusinessException(StatusCode.SYSTEM_ERROR, "用户权限不可用");
        }
    }



    private Notification getSimplifiedNotification(Notification notification){
        Notification simplifiedNotification = new Notification();

        simplifiedNotification.setId(notification.getId());
        simplifiedNotification.setTitle(notification.getTitle());

        return simplifiedNotification;
    }
}
