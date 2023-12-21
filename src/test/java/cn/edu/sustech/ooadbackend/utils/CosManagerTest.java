package cn.edu.sustech.ooadbackend.utils;

import cn.edu.sustech.ooadbackend.common.FileType;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * @author DYH
 * @version 1.0
 * @className CosManagerTest
 * @since 2023/12/19 15:27
 */
@SpringBootTest
public class CosManagerTest {
    
    @Resource
    private CosManager cosManager;
    
    @Test
    void testCos() {
        cosManager.putObject("src/test.txt", "src/test/resources/test.txt", FileType.MISC);
    }
}
