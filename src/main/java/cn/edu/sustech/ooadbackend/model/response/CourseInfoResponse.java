package cn.edu.sustech.ooadbackend.model.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * @className: CourseInfoResponse
 * @Package: cn.edu.sustech.ooadbackend.model.response
 * @Description:
 * @Author: XXX
 * @Date: 2023/10/31 16:39
 * @Version:1.0
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CourseInfoResponse {

    private String courseName;

    private String teacherName;

    private String[] taNameList;

    private Long studentNum;

    private Date createTime;

}
