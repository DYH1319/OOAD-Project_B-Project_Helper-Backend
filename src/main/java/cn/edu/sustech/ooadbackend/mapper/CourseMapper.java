package cn.edu.sustech.ooadbackend.mapper;

import cn.edu.sustech.ooadbackend.model.domain.Course;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;


/**
 * @className CourseMapper
 * @version 1.0
 * @author DYH
 * @since 2023/10/9 12:41
 */
    
@Mapper
public interface CourseMapper extends BaseMapper<Course> {
    boolean updateCourse(Course newCourse);

}