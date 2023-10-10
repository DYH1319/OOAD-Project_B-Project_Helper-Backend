package cn.edu.sustech.ooadbackend.service.impl;

import cn.edu.sustech.ooadbackend.mapper.UserProjectMapper;
import cn.edu.sustech.ooadbackend.model.domain.UserProject;
import cn.edu.sustech.ooadbackend.service.UserProjectService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * @className UserProjectServiceImpl
 * @version 1.0
 * @author DYH
 * @since 2023/10/9 12:44
 */
    
@Service
public class UserProjectServiceImpl extends ServiceImpl<UserProjectMapper, UserProject> implements UserProjectService{

}
