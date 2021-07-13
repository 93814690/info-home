package top.liyf.infohome.dao;

import org.apache.ibatis.annotations.Mapper;
import top.liyf.infohome.dto.UserSubscription;
import top.liyf.infohome.model.UserChanify;

import java.util.List;

/**
 * @author liyf
 * Created in 2021-07-13
 */
@Mapper
public interface UserChanifyMapper {
    int insert(UserChanify record);

    UserChanify selectByPrimaryKey(Long id);


    List<UserSubscription> getUserSubscriptionWithHotSearch();
}