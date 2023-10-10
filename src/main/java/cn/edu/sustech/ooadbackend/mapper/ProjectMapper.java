package cn.edu.sustech.ooadbackend.mapper;

import cn.edu.sustech.ooadbackend.model.domain.Project;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;


/**
 * @className ProjectMapper
 * @version 1.0
 * @author DYH
 * @since 2023/10/9 12:43
 */
    
@Mapper
public interface ProjectMapper extends BaseMapper<Project> {
}