package cn.edu.sustech.ooadbackend.model.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * @className: NotificationInfoResponse
 * @Package: cn.edu.sustech.ooadbackend.model.response
 * @Description:
 * @Author: XXX
 * @Date: 2023/10/31 19:51
 * @Version:1.0
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class NotificationInfoResponse {

    private Long id;

    private String senderName;

    private String title;

    private String message;

    private String courseName;

    private String projectName;

    private Date sendTime;

    private Byte isRead;

}
