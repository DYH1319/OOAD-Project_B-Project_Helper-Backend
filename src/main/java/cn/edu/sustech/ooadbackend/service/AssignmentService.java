package cn.edu.sustech.ooadbackend.service;

import cn.edu.sustech.ooadbackend.model.domain.Assignment;
import cn.edu.sustech.ooadbackend.model.request.AssignmentInsertRequest;
import cn.edu.sustech.ooadbackend.model.request.AssignmentUpdateRequest;
import com.baomidou.mybatisplus.extension.service.IService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * @className AssignmentService
 * @version 1.0
 * @author DYH
 * @since 2023/10/9 12:37
 */
    
public interface AssignmentService extends IService<Assignment> {
    List<Assignment> list (Long courseId);
    Long insertAssignment(AssignmentInsertRequest assignmentInsertRequest);
    Boolean updateAssignment(AssignmentUpdateRequest assignmentUpdateRequest);
    Boolean deleteAssignment(Long id);
    
    /**
     * 用户上传作业文件
     * @param file 作业文件
     * @param request HttpServletRequest
     * @return 带有过期时间的预签名访问链接
     */
    String assignmentFileUpload(MultipartFile file, HttpServletRequest request);
}
