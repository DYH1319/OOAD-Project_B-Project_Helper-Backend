package cn.edu.sustech.ooadbackend.controller;
import cn.edu.sustech.ooadbackend.common.BaseResponse;
import cn.edu.sustech.ooadbackend.common.StatusCode;
import cn.edu.sustech.ooadbackend.constant.UserConstant;
import cn.edu.sustech.ooadbackend.exception.BusinessException;
import cn.edu.sustech.ooadbackend.model.domain.*;
import cn.edu.sustech.ooadbackend.model.request.*;
import cn.edu.sustech.ooadbackend.model.response.AssignmentInfoResponse;
import cn.edu.sustech.ooadbackend.model.response.CourseInfoResponse;
import cn.edu.sustech.ooadbackend.model.response.GroupInfoResponse;
import cn.edu.sustech.ooadbackend.service.AssignmentService;
import cn.edu.sustech.ooadbackend.service.CourseService;
import cn.edu.sustech.ooadbackend.service.NotificationService;
import cn.edu.sustech.ooadbackend.service.TeacherAssistantCourseService;
import cn.edu.sustech.ooadbackend.utils.ResponseUtils;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

/**
 * @className: AssignmentController
 * @Package: cn.edu.sustech.ooadbackend.controller
 * @Version: 1.0
 */
@RestController
@RequestMapping("/assignment")
public class AssignmentController {
    @Resource
    private AssignmentService assignmentService;
    @Resource
    private CourseService courseService;

    /**
     * 获取作业列表
     *
     * @param request HttpServletRequest
     * @return 作业列表
     */
    @GetMapping("/list")
    public BaseResponse<Assignment[]> listAssignment(HttpServletRequest request, @RequestParam Long courseId) {
        if (request == null) throw new BusinessException(StatusCode.SYSTEM_ERROR);
        User currentUser = (User) request.getSession().getAttribute(UserConstant.USER_LOGIN_STATE);
        if (currentUser == null) throw new BusinessException(StatusCode.NOT_LOGIN, "用户未登录");
        if (currentUser.getUserRole() < UserConstant.TEACHER_ASSISTANT_ROLE)
            throw new BusinessException(StatusCode.NO_AUTH, "权限不足，不能查询作业信息");
        List<Assignment> assignmentList = assignmentService.list(courseId);
        return ResponseUtils.success(assignmentList.toArray(Assignment[]::new));
    }

    /**
     * 发布作业信息
     *
     * @param request                 HttpServletRequest
     * @param assignmentInsertRequest
     * @return 新增用户ID
     */
    @PostMapping("/insert")
    public BaseResponse<Long> insert(HttpServletRequest request, @RequestBody AssignmentInsertRequest assignmentInsertRequest) {

        // 获取用户登录态
        User currentUser = (User) request.getSession().getAttribute(UserConstant.USER_LOGIN_STATE);

        // 确认用户是否登录
        if (currentUser == null) throw new BusinessException(StatusCode.NOT_LOGIN);

        // 确认用户是否有管理员权限
        if (currentUser.getUserRole() < UserConstant.TEACHER_ASSISTANT_ROLE)
            throw new BusinessException(StatusCode.NO_AUTH, "非教师助理用户不能发布作业");

        Long insertassignment = assignmentService.insertAssignment(assignmentInsertRequest);
        return ResponseUtils.success(insertassignment, "成功发布作业");

    }

    /**
     * 根据id更新作业信息
     *
     * @param request             HttpServletRequest
     * @param assignmentUpdateRequest 更新的作业信息
     * @return 是否成功更新
     */
    @PostMapping("/update")
    public BaseResponse<Boolean> update(HttpServletRequest request, @RequestBody AssignmentUpdateRequest assignmentUpdateRequest) {

        // 校验参数
        if (assignmentUpdateRequest == null) throw new BusinessException(StatusCode.PARAMS_ERROR, "更新作业参数出错");

        // 获取用户登录态
        User currentUser = (User) request.getSession().getAttribute(UserConstant.USER_LOGIN_STATE);

        // 确认用户是否登录
        if (currentUser == null) throw new BusinessException(StatusCode.NOT_LOGIN);

        // 确认用户是否有教师助理权限
        if (currentUser.getUserRole() < UserConstant.TEACHER_ASSISTANT_ROLE)
            throw new BusinessException(StatusCode.NO_AUTH, "非教师助理用户不能更新作业");

        Boolean updated = assignmentService.updateAssignment(assignmentUpdateRequest);
        return ResponseUtils.success(updated);
    }

