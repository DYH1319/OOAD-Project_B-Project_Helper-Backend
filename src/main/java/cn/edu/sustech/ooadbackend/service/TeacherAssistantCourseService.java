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


}
