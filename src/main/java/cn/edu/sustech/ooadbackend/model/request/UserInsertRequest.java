package cn.edu.sustech.ooadbackend.model.request;
import lombok.Data;
@Data
public class UserInsertRequest {

    private String userAccount;
    private String username;
    private Integer userRole;
    private Integer age;
    private Byte gender;
    private String email;
    private String avatarUrl;
}
