package top.liyf.infohome.feign;

import org.springframework.cloud.openfeign.FeignClient;
import top.liyf.fly.push.api.api.ChanifyApi;

/**
 * @author liyf
 * Created in 2021-05-26
 */
@FeignClient("fly-push")
public interface ChanifyClient extends ChanifyApi {

}
