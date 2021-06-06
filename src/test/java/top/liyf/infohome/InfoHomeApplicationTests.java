package top.liyf.infohome;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import top.liyf.infohome.service.ZsxqService;

@SpringBootTest
class InfoHomeApplicationTests {

    @Autowired
    ZsxqService service;

    @Test
    void contextLoads() throws Exception {
        service.getData(222454121411L, "2020-09-01T08:35:57.700+0800", false);
    }

}
