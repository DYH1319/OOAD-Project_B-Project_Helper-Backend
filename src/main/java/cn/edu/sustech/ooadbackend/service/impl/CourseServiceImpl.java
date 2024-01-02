package cn.edu.sustech.ooadbackend.service.impl;

import cn.edu.sustech.ooadbackend.common.StatusCode;
import cn.edu.sustech.ooadbackend.constant.UserConstant;
import cn.edu.sustech.ooadbackend.exception.BusinessException;
import cn.edu.sustech.ooadbackend.mapper.CourseMapper;
import cn.edu.sustech.ooadbackend.model.domain.*;
import cn.edu.sustech.ooadbackend.model.request.CourseInsertRequest;
import cn.edu.sustech.ooadbackend.model.request.CourseUpdateRequest;
import cn.edu.sustech.ooadbackend.model.request.NotificationInsertRequest;
import cn.edu.sustech.ooadbackend.model.response.CourseInfoResponse;
import cn.edu.sustech.ooadbackend.service.*;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Consumer;

/**
 * @className CourseServiceImpl
 * @version 1.0
 * @author DYH
 * @since 2023/10/9 12:41
 */
    
@Service
public class CourseServiceImpl extends ServiceImpl<CourseMapper, Course> implements CourseService{

    @Resource
    private UserService userService;

    @Resource
    private CourseMapper courseMapper;

    @Resource
    private UserCourseService userCourseService;

    @Resource
    private TeacherAssistantCourseService teacherAssistantCourseService;

    @Resource
    private NotificationService notificationService;

    @Resource
    private UserNotificationService userNotificationService;

    @Override
    public List<Course> listCourse(HttpServletRequest request) {
        // 获取用户登录态
        User currentUser = (User) request.getSession().getAttribute(UserConstant.USER_LOGIN_STATE);
        if (currentUser == null) throw new BusinessException(StatusCode.NOT_LOGIN, "用户未登录");

        if (currentUser.getUserRole() == UserConstant.STUDENT_ROLE){

            // 查询对应用户的课程关系
            List<UserCourse> userCourseList = userCourseService.listUserCourseByUserId(currentUser.getId());

            // 将课程关系转为课程id，方便后续查询
            List<Long> courseIdList = userCourseList.stream().map(UserCourse::getCourseId).toList();

            // 查询当前学生对应的课程列表
            List<Course> courseList = this.listByIds(courseIdList);
            if (courseList == null || courseList.isEmpty()) throw new BusinessException(StatusCode.NULL_ERROR, "查找不到用户的课程信息");
            return courseList.stream().map(this::getSafetyCourse).toList();

        } else if (currentUser.getUserRole() == UserConstant.TEACHER_ASSISTANT_ROLE) {

            // 获取教师助理对应的课程id列表
            List<TeacherAssistantCourse> teacherAssistantCourseList = teacherAssistantCourseService.listByTeacherAssistantId(currentUser.getId());

            // 将课程关系转为课程id，方便后续查询
            List<Long> courseIdList = teacherAssistantCourseList.stream().map(TeacherAssistantCourse::getCourseId).toList();

            // 查询助教的课程列表
            List<Course> courseList = this.listByIds(courseIdList);
            if (courseList == null || courseList.isEmpty()) throw new BusinessException(StatusCode.NULL_ERROR, "查找不到用户的课程信息");
            return courseList.stream().map(this::getSafetyCourse).toList();

        }else if (currentUser.getUserRole() == UserConstant.TEACHER_ROLE){

            // 查询教师的课程列表
            QueryWrapper<Course> courseQueryWrapper = new QueryWrapper<>();
            courseQueryWrapper.eq("teacher_id", currentUser.getId());
            List<Course> courseList = this.list(courseQueryWrapper);
            if (courseList == null || courseList.isEmpty()) throw new BusinessException(StatusCode.NULL_ERROR, "查找不到用户的课程信息");
            return courseList.stream().map(this::getSafetyCourse).toList();

        } else if (currentUser.getUserRole() == UserConstant.ADMIN_ROLE){

            // 查询所有课程列表
            List<Course> courseList = this.list();
            if (courseList == null || courseList.isEmpty()) throw new BusinessException(StatusCode.NULL_ERROR, "课程库当前无课程信息");
            return courseList.stream().map(this::getSafetyCourse).toList();

        }else{
            throw new BusinessException(StatusCode.UNKNOWN_ERROR, "用户会话数据错误");
        }
    }

