package cn.edu.sustech.ooadbackend.model.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @className: NotificationInsertRequest
 * @Package: cn.edu.sustech.ooadbackend.model.request
 * @Description:
 * @Author: XXX
 * @Date: 2023/10/31 17:35
 * @Version:1.0
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class NotificationInsertRequest {

    private Long courseId;

    private String title;

    private String message;

    private Long[] receivers;

}
