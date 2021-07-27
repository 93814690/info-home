package top.liyf.infohome.dao;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import top.liyf.infohome.model.weibo.HotSearchPush;

/**
 * @author liyf
 * Created in 2021-07-13
 */
@Mapper
public interface HotSearchPushMapper {
    int insert(HotSearchPush record);

    HotSearchPush selectByPrimaryKey(Long id);

    HotSearchPush selectOneByUserIdAndWord(@Param("userId") String userId, @Param("word") String word);


}