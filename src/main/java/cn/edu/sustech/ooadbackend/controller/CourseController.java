package cn.edu.sustech.ooadbackend.controller;

import cn.edu.sustech.ooadbackend.common.BaseResponse;
import cn.edu.sustech.ooadbackend.common.StatusCode;
import cn.edu.sustech.ooadbackend.constant.UserConstant;
import cn.edu.sustech.ooadbackend.exception.BusinessException;
import cn.edu.sustech.ooadbackend.model.domain.Course;
import cn.edu.sustech.ooadbackend.model.domain.User;
import cn.edu.sustech.ooadbackend.model.request.CourseDeleteRequest;
import cn.edu.sustech.ooadbackend.model.request.CourseInsertRequest;
import cn.edu.sustech.ooadbackend.model.request.CourseModifyMembersRequest;
import cn.edu.sustech.ooadbackend.model.request.CourseUpdateRequest;
import cn.edu.sustech.ooadbackend.model.response.CourseInfoResponse;
import cn.edu.sustech.ooadbackend.service.CourseService;
import cn.edu.sustech.ooadbackend.service.TeacherAssistantCourseService;
import cn.edu.sustech.ooadbackend.service.UserCourseService;
import cn.edu.sustech.ooadbackend.utils.ResponseUtils;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;


/**
 * @className: CourseController
 * @Package: cn.edu.sustech.ooadbackend.controller
 * @Description:
 * @Author: Daniel
 * @Date: 2023/10/18 12:21
 * @Version: 1.0
 */
@RestController
@RequestMapping("/course")
public class CourseController {
    @Resource
    private CourseService courseService;

    @Resource
    private TeacherAssistantCourseService teacherAssistantCourseService;

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
     * @param courseDeleteRequest 待删除的课程请求
     * @return 是否成功删除
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteCourse(HttpServletRequest request, @RequestBody CourseDeleteRequest courseDeleteRequest){

        // 校验参数
        if (courseDeleteRequest == null || courseDeleteRequest.getCourseId() <= 0) throw new BusinessException(StatusCode.PARAMS_ERROR, "删除课程参数出错");

        // 获取用户登录态
        User currentUser = (User) request.getSession().getAttribute(UserConstant.USER_LOGIN_STATE);

        // 确认用户是否登录
        if (currentUser == null) throw new BusinessException(StatusCode.NOT_LOGIN);

        // 确认用户是否有管理员权限
        if (currentUser.getUserRole() != UserConstant.ADMIN_ROLE) throw new BusinessException(StatusCode.NO_AUTH, "非管理员用户不能修改课程信息");

        Boolean deleted = courseService.deleteCourse(courseDeleteRequest.getCourseId());
        return ResponseUtils.success(deleted, "成功删除课程信息");
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

        Long insertCourse = courseService.insertCourse(courseId);
        return ResponseUtils.success(insertCourse, "成功新增课程");

    }

    /**
     * 新增课程的ta
     * @param request HttpServletRequest 用户检验用户权限
     * @param courseAddTaRequest 增加的课程ta信息
     * @return 是否新增成功
     */
    @PostMapping("/add/ta")
    public BaseResponse<Boolean> addCourseTa(HttpServletRequest request, @RequestBody CourseModifyMembersRequest courseAddTaRequest){

        // 获取用户登录态
        User currentUser = (User) request.getSession().getAttribute(UserConstant.USER_LOGIN_STATE);

        // 确认用户是否登录
        if (currentUser == null) throw new BusinessException(StatusCode.NOT_LOGIN);

        // 确认用户是否为管理员或者课程老师
        if (!(currentUser.getUserRole() == UserConstant.ADMIN_ROLE ||(currentUser.getUserRole() == UserConstant.TEACHER_ROLE && courseService.isCourseTeacher(currentUser.getId(), courseAddTaRequest.getCourseId())))) throw new BusinessException(StatusCode.NO_AUTH, "您无权修改该课程的信息");

        Boolean b = courseService.addCourseTas(courseAddTaRequest.getTaId(), courseAddTaRequest.getCourseId());

        return ResponseUtils.success(b);
    }

