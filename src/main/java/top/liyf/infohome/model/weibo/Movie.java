package top.liyf.infohome.model.weibo;

import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * @author liyf
 * Created in 2022-01-07
 */
@Data
public class Movie implements Serializable {
    private Long id;

    private String imdbCode;

    private Integer dbCode;

    private String chineseTitle;

    private String originalTitle;

    private String otherTitle;

    private Integer year;

    /**
     * 电影的类型
     */
    private String genre;

    /**
     * 制片国家/地区
     */
    private String country;

    /**
     * 上映日期
     */
    private String initialReleaseDate;

    /**
     * 电影长度
     */
    private Integer runtime;

    /**
     * 美国的电影分级
     */
    private String mpaaRating;

    private LocalDateTime updateTime;

    private static final long serialVersionUID = 1L;
}