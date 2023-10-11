package cn.edu.sustech.ooadbackend.service.impl;

import org.springframework.stereotype.Service;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import cn.edu.sustech.ooadbackend.model.domain.UserCourse;
import cn.edu.sustech.ooadbackend.mapper.UserCourseMapper;
import cn.edu.sustech.ooadbackend.service.UserCourseService;

/**
 * @className UserCourseServiceImpl
 * @version 1.0
 * @author DYH
 * @since 2023/10/11 15:09
 */
    
@Service
public class UserCourseServiceImpl extends ServiceImpl<UserCourseMapper, UserCourse> implements UserCourseService{

}
