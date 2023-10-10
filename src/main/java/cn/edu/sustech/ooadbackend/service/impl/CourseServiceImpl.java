package cn.edu.sustech.ooadbackend.service.impl;

import cn.edu.sustech.ooadbackend.mapper.CourseMapper;
import cn.edu.sustech.ooadbackend.model.domain.Course;
import cn.edu.sustech.ooadbackend.service.CourseService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * @className CourseServiceImpl
 * @version 1.0
 * @author DYH
 * @since 2023/10/9 12:41
 */
    
@Service
public class CourseServiceImpl extends ServiceImpl<CourseMapper, Course> implements CourseService{

}
