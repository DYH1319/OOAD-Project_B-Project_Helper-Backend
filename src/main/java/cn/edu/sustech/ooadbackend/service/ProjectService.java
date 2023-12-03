package cn.edu.sustech.ooadbackend.service;

import cn.edu.sustech.ooadbackend.model.domain.Project;
import cn.edu.sustech.ooadbackend.model.domain.User;
import cn.edu.sustech.ooadbackend.model.request.ProjectInsertRequest;
import cn.edu.sustech.ooadbackend.model.request.ProjectUpdateRequest;
import cn.edu.sustech.ooadbackend.model.request.UserInsertRequest;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * @className ProjectService
 * @version 1.0
 * @author DYH
 * @since 2023/10/9 12:43
 */
    
public interface ProjectService extends IService<Project>{
    List<Project> list(User user,Long courseId);
    Long insertProject(ProjectInsertRequest projectInsertRequest);
    Boolean updateProject(ProjectUpdateRequest projectUpdateRequest);
    Boolean deleteProject(Long projectId);


}