    @Override
    @Transactional
    public Boolean updateCourse(CourseUpdateRequest courseUpdateRequest) {
        Course course = new Course();
        course.setId(courseUpdateRequest.getId());
        course.setTeacherId(courseUpdateRequest.getTeacherId());
        course.setCourseName(courseUpdateRequest.getCourseName());

        boolean isUpdated = courseMapper.updateCourse(course);
//        if (!isUpdated) throw new BusinessException(StatusCode.PARAMS_ERROR, "课程信息更新失败");

        // 校验助教Id列表是否合法
        QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
        userQueryWrapper.eq("user_role", UserConstant.TEACHER_ASSISTANT_ROLE);
        List<User> taUserList = userService.list(userQueryWrapper);
        List<Long> toIdList = taUserList.stream().map(User::getId).toList();
        List<Long> newtTaIdList = new ArrayList<>();
        if (courseUpdateRequest.getTaIdList() != null) newtTaIdList = Arrays.asList(courseUpdateRequest.getTaIdList());
        if (!new HashSet<>(toIdList).containsAll(newtTaIdList)) throw new BusinessException(StatusCode.PARAMS_ERROR, "助教列表中含有非法用户");

        Boolean b = teacherAssistantCourseService.removeByCourseId(courseUpdateRequest.getId());

        List<TeacherAssistantCourse> newTeacherAssistantCourses = newtTaIdList.stream().map(aLong -> {
            TeacherAssistantCourse teacherAssistantCourse = new TeacherAssistantCourse();
            teacherAssistantCourse.setCourseId(courseUpdateRequest.getId());
            teacherAssistantCourse.setTeacherAssistantId(aLong);
            return teacherAssistantCourse;
        }).toList();

        Boolean saveBatch = teacherAssistantCourseService.saveBatch(newTeacherAssistantCourses);
        if (saveBatch != true && courseUpdateRequest.getTaIdList() != null) throw new BusinessException(StatusCode.PARAMS_ERROR, "课程信息更新失败");

        return true;
    }

    @Override
    @Transactional
    public Boolean deleteCourse(Long courseId){

        // 删除课程表中的数据
        QueryWrapper<Course> courseQueryWrapper = new QueryWrapper<>();
        courseQueryWrapper.eq("id", courseId);
        Boolean courseRemoved = this.remove(courseQueryWrapper);
//        if (!courseRemoved) throw new BusinessException(StatusCode.PARAMS_ERROR, "课程信息删除失败");

        // 删除助教课程关系表中的相关数据
        QueryWrapper<TeacherAssistantCourse> TACourseQueryWrapper = new QueryWrapper<>();
        TACourseQueryWrapper.eq("course_id", courseId);
        boolean taCourseRemoved = teacherAssistantCourseService.remove(TACourseQueryWrapper);

        // 删除学生课程关系表中的相关数据
        QueryWrapper<UserCourse> UserCourseQueryWrapper = new QueryWrapper<>();
        UserCourseQueryWrapper.eq("course_id", courseId);
        boolean userCourseRemoved = userCourseService.remove(UserCourseQueryWrapper);

        return courseRemoved;
    }

