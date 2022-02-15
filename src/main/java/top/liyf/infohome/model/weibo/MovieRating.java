package top.liyf.infohome.model.weibo;

import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 
 * @author liyf
 * Created in 2022-01-08
 */
@Data
public class MovieRating implements Serializable {
    private Long movieId;

    private Double dbRating;

    private Integer dbRatingPeople;

    private Double imdbRating;

    private Integer imdbRatingPeople;

    private LocalDateTime updateTime;

    private static final long serialVersionUID = 1L;
}