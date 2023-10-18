package cn.edu.sustech.ooadbackend.service.impl;

import cn.edu.sustech.ooadbackend.common.StatusCode;
import cn.edu.sustech.ooadbackend.constant.UserConstant;
import cn.edu.sustech.ooadbackend.exception.BusinessException;
import cn.edu.sustech.ooadbackend.mapper.UserMapper;
import cn.edu.sustech.ooadbackend.model.domain.User;
import cn.edu.sustech.ooadbackend.service.UserService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.nio.charset.StandardCharsets;

/**
 * @className UserServiceImpl
 * @version 1.0
 * @author DYH
 * @since 2023/10/9 12:44
 */
    
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
    
    @Autowired
    private UserMapper userMapper;
    
    @Override
    public long userRegister(String userAccount, String username, String userPassword, String checkPassword) {
        // 1. 校验
        // 校验非null非空
        if (StringUtils.isAnyBlank(userAccount, username, userPassword, checkPassword)) throw new BusinessException(StatusCode.PARAMS_ERROR, "参数为空");
        // 校验校验密码和密码相同
        if (!userPassword.equals(checkPassword)) throw new BusinessException(StatusCode.PARAMS_ERROR, "两次输入的密码不相同");
        // 校验账户不能与已有账户重复（需要访问数据库的校验应该在校验的最后执行以优化性能）
        LambdaQueryWrapper<User> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(User::getUserAccount, userAccount);
        User selectUser = this.getOne(lambdaQueryWrapper);
        if (selectUser != null) throw new BusinessException(StatusCode.PARAMS_ERROR, "账户名已存在");
        // 2. 加密
        String encryptPassword = DigestUtils.md5DigestAsHex((UserConstant.SALT + userPassword).getBytes(StandardCharsets.UTF_8));
        // 3. 插入数据
        User user = new User();
        user.setUserAccount(userAccount);
        user.setUsername(username);
        user.setUserPassword(encryptPassword);
        boolean saveResult = this.save(user);
        if (!saveResult) throw new BusinessException(StatusCode.SYSTEM_ERROR, "无法新建用户");
        return user.getId();
    }
    
    @Override
    public User userLogin(String userAccount, String userPassword, HttpServletRequest request) {
        // 1. 校验
        // 校验非null非空
        if (StringUtils.isAnyBlank(userAccount, userPassword)) throw new BusinessException(StatusCode.PARAMS_ERROR, "参数为空");
        // 2. 加密并校验账户密码是否正确
        String encryptPassword = DigestUtils.md5DigestAsHex((UserConstant.SALT + userPassword).getBytes(StandardCharsets.UTF_8));
        // 查询用户是否存在
        LambdaQueryWrapper<User> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(User::getUserAccount, userAccount);
        User user = this.getOne(lambdaQueryWrapper);
        if (user == null) {
            throw new BusinessException(StatusCode.PARAMS_ERROR, "账号不存在");
        }
        // 查询账户密码是否匹配
        lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(User::getUserAccount, userAccount);
        lambdaQueryWrapper.eq(User::getUserPassword, encryptPassword);
        user = this.getOne(lambdaQueryWrapper);
        if (user == null) {
            throw new BusinessException(StatusCode.PARAMS_ERROR, "账号与密码不匹配");
        }
        // 3. 用户脱敏
        User safetyUser = getSafetyUser(user);
        // 4. 记录用户的登录态
        request.getSession().setAttribute(UserConstant.USER_LOGIN_STATE, safetyUser);
        return safetyUser;
    }
    
    @Override
    public int userLogout(HttpServletRequest request) {
        // 移除登录态
        request.getSession().removeAttribute(UserConstant.USER_LOGIN_STATE);
        return 0;
    }
    
    @Override
    public User getCurrentUser(HttpServletRequest request) {
        User currentUser = (User) request.getSession().getAttribute(UserConstant.USER_LOGIN_STATE);
        if (currentUser == null) {
            throw new BusinessException(StatusCode.NOT_LOGIN, "未登录，请先登录");
        }
        long userId = currentUser.getId();
        User user = this.getById(userId);
        User safetyUser = this.getSafetyUser(user);
        request.getSession().setAttribute(UserConstant.USER_LOGIN_STATE, safetyUser);
        return this.getSafetyUser(safetyUser);
    }
    
    private User getSafetyUser(User originUser) {
        if (originUser == null) throw new BusinessException(StatusCode.PARAMS_ERROR, "用户不存在");
        User safetyUser = new User();
        safetyUser.setId(originUser.getId());
        safetyUser.setUserAccount(originUser.getUserAccount());
        safetyUser.setUserRole(originUser.getUserRole());
        safetyUser.setUsername(originUser.getUsername());
        safetyUser.setAge(originUser.getAge());
        safetyUser.setGender(originUser.getGender());
        safetyUser.setEmail(originUser.getEmail());
        safetyUser.setAvatarUrl(originUser.getAvatarUrl());
        safetyUser.setTechnicalStack(originUser.getTechnicalStack());
        safetyUser.setProgrammingSkills(originUser.getProgrammingSkills());
        safetyUser.setIntendedTeammates(originUser.getIntendedTeammates());
        safetyUser.setCreateTime(originUser.getCreateTime());
        return safetyUser;
    }

    @Override
    public Boolean currentUserUpdate(HttpServletRequest request, User user) {
        // 获取当前用户态
        // 1.判断用户是否处于登陆状态
        User currentUser = (User)request.getSession().getAttribute(UserConstant.USER_LOGIN_STATE);
        if (currentUser == null) throw new BusinessException(StatusCode.NOT_LOGIN, "用户未登陆");

        // 2.判断用户是否在进行非法撰写
        if (!currentUser.getId().equals(user.getId())) throw new BusinessException(StatusCode.NO_AUTH, "无权限修改当前用户");

        User oldCurrentUser = this.getById(currentUser.getId());

        if (oldCurrentUser == null) throw new BusinessException(StatusCode.NULL_ERROR, "修改的当前用户不存在");
        oldCurrentUser.setAge(user.getAge());
        oldCurrentUser.setAvatarUrl(user.getAvatarUrl());
        oldCurrentUser.setTechnicalStack(user.getTechnicalStack());
        oldCurrentUser.setProgrammingSkills(user.getProgrammingSkills());
        oldCurrentUser.setIntendedTeammates(user.getIntendedTeammates());

        QueryWrapper<User> currentUserUpdateWrapper = new QueryWrapper<>();
        currentUserUpdateWrapper.eq("id", oldCurrentUser.getId());
        Boolean update = this.update(oldCurrentUser, currentUserUpdateWrapper);
        if (!update) throw new BusinessException(StatusCode.SYSTEM_ERROR, "无法修改当前用户信息");
        return update;
    }
}
