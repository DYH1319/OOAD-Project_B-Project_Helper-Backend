package cn.edu.sustech.ooadbackend.model.domain;

import com.baomidou.mybatisplus.annotation.*;

import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


/**
 * @className User
 * @version 1.0
 * @author DYH
 * @since 2023/10/9 12:44
 */
    
@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName(value = "t_user")
public class User {
    /**
     * 主键，自增
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 用户账号，注册时填写，账号密码登录时使用，非空，唯一
     */
    @TableField(value = "user_account")
    private String userAccount;

    /**
     * MD5加密后的用户密码，注册时填写，账号密码登录时使用，非空
     */
    @TableField(value = "user_password")
    private String userPassword;

    /**
     * 用户身份，用于决定用户权限，默认为学生(0)，非空<br/>0：学生，1：教师，2：管理员，3：教师助理
     */
    @TableField(value = "user_role")
    private Integer userRole;

    /**
     * 年龄
     */
    @TableField(value = "age")
    private Integer age;

    /**
     * 性别，默认为未指定(0)，非空<br/>0：未指定，1：男，2：女
     */
    @TableField(value = "gender")
    private Byte gender;

    /**
     * 邮箱
     */
    @TableField(value = "email")
    private String email;

    /**
     * 头像地址，非空<br/>默认为"https://pic.616pic.com/ys_img/00/05/09/8LfaQcDrWd.jpg"
     */
    @TableField(value = "avatar_url")
    private String avatarUrl;

    /**
     * 技术栈
     */
    @TableField(value = "technical_stack")
    private String technicalStack;

    /**
     * 编程技能
     */
    @TableField(value = "programming_skills")
    private String programmingSkills;

    /**
     * 意向队友
     */
    @TableField(value = "intended_teammates")
    private String intendedTeammates;

    /**
     * 是否逻辑删除，默认值为正常(0)，非空<br/>0：正常，1：已被逻辑删除
     */
    @TableField(value = "is_deleted")
    private Byte isDeleted;

    /**
     * 创建时间，insert时数据库自动生成，非空
     */
    @TableField(value = "create_time")
    private Date createTime;

    /**
     * 修改时间，insert或update时数据库自动生成或修改，非空
     */
    @TableField(value = "update_time")
    private Date updateTime;
}