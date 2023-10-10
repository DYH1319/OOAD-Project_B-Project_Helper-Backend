package cn.edu.sustech.ooadbackend.model.domain;

import com.baomidou.mybatisplus.annotation.*;

import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


/**
 * @className Assignment
 * @version 1.0
 * @author DYH
 * @since 2023/10/9 12:37
 */
    
@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName(value = "t_assignment")
public class Assignment {
    /**
     * 主键，自增
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 作业标题，非空
     */
    @TableField(value = "title")
    private String title;

    /**
     * 作业描述
     */
    @TableField(value = "description")
    private String description;

    /**
     * 作业开始时间
     */
    @TableField(value = "start_time")
    private Date startTime;

    /**
     * 作业截至时间
     */
    @TableField(value = "end_time")
    private Date endTime;

    /**
     * 作业类型，非空<br/>0：个人作业，1：小组作业
     */
    @TableField(value = "assignment_type")
    private Byte assignmentType;

    /**
     * 外键，关联课程id
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