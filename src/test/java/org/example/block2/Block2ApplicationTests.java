package org.example.block2;

import org.example.block2.utils.TestcontainersConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
class Block2ApplicationTests {

    @Test
    void contextLoads() {
    }

}
