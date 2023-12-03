package cn.edu.sustech.ooadbackend.service;

import cn.edu.sustech.ooadbackend.model.domain.UserProject;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * @className UserProjectService
 * @version 1.0
 * @author DYH
 * @since 2023/10/9 12:44
 */
    
public interface UserProjectService extends IService<UserProject>{
    List<UserProject> listUserProjectByUserId(Long userID);


}
