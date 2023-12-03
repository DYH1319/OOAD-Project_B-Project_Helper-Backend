package cn.edu.sustech.ooadbackend.service.impl;

import cn.edu.sustech.ooadbackend.common.StatusCode;
import cn.edu.sustech.ooadbackend.exception.BusinessException;
import cn.edu.sustech.ooadbackend.mapper.UserProjectMapper;
import cn.edu.sustech.ooadbackend.model.domain.UserProject;
import cn.edu.sustech.ooadbackend.service.UserProjectService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @className UserProjectServiceImpl
 * @version 1.0
 * @author DYH
 * @since 2023/10/9 12:44
 */
    
@Service
public class UserProjectServiceImpl extends ServiceImpl<UserProjectMapper, UserProject> implements UserProjectService{

    @Override
    public List<UserProject> listUserProjectByUserId(Long userID) {
        QueryWrapper<UserProject> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id",userID);
        List<UserProject> list = this.list(queryWrapper);
        if (list == null || list.isEmpty()) throw new BusinessException(StatusCode.NULL_ERROR, "查找不到用户的项目信息");

        return list;
    }
}
