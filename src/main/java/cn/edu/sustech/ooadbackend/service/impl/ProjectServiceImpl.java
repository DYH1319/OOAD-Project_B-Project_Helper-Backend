package cn.edu.sustech.ooadbackend.service.impl;

import cn.edu.sustech.ooadbackend.mapper.ProjectMapper;
import cn.edu.sustech.ooadbackend.model.domain.Project;
import cn.edu.sustech.ooadbackend.service.ProjectService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * @className ProjectServiceImpl
 * @version 1.0
 * @author DYH
 * @since 2023/10/9 12:43
 */
    
@Service
public class ProjectServiceImpl extends ServiceImpl<ProjectMapper, Project> implements ProjectService{

}
