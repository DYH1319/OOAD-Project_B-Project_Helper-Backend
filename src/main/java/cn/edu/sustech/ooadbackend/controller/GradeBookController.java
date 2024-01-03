package cn.edu.sustech.ooadbackend.controller;

import cn.edu.sustech.ooadbackend.common.BaseResponse;
import cn.edu.sustech.ooadbackend.common.StatusCode;
import cn.edu.sustech.ooadbackend.exception.BusinessException;
import cn.edu.sustech.ooadbackend.model.domain.GradeBook;
import cn.edu.sustech.ooadbackend.model.response.GradeBookResponse;
import cn.edu.sustech.ooadbackend.model.response.ReviewListResponse;
import cn.edu.sustech.ooadbackend.service.GradeBookService;
import cn.edu.sustech.ooadbackend.utils.ResponseUtils;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

/**
 * @author DYH
 * @version 1.0
 * @className GradeBookController
 * @since 2024/1/3 21:37
 */
@RestController
@RequestMapping("/gradeBook")
public class GradeBookController {
    
    @Resource
    private GradeBookService gradeBookService;
    
    @GetMapping("")
    public BaseResponse<List<GradeBookResponse>> getGradeBook(@RequestParam Long courseId, HttpServletRequest request) {
        if (courseId == null || courseId <= 0) {
            throw new BusinessException(StatusCode.PARAMS_ERROR);
        }
        List<GradeBookResponse> result = gradeBookService.getGradeBook(courseId, request);
        return ResponseUtils.success(result);
    }
    
    @PostMapping("/upload")
    public BaseResponse<Boolean> gradeBookUpload(@RequestParam MultipartFile file, HttpServletRequest request) {
        if (request == null) {
            throw new BusinessException(StatusCode.PARAMS_ERROR);
        }
        boolean res = gradeBookService.gradeBookUpload(file, request);
        return ResponseUtils.success(res, "成功上传成绩册！");
    }
}
