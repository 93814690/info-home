package top.liyf.infohome.task;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import top.liyf.fly.push.api.domain.ChanifyText;
import top.liyf.infohome.dao.HotSearchPushMapper;
import top.liyf.infohome.dao.UserChanifyMapper;
import top.liyf.infohome.dto.UserSubscription;
import top.liyf.infohome.feign.ChanifyClient;
import top.liyf.infohome.model.weibo.HotSearchPush;
import top.liyf.infohome.model.weibo.HotSearchV2;
import top.liyf.infohome.model.weibo.SubscriptionWeiboHotSearch;
import top.liyf.infohome.service.WeiboService;
import top.liyf.infohome.util.ChanifyConst;
import top.liyf.infohome.util.RedisConst;
import top.liyf.redis.service.RedisService;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

/**
 * @author liyf
 * Created in 2021-05-26
 */
@Component
@Slf4j
public class WeiboTask {

    @Autowired
    private RedisService redisService;
    @Autowired
    private ChanifyClient chanifyClient;
    @Autowired
    private UserChanifyMapper userChanifyMapper;
    @Autowired
    private HotSearchPushMapper pushMapper;
    @Autowired
    private WeiboService weiboService;

    /**
     * 功能描述: 读取微博热搜
     *
     * @author liyf
     */
    @Scheduled(cron = "0 */1 * * * ?")
    public void getHotSearch() throws Exception {
        weiboService.getHotSearch();
    }

    /**
     * 功能描述: 推送微博热搜
     *
     * @author liyf
     */
    @Scheduled(cron = "0 */1 7-23 * * ?")
    public void pushHotSearch() {
        Object o = redisService.lPop(RedisConst.WB_HOTSEARCH_PUSH_LIST);
        ArrayList<HotSearchV2> list = new ArrayList<>();
        while (o != null) {
            if (o instanceof HotSearchV2) {
                HotSearchV2 hotSearchV2 = (HotSearchV2) o;
                list.add(hotSearchV2);
            }
            o = redisService.lPop(RedisConst.WB_HOTSEARCH_PUSH_LIST);
        }
        if (list.size() > 0) {
            push(list);
            redisService.set(RedisConst.WB_ERROR_OTHER, false);
        }
    }

    /**
     * 功能描述: 推送方法
     *
     * @param list 热搜列表
     * @author liyf
     */
    private void push(ArrayList<HotSearchV2> list) {
        // 获取用户及推送规则
        List<UserSubscription> userList = userChanifyMapper.getUserSubscriptionWithHotSearch();
        for (HotSearchV2 hotSearchV2 : list) {
            String key = RedisConst.WB_HOTSEARCH_PUSHED + hotSearchV2.getWord();
            // 判断及推送
            for (UserSubscription userSubscription : userList) {
                String userId = userSubscription.getUserId();
                Boolean pushed = redisService.sIsMember(key, userId);
                if (pushed) {
                    continue;
                }
                ChanifyText text = null;
                HashSet<String> set = new HashSet<>();
                for (SubscriptionWeiboHotSearch rule : userSubscription.getHotSearchRuleList()) {
                    if (judge(hotSearchV2, rule)) {
                        if (text == null) {
                            text = new ChanifyText();
                            String title = getTitle(hotSearchV2);
                            text.setTitle(title);
                            text.setText(hotSearchV2.getWord());
                            String encode = URLEncoder.encode(hotSearchV2.getWord(), StandardCharsets.UTF_8);
                            String action = "查看|sinaweibo://searchall?q=" + encode;
                            text.setActions(Collections.singletonList(action));
                            text.setToken(userSubscription.getChanifyToken());
                        }
                        if (rule.getSound()) {
                            text.setSound(1);
                        }
                        set.add(rule.getInterruptionLevel());
                    }
                }
                if (text != null) {
                    // 设置中断级别
                    if (set.contains(ChanifyConst.INTERRUPTION_TIME_SENSITIVE)) {
                        text.setInterruptionLevel(ChanifyConst.INTERRUPTION_TIME_SENSITIVE);
                    } else if (set.contains(ChanifyConst.INTERRUPTION_ACTIVE)) {
                        text.setInterruptionLevel(ChanifyConst.INTERRUPTION_ACTIVE);
                    } else {
                        text.setInterruptionLevel(ChanifyConst.INTERRUPTION_PASSIVE);
                    }
                    chanifyClient.text(text);
                    HotSearchPush push = new HotSearchPush();
                    push.setUserId(userId);
                    push.setWord(hotSearchV2.getWord());
                    push.setPushTime(LocalDateTime.now());
                    pushMapper.insert(push);
                    redisService.sAdd(key, userId);
                }

            }

        }

    }

    /**
     * 功能描述: 构造标题
     *
     * @param hotSearchV2
     * @return java.lang.String
     * @author liyf
     */
    private String getTitle(HotSearchV2 hotSearchV2) {
        String emoticon = hotSearchV2.getEmoticon();
        String title = "微博 - ";
        if (StringUtils.hasText(emoticon)) {
            title += emoticon + " - ";
        }
        title += "热搜";
        Integer state = hotSearchV2.getState();
        if (state.equals(1)) {
            title += " - [新] - ";
        } else if (state.equals(2)) {
            title += " - [热] - ";
        } else if (state.equals(3)) {
            title += " - [沸] - ";
        } else if (state.equals(4)) {
            title += " - [爆] - ";
        } else {
            title += " - ";
        }
        title += hotSearchV2.getRank();
        return title;
    }

    /**
     * 功能描述: 判断是否推送
     *
     * @param hotSearchV2 热搜
     * @param rule        推送规则
     * @return boolean
     * @author liyf
     */
    private boolean judge(HotSearchV2 hotSearchV2, SubscriptionWeiboHotSearch rule) {
        if (rule.getRankingList() != 0 && rule.getRankingList() < hotSearchV2.getRank()) {
            return false;
        }
        if (rule.getState() != 0 && !rule.getState().equals(hotSearchV2.getState())) {
            return false;
        }
        return true;
    }
}
