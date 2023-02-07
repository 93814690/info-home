package top.liyf.infohome.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import top.liyf.flyauthapi.api.AuthUserApi;
import top.liyf.flyauthapi.bo.AuthUserBO;

/**
 * @author liyf
 * Created in 2021-07-06
 */
@FeignClient("fly-auth")
public interface AuthUserClient {
    @PostMapping({"/api/user/bo"})
    AuthUserBO getUserBO(@RequestHeader("Authorization") String var1);
}
