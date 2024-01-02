package cn.edu.sustech.ooadbackend.controller;

import cn.edu.sustech.ooadbackend.common.BaseResponse;
import cn.edu.sustech.ooadbackend.common.StatusCode;
import cn.edu.sustech.ooadbackend.constant.UserConstant;
import cn.edu.sustech.ooadbackend.exception.BusinessException;
import cn.edu.sustech.ooadbackend.model.domain.Group;
import cn.edu.sustech.ooadbackend.model.domain.Project;
import cn.edu.sustech.ooadbackend.model.domain.User;
import cn.edu.sustech.ooadbackend.model.domain.UserGroup;
import cn.edu.sustech.ooadbackend.model.request.GroupDetailUpdateRequest;
import cn.edu.sustech.ooadbackend.model.request.GroupUserRequest;
import cn.edu.sustech.ooadbackend.model.response.GroupInfoResponse;
import cn.edu.sustech.ooadbackend.service.*;
import cn.edu.sustech.ooadbackend.utils.ResponseUtils;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

/**
 * @author DYH
 * @version 1.0
 * @className GroupController
 * @since 2024/1/2 17:37
 */
@RestController
@RequestMapping("/group")
public class GroupController {
    
    @Resource
    private UserGroupService userGroupService;
    @Resource
    private ProjectService projectService;
    @Resource
    private CourseService courseService;
    @Resource
    private GroupService groupService;
    
    @PostMapping("/leave")
    public BaseResponse<Boolean> leaveProjectGroup(HttpServletRequest request, @RequestBody GroupUserRequest groupRequest) {
        User currentUser = (User) request.getSession().getAttribute(UserConstant.USER_LOGIN_STATE);
        Group targetGroup = groupService.getById(groupRequest.getGroupId());
        QueryWrapper<UserGroup> userGroupQueryWrapper = new QueryWrapper<>();
        userGroupQueryWrapper.eq("user_id", currentUser.getId());
        userGroupQueryWrapper.and(w->w.eq("group_id", targetGroup.getId()));
        boolean remove = userGroupService.remove(userGroupQueryWrapper);
        if (!remove) throw new BusinessException(StatusCode.PARAMS_ERROR, "参数错误，用户可能不在该小组中");
        if (Objects.equals(targetGroup.getGroupLeader(), currentUser.getId())) {
            if (groupService.getGroupCurrentNumber(groupRequest.getGroupId()) == 0) {
                targetGroup.setGroupLeader(null);
            } else {
                userGroupQueryWrapper = new QueryWrapper<>();
                userGroupQueryWrapper.eq("group_id", targetGroup.getId());
                UserGroup nextUser = userGroupService.getOne(userGroupQueryWrapper);
                targetGroup.setGroupLeader(nextUser.getUserId());
            }
            groupService.updateById(targetGroup);
        }
        return ResponseUtils.success(true);
    }
    
    @PostMapping("/join")
    public BaseResponse<Boolean> joinProjectGroup(HttpServletRequest request, @RequestBody GroupUserRequest groupRequest) {
        User currentUser = (User) request.getSession().getAttribute(UserConstant.USER_LOGIN_STATE);
        Group targetGroup = groupService.getById(groupRequest.getGroupId());
        Project target = projectService.getById(targetGroup.getProjectId());
        
        QueryWrapper<Group> groupQueryWrapper = new QueryWrapper<>();
        groupQueryWrapper.eq("project_id", target.getId());
        List<Group> groupList = groupService.list(groupQueryWrapper);
        
        for (Group group : groupList) {
            QueryWrapper<UserGroup> userGroupQueryWrapper = new QueryWrapper<>();
            userGroupQueryWrapper.eq("user_id", currentUser.getId());
            userGroupQueryWrapper.and( w -> w.eq("group_id", group.getId()));
            long count = userGroupService.count(userGroupQueryWrapper);
            if (count > 0) {
                throw new BusinessException(StatusCode.PARAMS_ERROR, "当前用户已经加入了该项目的另一小组");
            }
        }
        
        if (courseService.checkCourseEnroll(currentUser.getId(), target.getCourseId())) {
            Integer currentGroupNumber = groupService.getGroupCurrentNumber(targetGroup.getId());
            if (currentGroupNumber == 0) {
                targetGroup.setGroupLeader(currentUser.getId());
            }
            boolean b = groupService.updateById(targetGroup);
            if (!b) throw new BusinessException(StatusCode.SYSTEM_ERROR, "修改组长角色失败,系统异常");
            UserGroup newMember = new UserGroup();
            newMember.setGroupId(groupRequest.getGroupId());
            newMember.setUserId(currentUser.getId());
            boolean save = userGroupService.save(newMember);
            if (!save) throw new BusinessException(StatusCode.PARAMS_ERROR, "保存用户小组信息失败");
        } else throw new BusinessException(StatusCode.NO_AUTH, "当前用户无权限加入该项目的小组");
        return ResponseUtils.success(true);
    }
    
