package cn.edu.sustech.ooadbackend.exception;

import cn.edu.sustech.ooadbackend.common.StatusCode;
import lombok.Getter;

/**
 * 自定义事务异常
 * @author DYH
 * @version 1.0
 * @className BusinessException
 * @since 2023/10/9 15:40
 */
@Getter
public class BusinessException extends RuntimeException {
    
    private final int code;
    private final String description;
    private final StatusCode statusCode;
    
    public BusinessException(int code, String message, String description, StatusCode statusCode) {
        super(message);
        this.code = code;
        this.description = description;
        this.statusCode = statusCode;
    }
    
    public BusinessException(StatusCode statusCode) {
        this(statusCode.getCode(), statusCode.getMessage(), statusCode.getDescription(), statusCode);
    }
    
    public BusinessException(StatusCode statusCode, String description) {
        this(statusCode.getCode(), statusCode.getMessage(), description, statusCode);
    }
}
