package top.liyf.infohome.feign;

import org.springframework.cloud.openfeign.FeignClient;
import top.liyf.flyauthapi.api.AuthUserApi;

/**
 * @author liyf
 * Created in 2021-07-06
 */
@FeignClient("fly-auth")
public interface AuthUserClient extends AuthUserApi {

}
