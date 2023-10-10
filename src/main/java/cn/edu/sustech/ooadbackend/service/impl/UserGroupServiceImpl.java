package cn.edu.sustech.ooadbackend.service.impl;

import cn.edu.sustech.ooadbackend.mapper.UserGroupMapper;
import cn.edu.sustech.ooadbackend.model.domain.UserGroup;
import cn.edu.sustech.ooadbackend.service.UserGroupService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * @className UserGroupServiceImpl
 * @version 1.0
 * @author DYH
 * @since 2023/10/9 12:44
 */
    
@Service
public class UserGroupServiceImpl extends ServiceImpl<UserGroupMapper, UserGroup> implements UserGroupService{

}
