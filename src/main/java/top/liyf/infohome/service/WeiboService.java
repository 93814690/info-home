package top.liyf.infohome.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import top.liyf.fly.common.core.util.HttpClientResult;
import top.liyf.fly.common.core.util.HttpUtils;
import top.liyf.infohome.dao.HotSearchV2Mapper;
import top.liyf.infohome.model.weibo.HotSearchV2;
import top.liyf.infohome.model.weibo.TidResult;
import top.liyf.infohome.model.weibo.VisitorCookieResult;
import top.liyf.infohome.model.weibo.WeiBoResult;
import top.liyf.infohome.util.RedisConst;
import top.liyf.infohome.util.RegexUtil;
import top.liyf.redis.service.RedisService;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * @author liyf
 * Created in 2021-05-25
 */
@Service
@Slf4j
public class WeiboService {

    @Autowired
    private RedisService redisService;
    @Autowired
    private HotSearchV2Mapper hotSearchV2Mapper;

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
        int c = tidResult.getConfidence() == null ? 100 : tidResult.getConfidence();
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
    public void getHotSearch() throws Exception {
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
