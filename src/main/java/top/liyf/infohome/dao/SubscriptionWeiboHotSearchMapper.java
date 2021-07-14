package top.liyf.infohome.dao;

import org.apache.ibatis.annotations.Mapper;
import top.liyf.infohome.model.weibo.SubscriptionWeiboHotSearch;

/**
 * @author liyf
 * Created in 2021-07-13
 */
@Mapper
public interface SubscriptionWeiboHotSearchMapper {
    int insert(SubscriptionWeiboHotSearch record);

    SubscriptionWeiboHotSearch selectByPrimaryKey(Long id);
}