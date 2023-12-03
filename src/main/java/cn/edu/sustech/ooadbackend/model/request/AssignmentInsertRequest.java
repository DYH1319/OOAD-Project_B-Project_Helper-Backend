package cn.edu.sustech.ooadbackend.model.request;

import lombok.Data;

import java.util.Date;

@Data
public class AssignmentInsertRequest {
    //courseId, title, description, startTime, endTime, assignmentType
private Long courseId;
private String title;
private String description;
private Date startTime;
private Date endTime;
private Byte assignmentType;

}
