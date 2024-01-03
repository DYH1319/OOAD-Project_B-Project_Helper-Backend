package cn.edu.sustech.ooadbackend.service;

import cn.edu.sustech.ooadbackend.model.domain.GradeBook;
import cn.edu.sustech.ooadbackend.model.response.GradeBookResponse;
import com.baomidou.mybatisplus.extension.service.IService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * @className GradeBookService
 * @version 1.0
 * @author DYH
 * @since 2024/1/3 21:33
 */
    
public interface GradeBookService extends IService<GradeBook>{
    
    
    List<GradeBookResponse> getGradeBook(Long courseId, HttpServletRequest request);
    
    boolean gradeBookUpload(MultipartFile file, HttpServletRequest request);
}
