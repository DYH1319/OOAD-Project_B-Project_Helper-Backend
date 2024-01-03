package cn.edu.sustech.ooadbackend.model.response;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author DYH
 * @version 1.0
 * @className GradeBookResponse
 * @since 2024/1/3 21:51
 */
@Data
public class GradeBookResponse {
    private Long id;
    private String title;
    private String submitterSid;
    private String submitterName;
    private BigDecimal score;
    private Boolean isReviewed;
}
