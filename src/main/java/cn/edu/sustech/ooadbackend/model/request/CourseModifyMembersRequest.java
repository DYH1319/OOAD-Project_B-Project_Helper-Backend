package cn.edu.sustech.ooadbackend.model.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author Daniel
 * @version 1.0
 * @className CourseAddTaRequest
 * @description todo
 * @date 2023/10/30 11:34
 * @since 1.0
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CourseModifyMembersRequest {

    // 要修改的课程id
    private Long courseId;

    // 加入课程的taid的列表
    private Long[] taId;

    // 加入课程的学生id的列表
    private Long[] studentId;

}
