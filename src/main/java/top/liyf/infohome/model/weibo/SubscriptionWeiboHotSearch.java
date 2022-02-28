package top.liyf.infohome.model.weibo;

import java.io.Serializable;
import lombok.Data;

/**
 * @author liyf
 * Created in 2021-07-14
 */
@Data
public class SubscriptionWeiboHotSearch implements Serializable {
    private Long id;

    private String userId;

    private Integer rankingList;

    private Integer state;

    private Boolean sound;

    private String interruptionLevel;

    /**
     * 最小搜索量
     */
    private Long minNum;

    private static final long serialVersionUID = 1L;
}