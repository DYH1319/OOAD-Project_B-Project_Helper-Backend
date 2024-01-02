package cn.edu.sustech.ooadbackend.model.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GroupInfoResponse {
    private String groupName;
    private Long groupLeader;
    private Long defenceTeacher;
    private Date presentationTime;
    private String publicInfo;
}
