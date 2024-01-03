package cn.edu.sustech.ooadbackend.model.request;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author DYH
 * @version 1.0
 * @className ReviewSubmitRequest
 * @since 2024/1/3 15:39
 */
@Data
public class ReviewSubmitRequest {
    private Long submissionId;
    private BigDecimal score;
    private String comment;
}
