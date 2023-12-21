package cn.edu.sustech.ooadbackend.common;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author DYH
 * @version 1.0
 * @className FileType
 * @since 2023/12/21 12:37
 */
@Getter
@AllArgsConstructor
public enum FileType {
    AVATAR_IMAGE("Avatar/"),
    ASSIGNMENT_FILE("AssignmentFile/"),
    MISC("Misc/");
    
    /**
     * 上传到cos的目录
     */
    private final String folder;
}