    @GetMapping("")
    public BaseResponse<GroupInfoResponse> groupInfo (HttpServletRequest request, @RequestParam Long groupId){
        User currentUser = (User) request.getSession().getAttribute(UserConstant.USER_LOGIN_STATE);
        
        if (currentUser == null) throw new BusinessException(StatusCode.NOT_LOGIN);
        GroupInfoResponse response = new GroupInfoResponse();
        Group target = groupService.getById(groupId);
        
        Long projectId = target.getProjectId();
        QueryWrapper<Project> projectQueryWrapper = new QueryWrapper<>();
        projectQueryWrapper.eq("id",projectId);
        Long courseId = projectService.getOne(projectQueryWrapper).getCourseId();
        
        //查询用户是否有权限
        if (courseService.checkCourseEnroll(currentUser.getId(), courseId)) {
            response.setGroupName(target.getGroupName());
            response.setGroupLeader(target.getGroupLeader());
            response.setDefenceTeacher(target.getDefenceTeacher());
            response.setPresentationTime(target.getPresentationTime());
            response.setPublicInfo(target.getPublicInfo());
            
        } else throw new BusinessException(StatusCode.NO_AUTH, "当前用户无权访问该小组信息");
        return ResponseUtils.success(response);
    }
    
    @PostMapping("/detail/update")
    public BaseResponse<Boolean> groupDetailUpdate(HttpServletRequest request,  @RequestBody GroupDetailUpdateRequest updateRequest){
        
        // 检查用户态以及用户权限
        User currentUser = (User) request.getSession().getAttribute(UserConstant.USER_LOGIN_STATE);
        
        if (currentUser == null) throw new BusinessException(StatusCode.NOT_LOGIN);
        
        Group target = groupService.getById(updateRequest.getId());
        Long projectId = target.getProjectId();
        QueryWrapper<Project> projectQueryWrapper = new QueryWrapper<>();
        projectQueryWrapper.eq("id",projectId);
        Long courseId = projectService.getOne(projectQueryWrapper).getCourseId();
        
        if ((currentUser.getUserRole() == UserConstant.ADMIN_ROLE || currentUser.getUserRole() == UserConstant.TEACHER_ASSISTANT_ROLE || currentUser.getUserRole() == UserConstant.TEACHER_ROLE) && courseService.checkCourseEnroll(currentUser.getId(), courseId)) {
            target.setId(updateRequest.getId());
            target.setGroupName(updateRequest.getGroupName());
            target.setGroupLeader(updateRequest.getGroupLeader());
            target.setDefenceTeacher(updateRequest.getDefenceTeacher());
            target.setPresentationTime(updateRequest.getPresentationTime());
            target.setPublicInfo(updateRequest.getPublicInfo());
            boolean b = groupService.updateById(target);
            if (!b) throw new BusinessException(StatusCode.SYSTEM_ERROR, "修改小组详细信息失败");
        }else if (Objects.equals(currentUser.getId(), updateRequest.getGroupLeader()) && currentUser.getUserRole() == UserConstant.STUDENT_ROLE){
            target.setGroupName(updateRequest.getGroupName());
            target.setPublicInfo(updateRequest.getPublicInfo());
            boolean b = groupService.updateById(target);
            if (!b) throw new BusinessException(StatusCode.SYSTEM_ERROR, "修改小组名和对外展示信息失败");
        } else throw new BusinessException(StatusCode.NO_AUTH, "当前用户无权限修改项目信息");
        return ResponseUtils.success(true);
        
    }
}
