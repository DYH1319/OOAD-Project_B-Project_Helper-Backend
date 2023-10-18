package cn.edu.sustech.ooadbackend.model.request;

import lombok.Data;

/**
 * @className: UserUpdateRequest
 * @Package: cn.edu.sustech.ooadbackend.model.request
 * @Description:
 * @Author: Daniel
 * @Date: 2023/10/18 16:32
 * @Version:1.0
 */
@Data
public class CurrentUserUpdateRequest {

    /**
     * 用户id
     */
    private Long id;
    /**
     * 用户修改后的年龄
     */
    private Integer age;
    /**
     * 用户修改后的头像url
     */
    private String avatarUrl;
    /**
     * 用户修改后的技术栈
     */
    private String technicalStack;
    /**
     * 用户修改后的编程技能
     */
    private String programmingSkills;
    /**
     * 用户修改后的意向队友
     */
    private String intendedTeammates;
}
