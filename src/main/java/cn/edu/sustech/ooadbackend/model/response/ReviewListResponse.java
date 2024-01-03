package cn.edu.sustech.ooadbackend.model.response;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @author DYH
 * @version 1.0
 * @className ReviewListResponse
 * @since 2024/1/3 14:24
 */
@Data
public class ReviewListResponse {
    private Long submissionId;
    private String submitterSid;
    private String submitterName;
    private Date submitTime;
    private BigDecimal score;
    private Boolean isReviewed;
}
