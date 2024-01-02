package cn.edu.sustech.ooadbackend.service.impl;

import cn.edu.sustech.ooadbackend.common.FileType;
import cn.edu.sustech.ooadbackend.common.StatusCode;
import cn.edu.sustech.ooadbackend.constant.UserConstant;
import cn.edu.sustech.ooadbackend.exception.BusinessException;
import cn.edu.sustech.ooadbackend.mapper.SubmissionMapper;
import cn.edu.sustech.ooadbackend.model.domain.Submission;
import cn.edu.sustech.ooadbackend.model.domain.User;
import cn.edu.sustech.ooadbackend.service.SubmissionService;
import cn.edu.sustech.ooadbackend.utils.CosManager;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;

/**
 * @className SubmissionServiceImpl
 * @version 1.0
 * @author DYH
 * @since 2023/10/9 12:43
 */
    
@Service
public class SubmissionServiceImpl extends ServiceImpl<SubmissionMapper, Submission> implements SubmissionService {
    
    @Resource
    private SubmissionMapper submissionMapper;
    
    @Resource
    private CosManager cosManager;
    
    @Override
    public Submission getSubmissionById(Long submissionId, HttpServletRequest request) {
        User user = (User) request.getSession().getAttribute(UserConstant.USER_LOGIN_STATE);
        if (user == null) {
            throw new BusinessException(StatusCode.NOT_LOGIN);
        }
        Submission submission = submissionMapper.selectOne(new LambdaQueryWrapper<Submission>().eq(Submission::getId, submissionId));
        // 学生
        if (user.getUserRole() < UserConstant.TEACHER_ASSISTANT_ROLE && !submission.getSubmitterId().equals(user.getId())) {
            throw new BusinessException(StatusCode.NO_AUTH, "您无权限访问此提交记录");
        }
        if (!submission.getContentType().equals("TEXT")) {
            submission.setContent(cosManager.getObjectPresignedUrl(submission.getContent(), FileType.ASSIGNMENT_FILE));
        }
        return submission;
    }
}
