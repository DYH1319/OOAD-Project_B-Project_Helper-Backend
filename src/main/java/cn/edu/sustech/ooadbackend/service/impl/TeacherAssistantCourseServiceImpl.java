package cn.edu.sustech.ooadbackend.service.impl;

import cn.edu.sustech.ooadbackend.common.StatusCode;
import cn.edu.sustech.ooadbackend.exception.BusinessException;
import cn.edu.sustech.ooadbackend.mapper.TeacherAssistantCourseMapper;
import cn.edu.sustech.ooadbackend.model.domain.TeacherAssistantCourse;
import cn.edu.sustech.ooadbackend.service.TeacherAssistantCourseService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @className TeacherAssistantCourseServiceImpl
 * @version 1.0
 * @author DYH
 * @since 2023/10/9 12:43
 */
    
@Service
public class TeacherAssistantCourseServiceImpl extends ServiceImpl<TeacherAssistantCourseMapper, TeacherAssistantCourse> implements TeacherAssistantCourseService{

    @Override
    public List<TeacherAssistantCourse> listByTeacherAssistantId(Long id) {
        // 获取教师助理对应的课程关系列表
        QueryWrapper<TeacherAssistantCourse> teacherAssistantCourseQueryWrapper = new QueryWrapper<>();
        teacherAssistantCourseQueryWrapper.eq("teacher_assistant_id", id);
        List<TeacherAssistantCourse> teacherAssistantCourseList = this.list(teacherAssistantCourseQueryWrapper);
        if (teacherAssistantCourseList == null || teacherAssistantCourseList.isEmpty()) throw new BusinessException(StatusCode.NULL_ERROR, "查找不到用户的课程信息");
        return teacherAssistantCourseList;
    }

    @Override
    public List<TeacherAssistantCourse> listByCourseId(Long id) {
        // 获取课程对应的课程关系列表
        QueryWrapper<TeacherAssistantCourse> teacherAssistantCourseQueryWrapper = new QueryWrapper<>();
        teacherAssistantCourseQueryWrapper.eq("course_id", id);
        List<TeacherAssistantCourse> teacherAssistantCourseList = this.list(teacherAssistantCourseQueryWrapper);
        if (teacherAssistantCourseList == null || teacherAssistantCourseList.isEmpty()) throw new BusinessException(StatusCode.NULL_ERROR, "查找不到课程信息");
        return teacherAssistantCourseList;
    }

    @Override
    public Boolean removeByCourseId(Long id) {
        QueryWrapper<TeacherAssistantCourse> teacherAssistantCourseQueryWrapper = new QueryWrapper<>();
        teacherAssistantCourseQueryWrapper.eq("course_id", id);
        return this.remove(teacherAssistantCourseQueryWrapper);
    }

    @Override
    public boolean isCourseTa(Long userId, Long courseId) {

        // 从数据库teacher_assistant_course中查询是否存在userId和courseId符合的数据段
        QueryWrapper<TeacherAssistantCourse> courseQueryWrapper = new QueryWrapper<>();
        courseQueryWrapper.eq("course_id", courseId);
        courseQueryWrapper.and(wrapper -> wrapper.eq("teacher_assistant_id", userId));

        TeacherAssistantCourse targetTeacherAssistantCourse = this.getOne(courseQueryWrapper);

        if (targetTeacherAssistantCourse == null) {
            return false;
        } else {
            return true;
        }

    }
}
