package cn.edu.sustech.ooadbackend.model.request;

import lombok.Data;

import java.util.Date;

@Data

public class UserUpdateRequest {
    //id, userAccount, userRole, age, gender, email, avatarUrl, createTime
    private Long id;
    private String userAccount;
    private String username;
    private Integer userRole;
    private Integer age;
    private Byte gender;
    private String email;
    private String avatarUrl;
    private Date createTime;
}
