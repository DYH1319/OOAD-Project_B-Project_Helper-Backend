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
 * @className PeerReview
 * @since 2023/10/9 15:24
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName(value = "t_peer_review")
public class PeerReview {
    /**
     * 主键，自增
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    
    /**
     * 外键，关联小组id，代表被评价的小组
     */
    @TableField(value = "group_id")
    private Long groupId;
    
    /**
     * 外键，关联小组id，代表评价的小组
     */
    @TableField(value = "review_group_id")
    private Long reviewGroupId;
    
    /**
     * 小组评分
     */
    @TableField(value = "score")
    private BigDecimal score;
    
    /**
     * 评分时间
     */
    @TableField(value = "score_time")
    private Date scoreTime;
    
    /**
     * 小组评价
     */
    @TableField(value = "group_comment")
    private String groupComment;
    
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