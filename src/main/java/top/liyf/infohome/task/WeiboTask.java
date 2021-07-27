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
import top.liyf.infohome.util.RedisConst;
import top.liyf.redis.service.RedisService;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
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

    @Scheduled(cron = "0 */1 7-23 * * ?")
    public void pushHotSearch() {
        Object o = redisService.lPop(RedisConst.WB_LIST_HOTSEARCH);
        ArrayList<HotSearchV2> list = new ArrayList<>();
        while (o != null) {
            if (o instanceof HotSearchV2) {
                HotSearchV2 hotSearchV2 = (HotSearchV2) o;
                list.add(hotSearchV2);
            }
            o = redisService.lPop(RedisConst.WB_LIST_HOTSEARCH);
        }
        if (list.size() > 0) {
            push(list);
            redisService.set(RedisConst.WB_ERROR_OTHER, false);
        }
    }

    private void push(ArrayList<HotSearchV2> list) {
        // 获取用户及推送规则
        List<UserSubscription> userList = userChanifyMapper.getUserSubscriptionWithHotSearch();
        for (HotSearchV2 hotSearchV2 : list) {
            // 判断及推送
            for (UserSubscription userSubscription : userList) {
                HotSearchPush hotSearchPush = pushMapper.selectOneByUserIdAndWord(userSubscription.getUserId(), hotSearchV2.getWord());
                if (hotSearchPush != null) {
                    continue;
                }
                ChanifyText text = null;
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
                    }
                }
                if (text != null) {
                    chanifyClient.text(text);
                    HotSearchPush push = new HotSearchPush();
                    push.setUserId(userSubscription.getUserId());
                    push.setWord(hotSearchV2.getWord());
                    push.setPushTime(LocalDateTime.now());
                    pushMapper.insert(push);
                }

            }

        }

    }

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
        } else {
            title += " - ";
        }
        title += hotSearchV2.getRank();
        return title;
    }

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
