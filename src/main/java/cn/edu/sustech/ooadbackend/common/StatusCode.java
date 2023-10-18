package cn.edu.sustech.ooadbackend.common;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 状态码枚举类
 * @author DYH
 * @version 1.0
 * @className StatusCode
 * @since 2023/10/9 15:31
 */
@Getter
@AllArgsConstructor
public enum StatusCode {
    SUCCESS(20000, "OK", "成功"),
    PARAMS_ERROR(40000, "请求参数错误", ""),
    NULL_ERROR(40001, "响应数据为空", ""),
    NOT_LOGIN(40100, "未登录", ""),
    NO_AUTH(40101, "无权限", ""),
    SYSTEM_ERROR(50000, "系统内部异常", ""),
    UNKNOWN_ERROR(55555, "未知异常", "");
    
    /**
     * 状态码
     */
    private final int code;
    
    /**
     * 状态码信息（简述）
     */
    private final String message;
    
    /**
     * 状态码描述（详情）
     */
    private final String description;
}
