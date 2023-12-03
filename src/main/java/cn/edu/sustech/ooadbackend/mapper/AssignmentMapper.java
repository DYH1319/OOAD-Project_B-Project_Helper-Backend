package cn.edu.sustech.ooadbackend.mapper;

import cn.edu.sustech.ooadbackend.model.domain.Assignment;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;


/**
 * @className AssignmentMapper
 * @version 1.0
 * @author DYH
 * @since 2023/10/9 12:37
 */
    
@Mapper
public interface AssignmentMapper extends BaseMapper<Assignment> {
    boolean update(Assignment newAssignment);
}