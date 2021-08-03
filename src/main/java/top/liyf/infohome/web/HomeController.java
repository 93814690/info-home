package top.liyf.infohome.web;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;

/**
 * @author liyf
 * Created in 2021-05-21
 */
@RestController
public class HomeController {

    @GetMapping("/")
    public String home() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return "Hello, " + authentication.getName() + ", 后台即将上线，敬请期待！";
    }

    @GetMapping("/a")
    public String a() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        return authentication.getName();
    }

    @PreAuthorize("hasAnyAuthority('infouser')")
    @GetMapping("/b")
    public String b() {
        return "b";
    }

    @PreAuthorize("hasAuthority('aaa')")
    @GetMapping("/c")
    public String c() {
        return "c";
    }
}
