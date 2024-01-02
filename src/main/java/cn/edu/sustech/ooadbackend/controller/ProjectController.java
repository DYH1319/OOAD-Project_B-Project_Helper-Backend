package cn.edu.sustech.ooadbackend.controller;
import cn.edu.sustech.ooadbackend.common.BaseResponse;
import cn.edu.sustech.ooadbackend.common.StatusCode;
import cn.edu.sustech.ooadbackend.constant.UserConstant;
import cn.edu.sustech.ooadbackend.exception.BusinessException;
import cn.edu.sustech.ooadbackend.model.domain.*;
import cn.edu.sustech.ooadbackend.model.request.*;
import cn.edu.sustech.ooadbackend.model.response.GroupInfoResponse;
import cn.edu.sustech.ooadbackend.model.response.GroupsDetailResponse;
import cn.edu.sustech.ooadbackend.model.response.ProjectInfoResponse;
import cn.edu.sustech.ooadbackend.service.*;
import cn.edu.sustech.ooadbackend.utils.ResponseUtils;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

@RestController
@RequestMapping("/project")
public class ProjectController {
    @Resource
    private UserGroupService userGroupService;
    @Resource
    private ProjectService projectService;
    @Resource
    private CourseService courseService;
    @Resource
    private GroupService groupService;
    @Resource
    private NotificationService notificationService;
    @Resource
    private TeacherAssistantCourseService teacherAssistantCourseService;

    @PostMapping("/group/leave")
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

    @PostMapping("/group/join")
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

    @GetMapping("/detail/group")
    public BaseResponse<Long> getCurrentUserGroup(HttpServletRequest request, @RequestParam Long projectId) {
        User currentUser = (User) request.getSession().getAttribute(UserConstant.USER_LOGIN_STATE);
        QueryWrapper<Group> groupQueryWrapper = new QueryWrapper<>();
        groupQueryWrapper.eq("project_id", projectId);
        List<Group> groupsList = groupService.list(groupQueryWrapper);
        for (Group group : groupsList) {
            QueryWrapper<UserGroup> userGroupQueryWrapper = new QueryWrapper<>();
            userGroupQueryWrapper.eq("user_id", currentUser.getId());
            userGroupQueryWrapper.and( w -> w.eq("group_id", group.getId()));
            long count = userGroupService.count(userGroupQueryWrapper);
            if (count == 1) {
                return ResponseUtils.success(group.getId());
            }
        }
        throw new BusinessException(StatusCode.PARAMS_ERROR, "当前用户无组队信息");
    }


    @GetMapping("/detail/groups")
    public BaseResponse<List<GroupsDetailResponse>> getProjectGroupsDetail(HttpServletRequest request, @RequestParam Long projectId) {
        User currentUser = (User) request.getSession().getAttribute(UserConstant.USER_LOGIN_STATE);
        Project target = projectService.getById(projectId);
        if (courseService.checkCourseEnroll(currentUser.getId(), target.getCourseId())) {
            QueryWrapper<Group> groupQueryWrapper = new QueryWrapper<>();
            groupQueryWrapper.eq("project_id", projectId);
            List<Group> groupsList = groupService.list(groupQueryWrapper);
            List<GroupsDetailResponse> list = groupsList.stream().map(new Function<Group, GroupsDetailResponse>() {
                @Override
                public GroupsDetailResponse apply(Group group) {
                    GroupsDetailResponse groupDetail = new GroupsDetailResponse();
                    groupDetail.setGroupCurrentNumber(groupService.getGroupCurrentNumber(group.getId()));
                    groupDetail.setGroupMaxNumber(target.getMaxNumber());
                    groupDetail.setName(group.getGroupName());
                    groupDetail.setPublicInfo(group.getPublicInfo());
                    return groupDetail;
                }
            }).toList();
            return ResponseUtils.success(list);
        } else throw new BusinessException(StatusCode.NO_AUTH, "当前用户无权限查看分组信息");
    }

