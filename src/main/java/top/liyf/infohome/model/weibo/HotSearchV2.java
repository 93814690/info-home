package top.liyf.infohome.model.weibo;

import lombok.Data;
import org.springframework.util.StringUtils;
import top.liyf.infohome.gecco.HSItem;

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
     * 4:爆
     */
    private Integer state;

    private LocalDateTime recordTime;

    private static final long serialVersionUID = 1L;

    public HotSearchV2() {
    }

    public HotSearchV2(HSItem item) {
        this.word = item.getWord();
        this.emoticon = item.getEmoticon();
        this.rank = item.getRank();
        this.num = item.getNum();
        if (!StringUtils.hasText(item.getState())) {
            this.state = 0;
        } else if ("新".equals(item.getState())) {
            this.state = 1;
        } else if ("热".equals(item.getState())) {
            this.state = 2;
        } else if ("沸".equals(item.getState())) {
            this.state = 3;
        } else if ("爆".equals(item.getState())) {
            this.state = 4;
        } else {
            this.state = 999;
        }
        this.recordTime = LocalDateTime.now();
    }

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
        this.recordTime = LocalDateTime.now();
    }
}