    @Override
    @Transactional
    public Long insertCourse(CourseInsertRequest courseInsertRequest) {

        Course course = new Course();
        course.setTeacherId(courseInsertRequest.getTeacherId());
        course.setCourseName(courseInsertRequest.getCourseName());

        Boolean isInsert = this.save(course);
        if (!isInsert) throw new BusinessException(StatusCode.PARAMS_ERROR, "课程新增更新失败");

        // 校验助教Id列表是否合法
        QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
        userQueryWrapper.eq("user_role", UserConstant.TEACHER_ASSISTANT_ROLE);
        List<User> taUserList = userService.list(userQueryWrapper);
        List<Long> toIdList = taUserList.stream().map(User::getId).toList();
        List<Long> newtTaIdList = Arrays.asList(courseInsertRequest.getTaIdList());
        if (!new HashSet<>(toIdList).containsAll(newtTaIdList)) throw new BusinessException(StatusCode.PARAMS_ERROR, "助教列表中含有非法用户");


        List<TeacherAssistantCourse> newTeacherAssistantCourses = newtTaIdList.stream().map(aLong -> {
            TeacherAssistantCourse teacherAssistantCourse = new TeacherAssistantCourse();
            teacherAssistantCourse.setCourseId(course.getId());
            teacherAssistantCourse.setTeacherAssistantId(aLong);
            return teacherAssistantCourse;
        }).toList();

        Boolean saveBatch = teacherAssistantCourseService.saveBatch(newTeacherAssistantCourses);
        if (!saveBatch && courseInsertRequest.getTaIdList().length > 0) throw new BusinessException(StatusCode.PARAMS_ERROR, "课程信息插入失败");

        return course.getId();
    }

    @Override
    public Boolean isCourseTeacher(Long userId, Long courseId) {

        // 从数据库course中查找是否存在符合userId和courseId的数据段
        QueryWrapper<Course> courseQueryWrapper = new QueryWrapper<>();
        courseQueryWrapper.eq("id", courseId);
        courseQueryWrapper.and(wrapper -> wrapper.eq("teacher_id", userId));

        Course targetCourse = this.getOne(courseQueryWrapper);
        if (targetCourse == null) {
            return false;
        } else {
            return true;
        }
    }

    @Override
    public Boolean checkCourseEnroll(Long userId, Long courseId) {
        User userInfo = userService.getById(userId);
        Integer role = userInfo.getUserRole();
        Boolean enrolled = false;
        switch (role) {
            case UserConstant.TEACHER_ASSISTANT_ROLE -> enrolled = teacherAssistantCourseService.isCourseTa(userId, courseId);
            case UserConstant.TEACHER_ROLE -> enrolled = this.isCourseTeacher(userId, courseId);
            case UserConstant.STUDENT_ROLE -> {
                QueryWrapper<UserCourse> userCourseQueryWrapper = new QueryWrapper<>();
                userCourseQueryWrapper.eq("user_id", userId);
                userCourseQueryWrapper.and(w -> w.eq("course_id", courseId));

                List<UserCourse> list = userCourseService.list(userCourseQueryWrapper);
                enrolled = !list.isEmpty();
            }
            default -> enrolled = true;
        }

        return enrolled;
    }

    @Override
    public Long getStudentNum(Long courseId) {
        QueryWrapper<UserCourse> userCourseQueryWrapper = new QueryWrapper<>();
        userCourseQueryWrapper.eq("course_id", courseId);
        List<UserCourse> student = userCourseService.list(userCourseQueryWrapper);
        if (student == null) throw new BusinessException(StatusCode.SYSTEM_ERROR, "查询课程信息失败");
        return (long) student.size();
    }

    @Override
    public Boolean addCourseStudents(Long[] studentIds, Long courseId) {

        // TODO: 验证添加的学生身份以及是否存在

        List<UserCourse> userCourseList = userCourseService.listUserCourseByCourseId(courseId);

        List<Long> studentsIds = userCourseList.stream().map(UserCourse::getUserId).toList();

        List<Long> newStudentsIds = Arrays.stream(studentIds).toList();

        if (!Collections.disjoint(studentsIds, newStudentsIds)) throw new BusinessException(StatusCode.PARAMS_ERROR, "学生列表中存在已在课程中的学生");

        List<UserCourse> newUserCourseList = newStudentsIds.stream().map(studentId -> {
            UserCourse userCourse = new UserCourse();
            userCourse.setCourseId(courseId);
            userCourse.setUserId(studentId);
            return userCourse;
        }).toList();

        boolean b = userCourseService.saveBatch(newUserCourseList);

        if (!b) throw new BusinessException(StatusCode.PARAMS_ERROR, "向课程中添加新学生失败");

        return b;
    }

