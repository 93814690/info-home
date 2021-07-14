package top.liyf.infohome.task;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import top.liyf.fly.common.core.exception.BusinessException;
import top.liyf.fly.push.api.domain.ChanifyText;
import top.liyf.infohome.dao.HotSearchPushMapper;
import top.liyf.infohome.dao.UserChanifyMapper;
import top.liyf.infohome.dto.UserSubscription;
import top.liyf.infohome.feign.ChanifyClient;
import top.liyf.infohome.model.weibo.HotSearchPush;
import top.liyf.infohome.model.weibo.HotSearchV2;
import top.liyf.infohome.model.weibo.SubscriptionWeiboHotSearch;
import top.liyf.infohome.service.WeiboService;
import top.liyf.infohome.util.RedisConst;
import top.liyf.redis.service.RedisService;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
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
    private WeiboService service;
    @Autowired
    private RedisService redisService;
    @Autowired
    private ChanifyClient chanifyClient;
    @Autowired
    private UserChanifyMapper userChanifyMapper;
    @Autowired
    private HotSearchPushMapper pushMapper;

    @Scheduled(cron = "0 */1 7-23 * * ?")
    public void monitorHotSearch() {
        try {
            ArrayList<HotSearchV2> list = service.getHotSearch();
            push(list);
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
                text.setSound(1);
                text.setPriority(10);
                text.setToken("CICy4YgGEiJBREpGVTM3RFpNNEZMRlZaN1FCWDVGSE5BTlY0TVM0RFpNGhRmU0vjxji92dxl8bfsQfWCC4Km-SIECAEQASoiQUhSN1pLV1czUkNRQVFJUlpCNUVDVElFS09WWFBSU05TTQ..sbiZSJu63KdZK1dm2l0Rtljnz-btD3V3tdLX3SeRimA");
                chanifyClient.text(text);
            }
        }
    }

    private void push(ArrayList<HotSearchV2> list) {
        // 获取用户及推送规则
        // todo redis 缓存
        List<UserSubscription> userList = userChanifyMapper.getUserSubscriptionWithHotSearch();
        for (HotSearchV2 hotSearchV2 : list) {
            // 判断及推送
            for (UserSubscription userSubscription : userList) {
                HotSearchPush hotSearchPush = pushMapper.selectOneByUserIdAndWordAndListTime(userSubscription.getUserId(), hotSearchV2.getWord(), hotSearchV2.getListTime());
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
                        // todo sound
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
                    push.setListTime(hotSearchV2.getListTime());
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
