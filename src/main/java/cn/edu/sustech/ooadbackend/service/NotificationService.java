package cn.edu.sustech.ooadbackend.service;

import cn.edu.sustech.ooadbackend.model.domain.Notification;
import cn.edu.sustech.ooadbackend.model.response.NotificationInfoResponse;
import com.baomidou.mybatisplus.extension.service.IService;
import jakarta.servlet.http.HttpServletRequest;

/**
 * @className NotificationService
 * @version 1.0
 * @author DYH
 * @since 2023/10/9 12:42
 */
    
public interface NotificationService extends IService<Notification>{

    /**
     * 获取指定通知的详细信息
     * @param request HttpServletRequest
     * @param notificationId 希望查询的通知ID
     * @return 有关通知的详细信息
     */
    public NotificationInfoResponse getNotificationInfo(HttpServletRequest request, Long notificationId);

    public Notification[] listNotification(HttpServletRequest request);


}