    @Override
    public Boolean addCourseTas(Long[] taIds, Long courseId) {

        // TODO: 验证添加的TA身份以及是否存在
        List<TeacherAssistantCourse> assistantCourseList = teacherAssistantCourseService.listByCourseId(courseId);

        List<Long> taIdLists = assistantCourseList.stream().map(TeacherAssistantCourse::getTeacherAssistantId).toList();

        List<Long> newTaList = Arrays.stream(taIds).toList();

        if (!Collections.disjoint(newTaList, taIdLists)) throw new BusinessException(StatusCode.PARAMS_ERROR, "添加的TA已存在于课程中");

        List<TeacherAssistantCourse> newUserCourseList = newTaList.stream().map(taId -> {
            TeacherAssistantCourse userCourse = new TeacherAssistantCourse();
            userCourse.setCourseId(courseId);
            userCourse.setTeacherAssistantId(taId);
            return userCourse;
        }).toList();

        boolean b = teacherAssistantCourseService.saveBatch(newUserCourseList);

        if (!b) throw new BusinessException(StatusCode.SYSTEM_ERROR, "新增课程TA时发生错误");

        return b;
    }

    @Override
    public Boolean removeCourseStudents(Long[] studentIds, Long courseId) {

        // TODO: 验证添加的学生身份以及是否存在

        QueryWrapper<UserCourse> userCourseQueryWrapper = new QueryWrapper<>();

        userCourseQueryWrapper.eq("course_id", courseId);
        userCourseQueryWrapper.and(wrapper -> wrapper.in("user_id", Arrays.asList(studentIds)));

        boolean removed = userCourseService.remove(userCourseQueryWrapper);

        if (!removed) throw new BusinessException(StatusCode.SYSTEM_ERROR, "删除课程学生时发生系统错误");

        return removed;
    }

    @Override
    public Boolean removeCourseTas(Long[] taIds, Long courseId) {

        // TODO: 验证添加的TA身份以及是否存在

        QueryWrapper<TeacherAssistantCourse> userCourseQueryWrapper = new QueryWrapper<>();

        userCourseQueryWrapper.eq("course_id", courseId);
        userCourseQueryWrapper.and(wrapper -> wrapper.in("teacher_assistant_id",  Arrays.asList(taIds)));

        boolean removed = teacherAssistantCourseService.remove(userCourseQueryWrapper);

        if (!removed) throw new BusinessException(StatusCode.SYSTEM_ERROR, "删除TA时发生系统错误");
        return removed;
    }

    @Override
    public CourseInfoResponse getCourseInfo(Long courseId) {

        QueryWrapper<Course> courseQueryWrapper = new QueryWrapper<>();
        courseQueryWrapper.eq("id", courseId);

        Course course = this.getOne(courseQueryWrapper);

        if (course == null) throw new BusinessException(StatusCode.PARAMS_ERROR, "课程不存在或者已被删除");

        return getDetailedCourse(course);
    }

    private CourseInfoResponse getDetailedCourse(Course course){

        CourseInfoResponse detailedCourse = new CourseInfoResponse();

        // 获取课程老师姓名
        QueryWrapper<User> teacherQuery = new QueryWrapper<>();
        teacherQuery.eq("id", course.getTeacherId());

        User courseTeacher = userService.getOne(teacherQuery);

        // if (courseTeacher == null) throw new BusinessException(StatusCode.SYSTEM_ERROR, "查询课程教师时发生系统错误");

        // 获取课程Ta姓名列表
        QueryWrapper<TeacherAssistantCourse> taQuery = new QueryWrapper<>();
        taQuery.eq("course_id", course.getId());

        List<TeacherAssistantCourse> taList = teacherAssistantCourseService.list(taQuery);
        List<Long> taIdList = taList.stream().map(TeacherAssistantCourse::getTeacherAssistantId).toList();
        
        List<String> taNameList = null;
        if (taIdList.size() != 0) {
            QueryWrapper<User> taUserQuery = new QueryWrapper<>();
            taUserQuery.in("id", taIdList);
            List<User> tas = userService.list(taUserQuery);
            taNameList = tas.stream().map(User::getUsername).toList();
        }

        // 获取学生人数
        QueryWrapper<UserCourse> studentQuery = new QueryWrapper<>();
        studentQuery.eq("course_id", course.getId());

        long studentNum = userCourseService.count(studentQuery);

        detailedCourse.setCourseName(course.getCourseName());
        if (courseTeacher != null) detailedCourse.setTeacherName(courseTeacher.getUsername());
        if (taIdList.size() != 0) detailedCourse.setTaNameList(taNameList.toArray(String[]::new));
        detailedCourse.setStudentNum(studentNum);
        detailedCourse.setCreateTime(course.getCreateTime());

        return detailedCourse;
    }

