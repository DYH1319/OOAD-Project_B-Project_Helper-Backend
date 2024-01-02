package cn.edu.sustech.ooadbackend.service.impl;

import cn.edu.sustech.ooadbackend.common.FileType;
import cn.edu.sustech.ooadbackend.common.StatusCode;
import cn.edu.sustech.ooadbackend.constant.UserConstant;
import cn.edu.sustech.ooadbackend.exception.BusinessException;
import cn.edu.sustech.ooadbackend.mapper.AssignmentMapper;
import cn.edu.sustech.ooadbackend.mapper.SubmissionMapper;
import cn.edu.sustech.ooadbackend.model.domain.Assignment;
import cn.edu.sustech.ooadbackend.model.domain.Submission;
import cn.edu.sustech.ooadbackend.model.domain.User;
import cn.edu.sustech.ooadbackend.model.request.AssignmentInsertRequest;
import cn.edu.sustech.ooadbackend.model.request.AssignmentUpdateRequest;
import cn.edu.sustech.ooadbackend.service.AssignmentService;
import cn.edu.sustech.ooadbackend.utils.CosManager;
import cn.edu.sustech.ooadbackend.utils.DateTimeFormatTransferUtils;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author DYH
 * @version 1.0
 * @className AssignmentServiceImpl
 * @since 2023/10/9 12:37
 */

@Service
public class AssignmentServiceImpl extends ServiceImpl<AssignmentMapper, Assignment> implements AssignmentService {
    @Resource
    private AssignmentMapper assignmentMapper;
    
    @Resource
    private SubmissionMapper submissionMapper;
    
    @Resource
    private CosManager cosManager;
    
    @Override
    public List<Assignment> list(Long courseId) {
        List<Assignment> assignmentList = new ArrayList<>();
        QueryWrapper<Assignment> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("course_id", courseId);
        assignmentList = this.list(queryWrapper);
        if (assignmentList == null || assignmentList.isEmpty())
            throw new BusinessException(StatusCode.NULL_ERROR, "查找不到该课程的作业信息");
        
        return assignmentList.stream().map(this::getSafeAssignment).toList();
    }
    
    @Override
    @Transactional
    public Long insertAssignment(AssignmentInsertRequest assignmentInsertRequest) {
        Assignment newAssignment = new Assignment();
        newAssignment.setCourseId(assignmentInsertRequest.getCourseId());
        newAssignment.setDescription(assignmentInsertRequest.getDescription());
        newAssignment.setTitle(assignmentInsertRequest.getTitle());
        newAssignment.setStartTime(DateTimeFormatTransferUtils.frontDateTime2BackDate(assignmentInsertRequest.getStartTime()));
        newAssignment.setEndTime(DateTimeFormatTransferUtils.frontDateTime2BackDate(assignmentInsertRequest.getEndTime()));
        newAssignment.setAssignmentType(assignmentInsertRequest.getAssignmentType());
        Boolean isInsert = this.save(newAssignment);
        if (!isInsert) throw new BusinessException(StatusCode.PARAMS_ERROR, "作业发布失败");
        return newAssignment.getId();
    }
    
    @Override
    @Transactional
    public Boolean updateAssignment(AssignmentUpdateRequest assignmentUpdateRequest) {
        Assignment newAssignment = new Assignment();
        newAssignment.setId(assignmentUpdateRequest.getId());
        newAssignment.setDescription(assignmentUpdateRequest.getDescription());
        newAssignment.setTitle(assignmentUpdateRequest.getTitle());
        newAssignment.setStartTime(DateTimeFormatTransferUtils.frontDateTime2BackDate(assignmentUpdateRequest.getStartTime()));
        newAssignment.setEndTime(DateTimeFormatTransferUtils.frontDateTime2BackDate(assignmentUpdateRequest.getEndTime()));
        newAssignment.setAssignmentType(assignmentUpdateRequest.getAssignmentType());
        boolean isUpdated = assignmentMapper.update(newAssignment);
        if (!isUpdated) throw new BusinessException(StatusCode.PARAMS_ERROR, "作业信息更新失败");
        
        return isUpdated;
    }
    
    @Override
    @Transactional
    public Boolean deleteAssignment(Long id) {
        // 删除Assignment表中的数据
        QueryWrapper<Assignment> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("id", id);
        Boolean assignmentRemove = this.remove(queryWrapper);
        if (!assignmentRemove) throw new BusinessException(StatusCode.PARAMS_ERROR, "作业信息删除失败");
        
        
        return assignmentRemove;
    }
    
    private Assignment getSafeAssignment(Assignment assignment) {
        Assignment newAssignment = new Assignment();
        newAssignment.setId(assignment.getId());
        newAssignment.setTitle(assignment.getTitle());
        newAssignment.setDescription(assignment.getDescription());
        newAssignment.setStartTime(assignment.getStartTime());
        newAssignment.setEndTime(assignment.getEndTime());
        newAssignment.setAssignmentType(assignment.getAssignmentType());
        return newAssignment;
    }
    
    @Override
    public String assignmentFileUpload(MultipartFile file, HttpServletRequest request) {
        User currentUser = (User) request.getSession().getAttribute(UserConstant.USER_LOGIN_STATE);
        if (currentUser == null) {
            throw new BusinessException(StatusCode.NOT_LOGIN, "未登录，请先登录");
        }
        
        String originFileName = file.getOriginalFilename();
        if (originFileName == null) {
            throw new BusinessException(StatusCode.PARAMS_ERROR, "无法获取上传文件原始名称");
        }
        
        String fileType;
        if (originFileName.toLowerCase().endsWith(".txt")) {
            fileType = "TXT";
        } else if (originFileName.toLowerCase().endsWith(".pdf")) {
            fileType = "PDF";
        } else if (originFileName.toLowerCase().endsWith(".md")) {
            fileType = "MD";
        } else if (originFileName.toLowerCase().endsWith(".docx")) {
            fileType = "DOCX";
        } else {
            throw new BusinessException(StatusCode.PARAMS_ERROR, "文件类型不符合");
        }
        
        // 上传至对象存储桶
        // noinspection StringBufferReplaceableByString
        String key = new StringBuilder()
            .append(new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date()))
            .append('-')
            .append(UUID.randomUUID())
            .append('-')
            .append(file.getOriginalFilename())
            .toString();
        try {
            cosManager.putObject(key, file.getInputStream(), file.getSize(), FileType.ASSIGNMENT_FILE);
        } catch (IOException e) {
            throw new BusinessException(StatusCode.SYSTEM_ERROR, "上传失败");
        }
        
        // TODO 数据库新建提交
        Submission submission = new Submission();
        submission.setSubmitterId(9L);
        submission.setAssignmentId(1L);
        submission.setContent(key);
        submission.setSubmitTime(new Date());
        
        submission.setContentType(fileType);
        int count = submissionMapper.insert(submission);
        
        if (count != 1) {
            throw new BusinessException(StatusCode.SYSTEM_ERROR, "数据库异常，请联系管理员");
        }
        
        return cosManager.getObjectPresignedUrl(key, FileType.ASSIGNMENT_FILE);
    }
}
