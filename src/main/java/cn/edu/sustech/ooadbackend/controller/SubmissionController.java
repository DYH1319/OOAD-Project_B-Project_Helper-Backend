package cn.edu.sustech.ooadbackend.controller;

import cn.edu.sustech.ooadbackend.common.BaseResponse;
import cn.edu.sustech.ooadbackend.common.StatusCode;
import cn.edu.sustech.ooadbackend.exception.BusinessException;
import cn.edu.sustech.ooadbackend.model.domain.Submission;
import cn.edu.sustech.ooadbackend.model.request.ReviewSubmitRequest;
import cn.edu.sustech.ooadbackend.model.response.ReviewListResponse;
import cn.edu.sustech.ooadbackend.service.SubmissionService;
import cn.edu.sustech.ooadbackend.utils.ResponseUtils;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author DYH
 * @version 1.0
 * @className SubmissionController
 * @since 2024/1/1 14:57
 */
@RestController
@RequestMapping("/submission")
public class SubmissionController {
    
    @Resource
    private SubmissionService submissionService;
    
    @PostMapping("")
    public BaseResponse<Submission> getSubmissionById(@RequestBody(required = false) Long submissionId, HttpServletRequest request) {
        if (submissionId == null || submissionId <= 0) {
            throw new BusinessException(StatusCode.PARAMS_ERROR);
        }
        Submission result = submissionService.getSubmissionById(submissionId, request);
        return ResponseUtils.success(result);
    }
    
    @GetMapping("/review/list")
    public BaseResponse<List<ReviewListResponse>> reviewListByAssignmentId(@RequestParam Long assignmentId, HttpServletRequest request) {
        if (assignmentId == null || assignmentId <= 0) {
            throw new BusinessException(StatusCode.PARAMS_ERROR);
        }
        List<ReviewListResponse> result = submissionService.reviewListByAssignmentId(assignmentId, request);
        return ResponseUtils.success(result);
    }
    
    @PostMapping("/review/submit")
    public BaseResponse<Boolean> reviewSubmit(@RequestBody(required = false) ReviewSubmitRequest reviewSubmitRequest, HttpServletRequest request) {
        if (reviewSubmitRequest == null || reviewSubmitRequest.getScore() == null) {
            throw new BusinessException(StatusCode.PARAMS_ERROR);
        }
        boolean result = submissionService.reviewSubmit(reviewSubmitRequest, request);
        return ResponseUtils.success(result, "评分成功");
    }
}
