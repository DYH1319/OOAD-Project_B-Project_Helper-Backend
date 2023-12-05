package cn.edu.sustech.ooadbackend.controller;
import cn.edu.sustech.ooadbackend.common.BaseResponse;
import cn.edu.sustech.ooadbackend.common.StatusCode;
import cn.edu.sustech.ooadbackend.constant.UserConstant;
import cn.edu.sustech.ooadbackend.exception.BusinessException;
import cn.edu.sustech.ooadbackend.model.domain.Assignment;
import cn.edu.sustech.ooadbackend.model.domain.Project;
import cn.edu.sustech.ooadbackend.model.domain.User;
import cn.edu.sustech.ooadbackend.model.request.*;
import cn.edu.sustech.ooadbackend.service.ProjectService;
import cn.edu.sustech.ooadbackend.service.UserService;
import cn.edu.sustech.ooadbackend.utils.ResponseUtils;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;
@RestController
@RequestMapping("/project")
public class ProjectController {

    @Resource
    private  ProjectService projectService;
    /**
     * 获取项目列表
     * @param courseId
     * @param request HttpServletRequest
     * @return 项目列表
     */
    @GetMapping("/list")
    public BaseResponse<Project[]> list(HttpServletRequest request, @RequestParam Long courseId) {
        if (request == null) throw new BusinessException(StatusCode.SYSTEM_ERROR);
        User currentUser = (User) request.getSession().getAttribute(UserConstant.USER_LOGIN_STATE);
        if (currentUser == null) throw new BusinessException(StatusCode.NOT_LOGIN, "用户未登录");

        List<Project> projectList = projectService.list(currentUser,courseId);
        return ResponseUtils.success(projectList.toArray(Project[]::new));
    }
    /**
     * 新增项目信息
     * @param request HttpServletRequest
     * @param projectId 新增项目信息的包装
     * @return 新增项目ID
     */
    @PostMapping("/insert")
    public BaseResponse<Long> insert(HttpServletRequest request, @RequestBody ProjectInsertRequest projectId){

        // 获取用户登录态
        User currentUser = (User) request.getSession().getAttribute(UserConstant.USER_LOGIN_STATE);

        // 确认用户是否登录
        if (currentUser == null) throw new BusinessException(StatusCode.NOT_LOGIN);

        // 确认用户是否有教师助理权限
        if (currentUser.getUserRole() < UserConstant.TEACHER_ASSISTANT_ROLE) throw new BusinessException(StatusCode.NO_AUTH, "非教师助理用户不能修改课程信息");

        Long insertCourse = projectService.insertProject(projectId);
        return ResponseUtils.success(insertCourse, "成功新增项目");

    }
    /**
     * 根据id更新项目信息
     * @param request HttpServletRequest
     * @param projectUpdateRequest 更新的课程信息
     * @return 是否成功更新
     */
    @PostMapping("/update")
    public BaseResponse<Boolean> update(HttpServletRequest request, @RequestBody ProjectUpdateRequest projectUpdateRequest){

        // 校验参数
        if (projectUpdateRequest == null) throw new BusinessException(StatusCode.PARAMS_ERROR, "更新课程参数出错");

        // 获取用户登录态
        User currentUser = (User) request.getSession().getAttribute(UserConstant.USER_LOGIN_STATE);

        // 确认用户是否登录
        if (currentUser == null) throw new BusinessException(StatusCode.NOT_LOGIN);

        // 确认用户是否有教师助理权限
        if (currentUser.getUserRole() < UserConstant.TEACHER_ASSISTANT_ROLE) throw new BusinessException(StatusCode.NO_AUTH, "非教师助理不能修改课程信息");

        Boolean updated = projectService.updateProject(projectUpdateRequest);
        return ResponseUtils.success(updated);
    }
    /**
     * 删除项目信息
     * @param request HttpServletRequest
     * @param id 待删除的项目请求
     * @return 是否成功删除
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> delete(HttpServletRequest request, @RequestBody Long id){

        // 校验参数
        if (id == null || id <= 0) throw new BusinessException(StatusCode.PARAMS_ERROR, "删除课程参数出错");

        // 获取用户登录态
        User currentUser = (User) request.getSession().getAttribute(UserConstant.USER_LOGIN_STATE);

        // 确认用户是否登录
        if (currentUser == null) throw new BusinessException(StatusCode.NOT_LOGIN);

        // 确认用户是否有教师助理权限
        if (currentUser.getUserRole() < UserConstant.TEACHER_ASSISTANT_ROLE) throw new BusinessException(StatusCode.NO_AUTH, "非教师助理用户不能修改课程信息");

        Boolean deleted = projectService.deleteProject(id);
        return ResponseUtils.success(deleted, "成功删除项目信息");
    }



}
