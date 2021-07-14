package top.liyf.infohome.model.weibo;

import lombok.Data;

import java.io.Serializable;

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

    private static final long serialVersionUID = 1L;
}