    @Override
    public Notification[] listCourseNotification(HttpServletRequest request, Long courseId) {

        User currentUser = (User) request.getSession().getAttribute(UserConstant.USER_LOGIN_STATE);

        if (currentUser.getUserRole() == UserConstant.ADMIN_ROLE) {

            QueryWrapper<Notification> notificationQueryWrapper = new QueryWrapper<>();
            notificationQueryWrapper.eq("course_id", courseId);

            List<Notification> notificationList = notificationService.list(notificationQueryWrapper);
            if (notificationList.isEmpty()) throw new BusinessException(StatusCode.PARAMS_ERROR, "未找到与该用户有关的通知");

            return notificationList.stream().map(notificationService :: getSimplifiedNotification).toArray(Notification[] :: new);

        } else if (currentUser.getUserRole() == UserConstant.TEACHER_ROLE || currentUser.getUserRole() == UserConstant.TEACHER_ASSISTANT_ROLE) {

            // 获取当前用户作为接收方的所有通知
            QueryWrapper<UserNotification> notificationQueryWrapper = new QueryWrapper<>();
            notificationQueryWrapper.eq("receiver_id", currentUser.getId());
            List<UserNotification> notifications = userNotificationService.list(notificationQueryWrapper);
            List<Long> notificationsIdList = notifications.stream().map(UserNotification::getNotificationId).toList();

            // 获取当前用户作为发送方的所有通知
            QueryWrapper<Notification> notifyWrapper = new QueryWrapper<>();
            notifyWrapper.eq("sender_id", currentUser.getId());

            if (!notificationsIdList.isEmpty()){
                notifyWrapper.or(wrapper -> wrapper.in("id", notificationsIdList));
            }

            notifyWrapper.and(wrapper -> wrapper.eq("course_id", courseId));

            List<Notification> notificationList = notificationService.list(notifyWrapper);

            if (notificationList.isEmpty()) throw new BusinessException(StatusCode.PARAMS_ERROR, "未找到与用户相关的通知");

            return notificationList.stream().map( notificationService :: getSimplifiedNotification).toArray(Notification[] :: new);

        } else if (currentUser.getUserRole() == UserConstant.STUDENT_ROLE){

            // 获取当前用户作为接收方的所有通知
            QueryWrapper<UserNotification> notificationQueryWrapper = new QueryWrapper<>();
            notificationQueryWrapper.eq("receiver_id", currentUser.getId());
            List<UserNotification> notifications = userNotificationService.list(notificationQueryWrapper);
            List<Long> notificationsIdList = notifications.stream().map(UserNotification::getNotificationId).toList();

            if (notificationsIdList.isEmpty()) throw new BusinessException(StatusCode.PARAMS_ERROR, "未找到有关当前用户的任何信息");

            QueryWrapper<Notification> notifyWrapper = new QueryWrapper<>();
            notifyWrapper.in("id", notificationsIdList);
            notifyWrapper.and(wrapper -> wrapper.eq("course_id", courseId));

            List<Notification> notificationList = notificationService.list(notifyWrapper);

            if (notificationList.isEmpty()) throw new BusinessException(StatusCode.PARAMS_ERROR, "未找到有关当前用户的任何信息");

            return notificationList.stream().map(notificationService :: getSimplifiedNotification).toArray(Notification[] :: new);

        } else {
            throw new BusinessException(StatusCode.PARAMS_ERROR, "用户权限不可见");
        }
    }

