package cn.edu.sustech.ooadbackend.mapper;

import cn.edu.sustech.ooadbackend.model.domain.Submission;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;


/**
 * @author DYH
 * @version 1.0
 * @className SubmissionMapper
 * @since 2023/10/9 15:24
 */

@Mapper
public interface SubmissionMapper extends BaseMapper<Submission> {
}