    /**
     * 新增指定课程的学生
     * @param request HttpServletRequest 用户查询用户的权限
     * @param courseAddStudentRequest 增加的课程学生信息
     * @return 是否增加成功
     */
    @PostMapping("/add/student")
    public BaseResponse<Boolean> addCourseStudent(HttpServletRequest request, @RequestBody CourseModifyMembersRequest courseAddStudentRequest){

        // 获取用户登录态
        User currentUser = (User) request.getSession().getAttribute(UserConstant.USER_LOGIN_STATE);

        // 确认用户是否登录
        if (currentUser == null) throw new BusinessException(StatusCode.NOT_LOGIN);

        // 确认是否为该课程老师，管理员或者该课程TA
        if (!(currentUser.getUserRole() == UserConstant.ADMIN_ROLE || (currentUser.getUserRole() == UserConstant.TEACHER_ROLE && courseService.isCourseTeacher(currentUser.getId(), courseAddStudentRequest.getCourseId())) || (currentUser.getUserRole() == UserConstant.TEACHER_ASSISTANT_ROLE && teacherAssistantCourseService.isCourseTa(currentUser.getId(), courseAddStudentRequest.getCourseId())))) throw new BusinessException(StatusCode.NO_AUTH, "您无权修改该课程的信息");

        Boolean b = courseService.addCourseStudents(courseAddStudentRequest.getStudentId(), courseAddStudentRequest.getCourseId());

        return ResponseUtils.success(b);
    }

    /**
     * 删除指定课程的ta
     * @param request HttpServletRequest 用户查询用户的权限
     * @param courseRemoveTaRequest 移除的课程ta信息
     * @return 是否删除成功
     */
    @PostMapping("/remove/ta")
    public BaseResponse<Boolean> removeCourseTa(HttpServletRequest request, @RequestBody CourseModifyMembersRequest courseRemoveTaRequest){

        // 获取用户登录态
        User currentUser = (User) request.getSession().getAttribute(UserConstant.USER_LOGIN_STATE);

        // 确认用户是否登录
        if (currentUser == null) throw new BusinessException(StatusCode.NOT_LOGIN);

        // 确认是否为该课程老师或管理员
        if (!(currentUser.getUserRole() == UserConstant.ADMIN_ROLE ||(currentUser.getUserRole() == UserConstant.TEACHER_ROLE && courseService.isCourseTeacher(currentUser.getId(), courseRemoveTaRequest.getCourseId())))) throw new BusinessException(StatusCode.NO_AUTH, "您无权修改该课程的信息");

        Boolean b = courseService.removeCourseTas(courseRemoveTaRequest.getTaId(), courseRemoveTaRequest.getCourseId());

        return ResponseUtils.success(b);
    }

    /**
     * 删除特定课程的学生
     * @param request HttpServletRequest 用户查询用户的权限
     * @param courseRemoveStudentRequest 移除的课程学生信息
     * @return 是否删除成功
     */
    @PostMapping("/remove/student")
    public BaseResponse<Boolean> removeCourseStudent(HttpServletRequest request, @RequestBody CourseModifyMembersRequest courseRemoveStudentRequest){

        // 获取用户登录态
        User currentUser = (User) request.getSession().getAttribute(UserConstant.USER_LOGIN_STATE);

        // 确认用户是否登录
        if (currentUser == null) throw new BusinessException(StatusCode.NOT_LOGIN);

        // 确认是否为课程老师，管理员或者课程TA
        if (!(currentUser.getUserRole() == UserConstant.ADMIN_ROLE || (currentUser.getUserRole() == UserConstant.TEACHER_ROLE && courseService.isCourseTeacher(currentUser.getId(), courseRemoveStudentRequest.getCourseId())) || (currentUser.getUserRole() == UserConstant.TEACHER_ASSISTANT_ROLE && teacherAssistantCourseService.isCourseTa(currentUser.getId(), courseRemoveStudentRequest.getCourseId())))) throw new BusinessException(StatusCode.NO_AUTH, "您无权修改该课程的信息");

        Boolean b = courseService.removeCourseStudents(courseRemoveStudentRequest.getStudentId(), courseRemoveStudentRequest.getCourseId());

        return ResponseUtils.success(b);
    }

    @GetMapping("")
    public BaseResponse<CourseInfoResponse> getCourseInfo(HttpServletRequest request, @RequestParam Long courseId){

        // 获取当前用户登录态
        User currentUser = (User) request.getSession().getAttribute(UserConstant.USER_LOGIN_STATE);

        if (currentUser == null) throw new BusinessException(StatusCode.NOT_LOGIN, "未登录");

        // 检查课程查询参数
        if (courseId <= 0L) throw new BusinessException(StatusCode.PARAMS_ERROR, "课程查询参数错误");

        return ResponseUtils.success(courseService.getCourseInfo(courseId));

    }
}
