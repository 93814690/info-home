package top.liyf.infohome.task;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import top.liyf.infohome.service.ZsxqService;

/**
 * @author liyf
 * Created in 2021-06-06
 */
@Component
public class ZsxqTask {

    @Autowired
    private ZsxqService service;

    @Scheduled(cron = "0 0 18 * * ?")
    public void record() throws Exception {
        service.getData(222454121411L, null, false);
    }
}
