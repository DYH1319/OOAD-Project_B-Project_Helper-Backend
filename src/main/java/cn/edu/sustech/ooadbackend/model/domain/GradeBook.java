package cn.edu.sustech.ooadbackend.model.domain;

import com.baomidou.mybatisplus.annotation.*;

import java.math.BigDecimal;
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


/**
 * @author DYH
 * @version 1.0
 * @className GradeBook
 * @since 2024/1/3 21:49
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName(value = "t_grade_book")
public class GradeBook {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    
    @TableField(value = "student_id")
    private Long studentId;
    
    @TableField(value = "course_id")
    private Long courseId;
    
    @TableField(value = "title")
    private String title;
    
    @TableField(value = "score")
    private BigDecimal score;
    
    @TableLogic
    @TableField(value = "is_deleted")
    private Byte isDeleted;
    
    @TableField(value = "create_time")
    private Date createTime;
    
    @TableField(value = "update_time")
    private Date updateTime;
}