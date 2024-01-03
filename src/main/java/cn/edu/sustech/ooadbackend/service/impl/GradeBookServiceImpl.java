package cn.edu.sustech.ooadbackend.service.impl;

import cn.edu.sustech.ooadbackend.common.StatusCode;
import cn.edu.sustech.ooadbackend.constant.UserConstant;
import cn.edu.sustech.ooadbackend.exception.BusinessException;
import cn.edu.sustech.ooadbackend.mapper.AssignmentMapper;
import cn.edu.sustech.ooadbackend.mapper.GradeBookMapper;
import cn.edu.sustech.ooadbackend.mapper.SubmissionMapper;
import cn.edu.sustech.ooadbackend.mapper.UserMapper;
import cn.edu.sustech.ooadbackend.model.domain.Assignment;
import cn.edu.sustech.ooadbackend.model.domain.GradeBook;
import cn.edu.sustech.ooadbackend.model.domain.Submission;
import cn.edu.sustech.ooadbackend.model.domain.User;
import cn.edu.sustech.ooadbackend.model.response.GradeBookResponse;
import cn.edu.sustech.ooadbackend.service.GradeBookService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * @className GradeBookServiceImpl
 * @version 1.0
 * @author DYH
 * @since 2024/1/3 21:33
 */
    
@Service
public class GradeBookServiceImpl extends ServiceImpl<GradeBookMapper, GradeBook> implements GradeBookService {
    
    @Resource
    private GradeBookMapper gradeBookMapper;
    
    @Resource
    private UserMapper userMapper;
    
    @Resource
    private SubmissionMapper submissionMapper;
    
    @Resource
    private AssignmentMapper assignmentMapper;
    
    @Override
    public List<GradeBookResponse> getGradeBook(Long courseId, HttpServletRequest request) {
        User currentUser = (User) request.getSession().getAttribute(UserConstant.USER_LOGIN_STATE);
        if (currentUser == null) throw new BusinessException(StatusCode.NOT_LOGIN);
        
        List<GradeBookResponse> res = new ArrayList<>();
        
        List<GradeBook> gradeBooks;
        if (currentUser.getUserRole() < UserConstant.TEACHER_ASSISTANT_ROLE) {
            gradeBooks = gradeBookMapper.selectList(new LambdaQueryWrapper<GradeBook>().eq(GradeBook::getCourseId, courseId).eq(GradeBook::getStudentId, currentUser.getId()));
        } else {
            gradeBooks = gradeBookMapper.selectList(new LambdaQueryWrapper<GradeBook>().eq(GradeBook::getCourseId, courseId));
        }
        
        final long[] id = {-1L};
        gradeBooks.forEach(gradeBook -> {
            GradeBookResponse response = new GradeBookResponse();
            response.setId(id[0]);
            id[0]--;
            response.setTitle(gradeBook.getTitle());
            User submitter = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getId, gradeBook.getStudentId()));
            response.setSubmitterSid(submitter.getUserAccount());
            response.setSubmitterName(submitter.getUsername());
            response.setScore(gradeBook.getScore());
            response.setIsReviewed(true);
            res.add(response);
        });
        
        final List<Submission> submissions = new ArrayList<>();
        List<Assignment> assignments = assignmentMapper.selectList(new LambdaQueryWrapper<Assignment>().eq(Assignment::getCourseId, courseId));
        if (currentUser.getUserRole() < UserConstant.TEACHER_ASSISTANT_ROLE) {
            assignments.forEach(assignment -> {
                submissions.addAll(submissionMapper.selectList(new LambdaQueryWrapper<Submission>().eq(Submission::getAssignmentId, assignment.getId()).eq(Submission::getSubmitterId, currentUser.getId())));
            });
        } else {
            assignments.forEach(assignment -> {
                submissions.addAll(submissionMapper.selectList(new LambdaQueryWrapper<Submission>().eq(Submission::getAssignmentId, assignment.getId())));
            });
        }
        
        submissions.forEach(submission -> {
            GradeBookResponse response = new GradeBookResponse();
            response.setId(submission.getId());
            Assignment assignment = assignmentMapper.selectOne(new LambdaQueryWrapper<Assignment>().eq(Assignment::getId, submission.getAssignmentId()));
            response.setTitle(assignment.getTitle());
            User submitter = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getId, submission.getSubmitterId()));
            response.setSubmitterSid(submitter.getUserAccount());
            response.setSubmitterName(submitter.getUsername());
            response.setScore(submission.getScore());
            response.setIsReviewed(submission.getScore() != null);
            res.add(response);
        });
        
        return res;
    }
    
    @Override
    public boolean gradeBookUpload(MultipartFile file, HttpServletRequest request) {
        try (BufferedReader fileReader = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = fileReader.readLine()) != null) {
                // 处理每一行
                String[] data = line.split(","); // 或使用更复杂的解析逻辑
                if (data.length < 4) break;
                GradeBook gradeBook = new GradeBook();
                gradeBook.setTitle(data[0]);
                gradeBook.setStudentId(Long.parseLong(data[1]));
                gradeBook.setCourseId(Long.parseLong(data[2]));
                gradeBook.setScore(BigDecimal.valueOf(Double.parseDouble(data[3])));
                int count = gradeBookMapper.insert(gradeBook);
                if (count != 1) throw new BusinessException(StatusCode.SYSTEM_ERROR);
            }
        } catch (Exception e) {
            // ignore
        }
        return true;
    }
}
