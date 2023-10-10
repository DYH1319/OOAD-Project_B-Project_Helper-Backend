package cn.edu.sustech.ooadbackend.model.request;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 用户注册请求体
 * @author DYH
 * @version 1.0
 * @className UserRegisterRequest
 * @since 2023/9/8 10:02
 */
@Data
public class UserRegisterRequest implements Serializable {
    
    @Serial
    private static final long serialVersionUID = 475658407719779579L;
    
    private String userAccount;
    private String userPassword;
    private String checkPassword;
}
