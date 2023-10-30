package cn.edu.sustech.ooadbackend.service.impl;

import cn.edu.sustech.ooadbackend.common.StatusCode;
import cn.edu.sustech.ooadbackend.exception.BusinessException;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.stereotype.Service;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import cn.edu.sustech.ooadbackend.model.domain.UserCourse;
import cn.edu.sustech.ooadbackend.mapper.UserCourseMapper;
import cn.edu.sustech.ooadbackend.service.UserCourseService;

import java.util.List;

/**
 * @className UserCourseServiceImpl
 * @version 1.0
 * @author DYH
 * @since 2023/10/11 15:09
 */
    
@Service
public class UserCourseServiceImpl extends ServiceImpl<UserCourseMapper, UserCourse> implements UserCourseService{

    @Override
    public List<UserCourse> listUserCourseByUserId(Long id) {

        // 查询对应用户的课程关系
        QueryWrapper<UserCourse> userCourseQueryWrapper = new QueryWrapper<>();
        userCourseQueryWrapper.eq("user_id", id);
        List<UserCourse> userCourseList = this.list(userCourseQueryWrapper);
        if (userCourseList == null || userCourseList.isEmpty()) throw new BusinessException(StatusCode.NULL_ERROR, "查找不到用户的课程信息");
        return userCourseList;

    }

    @Override
    public List<UserCourse> listUserCourseByCourseId(Long id) {

        // 查询对应用户的课程关系
        QueryWrapper<UserCourse> userCourseQueryWrapper = new QueryWrapper<>();
        userCourseQueryWrapper.eq("course_id", id);
        List<UserCourse> userCourseList = this.list(userCourseQueryWrapper);
        if (userCourseList == null || userCourseList.isEmpty()) throw new BusinessException(StatusCode.NULL_ERROR, "查找不到用户的课程信息");
        return userCourseList;

    }
}
