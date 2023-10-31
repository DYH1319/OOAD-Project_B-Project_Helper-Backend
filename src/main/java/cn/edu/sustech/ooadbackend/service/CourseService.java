package cn.edu.sustech.ooadbackend.service;

import cn.edu.sustech.ooadbackend.model.domain.Course;
import cn.edu.sustech.ooadbackend.model.domain.Notification;
import cn.edu.sustech.ooadbackend.model.domain.User;
import cn.edu.sustech.ooadbackend.model.request.CourseInsertRequest;
import cn.edu.sustech.ooadbackend.model.request.CourseUpdateRequest;
import cn.edu.sustech.ooadbackend.model.request.NotificationInsertRequest;
import cn.edu.sustech.ooadbackend.model.response.CourseInfoResponse;
import com.baomidou.mybatisplus.extension.service.IService;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

/**
 * @className CourseService
 * @version 1.0
 * @author DYH
 * @since 2023/10/9 12:41
 */
    
public interface CourseService extends IService<Course>{

    /**
     * 根据当前用户不同的权限列举课程信息
     * @param request HttpServletRequest
     * @return 课程的列表
     */
    public List<Course> listCourse(HttpServletRequest request);

    /**
     * 更具请求中的参数更新Course的信息
     * @param courseUpdateRequest 课程更新请求的包装类
     * @return 是否更新成功
     */
    public Boolean updateCourse(CourseUpdateRequest courseUpdateRequest);

    /**
     * 删除对应id的课程
     * @param courseId 课程ID
     * @return 是否删除完成
     */
    public Boolean deleteCourse(Long courseId);

    /**
     * 新增课程
     * @param courseInsertRequest 来自前段的课程信息
     * @return 是否删除完成
     */
    public Long insertCourse(CourseInsertRequest courseInsertRequest);

    /**
     * 检查用户是否为指定课程的老师
     * @param userId 待认证的用户
     * @param courseId 指定的课程id
     * @return 是否是该课程的老师
     */
    public Boolean isCourseTeacher(Long userId, Long courseId);

    /**
     * 向特定课程添加学生
     * @param studentIds 特定学生id列表
     * @param courseId 指定的课程id
     * @return 是否添加成功
     */
    public Boolean addCourseStudents(Long[] studentIds, Long courseId);


    /**
     * 向特定课程添加TA
     * @param taIds 特定TAid列表
     * @param courseId 指定的课程id
     * @return
     */
    public Boolean addCourseTas(Long[] taIds, Long courseId);

    /**
     * 向特定课程删除学生
     * @param studentIds 特定删除的学生id列表
     * @param courseId 指定的课程id
     * @return
     */
    public Boolean removeCourseStudents(Long[] studentIds, Long courseId);

    /**
     * 向特定课程删除TA
     * @param taIds 特定删除的TAid列表
     * @param courseId 指定的课程id
     * @return
     */
    public Boolean removeCourseTas(Long[] taIds, Long courseId);

    public CourseInfoResponse getCourseInfo(Long courseId);

    public Notification[] listCourseNotification(HttpServletRequest request, Long courseId);

    public Boolean removeNotification(Long notificationId);

    public Long insertNotification(Long senderId, NotificationInsertRequest notificationRequest);

}