    @Override
    public Boolean removeNotification(Long notificationId) {



        QueryWrapper<Notification> notificationQueryWrapper = new QueryWrapper<>();

        notificationQueryWrapper.eq("id", notificationId);

        boolean removed = notificationService.remove(notificationQueryWrapper);

        if (!removed) throw new BusinessException(StatusCode.SYSTEM_ERROR, "无法删除该通知");

        QueryWrapper<UserNotification> userNotificationQueryWrapper = new QueryWrapper<>();

        userNotificationQueryWrapper.eq("notification_id", notificationId);

        boolean removedUser = userNotificationService.remove(userNotificationQueryWrapper);

        if (!removedUser) throw new BusinessException(StatusCode.SYSTEM_ERROR, "无法删除该通知相关的用户内容");

        return true;
    }

    @Override
    @Transactional
    public Long insertNotification(Long senderId, NotificationInsertRequest notificationRequest) {

        // TODO: 增强对学生是否存在的检验


        Notification newNotification = new Notification();

        newNotification.setTitle(notificationRequest.getTitle());
        newNotification.setMessage(notificationRequest.getMessage());
        newNotification.setSenderId(senderId);
        newNotification.setCourseId(notificationRequest.getCourseId());

        boolean saved = notificationService.save(newNotification);



        if (!saved) throw new BusinessException(StatusCode.SYSTEM_ERROR, "新建通知时发生错误");

        if (notificationRequest.getReceivers().length != 0) {

            List<UserNotification> userNotifications = Arrays.stream(notificationRequest.getReceivers()).map(receiverId -> {
                UserNotification userNotification = new UserNotification();
                userNotification.setIsRead((byte) 0);
                userNotification.setReceiverId(receiverId);
                userNotification.setNotificationId(newNotification.getId());
                return userNotification;
            }).toList();

            boolean b = userNotificationService.saveBatch(userNotifications);

            if (!b) throw new BusinessException(StatusCode.PARAMS_ERROR, "添加接收方失败");

        } else {

            // 获取该课程所有学生ID
            QueryWrapper<UserCourse> userCourseQueryWrapper = new QueryWrapper<>();

            userCourseQueryWrapper.eq("course_id", notificationRequest.getCourseId());

            List<UserCourse> studentList = userCourseService.list(userCourseQueryWrapper);

            List<Long> studentIdList = new java.util.ArrayList<>(studentList.stream().map(UserCourse::getUserId).toList());


            // 获取该课程所有TA的ID
            QueryWrapper<TeacherAssistantCourse> teacherAssistantCourseQueryWrapper = new QueryWrapper<>();

            teacherAssistantCourseQueryWrapper.eq("course_id", notificationRequest.getCourseId());

            List<TeacherAssistantCourse> taList = teacherAssistantCourseService.list(teacherAssistantCourseQueryWrapper);

            List<Long> studentIdList2 = taList.stream().map(TeacherAssistantCourse::getTeacherAssistantId).toList();

            studentIdList.addAll(studentIdList2);

            List<UserNotification> userNotifications = studentIdList.stream().map(receiverId -> {
                UserNotification userNotification = new UserNotification();
                userNotification.setIsRead((byte) 0);
                userNotification.setReceiverId(receiverId);
                userNotification.setNotificationId(newNotification.getId());
                return userNotification;
            }).toList();

            boolean b = userNotificationService.saveBatch(userNotifications);

            if (!b) throw new BusinessException(StatusCode.PARAMS_ERROR, "添加接收方失败");

        }

        return newNotification.getId();
    }

    private Course getSafetyCourse(Course course){
        Course safetyCourse = new Course();
        safetyCourse.setId(course.getId());
        safetyCourse.setCourseName(course.getCourseName());
        return safetyCourse;
    }
}
