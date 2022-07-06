package top.liyf.infohome.dao;

import org.apache.ibatis.annotations.Mapper;
import top.liyf.infohome.model.movie.MovieRating;

/**
 * 
 * @author liyf
 * Created in 2022-01-08
 */
@Mapper
public interface MovieRatingMapper {
    int insert(MovieRating record);

    MovieRating selectByPrimaryKey(Long movieId);

    int updateByPrimaryKeySelective(MovieRating record);


}