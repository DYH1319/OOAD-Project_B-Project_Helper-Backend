package cn.edu.sustech.ooadbackend.model.response;

import lombok.Data;

import java.util.Date;

@Data
public class AssignmentInfoResponse {

    private String title;
    private String description;
    private Date startTime;
    private Date endTime;
    private Byte assignmentType;

}
