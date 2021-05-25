package top.liyf.infohome.web;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * @author liyf
 * Created in 2021-05-21
 */
@RestController
public class HomeController {

    @PostMapping("/a")
    public void a(@RequestBody Map<String, List<String>> codes) {
        System.out.println("codes = " + codes);
    }
}
