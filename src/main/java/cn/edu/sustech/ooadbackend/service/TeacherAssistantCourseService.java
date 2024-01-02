package cn.edu.sustech.ooadbackend.service;

import cn.edu.sustech.ooadbackend.model.domain.TeacherAssistantCourse;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * @className TeacherAssistantCourseService
 * @version 1.0
 * @author DYH
 * @since 2023/10/9 12:43
 */
    
public interface TeacherAssistantCourseService extends IService<TeacherAssistantCourse>{

    List<TeacherAssistantCourse> listByTeacherAssistantId(Long id);

    List<TeacherAssistantCourse> listByCourseId(Long id);

    Boolean removeByCourseId(Long id);

    /**
     * 检查用户是否为指定课程的TA
     * @param userId 待认证的用户
     * @param courseId 指定的课程id
     * @return 是否是该课程的TA
     */
    public boolean isCourseTa(Long userId, Long courseId);


}
