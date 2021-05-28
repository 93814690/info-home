package top.liyf.infohome.task;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import top.liyf.infohome.service.WeiboService;

/**
 * @author liyf
 * Created in 2021-05-26
 */
@Component
public class WeiboTask {

    @Autowired
    private WeiboService service;

    @Scheduled(cron = "0 */2 7-23 * * ?")
    public void monitorHotSearch() throws Exception {
        service.getHotSearch();
    }
}
