package cn.edu.sustech.ooadbackend.service;

import cn.edu.sustech.ooadbackend.model.domain.Notification;
import cn.edu.sustech.ooadbackend.model.domain.Project;
import cn.edu.sustech.ooadbackend.model.domain.User;
import cn.edu.sustech.ooadbackend.model.request.NotificationInsertRequest;
import cn.edu.sustech.ooadbackend.model.request.ProjectInsertRequest;
import cn.edu.sustech.ooadbackend.model.request.ProjectUpdateRequest;
import cn.edu.sustech.ooadbackend.model.request.UserInsertRequest;
import com.baomidou.mybatisplus.extension.service.IService;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

/**
 * @className ProjectService
 * @version 1.0
 * @author DYH
 * @since 2023/10/9 12:43
 */
    
public interface ProjectService extends IService<Project>{
    List<Project> list(User user,Long courseId);
    Long insertProject(ProjectInsertRequest projectInsertRequest);
    Boolean updateProject(ProjectUpdateRequest projectUpdateRequest);
    Boolean deleteProject(Long projectId);
    Notification[] listNotification(HttpServletRequest request,Long projectId);
    Boolean removeNotification(Long notificationId);
    Long insertNotification(Long senderId, NotificationInsertRequest notificationRequest);



}
