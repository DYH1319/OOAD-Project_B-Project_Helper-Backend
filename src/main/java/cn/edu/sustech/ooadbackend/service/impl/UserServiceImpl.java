package cn.edu.sustech.ooadbackend.service.impl;

import cn.edu.sustech.ooadbackend.common.StatusCode;
import cn.edu.sustech.ooadbackend.common.FileType;
import cn.edu.sustech.ooadbackend.constant.UserConstant;
import cn.edu.sustech.ooadbackend.exception.BusinessException;
import cn.edu.sustech.ooadbackend.mapper.UserMapper;
import cn.edu.sustech.ooadbackend.model.domain.User;
import cn.edu.sustech.ooadbackend.model.request.CurrentUserUpdateRequest;
import cn.edu.sustech.ooadbackend.model.request.UserInsertRequest;
import cn.edu.sustech.ooadbackend.model.request.UserUpdateRequest;
import cn.edu.sustech.ooadbackend.service.UserService;
import cn.edu.sustech.ooadbackend.utils.CosManager;
import cn.edu.sustech.ooadbackend.utils.DateTimeFormatTransferUtils;
import cn.edu.sustech.ooadbackend.utils.RedisClient;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

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
    
    @Autowired
    private JavaMailSender javaMailSender;
    
    @Autowired
    private RedisClient redisClient;
    
    @Resource
    private CosManager cosManager;
    
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
        if (selectUser != null) throw new BusinessException(StatusCode.PARAMS_ERROR, "学工号已存在");
        // 2. 加密
        String encryptPassword = md5EncryptPassword(userPassword);
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
        String encryptPassword = md5EncryptPassword(userPassword);
        // 查询用户是否存在
        LambdaQueryWrapper<User> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(User::getUserAccount, userAccount);
        User user = this.getOne(lambdaQueryWrapper);
        if (user == null) {
            throw new BusinessException(StatusCode.PARAMS_ERROR, "学工号不存在");
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
    public boolean userLoginMailSend(String mailAddress, HttpServletRequest request) {
        
        // 校验邮箱地址的有效性
        if (!Pattern.compile("^[A-Za-z0-9+_.-]+@([A-Za-z0-9-]+\\.)+[A-Za-z]{2,8}$").matcher(mailAddress).matches()) {
            throw new BusinessException(StatusCode.PARAMS_ERROR, "邮箱地址不合法");
        }
        if (userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getEmail, mailAddress)) == null) {
            throw new BusinessException(StatusCode.PARAMS_ERROR, "邮箱地址不存在");
        }
        
        String code = generateVerificationCode();
        String mailAddressPrefix = mailAddress + "@@@";
        
        if (redisClient.get(mailAddressPrefix) != null) {
            throw new BusinessException(StatusCode.PARAMS_ERROR, "请勿频繁请求验证码");
        }
        
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("dyh1319@qq.com");
        message.setTo(mailAddress);
        message.setSubject("Project Helper 验证码");
        message.setText("【验证码】您的登录验证码为：" + code + "。打死也不要告诉别人！（5分钟内有效）");
        javaMailSender.send(message);
        
        // 设置验证码，有效期5分钟
        redisClient.set(mailAddress, code, 300);
        // 设置重发间隔，1分钟后可重发
        redisClient.set(mailAddressPrefix, "NoResend", 60);
        
        return true;
    }
    
    @Override
    public User userLoginMailCheck(String mailAddress, String verificationCode, HttpServletRequest request) {
        // 1. 校验
        // 校验非null非空
        if (StringUtils.isAnyBlank(mailAddress, verificationCode)) throw new BusinessException(StatusCode.PARAMS_ERROR, "参数为空");
        // 校验邮箱地址的有效性
        if (!Pattern.compile("^[A-Za-z0-9+_.-]+@([A-Za-z0-9-]+\\.)+[A-Za-z]{2,8}$").matcher(mailAddress).matches()) {
            throw new BusinessException(StatusCode.PARAMS_ERROR, "邮箱地址不合法");
        }
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getEmail, mailAddress));
        if (user == null) {
            throw new BusinessException(StatusCode.PARAMS_ERROR, "邮箱地址不存在");
        }
        // 查询邮箱验证码是否匹配
        if (!verificationCode.equals(redisClient.get(mailAddress))) {
            throw new BusinessException(StatusCode.PARAMS_ERROR, "验证码不正确");
        }
        // 2. 清除验证码缓存
        if (!redisClient.delete(mailAddress)) {
            throw new BusinessException(StatusCode.SYSTEM_ERROR);
        }
        // 3. 用户脱敏
        User safetyUser = getSafetyUser(user);
        // 4. 记录用户的登录态
        request.getSession().setAttribute(UserConstant.USER_LOGIN_STATE, safetyUser);
        return safetyUser;
    }
    
    @Override
    public int userLogout(HttpServletRequest request) {
        if (request.getSession().getAttribute(UserConstant.USER_LOGIN_STATE) == null) {
            throw new BusinessException(StatusCode.PARAMS_ERROR, "您尚未登录，无法注销");
        }
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
        if (!isUrl(safetyUser.getAvatarUrl())) {
            safetyUser.setAvatarUrl(cosManager.getObjectPresignedUrl(safetyUser.getAvatarUrl(), FileType.AVATAR_IMAGE));
        }
        request.getSession().setAttribute(UserConstant.USER_LOGIN_STATE, safetyUser);
        return safetyUser;
    }
    
    @Override
    public String avatarUpload(MultipartFile file, HttpServletRequest request) {
        User currentUser = (User) request.getSession().getAttribute(UserConstant.USER_LOGIN_STATE);
        if (currentUser == null) {
            throw new BusinessException(StatusCode.NOT_LOGIN, "未登录，请先登录");
        }
        
        // 上传至对象存储桶
        // noinspection StringBufferReplaceableByString
        String key = new StringBuilder()
            .append(new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date()))
            .append('-')
            .append(UUID.randomUUID())
            .append('-')
            .append(file.getOriginalFilename())
            .toString();
        try {
            cosManager.putObject(key, file.getInputStream(), file.getSize(), FileType.AVATAR_IMAGE);
        } catch (IOException e) {
            throw new BusinessException(StatusCode.SYSTEM_ERROR, "上传失败");
        }
        
        // 数据库存储键
        User user = new User();
        user.setId(currentUser.getId());
        user.setAvatarUrl(key);
        int count = userMapper.updateById(user);
        
        if (count != 1) {
            throw new BusinessException(StatusCode.SYSTEM_ERROR, "数据库异常，请联系管理员");
        }
        
        return cosManager.getObjectPresignedUrl(key, FileType.AVATAR_IMAGE);
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
    
    private String generateVerificationCode() {
        int min = 0;
        int max = 999999;
        
        int randomNum = ThreadLocalRandom.current().nextInt(min, max + 1);
        return String.format("%06d", randomNum);
    }
    
    private String md5EncryptPassword(String originPassword) {
        return DigestUtils.md5DigestAsHex((UserConstant.SALT + originPassword).getBytes(StandardCharsets.UTF_8));
    }
    
    private boolean isUrl(String text) {
        return text.toLowerCase().startsWith("http") || text.toLowerCase().startsWith("https");
    }

    @Override
    public Boolean currentUserUpdate(HttpServletRequest request, CurrentUserUpdateRequest currentUserRequest) {
        // 获取当前用户态
        // 1.判断用户是否处于登陆状态
        User currentUser = (User)request.getSession().getAttribute(UserConstant.USER_LOGIN_STATE);
        if (currentUser == null) throw new BusinessException(StatusCode.NOT_LOGIN, "用户未登陆");

        // 2.判断用户是否在进行非法撰写
        if (!currentUser.getId().equals(currentUserRequest.getId())) throw new BusinessException(StatusCode.NO_AUTH, "无权限修改当前用户");

        User newCurrentUser = new User();
        newCurrentUser.setId(currentUserRequest.getId());
        newCurrentUser.setAge(currentUserRequest.getAge());
        newCurrentUser.setAvatarUrl(currentUserRequest.getAvatarUrl());
        newCurrentUser.setTechnicalStack(currentUserRequest.getTechnicalStack());
        newCurrentUser.setProgrammingSkills(currentUserRequest.getProgrammingSkills());
        newCurrentUser.setIntendedTeammates(currentUserRequest.getIntendedTeammates());

        Boolean update = userMapper.updateCurrentUser(newCurrentUser);
        if (!update) throw new BusinessException(StatusCode.SYSTEM_ERROR, "无法修改当前用户信息");
        return update;
    }
    @Override
    public List<User> listTeacher(HttpServletRequest request) {
        User currentUser = (User) request.getSession().getAttribute(UserConstant.USER_LOGIN_STATE);
        if (currentUser == null) throw new BusinessException(StatusCode.NOT_LOGIN, "用户未登录");
        List<User> userList = this.list();
        List<User> teacherList ;
        if (userList == null) throw new BusinessException(StatusCode.NULL_ERROR, "教师列表为空");
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_role",UserConstant.TEACHER_ROLE);
        teacherList = this.list(queryWrapper);

        if (teacherList.isEmpty()) throw new BusinessException(StatusCode.NULL_ERROR, "教师列表为空");
        return teacherList.stream().map(this::getSafetyTeacher).toList();
    }

    @Override
    public List<User> listTa(HttpServletRequest request) {
        User currentUser = (User) request.getSession().getAttribute(UserConstant.USER_LOGIN_STATE);
        if (currentUser == null) throw new BusinessException(StatusCode.NOT_LOGIN, "用户未登录");
        List<User> userList = this.list();
        List<User> taList = new ArrayList<>();
        if (userList == null) throw new BusinessException(StatusCode.NULL_ERROR, "教师助理列表为空");
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_role",UserConstant.TEACHER_ASSISTANT_ROLE);
        taList = this.list(queryWrapper);
        if (taList.isEmpty()) throw new BusinessException(StatusCode.NULL_ERROR, "教师助理列表为空");
        return taList.stream().map(this::getSafetyTa).toList();
    }

    @Override
    public List<User> listByParam(HttpServletRequest request, String userAccount, String username, Integer userRole, Integer age, Byte gender, String email, String avatarUrl, String startTime, String endTime) {
        Date start = DateTimeFormatTransferUtils.frontDateTime2BackDate(startTime);
        Date end = DateTimeFormatTransferUtils.frontDateTime2BackDate(endTime);
        LambdaQueryWrapper<User> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        if (StringUtils.isNotBlank(userAccount)) {
            lambdaQueryWrapper.like(User::getUserAccount, userAccount);
        }
        if (StringUtils.isNotBlank(username)) {
            lambdaQueryWrapper.like(User::getUsername, username);
        }
        if (StringUtils.isNotBlank(avatarUrl)) {
            lambdaQueryWrapper.like(User::getAvatarUrl, avatarUrl);
        }
        if (gender != null) {
            lambdaQueryWrapper.eq(User::getGender, gender);
        }
        if (age != null) {
            lambdaQueryWrapper.eq(User::getAge, age);
        }
        if (StringUtils.isNotBlank(email)) {
            lambdaQueryWrapper.like(User::getEmail, email);
        }
        if (userRole != null) {
            lambdaQueryWrapper.eq(User::getUserRole, userRole);
        }
        if (start != null) {
            lambdaQueryWrapper.ge(User::getCreateTime, startTime);
        }
        if (end != null) {
            lambdaQueryWrapper.le(User::getCreateTime, endTime);
        }
        List<User> userList = userMapper.selectList(lambdaQueryWrapper);
        userList.stream().filter(user -> !isUrl(user.getAvatarUrl())).forEach(user -> {
            user.setAvatarUrl(cosManager.getObjectPresignedUrl(user.getAvatarUrl(), FileType.AVATAR_IMAGE));
        });
        return userList.stream().map(this::getSafetyUser).collect(Collectors.toList());
    }

    @Override
    public List<User> listStudent(HttpServletRequest request) {
        User currentUser = (User) request.getSession().getAttribute(UserConstant.USER_LOGIN_STATE);
        if (currentUser == null) throw new BusinessException(StatusCode.NOT_LOGIN, "用户未登录");
        List<User> userList = this.list();
        List<User> studentList = new ArrayList<>();
        if (userList == null) throw new BusinessException(StatusCode.NULL_ERROR, "学生列表为空");
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_role",UserConstant.STUDENT_ROLE);
        studentList = this.list(queryWrapper);
        if (studentList.isEmpty()) throw new BusinessException(StatusCode.NULL_ERROR, "学生列表为空");
        return studentList.stream().map(this::getSafetyTa).toList();
    }

    @Override
    @Transactional
    public Long insertUser(UserInsertRequest userInsertRequest) {
        // 校验账户不能与已有账户重复（需要访问数据库的校验应该在校验的最后执行以优化性能）
        LambdaQueryWrapper<User> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(User::getUserAccount, userInsertRequest.getUserAccount());
        User selectUser = this.getOne(lambdaQueryWrapper);
        if (selectUser != null) throw new BusinessException(StatusCode.PARAMS_ERROR, "学工号已存在");
        
        User newUser = new User();
        newUser.setUserAccount(userInsertRequest.getUserAccount());
        newUser.setUsername(userInsertRequest.getUsername());
        newUser.setUserPassword(md5EncryptPassword(userInsertRequest.getUserAccount()));
        newUser.setUserRole(userInsertRequest.getUserRole());
        newUser.setAge(userInsertRequest.getAge());
        newUser.setGender(userInsertRequest.getGender());
        newUser.setEmail(userInsertRequest.getEmail());
        boolean isInsert = this.save(newUser);
        if (!isInsert) throw new BusinessException(StatusCode.PARAMS_ERROR, "用户新增失败");

        return newUser.getId();
    }

    @Override
    @Transactional
    public Boolean deleteUser(Long userId) {
        //删除User表中的数据
        QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
        userQueryWrapper.eq("id",userId);
        Boolean userRemove = this.remove(userQueryWrapper);

        return userRemove;
    }
    @Override
    @Transactional
    public Boolean updateUser(UserUpdateRequest userUpdateRequest) {
        User user = new User();
        user.setId(userUpdateRequest.getId());
        user.setUserAccount(userUpdateRequest.getUserAccount());
        user.setUsername(userUpdateRequest.getUsername());
        user.setUserRole(userUpdateRequest.getUserRole());
        user.setAge(userUpdateRequest.getAge());
        user.setGender(userUpdateRequest.getGender());
        user.setEmail(userUpdateRequest.getEmail());
        user.setAvatarUrl(userUpdateRequest.getAvatarUrl());

        boolean isUpdated = userMapper.updateUser(user);
        if (!isUpdated) throw new BusinessException(StatusCode.PARAMS_ERROR, "用户信息更新失败");

        return isUpdated;
    }



    private User getSafetyTeacher(User user){
        User safeTeacher = new User();
        safeTeacher.setId(user.getId());
        safeTeacher.setUsername(user.getUsername());
        return safeTeacher;
    }
    private User getSafetyTa(User user){
        User safeTa = new User();
        safeTa.setId(user.getId());
        safeTa.setUsername(user.getUsername());
        return safeTa;
    }
    private User getSafeUser(User user){
        User safeUser = new User();
        safeUser.setId(user.getId());
        safeUser.setUserAccount(user.getUserAccount());
        safeUser.setUserRole(user.getUserRole());
        safeUser.setAge(user.getAge());
        safeUser.setGender(user.getGender());
        safeUser.setEmail(user.getEmail());
        safeUser.setAvatarUrl(user.getAvatarUrl());
        safeUser.setCreateTime(user.getCreateTime());
        return safeUser;
    }
}