    /**
     * 删除作业信息
     * @param request HttpServletRequest
     * @param id 待删除的课程请求
     * @return 是否成功删除
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> delete(HttpServletRequest request, @RequestBody Long id){

        // 校验参数
        if (id == null || id <= 0) throw new BusinessException(StatusCode.PARAMS_ERROR, "删除作业参数出错");

        // 获取用户登录态
        User currentUser = (User) request.getSession().getAttribute(UserConstant.USER_LOGIN_STATE);

        // 确认用户是否登录
        if (currentUser == null) throw new BusinessException(StatusCode.NOT_LOGIN);

        // 确认用户是否有管理员权限
        if (currentUser.getUserRole() < UserConstant.TEACHER_ASSISTANT_ROLE) throw new BusinessException(StatusCode.NO_AUTH, "非教师助理用户不能修改作业信息");

        Boolean deleted = assignmentService.deleteAssignment(id);
        return ResponseUtils.success(deleted, "成功删除作业信息");
    }
    @GetMapping("/")
    public BaseResponse<AssignmentInfoResponse> groupInfo (HttpServletRequest request, @RequestParam Long assignmentId){
        User currentUser = (User) request.getSession().getAttribute(UserConstant.USER_LOGIN_STATE);

        if (currentUser == null) throw new BusinessException(StatusCode.NOT_LOGIN);
        AssignmentInfoResponse response = new AssignmentInfoResponse();
        Assignment target = assignmentService.getById(assignmentId);
        Long courseId = target.getCourseId();

        //查询用户是否有权限
        if (courseService.checkCourseEnroll(currentUser.getId(), courseId)) {
            response.setTitle(target.getTitle());
            response.setDescription(target.getDescription());
            response.setStartTime(target.getStartTime());
            response.setEndTime(target.getEndTime());
            response.setAssignmentType(target.getAssignmentType());

        } else throw new BusinessException(StatusCode.NO_AUTH, "当前用户无权访问该作业信息");
        return ResponseUtils.success(response);
    }
    @PostMapping("/detail/update")
    public BaseResponse<Boolean> assignmentDetailUpdate(HttpServletRequest request,  @RequestBody AssignmentDetailUpdateRequest updateRequest){

        // 检查用户态以及用户权限
        User currentUser = (User) request.getSession().getAttribute(UserConstant.USER_LOGIN_STATE);

        if (currentUser == null) throw new BusinessException(StatusCode.NOT_LOGIN);

        Assignment target = assignmentService.getById(updateRequest.getId());

        Long courseId = target.getCourseId();

        if ((currentUser.getUserRole() == UserConstant.ADMIN_ROLE || currentUser.getUserRole() == UserConstant.TEACHER_ASSISTANT_ROLE || currentUser.getUserRole() == UserConstant.TEACHER_ROLE ) && courseService.checkCourseEnroll(currentUser.getId(), courseId)) {
            target.setId(updateRequest.getId());
            target.setTitle(updateRequest.getTitle());
            target.setDescription(updateRequest.getDescription());
            target.setStartTime(updateRequest.getStartTime());
            target.setEndTime(updateRequest.getEndTime());
            target.setAssignmentType(updateRequest.getAssignmentType());
            boolean b = assignmentService.updateById(target);
            if (!b) throw new BusinessException(StatusCode.SYSTEM_ERROR, "修改作业详细信息失败");
        } else throw new BusinessException(StatusCode.NO_AUTH, "当前用户无权限修改作业信息");
        return ResponseUtils.success(true);

    }


}