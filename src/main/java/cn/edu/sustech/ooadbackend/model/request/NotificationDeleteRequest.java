package cn.edu.sustech.ooadbackend.model.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @className: NotificationDeleteRequest
 * @Package: cn.edu.sustech.ooadbackend.model.request
 * @Description:
 * @Author: XXX
 * @Date: 2023/10/31 23:16
 * @Version:1.0
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NotificationDeleteRequest {

    private Long notificationId;

}
