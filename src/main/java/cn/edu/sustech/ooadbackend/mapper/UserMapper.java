package cn.edu.sustech.ooadbackend.mapper;

import cn.edu.sustech.ooadbackend.model.domain.User;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;


/**
 * @author DYH
 * @version 1.0
 * @className UserMapper
 * @since 2023/10/17 11:13
 */

@Mapper
public interface UserMapper extends BaseMapper<User> {
}