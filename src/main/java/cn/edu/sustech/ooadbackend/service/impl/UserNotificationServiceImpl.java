package cn.edu.sustech.ooadbackend.service.impl;

import cn.edu.sustech.ooadbackend.mapper.UserNotificationMapper;
import cn.edu.sustech.ooadbackend.model.domain.UserNotification;
import cn.edu.sustech.ooadbackend.service.UserNotificationService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * @className UserNotificationServiceImpl
 * @version 1.0
 * @author DYH
 * @since 2023/10/9 12:44
 */
    
@Service
public class UserNotificationServiceImpl extends ServiceImpl<UserNotificationMapper, UserNotification> implements UserNotificationService{

}
