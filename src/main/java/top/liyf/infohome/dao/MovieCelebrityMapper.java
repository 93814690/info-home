package top.liyf.infohome.dao;
import java.util.List;
import org.apache.ibatis.annotations.Param;

import org.apache.ibatis.annotations.Mapper;
import top.liyf.infohome.model.weibo.MovieCelebrity;

/**
 * @author liyf
 * Created in 2022-01-09
 */
@Mapper
public interface MovieCelebrityMapper {
    int insert(MovieCelebrity record);

    MovieCelebrity selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(MovieCelebrity record);

    int deleteByMovieId(@Param("movieId")Long movieId);

    int insertList(@Param("list")List<MovieCelebrity> list);


}