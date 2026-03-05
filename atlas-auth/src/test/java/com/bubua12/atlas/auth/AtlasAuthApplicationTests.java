package com.bubua12.atlas.auth;

import com.bubua12.atlas.common.core.utils.PasswordUtils;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 *
 *
 * @author bubua12
 * @since 2026/3/5 13:07
 */
@SpringBootTest
public class AtlasAuthApplicationTests {


    @Test
    void contextLoads() {
        System.out.println(PasswordUtils.encode("bubua12@atlas123"));
    }
}
