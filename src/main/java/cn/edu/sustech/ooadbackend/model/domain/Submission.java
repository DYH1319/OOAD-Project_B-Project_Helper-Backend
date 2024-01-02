package cn.edu.sustech.ooadbackend.model.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


/**
 * @author DYH
 * @version 1.0
 * @className Submission
 * @since 2023/10/9 15:24
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName(value = "t_submission")
public class Submission {
    /**
     * 主键，自增
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    
    /**
     * 外键，关联作业id
     */
    @TableField(value = "assignment_id")
    private Long assignmentId;
    
    /**
     * 外键，关联用户id，代表提交作业的用户
     */
    @TableField(value = "submitter_id")
    private Long submitterId;
    
    /**
     * 作业内容或链接，非空
     */
    @TableField(value = "content")
    private String content;
    
    /**
     * 提交类型
     */
    @TableField(value = "content_type")
    private String contentType;
    
    /**
     * 提交时间，非空
     */
    @TableField(value = "submit_time")
    private Date submitTime;
    
    /**
     * 外键，关联用户id，代表评分的用户
     */
    @TableField(value = "teacher_id")
    private Long teacherId;
    
    /**
     * 老师评分
     */
    @TableField(value = "score")
    private BigDecimal score;
    
    /**
     * 评分时间
     */
    @TableField(value = "score_time")
    private Date scoreTime;
    
    /**
     * 老师评价
     */
    @TableField(value = "teacher_comment")
    private String teacherComment;
    
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