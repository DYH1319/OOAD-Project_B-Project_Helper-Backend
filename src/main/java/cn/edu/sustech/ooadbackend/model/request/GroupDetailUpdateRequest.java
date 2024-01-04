package cn.edu.sustech.ooadbackend.model.request;

import lombok.Data;

import java.util.Date;

@Data
public class GroupDetailUpdateRequest {
    //groupId, groupName, groupLeader, defenceTeacher, presentationTime, publicInfo
    private Long groupId;
    private String groupName;
    private String groupLeader;
    private String defenceTeacher;
    private Date presentationTime;
    private String publicInfo;

}
