package cn.edu.sustech.ooadbackend.model.request;

import lombok.Data;

/**
 * @className: CourseUpdateRequest
 * @Package: cn.edu.sustech.ooadbackend.model.request
 * @Description:
 * @Author: XXX
 * @Date: 2023/10/18 12:23
 * @Version:1.0
 */
@Data
public class CourseUpdateRequest {
    /**
     * 课程id
     */
    private Long id;

    /**
     * 课程名称
     */
    private String courseName;

    /**
     * 任教老师id
     */
    private Long teacherId;

    /**
     * 所有助教的id
     */
    private Long[] taIdList;
}
