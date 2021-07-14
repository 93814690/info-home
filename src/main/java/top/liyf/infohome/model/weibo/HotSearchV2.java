package top.liyf.infohome.model.weibo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @author liyf
 * Created in 2021-07-12
 */
@Data
public class HotSearchV2 implements Serializable {
    private Long id;

    private String word;

    private String emoticon;

    /**
     * 排名
     */
    private Integer rank;

    private Long num;

    /**
     * 0:无
     * 1:新
     * 2:热
     * 3:沸
     */
    private Integer state;

    /**
     * 上榜时间
     */
    private Long listTime;

    private LocalDateTime createTime;

    private static final long serialVersionUID = 1L;

    public HotSearchV2(HotSearch old) {
        this.word = old.getWord();
        this.emoticon = old.getEmoticon();
        this.rank = old.getRealPos();
        this.num = old.getNum();
        if (old.getIsNew() == 1) {
            this.state = 1;
        } else if (old.getIsHot() == 1) {
            this.state = 2;
        } else if (old.getIsFei() == 1) {
            this.state = 3;
        } else {
            this.state = 0;
        }
        this.listTime = old.getOnboardTime();
        this.createTime = LocalDateTime.now();
    }
}