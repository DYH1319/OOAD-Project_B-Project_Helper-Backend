package cn.edu.sustech.ooadbackend.model.request;

import lombok.Data;

import java.util.Date;

@Data
public class GroupDetailUpdateRequest {
    //groupId, groupName, groupLeader, defenceTeacher, presentationTime, publicInfo
    private Long id;
    private String groupName;
    private Long groupLeader;
    private Long defenceTeacher;
    private Date presentationTime;
    private String publicInfo;

}
