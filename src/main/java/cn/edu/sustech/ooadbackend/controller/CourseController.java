package cn.edu.sustech.ooadbackend.controller;

import cn.edu.sustech.ooadbackend.common.BaseResponse;
import cn.edu.sustech.ooadbackend.common.StatusCode;
import cn.edu.sustech.ooadbackend.constant.UserConstant;
import cn.edu.sustech.ooadbackend.exception.BusinessException;
import cn.edu.sustech.ooadbackend.model.domain.Course;
import cn.edu.sustech.ooadbackend.model.domain.User;
import cn.edu.sustech.ooadbackend.model.request.CourseInsertRequest;
import cn.edu.sustech.ooadbackend.model.request.CourseUpdateRequest;
import cn.edu.sustech.ooadbackend.service.CourseService;
import cn.edu.sustech.ooadbackend.service.TeacherAssistantCourseService;
import cn.edu.sustech.ooadbackend.service.UserCourseService;
import cn.edu.sustech.ooadbackend.utils.ResponseUtils;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.web.bind.annotation.*;

import java.util.List;


/**
 * @className: CourseController
 * @Package: cn.edu.sustech.ooadbackend.controller
 * @Description:
 * @Author: Daniel
 * @Date: 2023/10/18 12:21
 * @Version:1.0
 */
@RestController
@RequestMapping("/course")
public class CourseController {
    @Resource
    private CourseService courseService;

    /**
     * 获取课程列表
     * @param request HttpServletRequest
     * @return 用户列表
     */
    @GetMapping("/list")
    public BaseResponse<Course[]> listCourse(HttpServletRequest request){
        if (request == null) throw new BusinessException(StatusCode.SYSTEM_ERROR);
        List<Course> courseList = courseService.listCourse(request);
        return ResponseUtils.success(courseList.toArray(Course[] :: new));
    }

    /**
     * 根据id更新课程信息
     * @param request HttpServletRequest
     * @param courseUpdateRequest 更新的课程信息
     * @return 是否成功更新
     */
    @PostMapping("/update")
    public BaseResponse<Boolean> updateCourse(HttpServletRequest request, @RequestBody CourseUpdateRequest courseUpdateRequest){

        // 校验参数
        if (courseUpdateRequest == null) throw new BusinessException(StatusCode.PARAMS_ERROR, "更新课程参数出错");

        // 获取用户登录态
        User currentUser = (User) request.getSession().getAttribute(UserConstant.USER_LOGIN_STATE);

        // 确认用户是否登录
        if (currentUser == null) throw new BusinessException(StatusCode.NOT_LOGIN);

        // 确认用户是否有管理员权限
        if (currentUser.getUserRole() != UserConstant.ADMIN_ROLE) throw new BusinessException(StatusCode.NO_AUTH, "非管理员用户不能修改课程信息");

        Boolean updated = courseService.updateCourse(courseUpdateRequest);
        return ResponseUtils.success(updated);
    }

    /**
     * 删除课程信息
     * @param request HttpServletRequest
     * @param courseId 待删除的课程ID
     * @return 是否成功删除
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteCourse(HttpServletRequest request, @RequestBody Long courseId){

        // 校验参数
        if (courseId == null || courseId <= 0) throw new BusinessException(StatusCode.PARAMS_ERROR, "删除课程参数出错");

        // 获取用户登录态
        User currentUser = (User) request.getSession().getAttribute(UserConstant.USER_LOGIN_STATE);

        // 确认用户是否登录
        if (currentUser == null) throw new BusinessException(StatusCode.NOT_LOGIN);

        // 确认用户是否有管理员权限
        if (currentUser.getUserRole() != UserConstant.ADMIN_ROLE) throw new BusinessException(StatusCode.NO_AUTH, "非管理员用户不能修改课程信息");

    }

    /**
     * 新增课程信息
     * @param request HttpServletRequest
     * @param courseId 新增课程信息的包装
     * @return 新增课程ID
     */
    @PostMapping("/insert")
    public BaseResponse<Long> insertCourse(HttpServletRequest request, @RequestBody CourseInsertRequest courseId){

        // 获取用户登录态
        User currentUser = (User) request.getSession().getAttribute(UserConstant.USER_LOGIN_STATE);

        // 确认用户是否登录
        if (currentUser == null) throw new BusinessException(StatusCode.NOT_LOGIN);

        // 确认用户是否有管理员权限
        if (currentUser.getUserRole() != UserConstant.ADMIN_ROLE) throw new BusinessException(StatusCode.NO_AUTH, "非管理员用户不能修改课程信息");
        return null;
    }

}
