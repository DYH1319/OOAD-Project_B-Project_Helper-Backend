package cn.edu.sustech.ooadbackend.model.request;

import lombok.Data;

@Data
public class GroupUserRequest {
    private Long groupId;
    private Long projectId;
}
