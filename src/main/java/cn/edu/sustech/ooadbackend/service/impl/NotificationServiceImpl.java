package cn.edu.sustech.ooadbackend.service.impl;

import cn.edu.sustech.ooadbackend.mapper.NotificationMapper;
import cn.edu.sustech.ooadbackend.model.domain.Notification;
import cn.edu.sustech.ooadbackend.service.NotificationService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * @className NotificationServiceImpl
 * @version 1.0
 * @author DYH
 * @since 2023/10/9 12:42
 */
    
@Service
public class NotificationServiceImpl extends ServiceImpl<NotificationMapper, Notification> implements NotificationService{

}
