package cn.edu.sustech.ooadbackend.service.impl;

import cn.edu.sustech.ooadbackend.mapper.GroupMapper;
import cn.edu.sustech.ooadbackend.model.domain.Group;
import cn.edu.sustech.ooadbackend.service.GroupService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * @className GroupServiceImpl
 * @version 1.0
 * @author DYH
 * @since 2023/10/9 12:42
 */
    
@Service
public class GroupServiceImpl extends ServiceImpl<GroupMapper, Group> implements GroupService{

}
