package cn.edu.sustech.ooadbackend.model.response;

import lombok.Data;

import java.util.List;

@Data
public class GroupsDetailResponse {
    private Long groupId;
    private String name;
    private Integer groupCurrentNumber;
    private Integer groupMaxNumber;
    private String publicInfo;
}
