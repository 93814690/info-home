package top.liyf.infohome.model.weibo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import javax.persistence.*;

/**
 * @author liyf
 * Created in 2021-05-25
 */
@Data
@Entity(name = "weibo_hot_search")
@Table(uniqueConstraints = {@UniqueConstraint(columnNames="mid")})
public class HotSearch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private String mid;

    private String name;

    private String note;

    private String url;

    private String word;

    private String emoticon;

    private long num;

    @JsonProperty(value = "onboard_time")
    private long onboardTime;

    @JsonProperty(value = "topic_flag")
    private String topicFlag;

    private String flag;

    @JsonProperty(value = "is_fei")
    private int isFei;

    @JsonProperty(value = "is_hot")
    private int isHot;

    @JsonProperty(value = "is_gov")
    private int isGov;

    @Transient
    @JsonProperty(value = "is_ad")
    private int isAd;
}
