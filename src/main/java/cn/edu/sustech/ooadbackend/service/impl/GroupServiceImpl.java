package cn.edu.sustech.ooadbackend.service.impl;

import cn.edu.sustech.ooadbackend.mapper.GroupMapper;
import cn.edu.sustech.ooadbackend.model.domain.Group;
import cn.edu.sustech.ooadbackend.model.domain.UserGroup;
import cn.edu.sustech.ooadbackend.service.GroupService;
import cn.edu.sustech.ooadbackend.service.UserGroupService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

/**
 * @className GroupServiceImpl
 * @version 1.0
 * @author DYH
 * @since 2023/10/9 12:42
 */
    
@Service
public class GroupServiceImpl extends ServiceImpl<GroupMapper, Group> implements GroupService{

    @Resource
    private UserGroupService userGroupService;
    @Override
    public Integer getGroupCurrentNumber(Long groupId) {
        QueryWrapper<UserGroup> userGroupQueryWrapper = new QueryWrapper<>();
        userGroupQueryWrapper.eq("group_id", groupId);
        long count = userGroupService.count(userGroupQueryWrapper);
        return (int) count;
    }
}
