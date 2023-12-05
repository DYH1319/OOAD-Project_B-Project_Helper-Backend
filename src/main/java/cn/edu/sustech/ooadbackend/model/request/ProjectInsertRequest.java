package cn.edu.sustech.ooadbackend.model.request;

import lombok.Data;

import java.util.Date;

@Data
public class ProjectInsertRequest {
    //courseId, projectName, description, groupDeadline, endDeadline
    private  Long courseId;
    private String projectName;
    private String description;
    private String groupDeadline;
    private String endDeadline;

}
