package top.liyf.infohome.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author liyf
 * Created in 2021-05-25
 */
@SpringBootTest
class TestWeiboService {

    @Autowired
    WeiboService weiboService;

    @Test
    void getHotSearch() throws Exception {
        weiboService.getHotSearch();
    }
}