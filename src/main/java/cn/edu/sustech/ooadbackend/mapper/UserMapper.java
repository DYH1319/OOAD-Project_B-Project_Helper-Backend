package cn.edu.sustech.ooadbackend.mapper;

import cn.edu.sustech.ooadbackend.model.domain.User;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;


/**
 * @className UserMapper
 * @version 1.0
 * @author DYH
 * @since 2023/10/9 12:44
 */
    
@Mapper
public interface UserMapper extends BaseMapper<User> {
}