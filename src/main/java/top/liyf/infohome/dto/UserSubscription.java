package top.liyf.infohome.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import top.liyf.infohome.model.UserChanify;
import top.liyf.infohome.model.weibo.SubscriptionWeiboHotSearch;

import java.util.List;

/**
 * @author liyf
 * Created in 2021-07-13
 */
@EqualsAndHashCode(callSuper = true)
@Data
@ToString(callSuper = true)
public class UserSubscription extends UserChanify {

    private List<SubscriptionWeiboHotSearch> hotSearchRuleList;
}
