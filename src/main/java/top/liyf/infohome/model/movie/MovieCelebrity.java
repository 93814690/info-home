package top.liyf.infohome.model.movie;

import java.io.Serializable;
import lombok.Data;

/**
 * @author liyf
 * Created in 2022-01-09
 */
@Data
public class MovieCelebrity implements Serializable {
    private Long id;

    private Long movieId;

    private Long celebrityId;

    private String celebrityName;

    /**
     * 0:导演
     * 1:编剧
     * 2:主演
     */
    private Integer type;

    private static final long serialVersionUID = 1L;
}