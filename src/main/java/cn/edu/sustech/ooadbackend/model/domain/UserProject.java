package cn.edu.sustech.ooadbackend.model.domain;

import com.baomidou.mybatisplus.annotation.*;

import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


/**
 * @className UserProject
 * @version 1.0
 * @author DYH
 * @since 2023/10/9 12:44
 */
    
@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName(value = "t_user_project")
public class UserProject {
    /**
     * 主键，自增
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 外键，关联学生的id
     */
    @TableField(value = "user_id")
    private Long userId;

    /**
     * 外键，关联项目的id
     */
    @TableField(value = "project_id")
    private Long projectId;

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