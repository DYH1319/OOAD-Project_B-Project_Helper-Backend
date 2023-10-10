package cn.edu.sustech.ooadbackend.mapper;

import cn.edu.sustech.ooadbackend.model.domain.Group;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;


/**
 * @className GroupMapper
 * @version 1.0
 * @author DYH
 * @since 2023/10/9 12:42
 */
    
@Mapper
public interface GroupMapper extends BaseMapper<Group> {
}