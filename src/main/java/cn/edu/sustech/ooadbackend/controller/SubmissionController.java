package cn.edu.sustech.ooadbackend.controller;

import cn.edu.sustech.ooadbackend.common.BaseResponse;
import cn.edu.sustech.ooadbackend.common.StatusCode;
import cn.edu.sustech.ooadbackend.exception.BusinessException;
import cn.edu.sustech.ooadbackend.model.domain.Submission;
import cn.edu.sustech.ooadbackend.service.SubmissionService;
import cn.edu.sustech.ooadbackend.utils.ResponseUtils;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
