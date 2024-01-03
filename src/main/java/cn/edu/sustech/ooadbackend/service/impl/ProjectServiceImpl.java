package cn.edu.sustech.ooadbackend.service.impl;

import cn.edu.sustech.ooadbackend.common.StatusCode;
import cn.edu.sustech.ooadbackend.constant.UserConstant;
import cn.edu.sustech.ooadbackend.exception.BusinessException;
import cn.edu.sustech.ooadbackend.mapper.ProjectMapper;
import cn.edu.sustech.ooadbackend.model.domain.*;
import cn.edu.sustech.ooadbackend.model.request.NotificationInsertRequest;
import cn.edu.sustech.ooadbackend.model.request.ProjectInsertRequest;
import cn.edu.sustech.ooadbackend.model.request.ProjectUpdateRequest;
import cn.edu.sustech.ooadbackend.service.*;
import cn.edu.sustech.ooadbackend.utils.DateTimeFormatTransferUtils;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

/**
 * @author DYH
 * @version 1.0
 * @className ProjectServiceImpl
 * @since 2023/10/9 12:43
 */

@Service
public class ProjectServiceImpl extends ServiceImpl<ProjectMapper, Project> implements ProjectService {
    @Resource
    private ProjectService projectService;
    @Resource
    private UserProjectService userProjectService;
    
    @Resource
    private ProjectMapper projectMapper;
    @Resource
    private NotificationService notificationService;
    @Resource
    private UserNotificationService userNotificationService;
    @Resource
    private UserCourseService userCourseService;
    
    @Resource
    private TeacherAssistantCourseService teacherAssistantCourseService;
    
    @Override
    public List<Project> list(User user, Long courseId) {
        if (user.getUserRole() == UserConstant.STUDENT_ROLE) {
            // 查询对应用户的项目关系
            List<UserProject> userProjectList = userProjectService.listUserProjectByUserId(user.getId());
            // 将项目关系转为项目id
            List<Long> projectIdList = userProjectList.stream().map(UserProject::getProjectId).toList();
            List<Project> projectList = this.listByIds(projectIdList);
            QueryWrapper<Project> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("course_id", courseId);
            List<Project> thisCourseProject = this.list(queryWrapper);
            if (projectList == null) throw new BusinessException(StatusCode.NULL_ERROR, "查找不到用户的项目信息");
            projectList.retainAll(thisCourseProject);
            
            if (projectList.isEmpty()) throw new BusinessException(StatusCode.NULL_ERROR, "查找不到用户的项目信息");
            return projectList.stream().map(this::getSafetyProject).toList();
            
            
        } else {
            QueryWrapper<Project> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("course_id", courseId);
            List<Project> thisCourseProject = this.list(queryWrapper);
            // if (thisCourseProject == null || thisCourseProject.isEmpty()) throw new BusinessException(StatusCode.NULL_ERROR, "查找不到用户的课程信息");
            return thisCourseProject.stream().map(this::getSafetyProject).toList();
            
        }
    }
    
    @Override
    @Transactional
    public Long insertProject(ProjectInsertRequest projectInsertRequest) {
        Project project = new Project();
        project.setProjectName(projectInsertRequest.getProjectName());
        project.setCourseId(projectInsertRequest.getCourseId());
        project.setDescription(projectInsertRequest.getDescription());
        project.setGroupDeadline(DateTimeFormatTransferUtils.frontDateTime2BackDate(projectInsertRequest.getGroupDeadline()));
        project.setEndDeadline(DateTimeFormatTransferUtils.frontDateTime2BackDate(projectInsertRequest.getEndDeadline()));
        
        Boolean isInsert = this.save(project);
        if (!isInsert) throw new BusinessException(StatusCode.PARAMS_ERROR, "项目发布失败");
        return project.getId();
        
    }
    
    @Override
    @Transactional
    public Boolean updateProject(ProjectUpdateRequest projectUpdateRequest) {
        Project project = new Project();
        project.setId(projectUpdateRequest.getId());
        project.setProjectName(projectUpdateRequest.getProjectName());
        project.setDescription(projectUpdateRequest.getDescription());
        project.setGroupDeadline(DateTimeFormatTransferUtils.frontDateTime2BackDate(projectUpdateRequest.getGroupDeadline()));
        project.setEndDeadline(DateTimeFormatTransferUtils.frontDateTime2BackDate(projectUpdateRequest.getEndDeadline()));
        
        boolean isUpdated = projectMapper.updateProject(project);
        if (!isUpdated) throw new BusinessException(StatusCode.PARAMS_ERROR, "项目信息更新失败");
        
        return isUpdated;
    }
    
