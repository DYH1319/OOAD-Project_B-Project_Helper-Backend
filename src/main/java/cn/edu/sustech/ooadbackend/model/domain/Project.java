package cn.edu.sustech.ooadbackend.model.domain;

import com.baomidou.mybatisplus.annotation.*;

import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


/**
 * @className Project
 * @version 1.0
 * @author DYH
 * @since 2023/10/9 12:43
 */
    
@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName(value = "t_project")
public class Project {
    /**
     * 主键，自增
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 项目名称，非空
     */
    @TableField(value = "project_name")
    private String projectName;

    /**
     * 项目描述
     */
    @TableField(value = "description")
    private String description;

    /**
     * 项目小组数
     */
    @TableField(value = "group_number")
    private Integer groupNumber;

    /**
     * 小组最大人数
     */
    @TableField(value = "max_number")
    private Integer maxNumber;

    /**
     * 项目组队DDL
     */
    @TableField(value = "group_deadline")
    private Date groupDeadline;

    /**
     * 项目结束DDL
     */
    @TableField(value = "end_deadline")
    private Date endDeadline;

    /**
     * 外键，关联t_course表中的id，项目所属课程的id
     */
    @TableField(value = "course_id")
    private Long courseId;

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