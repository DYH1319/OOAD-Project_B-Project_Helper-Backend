package cn.edu.sustech.ooadbackend.model.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProjectInfoResponse {
    private String projectName;
    private String description;
    private Integer groupNumber;
    private Integer maxNumber;
    private Date groupDeadline;
    private Date endDeadline;
}