    @PostMapping("/detail/update")
    public BaseResponse<Boolean> updateProjectInfo(HttpServletRequest request, @RequestBody ProjectDetailUpdateRequest updateRequest) {
        User currentUser = (User) request.getSession().getAttribute(UserConstant.USER_LOGIN_STATE);

        Project target = projectService.getById(updateRequest.getId());

        if ((currentUser.getUserRole() == UserConstant.ADMIN_ROLE || currentUser.getUserRole() == UserConstant.TEACHER_ASSISTANT_ROLE || currentUser.getUserRole() == UserConstant.TEACHER_ROLE) && courseService.checkCourseEnroll(currentUser.getId(), target.getCourseId())) {
            target.setDescription(updateRequest.getDescription());
            target.setProjectName(updateRequest.getProjectName());
            target.setGroupNumber(updateRequest.getGroupNumber());
            target.setEndDeadline(updateRequest.getEndDeadline());
            target.setMaxNumber(updateRequest.getMaxNumber());
            target.setGroupDeadline(updateRequest.getGroupDeadline());
            boolean b = projectService.updateById(target);
            if (!b) throw new BusinessException(StatusCode.SYSTEM_ERROR, "修改项目详细信息失败");
        } else throw new BusinessException(StatusCode.NO_AUTH, "当前用户无权限修改项目信息");
        return ResponseUtils.success(true);
    }
    /**
     * 获取项目信息
     * @param projectId 项目id
     * @param request HttpServletRequest
     * @return 项目信息
     */
    @GetMapping("/")
    public BaseResponse<ProjectInfoResponse> projectInfo(@RequestParam Long projectId, HttpServletRequest request){
        //获取用户登录态
        User currentUser = (User) request.getSession().getAttribute(UserConstant.USER_LOGIN_STATE);

        ProjectInfoResponse response = new ProjectInfoResponse();

        Project target = projectService.getById(projectId);
        //查询用户是否位于课程范畴内
            if (courseService.checkCourseEnroll(currentUser.getId(), target.getCourseId())) {

            response.setProjectName(target.getProjectName());
            response.setDescription(target.getDescription());
            response.setEndDeadline(target.getEndDeadline());
            response.setMaxNumber(target.getMaxNumber());
            response.setGroupDeadline(target.getGroupDeadline());
            response.setGroupNumber(target.getGroupNumber());

        } else throw new BusinessException(StatusCode.NO_AUTH, "当前用户无权访问该项目信息");
        return ResponseUtils.success(response);
    }

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
    /**
     *
     * @param request
     * @param projectId
     * @return
     */
    @GetMapping("/notification/list")
    public BaseResponse<Notification[]> listNotification (HttpServletRequest request, @RequestParam Long projectId){
        if (request.getSession().getAttribute(UserConstant.USER_LOGIN_STATE) == null) throw new BusinessException(StatusCode.NOT_LOGIN);
        return ResponseUtils.success(projectService.listNotification(request, projectId));
    }
    @PostMapping("/notification/delete")
    public BaseResponse<Boolean> deleteNotification(HttpServletRequest request, @RequestBody NotificationDeleteRequest notificationDeleteRequest){

        // 检查用户态以及用户权限
        User currentUser = (User) request.getSession().getAttribute(UserConstant.USER_LOGIN_STATE);

        if (currentUser == null) throw new BusinessException(StatusCode.NOT_LOGIN);

        QueryWrapper<Notification> notificationQueryWrapper = new QueryWrapper<>();

        notificationQueryWrapper.eq("id", notificationDeleteRequest.getNotificationId());

        Notification notification = notificationService.getOne(notificationQueryWrapper);

        if (notification == null) throw new BusinessException(StatusCode.SYSTEM_ERROR, "删除通知的时候发生异常");

        if (!(currentUser.getUserRole() == UserConstant.ADMIN_ROLE || (currentUser.getUserRole() == UserConstant.TEACHER_ROLE && courseService.isCourseTeacher(currentUser.getId(), notification.getCourseId())) || (currentUser.getUserRole() == UserConstant.TEACHER_ASSISTANT_ROLE && teacherAssistantCourseService.isCourseTa(currentUser.getId(), notification.getCourseId())))) throw new BusinessException(StatusCode.NO_AUTH, "您无权向该项目删除通知");

        return ResponseUtils.success(projectService.removeNotification(notificationDeleteRequest.getNotificationId()));
    }

    @PostMapping("/notification/insert")
    public BaseResponse<Long> insertNotification(HttpServletRequest request, @RequestBody NotificationInsertRequest notificationInsertRequest){

        // 检查用户态以及用户权限
        User currentUser = (User) request.getSession().getAttribute(UserConstant.USER_LOGIN_STATE);

        if (currentUser == null) throw new BusinessException(StatusCode.NOT_LOGIN);

        if (!(currentUser.getUserRole() == UserConstant.ADMIN_ROLE || (currentUser.getUserRole() == UserConstant.TEACHER_ROLE && courseService.isCourseTeacher(currentUser.getId(), notificationInsertRequest.getCourseId())) || (currentUser.getUserRole() == UserConstant.TEACHER_ASSISTANT_ROLE && teacherAssistantCourseService.isCourseTa(currentUser.getId(), notificationInsertRequest.getCourseId())))) throw new BusinessException(StatusCode.NO_AUTH, "您无权向该项目插入通知");

        return ResponseUtils.success(projectService.insertNotification(currentUser.getId(), notificationInsertRequest));
    }


    @GetMapping("/group")
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
    @PostMapping("/group/detail/update")
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
