package cn.edu.sustech.ooadbackend.service.impl;

import cn.edu.sustech.ooadbackend.common.StatusCode;
import cn.edu.sustech.ooadbackend.exception.BusinessException;
import cn.edu.sustech.ooadbackend.mapper.AssignmentMapper;
import cn.edu.sustech.ooadbackend.model.domain.Assignment;
import cn.edu.sustech.ooadbackend.model.request.AssignmentInsertRequest;
import cn.edu.sustech.ooadbackend.model.request.AssignmentUpdateRequest;
import cn.edu.sustech.ooadbackend.service.AssignmentService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @className AssignmentServiceImpl
 * @version 1.0
 * @author DYH
 * @since 2023/10/9 12:37
 */
 
@Service
public class AssignmentServiceImpl extends ServiceImpl<AssignmentMapper, Assignment> implements AssignmentService {
@Resource
private AssignmentMapper assignmentMapper;
    @Override
    public List<Assignment> list(Long courseId) {
        List<Assignment> assignmentList = new ArrayList<>();
        QueryWrapper<Assignment> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("course_id",courseId);
        assignmentList = this.list(queryWrapper);
        if (assignmentList == null || assignmentList.isEmpty()) throw new BusinessException(StatusCode.NULL_ERROR, "查找不到该课程的作业信息");

        return assignmentList.stream().map(this::getSafeAssignment).toList();
    }

    @Override
    @Transactional
    public Long insertAssignment(AssignmentInsertRequest assignmentInsertRequest) {
        Assignment newAssignment = new Assignment();
        newAssignment.setCourseId(assignmentInsertRequest.getCourseId());
        newAssignment.setDescription(assignmentInsertRequest.getDescription());
        newAssignment.setTitle(assignmentInsertRequest.getTitle());
        newAssignment.setStartTime(assignmentInsertRequest.getStartTime());
        newAssignment.setEndTime(assignmentInsertRequest.getEndTime());
        newAssignment.setAssignmentType(assignmentInsertRequest.getAssignmentType());
        Boolean isInsert = this.save(newAssignment);
        if (!isInsert) throw new BusinessException(StatusCode.PARAMS_ERROR, "作业发布失败");
        return newAssignment.getId();
    }

    @Override
    @Transactional
    public Boolean updateAssignment(AssignmentUpdateRequest assignmentUpdateRequest) {
    Assignment newAssignment = new Assignment();
        newAssignment.setId(assignmentUpdateRequest.getAssignmentId());
        newAssignment.setDescription(assignmentUpdateRequest.getDescription());
        newAssignment.setTitle(assignmentUpdateRequest.getTitle());
        newAssignment.setStartTime(assignmentUpdateRequest.getStartTime());
        newAssignment.setEndTime(assignmentUpdateRequest.getEndTime());
        newAssignment.setAssignmentType(assignmentUpdateRequest.getAssignmentType());
        boolean isUpdated = assignmentMapper.update(newAssignment);
        if (!isUpdated) throw new BusinessException(StatusCode.PARAMS_ERROR, "作业信息更新失败");

        return isUpdated;
    }

    @Override
    @Transactional
    public Boolean deleteAssignment(Long id) {
        //删除Assignment表中的数据
        QueryWrapper<Assignment> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("id",id);
        Boolean assignmentRemove = this.remove(queryWrapper);
        if (!assignmentRemove) throw new BusinessException(StatusCode.PARAMS_ERROR, "作业信息删除失败");


        return assignmentRemove;
    }

    private Assignment getSafeAssignment(Assignment assignment){
        Assignment newAssignment = new Assignment();
        newAssignment.setId(assignment.getId());
        newAssignment.setTitle(assignment.getTitle());
        newAssignment.setEndTime(assignment.getEndTime());
        return newAssignment;
    }
}
