package cn.edu.sustech.ooadbackend.service;

import cn.edu.sustech.ooadbackend.model.domain.Submission;
import cn.edu.sustech.ooadbackend.model.request.ReviewSubmitRequest;
import cn.edu.sustech.ooadbackend.model.response.ReviewListResponse;
import com.baomidou.mybatisplus.extension.service.IService;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

/**
 * @className SubmissionService
 * @version 1.0
 * @author DYH
 * @since 2023/10/9 12:43
 */
    
public interface SubmissionService extends IService<Submission> {
    
    /**
     * 根据ID获取提交
     * @param submissionId 提交的ID
     * @param request HttpServletRequest
     * @return 提交的详细信息
     */
    Submission getSubmissionById(Long submissionId, HttpServletRequest request);
    
    List<ReviewListResponse> reviewListByAssignmentId(Long assignmentId, HttpServletRequest request);
    
    boolean reviewSubmit(ReviewSubmitRequest reviewSubmitRequest, HttpServletRequest request);
}
