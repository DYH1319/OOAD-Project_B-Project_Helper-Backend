package cn.edu.sustech.ooadbackend.mapper;

import cn.edu.sustech.ooadbackend.model.domain.UserNotification;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;


/**
 * @className UserNotificationMapper
 * @version 1.0
 * @author DYH
 * @since 2023/10/9 12:44
 */
    
@Mapper
public interface UserNotificationMapper extends BaseMapper<UserNotification> {
}