    @Override
    @Transactional
    public Boolean deleteProject(Long projectId) {
        // 删除project表中的信息
        QueryWrapper<Project> projectQueryWrapper = new QueryWrapper<>();
        projectQueryWrapper.eq("id", projectId);
        Boolean projectRemoved = this.remove(projectQueryWrapper);
        if (!projectRemoved) throw new BusinessException(StatusCode.PARAMS_ERROR, "项目信息删除失败");
        return projectRemoved;
    }
    
    @Override
    public Notification[] listNotification(HttpServletRequest request, Long projectId) {
        User currentUser = (User) request.getSession().getAttribute(UserConstant.USER_LOGIN_STATE);
        
        if (currentUser.getUserRole() == UserConstant.ADMIN_ROLE) {
            
            QueryWrapper<Notification> notificationQueryWrapper = new QueryWrapper<>();
            
            notificationQueryWrapper.eq("project_id", projectId);
            
            List<Notification> notificationList = notificationService.list(notificationQueryWrapper);
            if (notificationList.isEmpty())
                throw new BusinessException(StatusCode.PARAMS_ERROR, "未找到与该用户有关的通知");
            
            return notificationList.stream().map(notificationService::getSimplifiedNotification).toArray(Notification[]::new);
            
        } else if (currentUser.getUserRole() == UserConstant.TEACHER_ROLE || currentUser.getUserRole() == UserConstant.TEACHER_ASSISTANT_ROLE) {
            
            // 获取当前用户作为接收方的所有通知
            QueryWrapper<UserNotification> notificationQueryWrapper = new QueryWrapper<>();
            notificationQueryWrapper.eq("receiver_id", currentUser.getId());
            List<UserNotification> notifications = userNotificationService.list(notificationQueryWrapper);
            List<Long> notificationsIdList = notifications.stream().map(UserNotification::getNotificationId).toList();
            
            // 获取当前用户作为发送方的所有通知
            QueryWrapper<Notification> notifyWrapper = new QueryWrapper<>();
            notifyWrapper.eq("sender_id", currentUser.getId());
            
            if (!notificationsIdList.isEmpty()) {
                notifyWrapper.or(wrapper -> wrapper.in("id", notificationsIdList));
            }
            
            notifyWrapper.and(wrapper -> wrapper.eq("project_id", projectId));
            
            List<Notification> notificationList = notificationService.list(notifyWrapper);
            
            if (notificationList.isEmpty())
                throw new BusinessException(StatusCode.PARAMS_ERROR, "未找到与用户相关的通知");
            
            return notificationList.stream().map(notificationService::getSimplifiedNotification).toArray(Notification[]::new);
            
        } else if (currentUser.getUserRole() == UserConstant.STUDENT_ROLE) {
            
            // 获取当前用户作为接收方的所有通知
            QueryWrapper<UserNotification> notificationQueryWrapper = new QueryWrapper<>();
            notificationQueryWrapper.eq("receiver_id", currentUser.getId());
            List<UserNotification> notifications = userNotificationService.list(notificationQueryWrapper);
            List<Long> notificationsIdList = notifications.stream().map(UserNotification::getNotificationId).toList();
            
            if (notificationsIdList.isEmpty())
                throw new BusinessException(StatusCode.PARAMS_ERROR, "未找到有关当前用户的任何信息");
            
            QueryWrapper<Notification> notifyWrapper = new QueryWrapper<>();
            notifyWrapper.in("id", notificationsIdList);
            notifyWrapper.and(wrapper -> wrapper.eq("project_id", projectId));
            
            List<Notification> notificationList = notificationService.list(notifyWrapper);
            
            if (notificationList.isEmpty())
                throw new BusinessException(StatusCode.PARAMS_ERROR, "未找到有关当前用户的任何信息");
            
            return notificationList.stream().map(notificationService::getSimplifiedNotification).toArray(Notification[]::new);
            
        } else {
            throw new BusinessException(StatusCode.PARAMS_ERROR, "用户权限不可见");
        }
    }
    
