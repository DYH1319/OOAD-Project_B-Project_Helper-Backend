package cn.edu.sustech.ooadbackend.mapper;

import cn.edu.sustech.ooadbackend.model.domain.Notification;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;


/**
 * @author DYH
 * @version 1.0
 * @className NotificationMapper
 * @since 2023/10/17 11:25
 */

@Mapper
public interface NotificationMapper extends BaseMapper<Notification> {
}