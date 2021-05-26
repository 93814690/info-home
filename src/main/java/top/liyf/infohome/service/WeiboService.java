package top.liyf.infohome.service;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import top.liyf.fly.common.core.exception.BusinessException;
import top.liyf.fly.common.core.result.ResultCode;
import top.liyf.fly.common.core.util.HttpClientResult;
import top.liyf.fly.common.core.util.HttpUtils;
import top.liyf.infohome.dao.WeiboConfigurationDao;
import top.liyf.infohome.dao.WeiboHotSearchDao;
import top.liyf.infohome.model.weibo.HotSearch;
import top.liyf.infohome.model.weibo.HotSearchResponse;
import top.liyf.infohome.model.weibo.WeiboConfiguration;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;

/**
 * @author liyf
 * Created in 2021-05-25
 */
@Service
public class WeiboService {

    @Autowired
    private WeiboHotSearchDao hotSearchDao;
    @Autowired
    private WeiboConfigurationDao configurationDao;

    public void getHotSearch() throws Exception {
        String url = "https://weibo.com/ajax/side/hotSearch";
        HashMap<String, String> header = getHeader();
        HttpClientResult result = HttpUtils.doGet(url, header, null);
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        HotSearchResponse response = mapper.readValue(result.getContent(), HotSearchResponse.class);
        System.out.println("response = " + response);
        if (response.getOk() == 1) {
            HotSearch hotgov = response.getData().getHotgov();
            if (hotgov != null) {
                handleHotSearch(hotgov);
            }
            List<HotSearch> realtime = response.getData().getRealtime();
            for (int i = 0; i < 12; i++) {
                HotSearch hotSearch = realtime.get(i);
                if (hotSearch.getIsAd() == 0) {
                    handleHotSearch(hotSearch);
                }
            }
        }
    }

    private void handleHotSearch(HotSearch hotgov) {
        Optional<HotSearch> byMid = hotSearchDao.findByMid(hotgov.getMid());
        if (byMid.isEmpty()) {
            hotSearchDao.save(hotgov);
            // todo push
        }
    }

    private HashMap<String, String> getHeader() {
        Optional<WeiboConfiguration> byId = configurationDao.findById(1);
        if (byId.isEmpty()) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }
        WeiboConfiguration configuration = byId.get();
        HashMap<String, String> header = new HashMap<>(16);
        header.put("authority", "weibo.com");
        header.put("Referer", "https://weibo.com/");
        header.put("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/90.0.4430.212 Safari/537.36");
        header.put("Accept", "application/json, text/plain, */*");
        header.put("accept-language", "zh-CN,zh;q=0.9");
        header.put("accept-encoding", "gzip, deflate, br");
        header.put("cookie", configuration.getCookie());
        header.put("x-requested-with", "XMLHttpRequest");
        header.put("x-xsrf-token", configuration.getXsrfToken());

        return header;
    }
}
