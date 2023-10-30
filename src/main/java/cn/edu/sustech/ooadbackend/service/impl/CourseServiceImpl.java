package cn.edu.sustech.ooadbackend.service.impl;

import cn.edu.sustech.ooadbackend.common.StatusCode;
import cn.edu.sustech.ooadbackend.constant.UserConstant;
import cn.edu.sustech.ooadbackend.exception.BusinessException;
import cn.edu.sustech.ooadbackend.mapper.CourseMapper;
import cn.edu.sustech.ooadbackend.model.domain.Course;
import cn.edu.sustech.ooadbackend.model.domain.TeacherAssistantCourse;
import cn.edu.sustech.ooadbackend.model.domain.User;
import cn.edu.sustech.ooadbackend.model.domain.UserCourse;
import cn.edu.sustech.ooadbackend.model.request.CourseInsertRequest;
import cn.edu.sustech.ooadbackend.model.request.CourseUpdateRequest;
import cn.edu.sustech.ooadbackend.service.CourseService;
import cn.edu.sustech.ooadbackend.service.TeacherAssistantCourseService;
import cn.edu.sustech.ooadbackend.service.UserCourseService;
import cn.edu.sustech.ooadbackend.service.UserService;
import cn.edu.sustech.ooadbackend.utils.ResponseUtils;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

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

        Boolean isUpdated = courseMapper.updateCourse(course);
        if (!isUpdated) throw new BusinessException(StatusCode.PARAMS_ERROR, "课程信息更新失败");

        // 校验助教Id列表是否合法
        QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
        userQueryWrapper.eq("user_role", UserConstant.TEACHER_ASSISTANT_ROLE);
        List<User> taUserList = userService.list(userQueryWrapper);
        List<Long> toIdList = taUserList.stream().map(User::getId).toList();
        List<Long> newtTaIdList = Arrays.asList(courseUpdateRequest.getTaIdList());
        if (!new HashSet<>(toIdList).containsAll(newtTaIdList)) throw new BusinessException(StatusCode.PARAMS_ERROR, "助教列表中含有非法用户");

        teacherAssistantCourseService.removeByCourseId(courseUpdateRequest.getId());

        List<TeacherAssistantCourse> newTeacherAssistantCourses = newtTaIdList.stream().map(aLong -> {
            TeacherAssistantCourse teacherAssistantCourse = new TeacherAssistantCourse();
            teacherAssistantCourse.setCourseId(courseUpdateRequest.getId());
            teacherAssistantCourse.setTeacherAssistantId(aLong);
            return teacherAssistantCourse;
        }).toList();

        Boolean saveBatch = teacherAssistantCourseService.saveBatch(newTeacherAssistantCourses);
        if (saveBatch != true) throw new BusinessException(StatusCode.PARAMS_ERROR, "课程信息更新失败");

        return saveBatch;
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
        if (!saveBatch) throw new BusinessException(StatusCode.PARAMS_ERROR, "课程信息插入失败");

        return course.getId();
    }

    @Override
    public Boolean isCourseTeacher(Long userId, Long courseId) {

        // 从数据库course中查找是否存在符合userId和courseId的数据段
        QueryWrapper<Course> courseQueryWrapper = new QueryWrapper<>();
        courseQueryWrapper.eq("id", courseId);
        courseQueryWrapper.eq("teacher_id", userId);

        Course targetCourse = this.getOne(courseQueryWrapper);
        if (targetCourse == null) {
            return false;
        } else {
            return true;
        }
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

        List<TeacherAssistantCourse> newUserCourseList = newTaList.stream().map(studentId -> {
            TeacherAssistantCourse userCourse = new TeacherAssistantCourse();
            userCourse.setCourseId(courseId);
            userCourse.setId(studentId);
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
        userCourseQueryWrapper.and(wrapper -> wrapper.in("user_id", (Object) studentIds));

        boolean removed = userCourseService.remove(userCourseQueryWrapper);

        if (!removed) throw new BusinessException(StatusCode.SYSTEM_ERROR, "删除课程学生时发生系统错误");

        return removed;
    }

    @Override
    public Boolean removeCourseTas(Long[] taIds, Long courseId) {

        // TODO: 验证添加的TA身份以及是否存在
        QueryWrapper<TeacherAssistantCourse> userCourseQueryWrapper = new QueryWrapper<>();

        userCourseQueryWrapper.eq("course_id", courseId);
        userCourseQueryWrapper.and(wrapper -> wrapper.in("teacher_assistant_id", (Object) taIds));

        boolean removed = teacherAssistantCourseService.remove(userCourseQueryWrapper);

        if (!removed) throw new BusinessException(StatusCode.SYSTEM_ERROR, "删除TA时发生系统错误");
        return removed;
    }

    private Course getSafetyCourse(Course course){
        Course safetyCourse = new Course();
        safetyCourse.setId(course.getId());
        safetyCourse.setCourseName(course.getCourseName());
        return safetyCourse;
    }
}
