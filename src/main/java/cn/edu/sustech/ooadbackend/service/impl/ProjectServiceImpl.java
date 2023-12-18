package cn.edu.sustech.ooadbackend.service.impl;

import cn.edu.sustech.ooadbackend.common.StatusCode;
import cn.edu.sustech.ooadbackend.constant.UserConstant;
import cn.edu.sustech.ooadbackend.exception.BusinessException;
import cn.edu.sustech.ooadbackend.mapper.ProjectMapper;
import cn.edu.sustech.ooadbackend.model.domain.*;
import cn.edu.sustech.ooadbackend.model.request.ProjectInsertRequest;
import cn.edu.sustech.ooadbackend.model.request.ProjectUpdateRequest;
import cn.edu.sustech.ooadbackend.service.ProjectService;
import cn.edu.sustech.ooadbackend.service.UserProjectService;
import cn.edu.sustech.ooadbackend.utils.DateTimeFormatTransferUtils;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @className ProjectServiceImpl
 * @version 1.0
 * @author DYH
 * @since 2023/10/9 12:43
 */
    
@Service
public class ProjectServiceImpl extends ServiceImpl<ProjectMapper, Project> implements ProjectService{
    @Resource
    private ProjectService projectService;
    @Resource
    private UserProjectService userProjectService;

    @Resource
    private ProjectMapper projectMapper;

    @Override
    public List<Project> list(User user, Long courseId) {
        if(user.getUserRole() == UserConstant.STUDENT_ROLE){
            // 查询对应用户的项目关系
            List<UserProject> userProjectList = userProjectService.listUserProjectByUserId(user.getId());
            // 将项目关系转为项目id
            List<Long> projectIdList = userProjectList.stream().map(UserProject::getProjectId).toList();
            List<Project> projectList = this.listByIds(projectIdList);
            QueryWrapper<Project> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("course_id",courseId);
            List<Project> thisCourseProject = this.list(queryWrapper);
            if (projectList == null ) throw new BusinessException(StatusCode.NULL_ERROR, "查找不到用户的项目信息");
            projectList.retainAll(thisCourseProject);

            if ( projectList.isEmpty()) throw new BusinessException(StatusCode.NULL_ERROR, "查找不到用户的项目信息");
            return projectList.stream().map(this::getSafetyProject).toList();


        }else {
            QueryWrapper<Project> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("course_id",courseId);
            List<Project> thisCourseProject = this.list(queryWrapper);
            if (thisCourseProject == null || thisCourseProject.isEmpty()) throw new BusinessException(StatusCode.NULL_ERROR, "查找不到用户的课程信息");
            return thisCourseProject.stream().map(this::getSafetyProject).toList();

        }
    }

    @Override
    @Transactional
    public Long insertProject(ProjectInsertRequest projectInsertRequest) {
        Project project = new Project();
        project.setProjectName(projectInsertRequest.getProjectName());
        project.setCourseId(projectInsertRequest.getCourseId());
        project.setDescription(projectInsertRequest.getDescription());
        project.setGroupDeadline(DateTimeFormatTransferUtils.frontDateTime2BackDate(projectInsertRequest.getGroupDeadline()));
        project.setEndDeadline(DateTimeFormatTransferUtils.frontDateTime2BackDate(projectInsertRequest.getEndDeadline()));

        Boolean isInsert = this.save(project);
        if (!isInsert) throw new BusinessException(StatusCode.PARAMS_ERROR, "项目发布失败");
        return project.getId();

    }

    @Override
    @Transactional
    public Boolean updateProject(ProjectUpdateRequest projectUpdateRequest) {
        Project project = new Project();
        project.setId(projectUpdateRequest.getId());
        project.setProjectName(projectUpdateRequest.getProjectName());
        project.setDescription(projectUpdateRequest.getDescription());
        project.setGroupDeadline(DateTimeFormatTransferUtils.frontDateTime2BackDate(projectUpdateRequest.getGroupDeadline()));
        project.setEndDeadline(DateTimeFormatTransferUtils.frontDateTime2BackDate(projectUpdateRequest.getEndDeadline()));

        boolean isUpdated = projectMapper.updateProject(project);
        if (!isUpdated) throw new BusinessException(StatusCode.PARAMS_ERROR, "项目信息更新失败");

        return isUpdated;
    }

    @Override
    @Transactional
    public Boolean deleteProject(Long projectId) {
        //删除project表中的信息
        QueryWrapper<Project> projectQueryWrapper = new QueryWrapper<>();
        projectQueryWrapper.eq("id",projectId);
        Boolean projectRemoved = this.remove(projectQueryWrapper);
        if (!projectRemoved) throw new BusinessException(StatusCode.PARAMS_ERROR, "项目信息删除失败");
        return projectRemoved;
    }

    public Project getSafetyProject(Project project){
        Project newProject = new Project();
        newProject.setId(project.getId());
        newProject.setProjectName(project.getProjectName());
        newProject.setDescription(project.getDescription());
        newProject.setGroupDeadline(project.getGroupDeadline());
        newProject.setEndDeadline(project.getEndDeadline());
        return newProject;

    }
}
