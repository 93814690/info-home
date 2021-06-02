package top.liyf.infohome.task;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import top.liyf.fly.common.core.exception.BusinessException;
import top.liyf.fly.push.api.domain.ChanifyText;
import top.liyf.infohome.feign.ChanifyClient;
import top.liyf.infohome.service.WeiboService;
import top.liyf.infohome.util.RedisConst;
import top.liyf.redis.service.RedisService;

/**
 * @author liyf
 * Created in 2021-05-26
 */
@Component
@Slf4j
public class WeiboTask {

    @Autowired
    private WeiboService service;
    @Autowired
    private RedisService redisService;
    @Autowired
    private ChanifyClient chanifyClient;

    @Scheduled(cron = "0 */2 7-23 * * ?")
    public void monitorHotSearch() {
        try {
            service.getHotSearch();
        } catch (BusinessException be) {
            log.error("BusinessException", be);
        } catch (Exception e) {
            log.error("exception", e);
            Boolean error = (Boolean) redisService.get(RedisConst.WB_ERROR_OTHER);
            if (error == null || !error) {
                redisService.set(RedisConst.WB_ERROR_OTHER, true);
                ChanifyText text = new ChanifyText();
                text.setTitle("error - 微博");
                text.setText("未知异常");
                text.setToken("CICy4YgGEiJBREpGVTM3RFpNNEZMRlZaN1FCWDVGSE5BTlY0TVM0RFpNGhRmU0vjxji92dxl8bfsQfWCC4Km-SIECAEQASoiQUhSN1pLV1czUkNRQVFJUlpCNUVDVElFS09WWFBSU05TTQ..sbiZSJu63KdZK1dm2l0Rtljnz-btD3V3tdLX3SeRimA");
                chanifyClient.text(text);
            }
        }
    }
}
