package top.liyf.infohome.feign;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import top.liyf.fly.push.api.feign.ChanifyClient;
import top.liyf.fly.push.api.domain.ChanifyText;

/**
 * @author liyf
 * Created in 2021-10-05
 */
@SpringBootTest
class TestChanifyClient {

    @Autowired
    private ChanifyClient chanifyClient;

    @Test
    public void testPush() {
        ChanifyText text = new ChanifyText();
        text.setTitle("testTitle");
        text.setText("testText");
        text.setToken("");
        text.setInterruptionLevel("active");
        chanifyClient.text(text);
    }
}