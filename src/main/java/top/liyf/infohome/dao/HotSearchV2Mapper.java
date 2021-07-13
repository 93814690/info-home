package top.liyf.infohome.dao;

import org.apache.ibatis.annotations.Mapper;
import top.liyf.infohome.model.weibo.HotSearchV2;

/**
 * @author liyf
 * Created in 2021-07-13
 */
@Mapper
public interface HotSearchV2Mapper {
    int insert(HotSearchV2 record);

    HotSearchV2 selectByPrimaryKey(Long id);
}