package cn.edu.sustech.ooadbackend.service;

import cn.edu.sustech.ooadbackend.model.domain.Group;
import com.baomidou.mybatisplus.extension.service.IService;
    
/**
 * @className GroupService
 * @version 1.0
 * @author DYH
 * @since 2023/10/9 12:42
 */
    
public interface GroupService extends IService<Group>{


    public Integer getGroupCurrentNumber(Long groupId);

}
