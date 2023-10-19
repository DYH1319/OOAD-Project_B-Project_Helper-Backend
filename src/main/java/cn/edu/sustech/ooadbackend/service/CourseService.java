package cn.edu.sustech.ooadbackend.service;

import cn.edu.sustech.ooadbackend.model.domain.Course;
import cn.edu.sustech.ooadbackend.model.request.CourseInsertRequest;
import cn.edu.sustech.ooadbackend.model.request.CourseUpdateRequest;
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

}
