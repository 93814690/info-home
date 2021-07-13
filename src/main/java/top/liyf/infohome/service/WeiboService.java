package top.liyf.infohome.service;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import top.liyf.fly.common.core.exception.BusinessException;
import top.liyf.fly.common.core.result.ResultCode;
import top.liyf.fly.common.core.util.HttpClientResult;
import top.liyf.fly.common.core.util.HttpUtils;
import top.liyf.fly.push.api.domain.ChanifyText;
import top.liyf.infohome.dao.HotSearchV2Mapper;
import top.liyf.infohome.dao.WeiboConfigurationDao;
import top.liyf.infohome.dao.WeiboHotSearchDao;
import top.liyf.infohome.feign.ChanifyClient;
import top.liyf.infohome.model.weibo.HotSearch;
import top.liyf.infohome.model.weibo.HotSearchResponse;
import top.liyf.infohome.model.weibo.HotSearchV2;
import top.liyf.infohome.model.weibo.WeiboConfiguration;
import top.liyf.infohome.util.RedisConst;
import top.liyf.redis.service.RedisService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

/**
 * @author liyf
 * Created in 2021-05-25
 */
@Service
@Slf4j
public class WeiboService {

    @Autowired
    private WeiboHotSearchDao hotSearchDao;
    @Autowired
    private WeiboConfigurationDao configurationDao;
    @Autowired
    private ChanifyClient chanifyClient;
    @Autowired
    private RedisService redisService;
    @Autowired
    private HotSearchV2Mapper hotSearchV2Mapper;

    public ArrayList<HotSearchV2> getHotSearch() throws Exception {
        ArrayList<HotSearchV2> list = new ArrayList<>();
        String url = "https://weibo.com/ajax/side/hotSearch";
        HashMap<String, String> header = getHeader();
        HttpClientResult result = HttpUtils.doGet(url, header, null);
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        HotSearchResponse response = mapper.readValue(result.getContent(), HotSearchResponse.class);
        if (response.getOk() == 1) {
            redisService.set(RedisConst.WB_COOKIE_EXPIRED, false);

            List<HotSearch> realtime = response.getData().getRealtime();
            for (HotSearch hotSearch : realtime) {
                if (hotSearch.getIsAd() == 0) {
                    HotSearchV2 hotSearchV2 = new HotSearchV2(hotSearch);
                    hotSearchV2Mapper.insert(hotSearchV2);
                    // 放入队列，判断推送
                    list.add(hotSearchV2);
                }
            }
            redisService.set(RedisConst.WB_ERROR_OTHER, false);
            return list;
        } else {
            if (response.getOk() == -100) {
                Boolean expired = (Boolean) redisService.get(RedisConst.WB_COOKIE_EXPIRED);
                if (expired == null || !expired) {
                    redisService.set(RedisConst.WB_COOKIE_EXPIRED, true);
                    ChanifyText text = new ChanifyText();
                    text.setTitle("error - 微博");
                    text.setText("cookie 已过期");
                    text.setSound(1);
                    text.setPriority(10);
                    text.setToken("CICy4YgGEiJBREpGVTM3RFpNNEZMRlZaN1FCWDVGSE5BTlY0TVM0RFpNGhRmU0vjxji92dxl8bfsQfWCC4Km-SIECAEQASoiQUhSN1pLV1czUkNRQVFJUlpCNUVDVElFS09WWFBSU05TTQ..sbiZSJu63KdZK1dm2l0Rtljnz-btD3V3tdLX3SeRimA");
                    chanifyClient.text(text);
                }
            }
            log.error("response = {}", response);
            throw new BusinessException(ResultCode.SYSTEM_ERROR);
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
