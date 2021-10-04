package top.liyf.infohome.service;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import top.liyf.fly.common.core.exception.BusinessException;
import top.liyf.fly.common.core.result.ResultCode;
import top.liyf.fly.common.core.util.HttpClientResult;
import top.liyf.fly.common.core.util.HttpUtils;
import top.liyf.fly.push.api.domain.ChanifyText;
import top.liyf.infohome.dao.HotSearchV2Mapper;
import top.liyf.infohome.dao.WeiboConfigurationDao;
import top.liyf.infohome.dao.WeiboHotSearchDao;
import top.liyf.infohome.feign.ChanifyClient;
import top.liyf.infohome.model.weibo.*;
import top.liyf.infohome.util.RedisConst;
import top.liyf.infohome.util.RegexUtil;
import top.liyf.redis.service.RedisService;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;

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

    private TidResult getTid() throws Exception {
        String url = "https://passport.weibo.com/visitor/genvisitor";
        HashMap<String, String> map = new HashMap<>(8);
        map.put("cb", "gen_callback");
        map.put("fp", "{\"os\":\"2\",\"browser\":\"Chrome94,0,4606,61\",\"fonts\":\"undefined\",\"screenInfo\":\"1920*1080*24\",\"plugins\":\"Portable Document Format::internal-pdf-viewer::PDF Viewer|Portable Document Format::internal-pdf-viewer::Chrome PDF Viewer|Portable Document Format::internal-pdf-viewer::Chromium PDF Viewer|Portable Document Format::internal-pdf-viewer::Microsoft Edge PDF Viewer|Portable Document Format::internal-pdf-viewer::WebKit built-in PDF\"}");
        HttpClientResult result = HttpUtils.doGet(url, null, map);
        log.info("get tid result : {}", result);
        String content = result.getContent();
        content = content.replaceAll("window.gen_callback && gen_callback\\(", "");
        content = content.replaceAll("\\);", "");
        ObjectMapper mapper = new ObjectMapper();
        WeiBoResult tResult = mapper.readValue(content, WeiBoResult.class);
        if (tResult.getRetcode() == 20000000) {
            String s = mapper.writeValueAsString(tResult.getData());
            TidResult data = mapper.readValue(s, TidResult.class);
            log.info("tidResult = {}", data);
            return data;
        }
        Thread.sleep(3000);
        return getTid();
    }

    public String getVisitorCookieByHttp() throws Exception {
        TidResult tidResult = getTid();
        String tid = tidResult.getTid();
        int c = tidResult.getConficence() == null ? 100 : tidResult.getConficence();
        int w;
        if (tidResult.isNewTid()) {
            w = 3;
        } else {
            w = 2;
        }
        String url = "https://passport.weibo.com/visitor/visitor?a=incarnate&t=" + tid + "&w=" + w + "&c=0" + c + "&gc=&cb=cross_domain&from=weibo&_rand=" + Math.random();
        HttpClientResult httpClientResult = HttpUtils.doGet(url, null, null);
        log.info("get cookie result : {}", httpClientResult);
        String content = httpClientResult.getContent();
        content = content.replaceAll("window.cross_domain && cross_domain\\(", "");
        content = content.replaceAll("\\);", "");
        ObjectMapper mapper = new ObjectMapper();
        WeiBoResult tResult = mapper.readValue(content, WeiBoResult.class);
        if (tResult.getRetcode() == 20000000) {
            String s = mapper.writeValueAsString(tResult.getData());
            VisitorCookieResult data = mapper.readValue(s, VisitorCookieResult.class);
            log.info("VisitorCookieResult = {}", data);
            String cookie = "SUB=" + data.getSub() + "; SUBP=" + data.getSubp();
            log.info("cookie = {}", cookie);
            return cookie;
        }
        Thread.sleep(2000);
        return getVisitorCookieByHttp();
    }

    /**
     * 功能描述: 获取访客cookie
     *
     * @return java.lang.String cookie
     * @author liyf
     */
    public String getVisitorCookie() {
        String cookie = redisService.getString(RedisConst.WB_COOKIE_VISITOR);
        if (StringUtils.hasText(cookie)) {
            return cookie;
        }
        synchronized (this) {
            cookie = redisService.getString(RedisConst.WB_COOKIE_VISITOR);
            if (!StringUtils.hasText(cookie)) {
                try {
                    cookie = getVisitorCookieByHttp();
                    redisService.setString(RedisConst.WB_COOKIE_VISITOR, cookie, 23, TimeUnit.HOURS);
                } catch (Exception e) {
                    log.error("get cookie error: {}", e.getMessage());
                }
            }
            return cookie;
        }
    }

    /**
     * 功能描述: 获取微博热搜，并存入 redis 推送队列
     *
     * @author liyf
     */
    public void parse() throws Exception {
        String url = "https://s.weibo.com/top/summary";
        String cookie = getVisitorCookie();
        Document doc = Jsoup.connect(url).header("cookie", cookie).get();
        Elements trs = doc.select("#pl_top_realtimehot > table > tbody > tr");

        Set<String> set = getHotSearchSet(trs);

        redisService.delete(RedisConst.WB_HOTSEARCH_NEW);
        redisService.sAdd(RedisConst.WB_HOTSEARCH_NEW, set.toArray());
        Set<String> diffString = redisService.sDiffString(RedisConst.WB_HOTSEARCH_OLD, RedisConst.WB_HOTSEARCH_NEW);
        log.info("下榜热搜: {}", diffString);
        for (String s : diffString) {
            redisService.delete(RedisConst.WB_HOTSEARCH_PUSHED + s);
        }
        redisService.delete(RedisConst.WB_HOTSEARCH_OLD);
        redisService.sAdd(RedisConst.WB_HOTSEARCH_OLD, set.toArray());

    }

    private Set<String> getHotSearchSet(Elements trs) {
        Set<String> set = new HashSet<>();
        for (Element tr : trs) {
            HotSearchV2 hotSearchV2 = new HotSearchV2();
            String rankString = tr.select("td.td-01.ranktop").text();
            if (StringUtils.hasText(rankString) && !rankString.contains("•")) {
                hotSearchV2.setRank(Integer.valueOf(rankString));
            } else {
                continue;
            }
            hotSearchV2.setWord(tr.select("td.td-02 > a").text());
            String numString = tr.select("td.td-02 > span").text();
            if (StringUtils.hasText(numString)) {
                // 去除标签
                if (RegexUtil.isContainChinese(numString)) {
                    numString = numString.split(" ")[1];
                }
                hotSearchV2.setNum(Long.valueOf(numString));
            }
            hotSearchV2.setEmoticon(tr.select("td.td-02 > img").attr("title"));
            String stateString = tr.select("td.td-03 > i").text();
            int state;
            if (!StringUtils.hasText(stateString)) {
                state = 0;
            } else if ("新".equals(stateString)) {
                state = 1;
            } else if ("热".equals(stateString)) {
                state = 2;
            } else if ("沸".equals(stateString)) {
                state = 3;
            } else if ("爆".equals(stateString)) {
                state = 4;
            } else {
                state = 999;
            }
            hotSearchV2.setState(state);
            hotSearchV2.setRecordTime(LocalDateTime.now());

            hotSearchV2Mapper.insert(hotSearchV2);
            redisService.rPush(RedisConst.WB_HOTSEARCH_PUSH_LIST, hotSearchV2);
            set.add(hotSearchV2.getWord());
        }
        return set;
    }
}