    @Override
    public Boolean removeNotification(Long notificationId) {
        QueryWrapper<Notification> notificationQueryWrapper = new QueryWrapper<>();
        
        notificationQueryWrapper.eq("id", notificationId);
        
        boolean removed = notificationService.remove(notificationQueryWrapper);
        
        if (!removed) throw new BusinessException(StatusCode.SYSTEM_ERROR, "无法删除该通知");
        
        QueryWrapper<UserNotification> userNotificationQueryWrapper = new QueryWrapper<>();
        
        userNotificationQueryWrapper.eq("notification_id", notificationId);
        
        boolean removedUser = userNotificationService.remove(userNotificationQueryWrapper);
        
        if (!removedUser) throw new BusinessException(StatusCode.SYSTEM_ERROR, "无法删除该通知相关的用户内容");
        
        return true;
    }
    
    @Override
    public Long insertNotification(Long senderId, NotificationInsertRequest notificationRequest) {
        Notification newNotification = new Notification();
        
        newNotification.setTitle(notificationRequest.getTitle());
        newNotification.setMessage(notificationRequest.getMessage());
        newNotification.setSenderId(senderId);
        newNotification.setCourseId(notificationRequest.getCourseId());
        
        boolean saved = notificationService.save(newNotification);
        
        if (!saved) throw new BusinessException(StatusCode.SYSTEM_ERROR, "新建通知时发生错误");
        if (notificationRequest.getReceivers() != null && notificationRequest.getReceivers().length != 0) {
            
            List<UserNotification> userNotifications = Arrays.stream(notificationRequest.getReceivers()).map(receiverId -> {
                UserNotification userNotification = new UserNotification();
                userNotification.setIsRead((byte) 0);
                userNotification.setReceiverId(receiverId);
                userNotification.setNotificationId(newNotification.getId());
                return userNotification;
            }).toList();
            
            boolean b = userNotificationService.saveBatch(userNotifications);
            
            if (!b) throw new BusinessException(StatusCode.PARAMS_ERROR, "添加接收方失败");
        } else {
            // 获取该课程所有学生ID
            QueryWrapper<UserCourse> userCourseQueryWrapper = new QueryWrapper<>();
            
            userCourseQueryWrapper.eq("course_id", notificationRequest.getCourseId());
            
            List<UserCourse> studentList = userCourseService.list(userCourseQueryWrapper);
            
            List<Long> studentIdList = new java.util.ArrayList<>(studentList.stream().map(UserCourse::getUserId).toList());
            
            
            // 获取该课程所有TA的ID
            QueryWrapper<TeacherAssistantCourse> teacherAssistantCourseQueryWrapper = new QueryWrapper<>();
            
            teacherAssistantCourseQueryWrapper.eq("course_id", notificationRequest.getCourseId());
            
            List<TeacherAssistantCourse> taList = teacherAssistantCourseService.list(teacherAssistantCourseQueryWrapper);
            
            List<Long> studentIdList2 = taList.stream().map(TeacherAssistantCourse::getTeacherAssistantId).toList();
            
            studentIdList.addAll(studentIdList2);
            
            List<UserNotification> userNotifications = studentIdList.stream().map(receiverId -> {
                UserNotification userNotification = new UserNotification();
                userNotification.setIsRead((byte) 0);
                userNotification.setReceiverId(receiverId);
                userNotification.setNotificationId(newNotification.getId());
                return userNotification;
            }).toList();
            
            boolean b = userNotificationService.saveBatch(userNotifications);
            
            if (!b) throw new BusinessException(StatusCode.PARAMS_ERROR, "添加接收方失败");
            
        }
        
        return newNotification.getId();
    }
    
    
    public Project getSafetyProject(Project project) {
        Project newProject = new Project();
        newProject.setId(project.getId());
        newProject.setProjectName(project.getProjectName());
        newProject.setDescription(project.getDescription());
        newProject.setGroupDeadline(project.getGroupDeadline());
        newProject.setEndDeadline(project.getEndDeadline());
        return newProject;
        
    }
}
