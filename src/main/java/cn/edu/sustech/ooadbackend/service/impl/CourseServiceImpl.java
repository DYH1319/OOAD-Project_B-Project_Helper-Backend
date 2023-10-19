package cn.edu.sustech.ooadbackend.service.impl;

import cn.edu.sustech.ooadbackend.common.StatusCode;
import cn.edu.sustech.ooadbackend.constant.UserConstant;
import cn.edu.sustech.ooadbackend.exception.BusinessException;
import cn.edu.sustech.ooadbackend.mapper.CourseMapper;
import cn.edu.sustech.ooadbackend.model.domain.Course;
import cn.edu.sustech.ooadbackend.model.domain.TeacherAssistantCourse;
import cn.edu.sustech.ooadbackend.model.domain.User;
import cn.edu.sustech.ooadbackend.model.domain.UserCourse;
import cn.edu.sustech.ooadbackend.model.request.CourseUpdateRequest;
import cn.edu.sustech.ooadbackend.service.CourseService;
import cn.edu.sustech.ooadbackend.service.TeacherAssistantCourseService;
import cn.edu.sustech.ooadbackend.service.UserCourseService;
import cn.edu.sustech.ooadbackend.service.UserService;
import cn.edu.sustech.ooadbackend.utils.ResponseUtils;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.function.Function;

/**
 * @className CourseServiceImpl
 * @version 1.0
 * @author DYH
 * @since 2023/10/9 12:41
 */
    
@Service
public class CourseServiceImpl extends ServiceImpl<CourseMapper, Course> implements CourseService{

    @Resource
    private UserService userService;

    @Resource
    private CourseMapper courseMapper;

    @Resource
    private UserCourseService userCourseService;

    @Resource
    private TeacherAssistantCourseService teacherAssistantCourseService;

    @Override
    public List<Course> listCourse(HttpServletRequest request) {
        // 获取用户登录态
        User currentUser = (User) request.getSession().getAttribute(UserConstant.USER_LOGIN_STATE);
        if (currentUser == null) throw new BusinessException(StatusCode.NOT_LOGIN, "用户未登录");

        if (currentUser.getUserRole() == UserConstant.STUDENT_ROLE){

            // 查询对应用户的课程关系
            List<UserCourse> userCourseList = userCourseService.listUserCourseByUserId(currentUser.getId());

            // 将课程关系转为课程id，方便后续查询
            List<Long> courseIdList = userCourseList.stream().map(UserCourse::getCourseId).toList();

            // 查询当前学生对应的课程列表
            List<Course> courseList = this.listByIds(courseIdList);
            if (courseList == null || courseList.isEmpty()) throw new BusinessException(StatusCode.NULL_ERROR, "查找不到用户的课程信息");
            return courseList.stream().map(this::getSafetyCourse).toList();

        } else if (currentUser.getUserRole() == UserConstant.TEACHER_ASSISTANT_ROLE) {

            // 获取教师助理对应的课程id列表
            List<TeacherAssistantCourse> teacherAssistantCourseList = teacherAssistantCourseService.listByTeacherAssistantId(currentUser.getId());

            // 将课程关系转为课程id，方便后续查询
            List<Long> courseIdList = teacherAssistantCourseList.stream().map(TeacherAssistantCourse::getCourseId).toList();

            // 查询助教的课程列表
            List<Course> courseList = this.listByIds(courseIdList);
            if (courseList == null || courseList.isEmpty()) throw new BusinessException(StatusCode.NULL_ERROR, "查找不到用户的课程信息");
            return courseList.stream().map(this::getSafetyCourse).toList();

        }else if (currentUser.getUserRole() == UserConstant.TEACHER_ROLE){

            // 查询教师的课程列表
            QueryWrapper<Course> courseQueryWrapper = new QueryWrapper<>();
            courseQueryWrapper.eq("teacher_id", currentUser.getId());
            List<Course> courseList = this.list(courseQueryWrapper);
            if (courseList == null || courseList.isEmpty()) throw new BusinessException(StatusCode.NULL_ERROR, "查找不到用户的课程信息");
            return courseList.stream().map(this::getSafetyCourse).toList();

        } else if (currentUser.getUserRole() == UserConstant.ADMIN_ROLE){

            // 查询所有课程列表
            List<Course> courseList = this.list();
            if (courseList == null || courseList.isEmpty()) throw new BusinessException(StatusCode.NULL_ERROR, "课程库当前无课程信息");
            return courseList.stream().map(this::getSafetyCourse).toList();

        }else{
            throw new BusinessException(StatusCode.UNKNOWN_ERROR, "用户会话数据错误");
        }
    }

    @Override
    @Transactional
    public Boolean updateCourse(CourseUpdateRequest courseUpdateRequest) {
        Course course = new Course();
        course.setId(courseUpdateRequest.getId());
        course.setTeacherId(courseUpdateRequest.getTeacherId());
        course.setCourseName(courseUpdateRequest.getCourseName());

        Boolean isUpdated = courseMapper.updateCourse(course);
        if (!isUpdated) throw new BusinessException(StatusCode.PARAMS_ERROR, "课程信息更新失败");

        // 校验助教Id列表是否合法
        QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
        userQueryWrapper.eq("user_role", UserConstant.TEACHER_ASSISTANT_ROLE);
        List<User> taUserList = userService.list(userQueryWrapper);
        Long[] toIdList = taUserList.stream().map(User::getId).toList().toArray(Long[] :: new);
        Long[] newtTaIdList = courseUpdateRequest.getTaIdList();
        if (!new HashSet<>(Arrays.asList(toIdList)).containsAll(Arrays.asList(newtTaIdList))) throw new BusinessException(StatusCode.PARAMS_ERROR, "助教列表中含有非法用户");

        teacherAssistantCourseService.removeByCourseId(courseUpdateRequest.getId());

        List<TeacherAssistantCourse> newTeacherAssistantCourses = Arrays.stream(newtTaIdList).map(aLong -> {
            TeacherAssistantCourse teacherAssistantCourse = new TeacherAssistantCourse();
            teacherAssistantCourse.setCourseId(courseUpdateRequest.getId());
            teacherAssistantCourse.setTeacherAssistantId(aLong);
            return teacherAssistantCourse;
        }).toList();

        Boolean saveBatch = teacherAssistantCourseService.saveBatch(newTeacherAssistantCourses);
        if (saveBatch != true) throw new BusinessException(StatusCode.PARAMS_ERROR, "课程信息更新失败");

        return saveBatch;
    }

    public Boolean deleteCourse(Long courseId){
        QueryWrapper<Course> courseQueryWrapper = new QueryWrapper<>();
        courseQueryWrapper.eq("course_id", courseId);
        this.remove()
    }

    private Course getSafetyCourse(Course course){
        Course safetyCourse = new Course();
        safetyCourse.setId(course.getId());
        safetyCourse.setCourseName(course.getCourseName());
        return safetyCourse;
    }

}
