package top.liyf.infohome.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * @author liyf
 * Created in 2021-05-25
 */
@SpringBootTest
class TestWeiboService {

    @Autowired
    WeiboService weiboService;

    @Test
    void parse() throws Exception {
        weiboService.getHotSearch();
    }


}