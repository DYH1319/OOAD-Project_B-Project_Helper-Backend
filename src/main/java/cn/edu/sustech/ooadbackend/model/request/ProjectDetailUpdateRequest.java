package cn.edu.sustech.ooadbackend.model.request;

import lombok.Data;

import java.util.Date;

@Data
public class ProjectDetailUpdateRequest {
    private Long groupId;
    private String projectName;
    private String description;
    private Date groupDeadline;
    private Date endDeadline;
    private Integer groupNumber;
    private Integer maxNumber;
}
