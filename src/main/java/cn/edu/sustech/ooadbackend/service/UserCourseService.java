package cn.edu.sustech.ooadbackend.service;

import cn.edu.sustech.ooadbackend.model.domain.UserCourse;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * @className UserCourseService
 * @version 1.0
 * @author DYH
 * @since 2023/10/11 15:09
 */
    
public interface UserCourseService extends IService<UserCourse>{

    public List<UserCourse> listUserCourseByUserId(Long id);

    public List<UserCourse> listUserCourseByCourseId(Long id);

}
