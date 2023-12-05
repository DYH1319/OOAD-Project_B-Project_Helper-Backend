package cn.edu.sustech.ooadbackend.model.request;

import lombok.Data;

import java.util.Date;
@Data
public class ProjectUpdateRequest {
    private  Long id;
    private String projectName;
    private String description;
    private String groupDeadline;
    private String endDeadline;
}
