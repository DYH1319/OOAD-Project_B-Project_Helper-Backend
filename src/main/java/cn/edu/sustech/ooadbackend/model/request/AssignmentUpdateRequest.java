package cn.edu.sustech.ooadbackend.model.request;

import lombok.Data;

import java.util.Date;

@Data
public class AssignmentUpdateRequest {
    //assignmentId, title, description, startTime, endTime, assignmentType
    private Long id;
    private String title;
    private String description;
    private String startTime;
    private String endTime;
    private Byte assignmentType;

}
