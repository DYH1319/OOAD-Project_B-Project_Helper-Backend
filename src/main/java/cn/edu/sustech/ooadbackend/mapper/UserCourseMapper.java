package cn.edu.sustech.ooadbackend.mapper;

import cn.edu.sustech.ooadbackend.model.domain.UserCourse;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;


/**
 * @className UserCourseMapper
 * @version 1.0
 * @author DYH
 * @since 2023/10/11 15:09
 */
    
@Mapper
public interface UserCourseMapper extends BaseMapper<UserCourse> {
}