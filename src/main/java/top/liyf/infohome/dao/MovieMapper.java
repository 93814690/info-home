package top.liyf.infohome.dao;
import org.apache.ibatis.annotations.Param;

import org.apache.ibatis.annotations.Mapper;
import top.liyf.infohome.model.weibo.Movie;

/**
 * @author liyf
 * Created in 2022-01-07
 */
@Mapper
public interface MovieMapper {
    int insert(Movie record);

    Movie selectOneByDbCode(@Param("dbCode")Integer dbCode);

    int updateByPrimaryKeySelective(Movie record);
}