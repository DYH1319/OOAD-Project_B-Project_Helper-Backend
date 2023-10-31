package cn.edu.sustech.ooadbackend.controller;

import cn.edu.sustech.ooadbackend.common.BaseResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @className: NotificationController
 * @Package: cn.edu.sustech.ooadbackend.controller
 * @Description:
 * @Author: Daniel
 * @Date: 2023/10/31 17:09
 * @Version: 1.0
 */
@RestController
@RequestMapping("/notification")
public class NotificationController {

//    @GetMapping("")
//    public

    @PostMapping("/insert")
    public BaseResponse<Long> insertNotification(){

        return null;
    }

}
