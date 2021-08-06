package top.liyf.infohome.web;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import top.liyf.fly.common.core.result.ResultBean;
import top.liyf.flyauthapi.bo.AuthUserBO;

import javax.servlet.http.HttpSession;

/**
 * @author liyf
 * Created in 2021-08-05
 */
@RestController
@RequestMapping("/user")
public class UserController {

    private final HttpSession session;

    public UserController(HttpSession session) {
        this.session = session;
    }

    @GetMapping("/info")
    public ResultBean<AuthUserBO> getUserInfo() {
        AuthUserBO user = (AuthUserBO) session.getAttribute("user");
        System.out.println("user = " + user);
        return new ResultBean<>(user);
    }
}
