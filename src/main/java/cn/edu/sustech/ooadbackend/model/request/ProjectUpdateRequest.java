package cn.edu.sustech.ooadbackend.model.request;

import lombok.Data;

import java.util.Date;
@Data
public class ProjectUpdateRequest {
    private  Long projectId;
    private String projectName;
    private String description;
    private Date groupDeadline;
    private Date endDeadline;
}
