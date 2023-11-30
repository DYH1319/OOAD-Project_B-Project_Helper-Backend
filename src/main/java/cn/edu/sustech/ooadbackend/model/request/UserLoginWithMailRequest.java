package cn.edu.sustech.ooadbackend.model.request;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * @author DYH
 * @version 1.0
 * @className UserLoginWithMailRequest
 * @since 2023/11/30 20:25
 */
@Data
public class UserLoginWithMailRequest implements Serializable {
    
    @Serial
    private static final long serialVersionUID = -7922207999730730696L;
    
    private String mailAddress;
    private String verificationCode;
}
